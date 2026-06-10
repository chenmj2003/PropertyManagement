package com.msb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.pojo.IncomeExpense;
import com.msb.service.IncomeExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✨新建✨ 收支管理控制器
 * 仅管理员可操作：增删改查 + 统计
 */
@RestController
@RequestMapping("/api/admin")
public class IncomeExpenseController {

    @Autowired
    private IncomeExpenseService incomeExpenseService;

    @Autowired
    private CacheService cacheService;

    /**
     * ✨新建✨ 收支列表（可选按类型筛选）
     * GET /api/admin/income-expenses?type=income
     */
    @GetMapping("/income-expenses")
    public Result<List<IncomeExpense>> list(
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) return Result.fail(403, "权限不足");
        // 返回合并后的全部数据（手动记账+系统缴费+车位收入），前端分页
        return Result.success(incomeExpenseService.listByType(type));
    }

    /**
     * ✨新建✨ 新增收支记录
     * POST /api/admin/income-expenses
     */
    @PostMapping("/income-expenses")
    public Result<Void> add(@RequestBody IncomeExpense record,
                            HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        try {
            incomeExpenseService.add(record);
            // 收支数据变更 → 清除收支统计缓存，下次访问时自动重建
            cacheService.clearIncomeExpenseStats();
            return Result.success("添加成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 修改收支记录
     * PUT /api/admin/income-expenses/{id}
     */
    @PutMapping("/income-expenses/{id}")
    public Result<Void> update(@PathVariable Integer id,
                               @RequestBody IncomeExpense record,
                               HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        try {
            record.setId(id);
            incomeExpenseService.update(record);
            // 收支数据变更 → 清除收支统计缓存，下次访问时自动重建
            cacheService.clearIncomeExpenseStats();
            return Result.success("修改成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 删除收支记录
     * DELETE /api/admin/income-expenses/{id}
     */
    @DeleteMapping("/income-expenses/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        try {
            incomeExpenseService.delete(id);
            // 收支数据变更 → 清除收支统计缓存，下次访问时自动重建
            cacheService.clearIncomeExpenseStats();
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 收支汇总统计
     * GET /api/admin/income-expenses/stats
     */
    @GetMapping("/income-expenses/stats")
    public Result<Map<String, Object>> stats(HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        Map<String, Object> stats = incomeExpenseService.getStats();
        return Result.success(stats);
    }
}
