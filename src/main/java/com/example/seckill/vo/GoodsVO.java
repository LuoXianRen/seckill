package com.example.seckill.vo;

import com.example.seckill.entity.SeckillGoods;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 商品展示对象，附带秒杀状态。
 *
 * @author example
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsVO extends SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀状态：0-未开始 1-进行中 2-已结束
     */
    private Integer seckillStatus;

    /**
     * 距离开始的剩余秒数（未开始时有效）
     */
    private Long remainSeconds;
}
