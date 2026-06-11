package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.component.CacheService;
import com.msb.mapper.BuildingMapper;
import com.msb.pojo.Building;
import com.msb.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 楼栋列表 — Cache-Aside 模式
     * 楼栋数据几乎不变，TTL 30分钟，无需主动清除
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Building> Buildings() {
        // 1. 先查 Redis
        List<?> cached = cacheService.getBuildingList();
        if (cached != null) {
            return (List<Building>) cached;
        }

        // 2. 未命中，查数据库
        List<Building> buildings = buildingMapper.selectList(
                new QueryWrapper<Building>().orderByAsc("id"));

        // 3. 写入 Redis
        cacheService.setBuildingList(buildings);

        return buildings;
    }
}
