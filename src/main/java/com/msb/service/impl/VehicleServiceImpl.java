package com.msb.service.impl;

import com.msb.mapper.OwnerMapper;
import com.msb.mapper.VehicleMapper;
import com.msb.pojo.Owner;
import com.msb.pojo.Vehicle;
import com.msb.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private OwnerMapper ownerMapper;

    @Override
    public List<Vehicle> getMyVehicle(Integer ownerId) {
        return vehicleMapper.selectByOwnerId(ownerId);
    }

    @Override
    public Vehicle getMyVehicleById(Integer vehicleId, Integer ownerId) {
        return vehicleMapper.selectByIdAndOwner(vehicleId,ownerId);
    }

    @Override
    public void addVehicle(Vehicle vehicle) {
        Owner owner = ownerMapper.selectById(vehicle.getOwnerId());
        if (owner != null){
            vehicle.setOwnerName(owner.getName());
        }
        vehicleMapper.insert(vehicle);
    }

    @Override
    public void updateVehicle(Vehicle vehicle, Integer ownerId) {
        // 校验车辆归属
        Vehicle vehicleExit = vehicleMapper.selectByIdAndOwner(vehicle.getId(), ownerId);
        if (vehicleExit == null){
            throw new RuntimeException("您无权操控此车辆");
        }
        vehicle.setOwnerId(ownerId);
        Owner owner = ownerMapper.selectById(ownerId);
        if (owner != null){
            vehicle.setOwnerName(owner.getName());
        }
        vehicleMapper.updateById(vehicle);
    }

    @Override
    public void deleteVehicle(Integer vehicleId, Integer ownerId) {
        int i = vehicleMapper.deleteByIdAndOwner(vehicleId, ownerId);
        if (i == 0){
            throw new RuntimeException("您无权操作此车辆");
        }
    }

    @Override
    public List<Vehicle> getAllVehicles(String keyword) {
        return vehicleMapper.selectAllWithSearch(keyword);
    }
}
