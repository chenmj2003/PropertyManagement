package com.msb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.mapper.ParkingSpotMapper;
import com.msb.mapper.TokenMapper;
import com.msb.pojo.*;
import com.msb.service.OwnerService;
import com.msb.service.ParkingSpotApplicationService;
import com.msb.service.PaymentNotificationService;
import com.msb.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {
    @Autowired
    private OwnerService ownerService;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ParkingSpotApplicationService parkingSpotApplicationService;

    @Autowired
    private ParkingSpotMapper parkingSpotMapper;

    @Autowired
    private CacheService cacheService;

    @PostMapping("/logout")
    public Result logout(@RequestHeader("token") String token){
        ownerService.logout(token);
        return Result.success("已退出登录");
    }

    /**
     * ✨新建✨ 更新头像
     */
    @PutMapping("/avatar")
    public Result updateAvatar(@RequestHeader("token") String token,
                                @RequestBody Map<String, String> params) {
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null) {
            return Result.fail(401, "请重新登录");
        }
        String avatar = params.get("avatar");
        if (avatar == null || avatar.isEmpty()) {
            return Result.fail(400, "请提供头像路径");
        }
        Owner owner = ownerService.getOwnerById(loginToken.getUserId());
        owner.setAvatar(avatar);
        ownerService.updateOwner(owner);
        return Result.success("头像更新成功", null);
    }

    /**
     * ✨新建✨ 修改密码
     */
    @PutMapping("/password")
    public Result changePassword(@RequestHeader("token") String token,
                                  @RequestBody Map<String, String> params) {
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null) {
            return Result.fail(401, "请重新登录");
        }
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()
                || newPassword == null || newPassword.isEmpty()) {
            return Result.fail(400, "请填写原密码和新密码");
        }
        try {
            ownerService.changePassword(loginToken.getUserId(), oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result getInfo(@RequestHeader("token") String token){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        Integer ownerId = loginToken.getUserId();
        Owner owner = ownerService.getOwnerById(ownerId);
        if (owner != null){
            owner.setPassword(null);
        }
        return Result.success(owner);
    }

    @PutMapping("/info")
    public Result updateInfo(@RequestHeader("token") String token,@RequestBody Owner owner){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登陆");
        }
        Integer ownerId = loginToken.getUserId();
        owner.setId(ownerId);
        owner.setPassword(null);
        ownerService.updateOwner(owner);
        return Result.success("修改成功");
    }

    @GetMapping("/vehicles")
    public Result getMyVehicles(@RequestHeader("token") String token){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        Integer ownerId = loginToken.getUserId();

        // Cache-Aside 读：先查 Redis
        @SuppressWarnings("unchecked")
        List<Vehicle> cached = (List<Vehicle>) cacheService.getOwnerVehicles(ownerId);
        if (cached != null) {
            return Result.success(cached);
        }

        List<Vehicle> myVehicle = vehicleService.getMyVehicle(ownerId);

        // 写入 Redis
        cacheService.setOwnerVehicles(ownerId, myVehicle);

        return Result.success(myVehicle);
    }

    @PostMapping("/vehicle")
    public Result addVehicle(@RequestHeader("token") String token,@RequestBody Vehicle vehicle){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        Integer ownerId = loginToken.getUserId();
        vehicle.setId(null);
        vehicle.setOwnerId(ownerId);
        vehicleService.addVehicle(vehicle);
        // 车辆数据变更 → 清除管理员全量缓存 + 当前业主缓存
        cacheService.clearVehicleAll();
        cacheService.clearOwnerVehicles(ownerId);
        return Result.success("添加成功");
    }

    @PutMapping("/vehicle/{id}")
    public Result updateVehicle(@RequestHeader("token") String token,
                                @RequestBody Vehicle vehicle,
                                @PathVariable Integer id){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        Integer ownerId = loginToken.getUserId();
        vehicle.setId(id);
        try {
            vehicleService.updateVehicle(vehicle, ownerId);
            // 车辆数据变更 → 清除管理员全量缓存 + 当前业主缓存
            cacheService.clearVehicleAll();
            cacheService.clearOwnerVehicles(ownerId);
            return Result.success("修改成功");
        }catch (Exception e){
            return Result.fail(403,e.getMessage());
        }
    }

    @DeleteMapping("/vehicle/{id}")
    public Result deleteVehicle(@RequestHeader("token") String token,@PathVariable Integer id){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        Integer ownerId = loginToken.getUserId();
        try {
            vehicleService.deleteVehicle(id, ownerId);
            // 车辆数据变更 → 清除管理员全量缓存 + 当前业主缓存
            cacheService.clearVehicleAll();
            cacheService.clearOwnerVehicles(ownerId);
            return Result.success("删除成功");
        }catch (Exception e){
            return Result.fail(403,e.getMessage());
        }
    }

    /**
     * 查看可购买的所有车位（带 Redis 缓存，TTL 3 分钟）
     */
    @GetMapping("/parking-spots")
    public Result<List<ParkingSpot>> getAvailableSpots(@RequestHeader("token") String token){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        // Cache-Aside 读
        List<ParkingSpot> cached = cacheService.getListValue(
                CacheService.PARKING_AVAILABLE_KEY, ParkingSpot.class);
        if (cached != null) {
            return Result.success(cached);
        }
        List<ParkingSpot> spots = parkingSpotMapper.selectList(
                new QueryWrapper<ParkingSpot>().in("status", "idle", "applying").orderByAsc("spot_code")
        );
        cacheService.setValue(CacheService.PARKING_AVAILABLE_KEY, spots,
                java.time.Duration.ofMinutes(3));
        return Result.success(spots);
    }

    @GetMapping("/parking-spot/{spotid}")
    public Result<ParkingSpot> getSpotDetail(@RequestHeader("token") String token, @PathVariable Long spotId){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        ParkingSpot spot = parkingSpotMapper.selectById(spotId);
        if (spot == null){
            return Result.fail(401,"车位不存在");
        }
        return Result.success(spot);
    }

    @PostMapping("/parking-apply/{spotId}")
    public Result applyForSpot(@RequestHeader("token") String token,@PathVariable Long spotId){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        try {
            parkingSpotApplicationService.apply(spotId,loginToken.getUserId());
            // 车位申请 → 清除仪表盘 + 可用车位缓存
            cacheService.clearDashboard();
            cacheService.clearAvailableParkingSpots();
            return Result.success("申请已提交，请等待管理员审核");
        }catch (RuntimeException e){
            return Result.fail(e.getMessage());
        }
    }

    // 查看车位购买记录
    @GetMapping("/parking-applications")
    public Result<List<ParkingSpotApplication>> getMyApplications(@RequestHeader("token") String token){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null){
            return Result.fail(401,"请重新登录");
        }
        List<ParkingSpotApplication> list = parkingSpotApplicationService.list(
                new LambdaQueryWrapper<ParkingSpotApplication>()
                        .eq(ParkingSpotApplication::getOwnerId, loginToken.getUserId())
                        .orderByAsc(ParkingSpotApplication::getApplyTime)
        );
        return Result.success(list);
    }
    // 直接支付
    @PostMapping("/parking-pay/{applicationId}")
    public Result<Void> payForSpot(@RequestHeader("token") String token,
                                   @PathVariable Long applicationId){
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null) {
            return Result.fail(401, "请重新登录");
        }
        try {
            parkingSpotApplicationService.directPay(applicationId, loginToken.getUserId());
            // 车位支付 → 清除仪表盘 + 收支统计 + 收支列表 + 可用车位
            cacheService.clearDashboard();
            cacheService.clearIncomeExpenseStats();
            cacheService.clearIncomeExpenseList();
            cacheService.clearAvailableParkingSpots();
            return Result.success("支付成功", null);
        }catch (RuntimeException e){
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查看已购车位
     */
    @GetMapping("/purchased-spots")
    public Result<List<Map<String, Object>>> getPurchasedSpots(@RequestHeader("token") String token) {
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken == null) {
            return Result.fail(401, "请重新登录");
        }

        // 查询该业主已支付的申请记录
        List<ParkingSpotApplication> apps = parkingSpotApplicationService.list(
                new LambdaQueryWrapper<ParkingSpotApplication>()
                        .eq(ParkingSpotApplication::getOwnerId, loginToken.getUserId())
                        .eq(ParkingSpotApplication::getStatus, "paid")
                        .orderByDesc(ParkingSpotApplication::getPayTime)
        );

        // 组装车位信息 + 支付信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (ParkingSpotApplication app : apps) {
            ParkingSpot spot = parkingSpotMapper.selectById(app.getSpotId());
            Map<String, Object> item = new HashMap<>();
            item.put("applicationId", app.getId());
            item.put("spotId", spot != null ? spot.getId() : app.getSpotId());
            item.put("spotCode", spot != null ? spot.getSpotCode() : "未知");
            item.put("buildingId", spot != null ? spot.getBuildingId() : null);
            item.put("location", spot != null ? spot.getLocation() : "");
            item.put("price", spot != null ? spot.getPrice() : app.getPayAmount());
            item.put("payTime", app.getPayTime());
            item.put("tradeNo", app.getTradeNo());
            result.add(item);
        }
        return Result.success(result);
    }
}
