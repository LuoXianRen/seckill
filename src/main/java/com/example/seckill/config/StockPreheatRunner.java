package com.example.seckill.config;

import com.example.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动后预热 Redis 库存。
 * <p>
 * Redis 不可用时不阻断启动（服务内部已做容错告警）。
 *
 * @author example
 */
@Slf4j
@Order(1)
@Component
public class StockPreheatRunner implements ApplicationRunner {

    @Autowired
    private SeckillService seckillService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            seckillService.preheatStock();
            log.info("秒杀商品库存预热完成");
        } catch (Exception e) {
            log.warn("库存预热跳过：{}", e.getMessage());
        }
    }
}
