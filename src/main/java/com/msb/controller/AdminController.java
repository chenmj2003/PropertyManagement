package com.msb.controller;

import com.msb.common.Result;
import com.msb.pojo.Building;
import com.msb.pojo.Vehicle;
import com.msb.service.BuildingService;
import com.msb.service.OwnerService;
import com.msb.service.VehicleService;
import com.msb.vo.OwnerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private VehicleService vehicleService;
    // 查询所有业主
    @GetMapping("/owners")
    public Result listOwners(){
        List<OwnerVo> ownerVos = ownerService.selectAllOwner();
        return Result.success(ownerVos);
    }
    @GetMapping("/buildings")
    public Result listBuildings(){
        List<Building> buildings = buildingService.Buildings();
        return Result.success(buildings);
    }
    @PostMapping("/logout")
    public Result logout(@RequestHeader("token") String token){
        ownerService.logout(token);
        return Result.success("已退出登录");
    }
    @GetMapping("/vehicles")
    public Result listVehicles(@RequestParam(required = false) String keyword){
        List<Vehicle> allVehicles = vehicleService.getAllVehicles(keyword);
        return Result.success(allVehicles);
    }
}
