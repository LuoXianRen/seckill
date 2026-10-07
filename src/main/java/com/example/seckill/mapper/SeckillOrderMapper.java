package com.example.seckill.mapper;

import com.example.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 秒杀订单 Mapper。
 *
 * @author example
 */
@Mapper
public interface SeckillOrderMapper {

    /**
     * 根据 用户ID + 商品ID 查询订单（一人一单校验 / 幂等）。
     */
    SeckillOrder selectByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    /**
     * 根据订单ID查询订单
     */
    SeckillOrder selectById(@Param("id") Long id);

    /**
     * 插入订单
     */
    int insertOrder(SeckillOrder order);

    /**
     * 将未支付订单置为已取消（乐观条件：仅未支付可取消）。
     */
    int cancelOrder(@Param("id") Long id);
}
