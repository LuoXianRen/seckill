package com.example.seckill.controller;

import com.example.seckill.entity.SeckillUser;
import com.example.seckill.service.GoodsService;
import com.example.seckill.util.UserContext;
import com.example.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 商品页面控制器。
 *
 * @author example
 */
@Controller
@RequestMapping("/seckill")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 秒杀商品列表页
     */
    @GetMapping("/list")
    public String list(Model model) {
        List<GoodsVO> goodsList = goodsService.listGoodsVO();
        model.addAttribute("goodsList", goodsList);
        model.addAttribute("user", UserContext.getUser());
        return "goodsList";
    }

    /**
     * 商品详情页（含倒计时）
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        GoodsVO goods = goodsService.getGoodsVOById(id);
        if (goods == null) {
            return "redirect:/seckill/list";
        }
        SeckillUser user = UserContext.getUser();
        model.addAttribute("goods", goods);
        model.addAttribute("user", user);
        return "goodsDetail";
    }
}
