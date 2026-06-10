package com.msb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.pojo.Building;
import com.msb.pojo.ParkingSpot;
import com.msb.service.BuildingService;
import com.msb.service.ParkSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/parking")
public class AdminParkingController {
    @Autowired
    private ParkSpotService parkSpotService;

    @Autowired
    private CacheService cacheService;

    @PostMapping("/spots")
    public Result addSpot(@RequestBody ParkingSpot parkingSpot){
        parkSpotService.addSpot(parkingSpot);
        // 车位数据变更 → 清除仪表盘缓存，下次访问时自动重建
        cacheService.clearDashboard();
        return Result.success("车位发布成功");
    }

    @GetMapping("/spots")
    public Result<IPage<ParkingSpot>> listSpots(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(parkSpotService.pageByBuilding(buildingId, page, pageSize));
    }

}
