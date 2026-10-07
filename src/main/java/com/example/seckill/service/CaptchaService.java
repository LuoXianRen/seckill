package com.example.seckill.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.example.seckill.redis.RedisKeys;
import com.example.seckill.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务：Hutool 生成验证码图片，答案存 Redis（2 分钟过期）。
 *
 * @author example
 */
@Slf4j
@Service
public class CaptchaService {

    @Autowired
    private RedisService redisService;

    /**
     * 生成验证码图片并写入输出流。
     */
    public void writeCaptcha(Long userId, Long goodsId, OutputStream outputStream) {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(140, 44, 4, 30);
        String code = captcha.getCode();
        redisService.set(RedisKeys.captchaKey(userId, goodsId), code.toLowerCase(), 2, TimeUnit.MINUTES);
        captcha.write(outputStream);
    }

    /**
     * 校验验证码（校验成功即删除，防止重复使用）。
     */
    public boolean checkCaptcha(Long userId, Long goodsId, String input) {
        if (input == null) {
            return false;
        }
        String key = RedisKeys.captchaKey(userId, goodsId);
        String expected = redisService.get(key);
        if (expected == null) {
            return false;
        }
        boolean passed = expected.equalsIgnoreCase(input.trim());
        if (passed) {
            redisService.delete(key);
        }
        return passed;
    }
}
