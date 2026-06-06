package com.msb.service;

import com.msb.pojo.Owner;
import com.msb.vo.OwnerVo;

import java.util.List;

public interface OwnerService {
    List<OwnerVo> selectAllOwner();

    void logout(String token);

    Owner getOwnerById(Integer id);

    void updateOwner(Owner owner);
}
