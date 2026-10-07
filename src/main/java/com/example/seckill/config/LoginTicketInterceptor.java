package com.example.seckill.config;

import com.example.seckill.entity.SeckillUser;
import com.example.seckill.redis.RedisKeys;
import com.example.seckill.redis.RedisService;
import com.example.seckill.util.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录态拦截器：从 Cookie / 参数中取 token，校验 Redis 登录态。
 *
 * @author example
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    public static final String COOKIE_NAME = "seckill-token";

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = resolveToken(request);
        if (StringUtils.isNotBlank(token)) {
            SeckillUser user = redisService.get(RedisKeys.tokenKey(token));
            if (user != null) {
                UserContext.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.remove();
    }

    private String resolveToken(HttpServletRequest request) {
        // 优先取 Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 兼容 header / 参数传递
        String header = request.getHeader(COOKIE_NAME);
        return StringUtils.isNotBlank(header) ? header : request.getParameter("token");
    }
}
