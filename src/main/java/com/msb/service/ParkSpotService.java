package com.msb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.pojo.ParkingSpot;

import java.util.List;

public interface ParkSpotService {
    void addSpot(ParkingSpot parkingSpot);
    List<ParkingSpot> listByBuilding(Long buildingId);
    /** ✨分页✨ */
    IPage<ParkingSpot> pageByBuilding(Long buildingId, int page, int pageSize);
}
