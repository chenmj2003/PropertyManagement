package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.mapper.AdminMapper;
import com.msb.mapper.BuildingMapper;
import com.msb.mapper.OwnerMapper;
import com.msb.pojo.Admin;
import com.msb.pojo.Building;
import com.msb.pojo.Owner;
import com.msb.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private OwnerMapper ownerMapper;
    @Autowired
    private BuildingMapper buildingMapper;

    @Override
    public Admin adminLogin(String account, String password) {
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("account",account).eq("password",password);
        return adminMapper.selectOne(wrapper);
    }

    @Override
    public Owner ownerLogin(String account, String password) {
        QueryWrapper<Owner> wrapper = new QueryWrapper<>();
        wrapper.eq("account",account).eq("password",password);
        return ownerMapper.selectOne(wrapper);
    }

    @Override
    public Admin adminLoginReturnId(String account, String password) {
        Admin admin = adminLogin(account, password);
        return admin; // 这个对象应该包含 id
    }

    @Override
    public Owner ownerLoginReturnId(String account, String password) {
        Owner owner = ownerLogin(account, password);
        return owner; // 这个对象应该包含 id
    }

    @Override
    public int registerOwner(Owner owner) {
        if (owner.getBuildingId() == null){
            return -1;
        }
        Building building = buildingMapper.selectById(owner.getBuildingId());
        if (building == null){
            return -2;
        }
        QueryWrapper<Owner> roomCheck = new QueryWrapper<>();
        roomCheck.eq("building_id",owner.getBuildingId())
                .eq("room_number",owner.getRoomNumber());
        if (ownerMapper.selectCount(roomCheck) > 0){
            return -3;
        }
        QueryWrapper<Owner> wrapper = new QueryWrapper<>();
        wrapper.eq("account",owner.getAccount());
        if (ownerMapper.selectCount(wrapper) > 0){
            return 0;
        }
        return ownerMapper.insert(owner);
    }

    private List<String> generateAllRooms(int buildingNum, int floorNum, int roomNum) {
        List<String> rooms = new ArrayList<>();
        for (int f = 1; f <= floorNum; f++) {
            for (int r = 1; r <= roomNum; r++) {
                rooms.add(String.format("%d-%d%02d", buildingNum, f, r));
            }
        }
        return rooms;
    }

    @Override
    public int resetOwnerPassword(String account, String phone, String newPassword) {
        QueryWrapper<Owner> wrapper = new QueryWrapper<>();
        wrapper.eq("account",account).eq("phone",phone);
        Owner owner = ownerMapper.selectOne(wrapper);
        if (owner != null){
            owner.setPassword(newPassword);
            return ownerMapper.updateById(owner);
        }
        // 账号或手机不匹配
        return 0;
    }
}
