package com.example.seckill.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口防刷注解。
 * <p>
 * 基于 Redis 计数，按 登录用户 + URL 限流。
 *
 * @author example
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    /**
     * 统计时间窗口（秒）
     */
    int seconds() default 5;

    /**
     * 时间窗口内最大访问次数
     */
    int maxCount() default 5;

    /**
     * 是否需要登录
     */
    boolean needLogin() default true;
}
