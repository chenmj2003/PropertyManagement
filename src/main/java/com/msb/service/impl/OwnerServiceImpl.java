package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.mapper.OwnerMapper;
import com.msb.pojo.Owner;
import com.msb.service.OwnerService;
import com.msb.service.TokenService;
import com.msb.vo.OwnerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OwnerServiceImpl implements OwnerService {

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private TokenService tokenService;

    @Override
    public List<OwnerVo> selectAllOwner() {
        QueryWrapper<OwnerVo> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return ownerMapper.selectAllOwners();
    }

    @Override
    public void logout(String token) {
        // 同时删除 MySQL 和 Redis 中的 token，避免登出后 token 仍可用
        tokenService.deleteToken(token);
    }

    @Override
    public Owner getOwnerById(Integer id) {
        return ownerMapper.selectById(id);
    }

    @Override
    public void updateOwner(Owner owner) {
        ownerMapper.updateById(owner);
    }

    /**
     * ✨新建✨ 修改密码
     * 验证旧密码正确后才允许更新为新密码
     */
    @Override
    public void changePassword(Integer ownerId, String oldPassword, String newPassword) {
        Owner owner = ownerMapper.selectById(ownerId);
        if (owner == null) {
            throw new RuntimeException("用户不存在");
        }
        // 校验旧密码
        if (!owner.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }
        // 更新为新密码
        owner.setPassword(newPassword);
        ownerMapper.updateById(owner);
    }

}
