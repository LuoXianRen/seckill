package com.example.seckill.mapper;

import com.example.seckill.dto.GoodsAddDTO;
import com.example.seckill.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 秒杀商品 Mapper。
 *
 * @author example
 */
@Mapper
public interface SeckillGoodsMapper {

    /**
     * 查询全部秒杀商品
     */
    List<SeckillGoods> selectAll();

    /**
     * 根据ID查询商品
     */
    SeckillGoods selectById(@Param("id") Long id);

    /**
     * 条件扣减库存：库存大于 0 时才扣减，返回受影响行数（最终防超卖）。
     */
    int reduceStock(@Param("id") Long id);

    /**
     * 回补库存（超时关单时调用）。
     */
    int increaseStock(@Param("id") Long id);

    /**
     * 新增秒杀商品
     */
    int insertGoods(GoodsAddDTO dto);
}
