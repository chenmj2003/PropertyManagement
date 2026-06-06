package com.msb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.pojo.IncomeExpense;
import org.apache.ibatis.annotations.Mapper;

/**
 * ✨新建✨ 收支记录 Mapper
 * 继承 BaseMapper，自带 CRUD，无需自定义 SQL
 */
@Mapper
public interface IncomeExpenseMapper extends BaseMapper<IncomeExpense> {
}
