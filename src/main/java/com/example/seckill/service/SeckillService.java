package com.example.seckill.service;

import com.example.seckill.common.ResultCode;
import com.example.seckill.entity.SeckillGoods;
import com.example.seckill.entity.SeckillOrder;
import com.example.seckill.exception.GlobalException;
import com.example.seckill.mapper.SeckillGoodsMapper;
import com.example.seckill.mapper.SeckillOrderMapper;
import com.example.seckill.mq.MQSender;
import com.example.seckill.mq.SeckillMessage;
import com.example.seckill.redis.RedisKeys;
import com.example.seckill.redis.RedisService;
import com.example.seckill.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 秒杀核心服务。
 * <p>
 * 请求侧：本地售罄标记 -> Redis Lua 原子预减（库存 + 一人一单）-> MQ 异步下单。
 * 消费侧：幂等校验 -> DB 条件扣减库存（最终防超卖）-> 创建订单 -> 发送超时关单延迟消息。
 *
 * @author example
 */
@Slf4j
@Service
public class SeckillService {

    /**
     * Redis 秒杀结果标记
     */
    public static final long RESULT_QUEUEING = 0L;
    public static final long RESULT_SOLD_OUT = -1L;
    public static final long RESULT_REPEATE = -2L;
    public static final long RESULT_SYSTEM_ERROR = -3L;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SeckillGoodsMapper goodsMapper;

    @Autowired
    private SeckillOrderMapper orderMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    @Value("${seckill.salt:seckill-secret-salt}")
    private String secretSalt;

    /**
     * 本地售罄标记：goodsId -> true，减少 Redis 访问
     */
    private final ConcurrentHashMap<Long, Boolean> soldOutMap = new ConcurrentHashMap<>();

    /**
     * Lua 预减库存脚本
     */
    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void initScript() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setResultType(Long.class);
        seckillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/seckill.lua")));
    }

    /* ==================== 请求侧 ==================== */

    /**
     * 秒杀入口（请求侧）：返回秒杀单号占位（0 表示已进入队列），失败抛出业务异常。
     */
    public long doSeckill(Long userId, Long goodsId, String path) {
        // 1. 校验动态秒杀地址
        checkPath(userId, goodsId, path);

        // 2. 本地售罄标记快速失败
        if (Boolean.TRUE.equals(soldOutMap.get(goodsId))) {
            throw new GlobalException(ResultCode.SECKILL_SOLD_OUT);
        }

        // 3. 时间 / 商品状态校验
        SeckillGoods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new GlobalException(ResultCode.SECKILL_GOODS_NOT_EXIST);
        }
        Date now = new Date();
        if (now.before(goods.getStartDate())) {
            throw new GlobalException(ResultCode.SECKILL_NOT_START);
        }
        if (now.after(goods.getEndDate())) {
            throw new GlobalException(ResultCode.SECKILL_END);
        }

        // 4. Redis Lua 原子预减库存 + 一人一单
        //    KEYS[1]=库存 key, KEYS[2]=已购用户集合 key, ARGV[1]=userId
        Long result = redisService.executeLua(seckillScript,
                java.util.Arrays.asList(RedisKeys.stockKey(goodsId), RedisKeys.boughtKey(goodsId)),
                String.valueOf(userId));
        long code = result == null ? RESULT_SYSTEM_ERROR : result;
        if (code == RESULT_SOLD_OUT) {
            soldOutMap.put(goodsId, Boolean.TRUE);
            throw new GlobalException(ResultCode.SECKILL_SOLD_OUT);
        }
        if (code == RESULT_REPEATE) {
            throw new GlobalException(ResultCode.SECKILL_REPEATE);
        }
        if (code != 0L) {
            throw new GlobalException(ResultCode.SECKILL_SYSTEM_BUSY);
        }

        // 5. 预先生成订单ID，发送异步下单消息
        long orderId = SnowflakeId.nextId();
        SeckillMessage message = new SeckillMessage(userId, goodsId, orderId);
        try {
            mqSender.sendSeckillMessage(message);
        } catch (Exception e) {
            // MQ 发送失败：回补 Redis 预减库存，保证数据一致
            log.error("秒杀消息发送失败，回补Redis库存: {}", message, e);
            rollbackRedisStock(goodsId, userId);
            throw new GlobalException(ResultCode.SECKILL_SYSTEM_BUSY);
        }

        // 6. 初始化结果为"排队中"，前端轮询
        redisService.setLong(RedisKeys.resultKey(userId, goodsId), RESULT_QUEUEING);
        return orderId;
    }

    /**
     * 查询秒杀结果：正数=订单号（成功），0=排队中，负数=失败原因。
     */
    public Long getSeckillResult(Long userId, Long goodsId) {
        Long result = redisService.getLong(RedisKeys.resultKey(userId, goodsId));
        if (result == null) {
            // Redis 无结果时，兜底查库（已成功但结果标记过期等场景）
            SeckillOrder order = orderMapper.selectByUserAndGoods(userId, goodsId);
            if (order != null) {
                return order.getId();
            }
            return RESULT_QUEUEING;
        }
        return result;
    }

    /**
     * 生成动态秒杀地址并缓存（有效期 5 分钟）。
     */
    public String createPath(Long userId, Long goodsId) {
        String path = MD5Util.seckillPath(userId, goodsId, secretSalt);
        redisService.set(RedisKeys.pathKey(userId, goodsId), path, 5,
                java.util.concurrent.TimeUnit.MINUTES);
        return path;
    }

    /**
     * 校验动态秒杀地址。
     */
    public void checkPath(Long userId, Long goodsId, String path) {
        if (path == null) {
            throw new GlobalException(ResultCode.SECKILL_PATH_INVALID);
        }
        String cached = redisService.get(RedisKeys.pathKey(userId, goodsId));
        if (cached == null || !cached.equals(path)) {
            throw new GlobalException(ResultCode.SECKILL_PATH_INVALID);
        }
    }

    /* ==================== 消费侧 ==================== */

    /**
     * 消费秒杀下单消息：幂等落库 + DB 条件扣减库存（最终防超卖）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleMessage(SeckillMessage message) {
        Long userId = message.getUserId();
        Long goodsId = message.getGoodsId();
        Long orderId = message.getOrderId();

        try {
            // 1. 幂等：已存在订单直接判定成功（消息重复投递场景）
            SeckillOrder existing = orderMapper.selectByUserAndGoods(userId, goodsId);
            if (existing != null) {
                redisService.setLong(RedisKeys.resultKey(userId, goodsId), existing.getId());
                return;
            }

            SeckillGoods goods = goodsMapper.selectById(goodsId);
            if (goods == null) {
                redisService.setLong(RedisKeys.resultKey(userId, goodsId), RESULT_SYSTEM_ERROR);
                return;
            }

            // 2. DB 条件扣减库存，最终防超卖
            int affected = goodsMapper.reduceStock(goodsId);
            if (affected == 0) {
                // DB 无库存（Redis 与 DB 极端不一致）：回补 Redis 预减，标记售罄
                log.warn("DB扣减库存失败（售罄）: goodsId={}", goodsId);
                soldOutMap.put(goodsId, Boolean.TRUE);
                rollbackRedisStock(goodsId, userId);
                redisService.setLong(RedisKeys.resultKey(userId, goodsId), RESULT_SOLD_OUT);
                return;
            }

            // 3. 创建订单（状态：未支付）
            SeckillOrder order = new SeckillOrder();
            order.setId(orderId);
            order.setUserId(userId);
            order.setGoodsId(goodsId);
            order.setPayAmount(goods.getSeckillPrice());
            order.setStatus(0);
            order.setCreateDate(new Date());
            orderMapper.insertOrder(order);

            // 4. 写入秒杀结果（订单号）
            redisService.setLong(RedisKeys.resultKey(userId, goodsId), orderId);

            // 5. 发送超时关单延迟消息（30 秒未支付）
            mqSender.sendOrderDelayMessage(message);
        } catch (Exception e) {
            log.error("消费秒杀消息异常: {}", message, e);
            redisService.setLong(RedisKeys.resultKey(userId, goodsId), RESULT_SYSTEM_ERROR);
            throw e;
        }
    }

    /**
     * 消费超时关单消息：未支付订单置为已取消，并回补 Redis + DB 库存。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeout(SeckillMessage message) {
        Long orderId = message.getOrderId();
        Long goodsId = message.getGoodsId();
        Long userId = message.getUserId();

        SeckillOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("超时关单：订单不存在 orderId={}", orderId);
            return;
        }
        // 仅未支付订单需要关单（乐观条件更新，返回 0 说明已支付/已取消）
        int affected = orderMapper.cancelOrder(orderId);
        if (affected == 0) {
            log.info("订单已支付或已取消，无需关单 orderId={}", orderId);
            return;
        }

        // 回补 DB 库存
        goodsMapper.increaseStock(goodsId);
        // 回补 Redis 库存并移除已购标记（允许用户重新抢购）
        rollbackRedisStock(goodsId, userId);
        soldOutMap.remove(goodsId);
        log.info("订单超时已取消并回补库存: orderId={}, goodsId={}", orderId, goodsId);
    }

    /* ==================== 库存预热 ==================== */

    /**
     * 启动时将商品库存预热到 Redis（Redis 不可用时仅告警，不阻断应用启动）。
     */
    public void preheatStock() {
        for (SeckillGoods goods : goodsService.listAll()) {
            try {
                redisService.stringSet(RedisKeys.stockKey(goods.getId()),
                        String.valueOf(goods.getStockCount()));
            } catch (Exception e) {
                log.warn("库存预热失败（请检查Redis是否可用）: goodsId={}", goods.getId(), e);
            }
        }
    }

    /* ==================== 内部方法 ==================== */

    /**
     * 回补 Redis：库存 +1，移除已购标记。
     */
    private void rollbackRedisStock(Long goodsId, Long userId) {
        try {
            String stockKey = RedisKeys.stockKey(goodsId);
            String current = redisService.stringGet(stockKey);
            long stock = current == null ? 0L : Long.parseLong(current);
            redisService.stringSet(stockKey, String.valueOf(stock + 1));
            // 移除已购集合成员，允许用户重新抢购
            redisService.removeSetMember(RedisKeys.boughtKey(goodsId), String.valueOf(userId));
        } catch (Exception e) {
            log.error("回补Redis库存异常: goodsId={}, userId={}", goodsId, userId, e);
        }
    }

    /**
     * 简单雪花 ID（单机版，生成全局唯一订单号）。
     */
    static final class SnowflakeId {
        private static final long EPOCH = 1700000000000L;
        private static long lastTimestamp = -1L;
        private static long sequence = 0L;

        private SnowflakeId() {
        }

        static synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & 4095;
                if (sequence == 0) {
                    while (timestamp <= lastTimestamp) {
                        timestamp = System.currentTimeMillis();
                    }
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            return ((timestamp - EPOCH) << 12) | sequence;
        }
    }
}
