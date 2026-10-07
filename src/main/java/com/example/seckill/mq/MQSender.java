package com.example.seckill.mq;

import com.example.seckill.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息发送者。
 *
 * @author example
 */
@Slf4j
@Service
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀下单消息。
     */
    public void sendSeckillMessage(SeckillMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY, message);
        log.info("秒杀下单消息已发送: {}", message);
    }

    /**
     * 发送订单超时检查消息（进入 TTL 队列，30 秒后投递到死信队列）。
     */
    public void sendOrderDelayMessage(SeckillMessage message) {
        rabbitTemplate.convertAndSend("", RabbitMQConfig.SECKILL_TTL_QUEUE, message);
        log.info("订单超时延迟消息已发送: {}", message);
    }
}
