package com.msb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.pojo.IncomeExpense;

import java.util.List;
import java.util.Map;

/**
 * ✨新建✨ 收支管理 Service 接口
 */
public interface IncomeExpenseService {

    /** 查询收支列表，可选按类型筛选 */
    List<IncomeExpense> listByType(String type);
    /** ✨分页✨ 分页查询收支 */
    IPage<IncomeExpense> pageByType(String type, int page, int pageSize);

    /** 新增收支记录 */
    void add(IncomeExpense record);

    /** 修改收支记录 */
    void update(IncomeExpense record);

    /** 删除收支记录 */
    void delete(Integer id);

    /** 汇总统计：总收入、总支出、结余 */
    Map<String, Object> getStats();
}
