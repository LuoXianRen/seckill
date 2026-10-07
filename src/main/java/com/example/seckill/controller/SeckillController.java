package com.example.seckill.controller;

import com.example.seckill.annotation.AccessLimit;
import com.example.seckill.common.Result;
import com.example.seckill.entity.SeckillUser;
import com.example.seckill.service.CaptchaService;
import com.example.seckill.service.SeckillService;
import com.example.seckill.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀核心接口（JSON）。
 *
 * @author example
 */
@Slf4j
@Tag(name = "秒杀接口")
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 图形验证码：返回验证码图片。
     */
    @Operation(summary = "获取图形验证码")
    @AccessLimit(seconds = 5, maxCount = 10)
    @GetMapping("/captcha/{goodsId}")
    public void captcha(@PathVariable("goodsId") Long goodsId, HttpServletResponse response)
            throws Exception {
        SeckillUser user = requireLogin();
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            captchaService.writeCaptcha(user.getId(), goodsId, out);
            out.flush();
        }
    }

    /**
     * 获取动态秒杀地址：先校验验证码，再返回隐藏 path。
     */
    @Operation(summary = "获取动态秒杀地址（需先通过验证码）")
    @AccessLimit(seconds = 5, maxCount = 5)
    @GetMapping("/path/{goodsId}")
    public Result<Map<String, String>> getPath(@PathVariable("goodsId") Long goodsId,
                                               @RequestParam("captcha") String captcha) {
        SeckillUser user = requireLogin();
        if (!captchaService.checkCaptcha(user.getId(), goodsId, captcha)) {
            return Result.error(com.example.seckill.common.ResultCode.SECKILL_CAPTCHA_ERROR);
        }
        String path = seckillService.createPath(user.getId(), goodsId);
        Map<String, String> data = new HashMap<>(2);
        data.put("path", path);
        return Result.success(data);
    }

    /**
     * 发起秒杀（隐藏地址）：预减库存 + MQ 异步下单，立即返回排队中。
     */
    @Operation(summary = "发起秒杀（动态path）")
    @AccessLimit(seconds = 5, maxCount = 5)
    @PostMapping("/{path}/do_seckill")
    public Result<Map<String, Object>> doSeckill(@PathVariable("path") String path,
                                                 @RequestParam("goodsId") Long goodsId) {
        SeckillUser user = requireLogin();
        long orderId = seckillService.doSeckill(user.getId(), goodsId, path);
        Map<String, Object> data = new HashMap<>(2);
        data.put("orderId", orderId);
        data.put("queueing", true);
        return Result.success(data);
    }

    /**
     * 轮询秒杀结果：正数=订单号，0=排队中，负数=失败原因码。
     */
    @Operation(summary = "查询秒杀结果")
    @AccessLimit(seconds = 5, maxCount = 10)
    @GetMapping("/result/{goodsId}")
    public Result<Long> result(@PathVariable("goodsId") Long goodsId) {
        SeckillUser user = requireLogin();
        return Result.success(seckillService.getSeckillResult(user.getId(), goodsId));
    }

    private SeckillUser requireLogin() {
        SeckillUser user = UserContext.getUser();
        if (user == null) {
            throw new com.example.seckill.exception.GlobalException(
                    com.example.seckill.common.ResultCode.SESSION_ERROR);
        }
        return user;
    }
}
