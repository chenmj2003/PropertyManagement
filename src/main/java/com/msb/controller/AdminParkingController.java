package com.msb.controller;

import com.msb.common.Result;
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

    @PostMapping("/spots")
    public Result addSpot(@RequestBody ParkingSpot parkingSpot){
        parkSpotService.addSpot(parkingSpot);
        return Result.success("车位发布成功");
    }

    @GetMapping("/spots")
    public Result listSpots(@RequestParam(required = false) Long buildingId){
        List<ParkingSpot> parkingSpots = parkSpotService.listByBuilding(buildingId);
        return Result.success(parkingSpots);
    }

}
