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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 管理员首页仪表盘
 * 11 个独立查询全部并行执行 → 耗时从 Σ变为 max(单次)
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

    /** 自定义线程池（4核心/8最大） */
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * ⚡多线程⚡ 仪表盘数据接口
     * 11 条独立查询全部异步并行，结果用 CompletableFuture.allOf 等待后组装
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {

        // ==================== Cache-Aside 读：先查 Redis ====================
        Map<String, Object> cached = cacheService.getDashboard();
        if (cached != null) {
            return Result.success(cached);
        }

        // ==================== 并行发起 11 个数据库查询 ====================

        // ── 1. 业主总数 ──
        CompletableFuture<Long> ownerCountFuture = CompletableFuture.supplyAsync(
                () -> ownerMapper.selectCount(null), taskExecutor);

        // ── 2. 车位统计（4 条） ──
        CompletableFuture<Long> spotTotalFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotMapper.selectCount(null), taskExecutor);
        CompletableFuture<Long> spotSoldFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotMapper.selectCount(
                        new QueryWrapper<ParkingSpot>().eq("status", "sold")), taskExecutor);
        CompletableFuture<Long> spotIdleFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotMapper.selectCount(
                        new QueryWrapper<ParkingSpot>().eq("status", "idle")), taskExecutor);
        CompletableFuture<Long> spotApplyingFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotMapper.selectCount(
                        new QueryWrapper<ParkingSpot>().eq("status", "applying")), taskExecutor);

        // ── 3. 车位申请统计（3 条） ──
        CompletableFuture<Long> pendingAppCountFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotApplicationMapper.selectCount(
                        new QueryWrapper<ParkingSpotApplication>().eq("status", "applying")), taskExecutor);
        CompletableFuture<Long> approvedAppCountFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotApplicationMapper.selectCount(
                        new QueryWrapper<ParkingSpotApplication>().eq("status", "approved")), taskExecutor);
        CompletableFuture<Long> paidAppCountFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotApplicationMapper.selectCount(
                        new QueryWrapper<ParkingSpotApplication>().eq("status", "paid")), taskExecutor);

        // ── 4. 缴费通知统计（2 条） ──
        CompletableFuture<Long> unpaidNoticeCountFuture = CompletableFuture.supplyAsync(
                () -> paymentNotificationMapper.selectCount(
                        new QueryWrapper<PaymentNotification>().eq("status", "unpaid")), taskExecutor);
        CompletableFuture<Long> paidNoticeCountFuture = CompletableFuture.supplyAsync(
                () -> paymentNotificationMapper.selectCount(
                        new QueryWrapper<PaymentNotification>().eq("status", "paid")), taskExecutor);

        // ── 5. 最近5条申请记录 ──
        CompletableFuture<List<ParkingSpotApplication>> recentAppsFuture = CompletableFuture.supplyAsync(
                () -> parkingSpotApplicationMapper.selectList(
                        new QueryWrapper<ParkingSpotApplication>()
                                .orderByDesc("apply_time")
                                .last("LIMIT 5")), taskExecutor);

        // ==================== 等待全部查询完成 ====================
        CompletableFuture.allOf(
                ownerCountFuture,
                spotTotalFuture, spotSoldFuture, spotIdleFuture, spotApplyingFuture,
                pendingAppCountFuture, approvedAppCountFuture, paidAppCountFuture,
                unpaidNoticeCountFuture, paidNoticeCountFuture,
                recentAppsFuture
        ).join(); // 阻塞直到全部完成，任一失败则抛异常

        // ==================== 组装结果（join() 不会阻塞，数据已就绪） ====================
        Map<String, Object> data = new HashMap<>();

        data.put("ownerCount", ownerCountFuture.join());
        data.put("spotTotal", spotTotalFuture.join());
        data.put("spotSold", spotSoldFuture.join());
        data.put("spotIdle", spotIdleFuture.join());
        data.put("spotApplying", spotApplyingFuture.join());
        data.put("pendingAppCount", pendingAppCountFuture.join());
        data.put("approvedAppCount", approvedAppCountFuture.join());
        data.put("paidAppCount", paidAppCountFuture.join());
        data.put("unpaidNoticeCount", unpaidNoticeCountFuture.join());
        data.put("paidNoticeCount", paidNoticeCountFuture.join());
        data.put("recentApps", recentAppsFuture.join());

        // ==================== Cache-Aside 写：将结果写入 Redis ====================
        cacheService.setDashboard(data);

        return Result.success(data);
    }
}
