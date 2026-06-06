package com.msb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.pojo.Vehicle;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VehicleMapper extends BaseMapper<Vehicle> {
    // 业主查询自己车辆
    @Select("SELECT * FROM vehicle WHERE owner_id = #{ownerId} ORDER BY id DESC")
    List<Vehicle> selectByOwnerId(Integer ownerId);

    // 业主端：根据id查询车辆
    @Select("SELECT * FROM vehicle WHERE id = #{id} AND owner_id = #{ownerId}")
    Vehicle selectByIdAndOwner(@Param("id") Integer id,@Param("ownerId") Integer ownerId);

    // 业主端：删除车辆
    @Delete("DELETE FROM vehicle WHERE id = #{id} AND owner_id = #{ownerId}")
    int deleteByIdAndOwner(@Param("id") Integer id,@Param("ownerId") Integer ownerId);

    // 管理员端：查询所有车辆
    @Select("<script>" +
            "SELECT v.*, o.name AS ownerName FROM vehicle v " +
            "LEFT JOIN owner o ON v.owner_id = o.id " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (v.plate_number LIKE CONCAT('%', #{keyword}, '%') " +
            "    OR v.brand LIKE CONCAT('%', #{keyword}, '%') " +
            "    OR v.owner_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "</where>" +
            "ORDER BY v.id DESC" +
            "</script>")
    List<Vehicle> selectAllWithSearch(@Param("keyword") String keyWord);
}
