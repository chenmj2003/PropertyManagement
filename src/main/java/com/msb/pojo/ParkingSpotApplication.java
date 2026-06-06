package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parking_spot_application")
public class ParkingSpotApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spotId;
    private Integer ownerId;
    private String status;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private LocalDateTime payTime;
    private Integer adminId;
    private String rejectReason;
    private String tradeNo;
    private Double payAmount;
}
