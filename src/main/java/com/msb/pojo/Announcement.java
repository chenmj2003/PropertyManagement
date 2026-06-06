package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ✨新建✨ 社区公告实体
 */
@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Integer id;                 // 公告ID
    private String title;               // 公告标题
    private String content;             // 公告内容
    private LocalDateTime createTime;   // 发布时间
}
