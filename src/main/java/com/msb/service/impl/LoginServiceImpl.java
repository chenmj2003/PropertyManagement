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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Admin adminLogin(String account, String password) {
        // 只按账号查，密码用 BCrypt 校验
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) return null;

        // BCrypt 匹配
        if (encoder.matches(password, admin.getPassword())) {
            return admin;
        }
        // 兼容旧明文密码（首次用明文登录后自动升级为 BCrypt）
        if (password.equals(admin.getPassword())) {
            admin.setPassword(encoder.encode(password));
            adminMapper.updateById(admin);
            return admin;
        }
        return null;
    }

    @Override
    public Owner ownerLogin(String account, String password) {
        QueryWrapper<Owner> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        Owner owner = ownerMapper.selectOne(wrapper);
        if (owner == null) return null;

        // BCrypt 匹配
        if (encoder.matches(password, owner.getPassword())) {
            return owner;
        }
        // 兼容旧明文密码（自动升级）
        if (password.equals(owner.getPassword())) {
            owner.setPassword(encoder.encode(password));
            ownerMapper.updateById(owner);
            return owner;
        }
        return null;
    }

    @Override
    public Admin adminLoginReturnId(String account, String password) {
        return adminLogin(account, password);
    }

    @Override
    public Owner ownerLoginReturnId(String account, String password) {
        return ownerLogin(account, password);
    }

    @Override
    public int registerOwner(Owner owner) {
        if (owner.getBuildingId() == null) {
            return -1;
        }
        Building building = buildingMapper.selectById(owner.getBuildingId());
        if (building == null) {
            return -2;
        }
        QueryWrapper<Owner> roomCheck = new QueryWrapper<>();
        roomCheck.eq("building_id", owner.getBuildingId())
                .eq("room_number", owner.getRoomNumber());
        if (ownerMapper.selectCount(roomCheck) > 0) {
            return -3;
        }
        QueryWrapper<Owner> wrapper = new QueryWrapper<>();
        wrapper.eq("account", owner.getAccount());
        if (ownerMapper.selectCount(wrapper) > 0) {
            return 0;
        }

        // BCrypt 加密密码再入库
        owner.setPassword(encoder.encode(owner.getPassword()));
        return ownerMapper.insert(owner);
    }

    @Override
    public int resetOwnerPassword(String account, String phone, String newPassword) {
        QueryWrapper<Owner> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account).eq("phone", phone);
        Owner owner = ownerMapper.selectOne(wrapper);
        if (owner != null) {
            // BCrypt 加密新密码
            owner.setPassword(encoder.encode(newPassword));
            return ownerMapper.updateById(owner);
        }
        return 0;
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
}
