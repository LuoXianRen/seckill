package com.example.seckill.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀下单消息体。
 * <p>
 * 实现 Serializable，便于 RabbitMQ 使用 JDK 序列化传输。
 *
 * @author example
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 秒杀单号（订单ID，发送端提前生成）
     */
    private Long orderId;
}
