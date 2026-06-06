package com.msb.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.common.Result;
import com.msb.pojo.LoginToken;
import com.msb.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 防止乱码
        response.setContentType("application/json;charset=UTF-8");
        // 获取请求头token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            Result<Void> result = Result.fail(401, "未登录，请先登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        LoginToken loginToken = tokenService.getByToken(token);

        if (loginToken == null) {
            Result<Void> result = Result.fail(401, "token已过期或无效，请重新登录");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
            return false;
        }

        request.setAttribute("userId",loginToken.getUserId());
        request.setAttribute("userType",loginToken.getUserType());

        return true;
    }
}
