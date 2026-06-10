package com.msb.service.impl;

import com.msb.mapper.TokenMapper;
import com.msb.pojo.LoginToken;
import com.msb.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "token:";    // Redis key 前缀
    private static final long TOKEN_TTL_HOURS = 24;         // Token 有效期

    /**
     * ✨Redis改造✨ 创建 token
     * 1. 写 MySQL（持久化）
     * 2. 写 Redis（缓存，24h TTL）
     */
    @Override
    public String createToken(int userId, String userType) {
        // 清除旧 token
        tokenMapper.deleteByUser(userId, userType);

        // 生成新 token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 写 MySQL
        LoginToken loginToken = new LoginToken();
        loginToken.setUserId(userId);
        loginToken.setUserType(userType);
        loginToken.setToken(token);
        loginToken.setExpireTime(LocalDateTime.now().plusHours(TOKEN_TTL_HOURS));
        tokenMapper.insert(loginToken);

        // ✨写 Redis — key=token:xxx, value=userId:userType, TTL=24h
        String redisKey = TOKEN_PREFIX + token;
        String redisValue = userId + ":" + userType;
        redisTemplate.opsForValue().set(redisKey, redisValue, Duration.ofHours(TOKEN_TTL_HOURS));

        return token;
    }

    /**
     * ✨Redis改造✨ 获取 token
     * 1. 先查 Redis（快）
     * 2. Redis 未命中再查 MySQL（兜底）
     * 3. MySQL 查到后回写 Redis
     */
    @Override
    public LoginToken getByToken(String token) {
        // 1. 先查 Redis
        String redisKey = TOKEN_PREFIX + token;
        String redisValue = redisTemplate.opsForValue().get(redisKey);

        if (redisValue != null) {
            // Redis 命中，直接返回
            String[] parts = redisValue.split(":");
            LoginToken loginToken = new LoginToken();
            loginToken.setUserId(Integer.parseInt(parts[0]));
            loginToken.setUserType(parts[1]);
            loginToken.setToken(token);
            return loginToken;
        }

        // 2. Redis 未命中，查 MySQL
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken != null) {
            if (loginToken.getExpireTime().isBefore(LocalDateTime.now())) {
                // 已过期，清理
                tokenMapper.deleteByToken(token);
                return null;
            }
            // 3. 回写 Redis
            String value = loginToken.getUserId() + ":" + loginToken.getUserType();
            redisTemplate.opsForValue().set(redisKey, value, Duration.ofHours(TOKEN_TTL_HOURS));
        }
        return loginToken;
    }

    /**
     * ✨Redis改造✨ 删除 token（同时删 MySQL 和 Redis）
     */
    @Override
    public void deleteToken(String token) {
        tokenMapper.deleteByToken(token);
        redisTemplate.delete(TOKEN_PREFIX + token);
    }
}
