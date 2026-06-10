package com.msb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.pojo.ParkingSpotApplication;
import com.msb.service.ParkingSpotApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/parking")
public class AdminParkingApplicationController {
    @Autowired
    private ParkingSpotApplicationService applicationService;

    @Autowired
    private CacheService cacheService;

    // 管理员查看所有申请
    @GetMapping("/applications")
    public Result<IPage<ParkingSpotApplication>> getAllApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(applicationService.page(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ParkingSpotApplication>()
                        .orderByDesc(ParkingSpotApplication::getApplyTime)));
    }
    // 审核通过
    @PutMapping("/approve/{applicationId}")
    public Result<Void> approve(@PathVariable Long applicationId,@RequestAttribute Integer userId){
        try {
            applicationService.approve(applicationId,userId);
            // 车位申请状态变更 → 清除仪表盘缓存，下次访问时自动重建
            cacheService.clearDashboard();
            return Result.success("审核通过",null);
        }catch (RuntimeException e){
            return Result.fail(e.getMessage());
        }
    }

    // 审核不通过
    @PutMapping("/reject/{applicationId}")
    public Result<Void> reject(@PathVariable Long applicationId,
                               @RequestAttribute Integer userId,
                               @RequestBody Map<String,String> body){
        try {
            applicationService.reject(applicationId,userId,body.get("reason"));
            // 车位申请状态变更 → 清除仪表盘缓存，下次访问时自动重建
            cacheService.clearDashboard();
            return Result.success("已拒绝",null);
        }catch (RuntimeException e){
            return Result.fail(e.getMessage());
        }
    }
}
