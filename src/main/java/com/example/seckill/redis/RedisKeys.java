package com.example.seckill.redis;

/**
 * Redis key 统一定义。
 *
 * @author example
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /**
     * 商品库存 key 前缀，完整 key：seckill:stock:{goodsId}
     */
    public static final String STOCK_PREFIX = "seckill:stock:";

    /**
     * 已购用户集合 key 前缀，完整 key：seckill:bought:{goodsId}
     */
    public static final String BOUGHT_PREFIX = "seckill:bought:";

    /**
     * 登录 token key 前缀，完整 key：seckill:token:{token}
     */
    public static final String TOKEN_PREFIX = "seckill:token:";

    /**
     * 秒杀动态地址 key 前缀，完整 key：seckill:path:{userId}:{goodsId}
     */
    public static final String PATH_PREFIX = "seckill:path:";

    /**
     * 验证码 key 前缀，完整 key：seckill:captcha:{userId}:{goodsId}
     */
    public static final String CAPTCHA_PREFIX = "seckill:captcha:";

    /**
     * 秒杀结果 key 前缀，完整 key：seckill:result:{userId}:{goodsId}
     */
    public static final String RESULT_PREFIX = "seckill:result:";

    /**
     * 接口访问计数 key 前缀，完整 key：seckill:access:{userId}:{url}
     */
    public static final String ACCESS_PREFIX = "seckill:access:";

    public static String stockKey(Long goodsId) {
        return STOCK_PREFIX + goodsId;
    }

    public static String boughtKey(Long goodsId) {
        return BOUGHT_PREFIX + goodsId;
    }

    public static String tokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    public static String pathKey(Long userId, Long goodsId) {
        return PATH_PREFIX + userId + ":" + goodsId;
    }

    public static String captchaKey(Long userId, Long goodsId) {
        return CAPTCHA_PREFIX + userId + ":" + goodsId;
    }

    public static String resultKey(Long userId, Long goodsId) {
        return RESULT_PREFIX + userId + ":" + goodsId;
    }

    public static String accessKey(Long userId, String url) {
        return ACCESS_PREFIX + userId + ":" + url;
    }
}
