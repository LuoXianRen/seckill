package com.example.seckill.mq;

import com.example.seckill.config.RabbitMQConfig;
import com.example.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 秒杀消息消费者。
 *
 * @author example
 */
@Slf4j
@Component
public class MQReceiver {

    @Autowired
    private SeckillService seckillService;

    /**
     * 秒杀下单消费者：幂等落库。
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void receiveSeckill(SeckillMessage message) {
        log.info("收到秒杀下单消息: {}", message);
        seckillService.handleMessage(message);
    }

    /**
     * 超时关单消费者（死信队列）。
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_DLX_QUEUE)
    public void receiveTimeout(SeckillMessage message) {
        log.info("收到订单超时消息: {}", message);
        seckillService.handleTimeout(message);
    }
}
