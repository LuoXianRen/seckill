package com.example.seckill.config;

import com.example.seckill.interceptor.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册拦截器。
 * <p>
 * 执行顺序：登录态拦截器（order=1） -> 防刷拦截器（order=2）。
 *
 * @author example
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private AccessInterceptor accessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/do_login",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**"
                );

        registry.addInterceptor(accessInterceptor)
                .order(2)
                .addPathPatterns("/**");
    }
}
