package com.msb.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 缴费通知实体
 */
@Data
public class PaymentNotification {
    private Integer id;                 // 通知ID
    private Integer ownerId;            // 业主ID
    private String ownerName;           // 业主姓名
    private Integer buildingId;         // 楼栋ID
    private String roomNumber;          // 房间号
    private String feeType;             // 缴费类型：property/water/electric
    private String feeTypeName;         // 缴费类型名称：物业费/水费/电费
    private Double amount;              // 缴费金额
    private String description;         // 缴费描述
    private String status;              // 缴费状态：unpaid/paid
    private LocalDateTime payTime;      // 缴费时间
    private LocalDateTime deadline;     // 缴费截止日期
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
