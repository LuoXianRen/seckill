package com.example.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置：秒杀下单队列 + 死信超时关单。
 * <p>
 * 链路：
 * <pre>
 * 下单消息 -> seckill.direct -> seckill.queue -> 消费者落库
 * 订单消息 -> seckill.ttl.queue（30s TTL，无消费者）-> 死信交换机 seckill.dlx -> seckill.dlx.queue -> 关单回补
 * </pre>
 *
 * @author example
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 秒杀下单交换机
     */
    public static final String SECKILL_EXCHANGE = "seckill.direct";

    /**
     * 秒杀下单队列
     */
    public static final String SECKILL_QUEUE = "seckill.queue";

    /**
     * 秒杀下单路由键
     */
    public static final String SECKILL_ROUTING_KEY = "seckill.routing";

    /**
     * 订单超时 TTL 队列（消息到期后进入死信交换机）
     */
    public static final String SECKILL_TTL_QUEUE = "seckill.ttl.queue";

    /**
     * 死信交换机
     */
    public static final String SECKILL_DLX_EXCHANGE = "seckill.dlx";

    /**
     * 死信队列（超时关单消费者）
     */
    public static final String SECKILL_DLX_QUEUE = "seckill.dlx.queue";

    /**
     * 死信路由键
     */
    public static final String SECKILL_DLX_ROUTING_KEY = "seckill.dlx.routing";

    /* ---------------- 下单 Direct 模型 ---------------- */

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(SECKILL_QUEUE).build();
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);
    }

    /* ---------------- 超时关单死信模型 ---------------- */

    /**
     * TTL 队列：消息进入后 30 秒过期，过期即投递到死信交换机。
     */
    @Bean
    public Queue seckillTtlQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-message-ttl", 30000);
        args.put("x-dead-letter-exchange", SECKILL_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", SECKILL_DLX_ROUTING_KEY);
        return QueueBuilder.durable(SECKILL_TTL_QUEUE).withArguments(args).build();
    }

    @Bean
    public DirectExchange seckillDlxExchange() {
        return new DirectExchange(SECKILL_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillDlxQueue() {
        return QueueBuilder.durable(SECKILL_DLX_QUEUE).build();
    }

    @Bean
    public Binding seckillDlxBinding() {
        return BindingBuilder.bind(seckillDlxQueue())
                .to(seckillDlxExchange())
                .with(SECKILL_DLX_ROUTING_KEY);
    }

    /**
     * 消息转换器：JSON 序列化（消息体带类型信息）。
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
