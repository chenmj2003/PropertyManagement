package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ✨新建✨ 报修申请实体
// * 对应数据库表 repair_request
 */
@Data
@TableName("repair_request")
public class RepairRequest {
    @TableId(type = IdType.AUTO)
    private Integer id;                 // 报修ID
    private Integer ownerId;            // 报修业主ID
    private String ownerName;           // 报修业主姓名
    private String roomNumber;          // 房间号
    private String title;               // 报修标题（如"水管漏水"）
    private String description;         // 报修描述（详细说明故障情况）
    private String status;              // 状态：pending-待处理, processing-处理中, completed-已完成
    private LocalDateTime createTime;   // 提交时间
    private LocalDateTime completeTime; // 完成时间
}
