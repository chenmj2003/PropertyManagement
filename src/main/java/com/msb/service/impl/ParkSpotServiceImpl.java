package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.mapper.ParkingSpotMapper;
import com.msb.pojo.ParkingSpot;
import com.msb.service.ParkSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ParkSpotServiceImpl implements ParkSpotService {
    @Autowired
    private ParkingSpotMapper parkingSpotMapper;
    @Override
    public void addSpot(ParkingSpot parkingSpot) {
        if (parkingSpotMapper.selectCount(new QueryWrapper<ParkingSpot>().eq("spot_code",parkingSpot.getSpotCode())) > 0){
            throw new RuntimeException("车位编号已存在");
        }
        parkingSpot.setStatus("idle");
        parkingSpotMapper.insert(parkingSpot);
    }

    @Override
    public List<ParkingSpot> listByBuilding(Long buildingId) {
        QueryWrapper<ParkingSpot> wrapper = new QueryWrapper<>();
        if (buildingId != null){
            wrapper.eq("building_id",buildingId);
        }
        return parkingSpotMapper.selectList(wrapper);
    }

    /** ✨分页✨ */
    @Override
    public IPage<ParkingSpot> pageByBuilding(Long buildingId, int page, int pageSize) {
        QueryWrapper<ParkingSpot> wrapper = new QueryWrapper<>();
        if (buildingId != null) wrapper.eq("building_id", buildingId);
        return parkingSpotMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }
}
