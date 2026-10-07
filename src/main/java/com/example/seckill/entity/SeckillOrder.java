package com.example.seckill.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀订单表实体。
 * <p>
 * 表上存在 (user_id, goods_id) 唯一索引，数据库层面保证一人一单。
 *
 * @author example
 */
@Data
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID（秒杀单号）
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 支付金额（秒杀价快照）
     */
    private BigDecimal payAmount;

    /**
     * 订单状态：0-未支付 1-已支付 2-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 支付时间
     */
    private Date payDate;
}
