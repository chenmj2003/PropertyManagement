package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
@Data
@TableName("parking_spot")
public class ParkingSpot {
    private Long id;
    private String spotCode;
    // 所属楼栋ID（关联building.id）
    private Long buildingId;
    // 车位位置描述
    private String location;
    // idle-空闲, booked-已预订, sold-已售, applying-申购中
    private String status;
    // 车位售价
    private Double price;
    private LocalDateTime createTime;
}
