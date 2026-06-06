package com.msb.service;

import com.msb.pojo.ParkingSpot;

import java.util.List;

public interface ParkSpotService {
    void addSpot(ParkingSpot parkingSpot);
    List<ParkingSpot> listByBuilding(Long buildingId);

}
