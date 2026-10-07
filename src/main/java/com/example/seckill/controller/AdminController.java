package com.example.seckill.controller;

import com.example.seckill.annotation.AccessLimit;
import com.example.seckill.common.Result;
import com.example.seckill.dto.GoodsAddDTO;
import com.example.seckill.service.GoodsService;
import com.example.seckill.util.UserContext;
import com.example.seckill.vo.GoodsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 简单管理后台：秒杀商品 CRUD（演示用，未做角色权限，登录即可访问）。
 *
 * @author example
 */
@Tag(name = "管理后台")
@Controller
@RequestMapping("/admin/goods")
public class AdminController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 商品管理页
     */
    @GetMapping
    public String goodsPage(Model model) {
        if (UserContext.getUser() == null) {
            return "redirect:/login";
        }
        List<GoodsVO> goodsList = goodsService.listGoodsVO();
        model.addAttribute("goodsList", goodsList);
        return "adminGoods";
    }

    /**
     * 新增秒杀商品
     */
    @Operation(summary = "新增秒杀商品")
    @AccessLimit(seconds = 10, maxCount = 10)
    @PostMapping("/add")
    @ResponseBody
    public Result<Long> add(@Validated @RequestBody GoodsAddDTO dto) {
        if (UserContext.getUser() == null) {
            return Result.error(com.example.seckill.common.ResultCode.SESSION_ERROR);
        }
        if (dto.getEndTimestamp() <= dto.getStartTimestamp()) {
            return Result.error(com.example.seckill.common.ResultCode.ERROR.getCode(),
                    "结束时间必须晚于开始时间");
        }
        return Result.success(goodsService.addGoods(dto));
    }
}
