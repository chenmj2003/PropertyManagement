package com.msb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.pojo.Owner;
import com.msb.vo.OwnerVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OwnerMapper extends BaseMapper<Owner> {
    @Select("SELECT o.id, o.name, o.age, o.gender, o.account, o.phone, " +
            "b.building_num AS buildingName, o.room_number AS roomNumber " +
            "FROM owner o " +
            "LEFT JOIN building b ON o.building_id = b.id " +
            "ORDER BY o.id DESC")
    List<OwnerVo> selectAllOwners();
}
