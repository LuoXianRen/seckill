package com.example.seckill.service;

import com.example.seckill.dto.GoodsAddDTO;
import com.example.seckill.entity.SeckillGoods;
import com.example.seckill.mapper.SeckillGoodsMapper;
import com.example.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商品服务。
 *
 * @author example
 */
@Service
public class GoodsService {

    @Autowired
    private SeckillGoodsMapper goodsMapper;

    /**
     * 查询商品列表并填充秒杀状态。
     */
    public List<GoodsVO> listGoodsVO() {
        List<SeckillGoods> goodsList = goodsMapper.selectAll();
        List<GoodsVO> result = new ArrayList<>(goodsList.size());
        Date now = new Date();
        for (SeckillGoods goods : goodsList) {
            result.add(fillStatus(goods, now));
        }
        return result;
    }

    /**
     * 查询商品详情并填充秒杀状态。
     */
    public GoodsVO getGoodsVOById(Long id) {
        SeckillGoods goods = goodsMapper.selectById(id);
        return goods == null ? null : fillStatus(goods, new Date());
    }

    public SeckillGoods getById(Long id) {
        return goodsMapper.selectById(id);
    }

    public List<SeckillGoods> listAll() {
        return goodsMapper.selectAll();
    }

    public Long addGoods(GoodsAddDTO dto) {
        goodsMapper.insertGoods(dto);
        return dto.getId();
    }

    /**
     * 填充秒杀状态：0-未开始 1-进行中 2-已结束。
     */
    private GoodsVO fillStatus(SeckillGoods goods, Date now) {
        GoodsVO vo = new GoodsVO();
        org.springframework.beans.BeanUtils.copyProperties(goods, vo);

        long start = goods.getStartDate().getTime();
        long end = goods.getEndDate().getTime();
        long current = now.getTime();

        if (current < start) {
            vo.setSeckillStatus(0);
            vo.setRemainSeconds((start - current) / 1000);
        } else if (current > end) {
            vo.setSeckillStatus(2);
            vo.setRemainSeconds(0L);
        } else {
            vo.setSeckillStatus(1);
            vo.setRemainSeconds((end - current) / 1000);
        }
        return vo;
    }
}
