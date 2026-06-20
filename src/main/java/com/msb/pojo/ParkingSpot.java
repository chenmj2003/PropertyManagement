package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String image;      // ✨新建✨ 车位图片路径
    private LocalDateTime createTime;

    // ==================== 抢购车位字段 ====================
    /** 销售类型：normal-普通流程, flash_sale-抢购 */
    private String saleType;
    /** 抢购开始时间（前端格式 yyyy-MM-dd HH:mm:ss） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime flashSaleTime;
    /** 抢购价格（可设特价，null 则用普通 price） */
    private Double flashSalePrice;
}
