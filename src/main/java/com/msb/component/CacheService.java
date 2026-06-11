package com.msb.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ==================== 缓存服务组件 ====================
 * 统一管理 Redis 缓存：仪表盘、收支统计、楼栋、车辆、公告
 *
 * 缓存策略：Cache-Aside（旁路缓存）
 *   - 读：先查 Redis → 命中返回 / 未命中查 DB → 写入 Redis
 *   - 写：先更新 DB → 删除 Redis 缓存 → 下次读时自动重建
 *
 * TTL 一览：
 *   - 仪表盘：5分钟     - 收支统计：5分钟
 *   - 楼栋：30分钟（几乎不变）    - 车辆：10分钟
 *   - 公告：10分钟
 */
@Component
public class CacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 使用 Spring Boot 自动配置的 ObjectMapper（支持 LocalDateTime、JavaTimeModule 等）
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 缓存 Key 常量 ====================
    public static final String DASHBOARD_KEY            = "cache:dashboard";
    public static final String INCOME_EXPENSE_STATS_KEY = "cache:income_expense:stats";
    public static final String BUILDING_LIST_KEY        = "cache:building:all";              // 楼栋列表
    public static final String VEHICLE_ALL_KEY          = "cache:vehicle:all";              // 全部车辆（管理员）
    public static final String VEHICLE_OWNER_PREFIX     = "cache:vehicle:owner:";           // 业主车辆 → +ownerId
    public static final String ANNOUNCEMENT_PREFIX      = "cache:announcement:page:";       // 公告分页 → +page:size

    // ==================== TTL ====================
    private static final long DASHBOARD_TTL_MINUTES  = 5L;
    private static final long STATS_TTL_MINUTES      = 5L;
    private static final long BUILDING_TTL_MINUTES   = 30L;  // 楼栋几乎不变
    private static final long VEHICLE_TTL_MINUTES    = 10L;
    private static final long ANNOUNCEMENT_TTL_MINUTES = 10L;

    // ==================== 仪表盘缓存 ====================

    public Map<String, Object> getDashboard() {
        return getFromRedis(DASHBOARD_KEY, Map.class);
    }

    public void setDashboard(Map<String, Object> data) {
        setValue(DASHBOARD_KEY, data, Duration.ofMinutes(DASHBOARD_TTL_MINUTES));
    }

    public void clearDashboard() {
        redisTemplate.delete(DASHBOARD_KEY);
    }

    // ==================== 收支统计缓存 ====================

    public Map<String, Object> getIncomeExpenseStats() {
        return getFromRedis(INCOME_EXPENSE_STATS_KEY, Map.class);
    }

    public void setIncomeExpenseStats(Map<String, Object> data) {
        setValue(INCOME_EXPENSE_STATS_KEY, data, Duration.ofMinutes(STATS_TTL_MINUTES));
    }

    public void clearIncomeExpenseStats() {
        redisTemplate.delete(INCOME_EXPENSE_STATS_KEY);
    }

    // ==================== 楼栋缓存（只读，无清除） ====================

    /**
     * 读取楼栋列表缓存
     */
    public List<?> getBuildingList() {
        return getListFromRedis(BUILDING_LIST_KEY, Object.class);
    }

    /**
     * 写入楼栋列表缓存（TTL 30分钟）
     */
    public void setBuildingList(List<?> buildings) {
        setValue(BUILDING_LIST_KEY, buildings, Duration.ofMinutes(BUILDING_TTL_MINUTES));
    }

    // ==================== 车辆缓存 ====================

    /** 管理员端：读取所有车辆缓存 */
    public List<?> getVehicleAll() {
        return getListFromRedis(VEHICLE_ALL_KEY, Object.class);
    }

    /** 管理员端：写入所有车辆缓存 */
    public void setVehicleAll(List<?> vehicles) {
        setValue(VEHICLE_ALL_KEY, vehicles, Duration.ofMinutes(VEHICLE_TTL_MINUTES));
    }

    /** 业主端：读取某业主的车辆缓存 */
    public List<?> getOwnerVehicles(Integer ownerId) {
        return getListFromRedis(VEHICLE_OWNER_PREFIX + ownerId, Object.class);
    }

    /** 业主端：写入某业主的车辆缓存 */
    public void setOwnerVehicles(Integer ownerId, List<?> vehicles) {
        setValue(VEHICLE_OWNER_PREFIX + ownerId, vehicles, Duration.ofMinutes(VEHICLE_TTL_MINUTES));
    }

    /** 车辆增删改后清除全部车辆缓存和管理员缓存 */
    public void clearVehicleAll() {
        redisTemplate.delete(VEHICLE_ALL_KEY);
    }

    /** 车辆增删改后清除某个业主的车辆缓存 */
    public void clearOwnerVehicles(Integer ownerId) {
        redisTemplate.delete(VEHICLE_OWNER_PREFIX + ownerId);
    }

    // ==================== 公告缓存 ====================
    // 公告缓存存储完整的 IPage<Announcement> 对象（含 records + total + pages 等分页信息）

    /** 读取某页公告缓存 */
    @SuppressWarnings("unchecked")
    public <T> com.baomidou.mybatisplus.core.metadata.IPage<T> getAnnouncementPage(
            long page, long pageSize, Class<T> elementType) {
        String key = ANNOUNCEMENT_PREFIX + page + ":" + pageSize;
        JavaType pageType = objectMapper.getTypeFactory()
                .constructParametricType(
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page.class,
                        elementType);
        return getValueWithType(key, pageType);
    }

    /** 写入某页公告缓存（存储完整 IPage 对象） */
    public <T> void setAnnouncementPage(long page, long pageSize,
                                         com.baomidou.mybatisplus.core.metadata.IPage<T> data) {
        String key = ANNOUNCEMENT_PREFIX + page + ":" + pageSize;
        setValue(key, data, Duration.ofMinutes(ANNOUNCEMENT_TTL_MINUTES));
    }

    /** 公告增删改后清除所有分页缓存（用通配删除） */
    public void clearAnnouncements() {
        redisTemplate.delete(redisTemplate.keys(ANNOUNCEMENT_PREFIX + "*"));
    }

    // ==================== 通用工具：公开 ====================

    /**
     * 通用缓存写入——将任意对象序列化为 JSON 写入 Redis
     */
    public void setValue(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            System.err.println("[CacheService] JSON 序列化失败 key=" + key + ": " + e.getMessage());
        }
    }

    /**
     * 通用缓存读取——从 Redis 读取 JSON 并反序列化为指定类型
     */
    public <T> T getValue(String key, Class<T> clazz) {
        return getFromRedis(key, clazz);
    }

    /**
     * 通用缓存读取——支持泛型类型（如 Page<Announcement>、List<Vehicle>）
     * 用法：JavaType type = mapper.getTypeFactory().constructParametricType(Page.class, Announcement.class);
     */
    public <T> T getValueWithType(String key, JavaType type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            redisTemplate.delete(key); // 脏数据清除
            return null;
        }
    }

    /**
     * 通用删除
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // ==================== 内部工具方法 ====================

    /**
     * Redis → JSON → 对象
     */
    private <T> T getFromRedis(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            redisTemplate.delete(key); // 脏数据清除
            return null;
        }
    }

    /**
     * Redis → JSON → List（用于车辆/楼栋等简单列表）
     * 注：List 内部元素是 LinkedHashMap，调用方需要自行类型转换
     */
    @SuppressWarnings("unchecked")
    private List<?> getListFromRedis(String key, Class<?> elementType) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            redisTemplate.delete(key);
            return null;
        }
    }
}
