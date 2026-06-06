package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ✨新建✨ 收支记录实体
 * 对应数据库表 income_expense
 */
@Data
@TableName("income_expense")
public class IncomeExpense {
    @TableId(type = IdType.AUTO)
    private Integer id;                 // 记录ID
    private String type;                // 类型：income-收入, expense-支出
    private String category;            // 分类：物业费/车位费/维修费/维修支出/办公用品/水电费/工资/其他
    private Double amount;              // 金额
    private String description;         // 备注说明
    private LocalDate recordDate;       // 收支日期
    private LocalDateTime createTime;   // 创建时间
}
