package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    @Override
    public List<Building> Buildings() {
        return buildingMapper.selectList
                (new QueryWrapper<Building>().orderByAsc("id"));
    }
}
