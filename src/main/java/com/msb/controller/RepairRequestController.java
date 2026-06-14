package com.msb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.pojo.RepairRequest;
import com.msb.service.RepairRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * ✨新建✨ 报修管理控制器
 * 业主端：提交报修 + 查看自己的报修列表
 * 管理员端：查看所有报修 + 处理报修
 */
@RestController
@RequestMapping("/api")
public class RepairRequestController {

    @Autowired
    private RepairRequestService repairRequestService;

    @Autowired
    private CacheService cacheService;

    // ==================== 业主端 ====================

    /**
     * ✨新建✨ 业主提交报修申请
     * POST /api/owner/repairs
     */
    @PostMapping("/owner/repairs")
    public Result<RepairRequest> submitRepair(@RequestBody RepairRequest repair,
                                              HttpServletRequest request) {
        // 从 TokenInterceptor 获取当前登录用户
        Integer userId = (Integer) request.getAttribute("userId");
        String userType = (String) request.getAttribute("userType");

        // 校验角色：仅业主可提交报修
        if (!"owner".equals(userType)) {
            return Result.fail(403, "权限不足，仅业主可提交报修");
        }

        try {
            RepairRequest result = repairRequestService.submitRepair(repair, userId);
            // 报修提交 → 清除业主报修缓存
            cacheService.clearRepairsByOwner(userId);
            return Result.success("报修提交成功", result);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 业主查看自己的报修列表
     * GET /api/owner/repairs
     */
    @GetMapping("/owner/repairs")
    public Result<IPage<RepairRequest>> getMyRepairs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String userType = (String) request.getAttribute("userType");
        if (!"owner".equals(userType)) return Result.fail(403, "权限不足");

        // Cache-Aside：按 ownerId + page + size 缓存
        String cacheKey = CacheService.REPAIR_OWNER_PREFIX + userId + ":" + page + ":" + pageSize;
        IPage<RepairRequest> cached = cacheService.getPageValue(cacheKey, RepairRequest.class);
        if (cached != null) {
            return Result.success(cached);
        }
        IPage<RepairRequest> result = repairRequestService.pageByOwnerId(userId, page, pageSize);
        cacheService.setValue(cacheKey, result, Duration.ofMinutes(5));
        return Result.success(result);
    }

    // ==================== 管理员端 ====================

    /**
     * ✨新建✨ 管理员查看所有报修列表
     * GET /api/admin/repairs
     */
    @GetMapping("/admin/repairs")
    public Result<IPage<RepairRequest>> getAllRepairs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) return Result.fail(403, "权限不足");

        // 仅无状态筛选时走缓存（最常用场景）
        if (status == null || status.isEmpty()) {
            String cacheKey = CacheService.REPAIR_ADMIN_PREFIX + page + ":" + pageSize;
            IPage<RepairRequest> cached = cacheService.getPageValue(cacheKey, RepairRequest.class);
            if (cached != null) {
                return Result.success(cached);
            }
            IPage<RepairRequest> result = repairRequestService.pageAllRepairs(page, pageSize, status);
            cacheService.setValue(cacheKey, result, Duration.ofMinutes(5));
            return Result.success(result);
        }
        return Result.success(repairRequestService.pageAllRepairs(page, pageSize, status));
    }

    /**
     * ✨新建✨ 管理员标记报修为"处理中"
     * PUT /api/admin/repairs/{id}/process
     */
    @PutMapping("/admin/repairs/{id}/process")
    public Result<Void> markProcessing(@PathVariable Integer id,
                                       HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");

        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }

        try {
            repairRequestService.markProcessing(id);
            // 报修状态变更 → 清除管理端报修缓存
            cacheService.clearAdminRepairs();
            return Result.success("已标记为处理中", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 管理员标记报修为"已完成"
     * PUT /api/admin/repairs/{id}/complete
     */
    @PutMapping("/admin/repairs/{id}/complete")
    public Result<Void> markComplete(@PathVariable Integer id,
                                     HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");

        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }

        try {
            repairRequestService.markComplete(id);
            // 报修状态变更 → 清除管理端报修缓存
            cacheService.clearAdminRepairs();
            return Result.success("已标记为已完成", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
