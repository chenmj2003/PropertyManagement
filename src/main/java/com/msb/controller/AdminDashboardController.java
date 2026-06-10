package com.msb.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.mapper.OwnerMapper;
import com.msb.mapper.ParkingSpotApplicationMapper;
import com.msb.mapper.ParkingSpotMapper;
import com.msb.mapper.PaymentNotificationMapper;
import com.msb.pojo.ParkingSpot;
import com.msb.pojo.ParkingSpotApplication;
import com.msb.pojo.PaymentNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员首页仪表盘
 * 聚合查询各项统计数据，不建 Service 层（仅简单统计，无需复用）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private ParkingSpotMapper parkingSpotMapper;

    @Autowired
    private ParkingSpotApplicationMapper parkingSpotApplicationMapper;

    @Autowired
    private PaymentNotificationMapper paymentNotificationMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 仪表盘数据接口 — 带 Redis 缓存
     * 返回业主数、车位统计、申请统计、缴费统计、最近申请
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {

        // ==================== Cache-Aside 读：先查 Redis ====================
        Map<String, Object> cached = cacheService.getDashboard();
        if (cached != null) {
            return Result.success(cached);
        }

        // ==================== Redis 未命中，查数据库 ====================
        Map<String, Object> data = new HashMap<>();

        // ==================== 1. 业主总数 ====================
        // Owner 表没有 status 字段，直接统计全表
        Long ownerCount = ownerMapper.selectCount(null);
        data.put("ownerCount", ownerCount);

        // ==================== 2. 车位统计 ====================
        // 车位总数
        Long spotTotal = parkingSpotMapper.selectCount(null);
        data.put("spotTotal", spotTotal);

        // 已售车位
        Long spotSold = parkingSpotMapper.selectCount(
                new QueryWrapper<ParkingSpot>().eq("status", "sold")
        );
        data.put("spotSold", spotSold);

        // 空闲车位
        Long spotIdle = parkingSpotMapper.selectCount(
                new QueryWrapper<ParkingSpot>().eq("status", "idle")
        );
        data.put("spotIdle", spotIdle);

        // 申购中车位（已被申请但未完成支付）
        Long spotApplying = parkingSpotMapper.selectCount(
                new QueryWrapper<ParkingSpot>().eq("status", "applying")
        );
        data.put("spotApplying", spotApplying);

        // ==================== 3. 车位申请统计 ====================
        // 待审核的申请
        Long pendingAppCount = parkingSpotApplicationMapper.selectCount(
                new QueryWrapper<ParkingSpotApplication>().eq("status", "applying")
        );
        data.put("pendingAppCount", pendingAppCount);

        // 已通过的申请（待支付）
        Long approvedAppCount = parkingSpotApplicationMapper.selectCount(
                new QueryWrapper<ParkingSpotApplication>().eq("status", "approved")
        );
        data.put("approvedAppCount", approvedAppCount);

        // 已支付的申请（购买完成）
        Long paidAppCount = parkingSpotApplicationMapper.selectCount(
                new QueryWrapper<ParkingSpotApplication>().eq("status", "paid")
        );
        data.put("paidAppCount", paidAppCount);

        // ==================== 4. 缴费通知统计 ====================
        // 待缴费通知数
        Long unpaidNoticeCount = paymentNotificationMapper.selectCount(
                new QueryWrapper<PaymentNotification>().eq("status", "unpaid")
        );
        data.put("unpaidNoticeCount", unpaidNoticeCount);

        // 已缴费通知数
        Long paidNoticeCount = paymentNotificationMapper.selectCount(
                new QueryWrapper<PaymentNotification>().eq("status", "paid")
        );
        data.put("paidNoticeCount", paidNoticeCount);

        // ==================== 5. 最近5条申请记录 ====================
        // 按申请时间倒序，取前5条
        List<ParkingSpotApplication> recentApps = parkingSpotApplicationMapper.selectList(
                new QueryWrapper<ParkingSpotApplication>()
                        .orderByDesc("apply_time")
                        .last("LIMIT 5")
        );
        data.put("recentApps", recentApps);

        // ==================== Cache-Aside 写：将结果写入 Redis ====================
        cacheService.setDashboard(data);

        return Result.success(data);
    }
}
