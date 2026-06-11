package com.msb.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ==================== 登录限流组件 ====================
 * 使用 Redis INCR + TTL 实现 IP + 账号双维度限流
 *
 * 策略：
 *   - IP 维度：60秒内最多失败 10 次，超过后锁定 15 分钟
 *   - 账号维度：60秒内最多失败 5 次，超过后锁定 15 分钟
 *   - 登录成功后自动清除该 IP 和账号的失败记录
 *
 * Redis Key 设计：
 *   login:rate:ip:{ip}       → 计数（60s TTL）
 *   login:rate:account:{acc} → 计数（60s TTL）
 *   login:lock:ip:{ip}       → 锁定标记（15min TTL）
 *   login:lock:account:{acc} → 锁定标记（15min TTL）
 */
@Component
public class LoginRateLimiter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // ==================== 限流参数 ====================
    private static final long WINDOW_SECONDS = 60L;          // 计数窗口：60秒
    private static final long MAX_IP_ATTEMPTS = 10L;         // IP 维度上限
    private static final long MAX_ACCOUNT_ATTEMPTS = 5L;     // 账号维度上限
    private static final long LOCK_MINUTES = 15L;            // 触发上限后锁定时间

    // ==================== Redis Key 前缀 ====================
    private static final String IP_COUNT_KEY = "login:rate:ip:";
    private static final String ACCOUNT_COUNT_KEY = "login:rate:account:";
    private static final String IP_LOCK_KEY = "login:lock:ip:";
    private static final String ACCOUNT_LOCK_KEY = "login:lock:account:";

    // ==================== 锁定提示模板 ====================
    private static final String IP_LOCKED_MSG = "当前网络登录过于频繁，已被临时锁定，请 %d 秒后重试";
    private static final String ACCOUNT_LOCKED_MSG = "该账号登录尝试次数过多，已被临时锁定，请 %d 秒后重试";

    /**
     * 登录前检查是否被限流
     *
     * @param ip      客户端真实 IP
     * @param account 登录账号
     * @return null 表示未被限流，可以继续登录；非 null 为错误提示信息
     */
    public String checkRateLimit(String ip, String account) {
        // 1. 检查 IP 是否被锁定
        String ipLocked = redisTemplate.opsForValue().get(IP_LOCK_KEY + ip);
        if (ipLocked != null) {
            Long ttl = redisTemplate.getExpire(IP_LOCK_KEY + ip);
            return String.format(IP_LOCKED_MSG, ttl != null ? ttl : LOCK_MINUTES * 60);
        }

        // 2. 检查账号是否被锁定
        String accountLocked = redisTemplate.opsForValue().get(ACCOUNT_LOCK_KEY + account);
        if (accountLocked != null) {
            Long ttl = redisTemplate.getExpire(ACCOUNT_LOCK_KEY + account);
            return String.format(ACCOUNT_LOCKED_MSG, ttl != null ? ttl : LOCK_MINUTES * 60);
        }

        return null; // 未被限流
    }

    /**
     * 登录失败后记录一次失败尝试
     * 达到上限则自动加锁
     *
     * @param ip      客户端真实 IP
     * @param account 登录账号
     */
    public void recordFailedAttempt(String ip, String account) {
        // ---- IP 维度 ----
        String ipKey = IP_COUNT_KEY + ip;
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        // 首次失败设置计数窗口
        if (ipCount != null && ipCount == 1) {
            redisTemplate.expire(ipKey, Duration.ofSeconds(WINDOW_SECONDS));
        }
        // 超过阈值 → 触发锁定时
        if (ipCount != null && ipCount >= MAX_IP_ATTEMPTS) {
            redisTemplate.opsForValue().set(IP_LOCK_KEY + ip, "1", Duration.ofMinutes(LOCK_MINUTES));
            redisTemplate.delete(ipKey); // 清空计数器，等锁过期后重新计数
        }

        // ---- 账号维度 ----
        String accountKey = ACCOUNT_COUNT_KEY + account;
        Long accountCount = redisTemplate.opsForValue().increment(accountKey);
        if (accountCount != null && accountCount == 1) {
            redisTemplate.expire(accountKey, Duration.ofSeconds(WINDOW_SECONDS));
        }
        if (accountCount != null && accountCount >= MAX_ACCOUNT_ATTEMPTS) {
            redisTemplate.opsForValue().set(ACCOUNT_LOCK_KEY + account, "1", Duration.ofMinutes(LOCK_MINUTES));
            redisTemplate.delete(accountKey);
        }
    }

    /**
     * 登录成功后清除该 IP 和账号的所有失败记录与锁定标记
     *
     * @param ip      客户端真实 IP
     * @param account 登录账号
     */
    public void clearFailedAttempts(String ip, String account) {
        redisTemplate.delete(IP_COUNT_KEY + ip);
        redisTemplate.delete(ACCOUNT_COUNT_KEY + account);
        redisTemplate.delete(IP_LOCK_KEY + ip);
        redisTemplate.delete(ACCOUNT_LOCK_KEY + account);
    }

    // ==================== 工具方法 ====================

    /**
     * 从 HttpServletRequest 中提取客户端真实 IP
     * 考虑反向代理、CDN 等情况
     */
    public static String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        // 1. X-Forwarded-For（最常用，格式: client, proxy1, proxy2）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多级代理时取第一个 IP 即为真实客户端 IP
            int commaIdx = ip.indexOf(',');
            if (commaIdx > 0) {
                return ip.substring(0, commaIdx).trim();
            }
            return ip.trim();
        }

        // 2. X-Real-IP（Nginx 常用）
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 3. Proxy-Client-IP（Apache 服务器）
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 4. WL-Proxy-Client-IP（WebLogic 插件）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        // 5. 兜底：直连 IP
        return request.getRemoteAddr();
    }
}
