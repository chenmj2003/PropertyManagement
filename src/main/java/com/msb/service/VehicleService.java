package com.msb.service;

import com.msb.pojo.Vehicle;

import java.util.List;

public interface VehicleService {
    // 查询自己所有车辆
    List<Vehicle> getMyVehicle(Integer ownerId);
    // 查询自己的一辆车
    Vehicle getMyVehicleById(Integer vehicleId,Integer ownerId);
    // 添加车辆
    void addVehicle(Vehicle vehicle);
    // 修改车辆
    void updateVehicle(Vehicle vehicle,Integer ownerId);
    // 删除车辆
    void deleteVehicle(Integer vehicleId,Integer ownerId);
    // 管理员端  -->  查询所有车辆
    List<Vehicle> getAllVehicles(String keyword);
}
