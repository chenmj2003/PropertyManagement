package com.msb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.pojo.ParkingSpot;
import com.msb.service.FlashSaleService;
import com.msb.service.ParkSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/parking")
public class AdminParkingController {
    @Autowired
    private ParkSpotService parkSpotService;

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private CacheService cacheService;

    @PostMapping("/spots")
    public Result addSpot(@RequestBody ParkingSpot parkingSpot){
        parkSpotService.addSpot(parkingSpot);
        cacheService.clearDashboard();
        cacheService.clearAvailableParkingSpots();
        return Result.success("车位发布成功");
    }

    @GetMapping("/spots")
    public Result<IPage<ParkingSpot>> listSpots(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(parkSpotService.pageByBuilding(buildingId, page, pageSize));
    }

    // ==================== 抢购车位管理 ====================

    /**
     * 管理员发布抢购车位
     * POST /api/admin/parking/flash-sale
     * Body: { spotCode, buildingId, location, price, flashSaleTime, flashSalePrice }
     *
     * 用 Map 接收参数，手动解析 flashSaleTime，避免 Jackson LocalDateTime 反序列化问题
     */
    @PostMapping("/flash-sale")
    public Result publishFlashSale(@RequestBody Map<String, Object> params) {
            ParkingSpot spot = new ParkingSpot();
            spot.setSpotCode((String) params.get("spotCode"));
            spot.setBuildingId(Long.valueOf(params.get("buildingId").toString()));
            spot.setLocation((String) params.get("location"));
            spot.setPrice(Double.valueOf(params.get("price").toString()));
            spot.setStatus("idle");
            spot.setSaleType("flash_sale");
            spot.setCreateTime(LocalDateTime.now());

            // 手动解析抢购时间: "yyyy-MM-dd HH:mm:ss"
            String timeStr = (String) params.get("flashSaleTime");
            if (timeStr != null && !timeStr.isEmpty()) {
                spot.setFlashSaleTime(LocalDateTime.parse(
                        timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // 抢购特价（可选）
            Object fp = params.get("flashSalePrice");
            if (fp != null) {
                spot.setFlashSalePrice(Double.valueOf(fp.toString()));
            }

            parkSpotService.addSpot(spot);

            cacheService.clearDashboard();
            cacheService.clearAvailableParkingSpots();
            return Result.success("抢购车位发布成功", null);

    }

    /**
     * 管理员查看所有抢购车位
     * GET /api/admin/parking/flash-sales
     */
    @GetMapping("/flash-sales")
    public Result listFlashSales() {
        return Result.success(flashSaleService.listFlashSaleSpots());
    }
}
