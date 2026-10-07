package com.example.seckill.interceptor;

import cn.hutool.json.JSONUtil;
import com.example.seckill.annotation.AccessLimit;
import com.example.seckill.common.Result;
import com.example.seckill.common.ResultCode;
import com.example.seckill.entity.SeckillUser;
import com.example.seckill.redis.RedisKeys;
import com.example.seckill.redis.RedisService;
import com.example.seckill.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 接口防刷拦截器：解析 {@link AccessLimit} 注解，基于 Redis 时间窗口计数。
 *
 * @author example
 */
@Slf4j
@Component
public class AccessInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
        if (accessLimit == null) {
            return true;
        }

        SeckillUser user = UserContext.getUser();
        if (accessLimit.needLogin() && user == null) {
            render(response, ResultCode.SESSION_ERROR);
            return false;
        }

        // 登录接口按 用户ID 限流；未登录接口按 客户端IP 限流
        String identity = user != null ? String.valueOf(user.getId())
                : String.format("ip_%d", request.getRemoteAddr() == null ? 0
                        : request.getRemoteAddr().hashCode());
        String key = RedisKeys.ACCESS_PREFIX + identity + ":" + request.getRequestURI();

        Long count = redisService.increment(key, accessLimit.seconds());
        if (count != null && count > accessLimit.maxCount()) {
            log.warn("接口访问过于频繁: uri={}, identity={}, count={}", request.getRequestURI(), identity, count);
            render(response, ResultCode.ACCESS_LIMIT_REACHED);
            return false;
        }
        return true;
    }

    private void render(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> result = Result.error(resultCode);
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}
