package com.example.seckill.controller;

import com.example.seckill.annotation.AccessLimit;
import com.example.seckill.common.Result;
import com.example.seckill.common.ResultCode;
import com.example.seckill.config.LoginTicketInterceptor;
import com.example.seckill.dto.LoginDTO;
import com.example.seckill.entity.SeckillUser;
import com.example.seckill.redis.RedisKeys;
import com.example.seckill.redis.RedisService;
import com.example.seckill.service.UserService;
import com.example.seckill.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 登录控制器。
 *
 * @author example
 */
@Slf4j
@Controller
public class LoginController {

    /**
     * 登录态有效期：30 分钟
     */
    private static final long TOKEN_EXPIRE_MINUTES = 30L;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 登录接口：未登录限流 5 秒 5 次（按 IP）。
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = false)
    @PostMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(@Validated @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        SeckillUser user = userService.login(loginDTO);
        if (user == null) {
            return Result.error(ResultCode.ERROR.getCode(), "账号或密码错误");
        }

        // 登录态写入 Redis，token 写入 Cookie
        String token = UUIDUtil.uuid();
        redisService.set(RedisKeys.tokenKey(token), user, TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        Cookie cookie = new Cookie(LoginTicketInterceptor.COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setMaxAge((int) (TOKEN_EXPIRE_MINUTES * 60));
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        log.info("用户登录成功: id={}, mobile={}", user.getId(), user.getMobile());
        return Result.success(token);
    }
}
