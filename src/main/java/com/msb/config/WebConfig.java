package com.msb.config;

import com.msb.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web拦截器配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/api/**") // 拦截/api下的所有请求
                .excludePathPatterns(  // 排除以下请求
                        "/api/adminLogin",       // 管理员登录
                        "/api/ownerLogin",       // 业主登录
                        "/api/register",         // 业主注册
                        "/api/resetPwd",         // 重置密码
                        "/api/buildings"         // 获取楼栋列表（注册时需要）
                        );
    }
}
