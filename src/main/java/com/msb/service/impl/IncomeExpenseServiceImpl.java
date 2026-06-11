package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.component.CacheService;
import com.msb.mapper.IncomeExpenseMapper;
import com.msb.mapper.ParkingSpotApplicationMapper;
import com.msb.mapper.ParkingSpotMapper;
import com.msb.mapper.PaymentNotificationMapper;
import com.msb.pojo.IncomeExpense;
import com.msb.pojo.ParkingSpot;
import com.msb.pojo.ParkingSpotApplication;
import com.msb.pojo.PaymentNotification;
import com.msb.service.IncomeExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✨新建✨ 收支管理 Service 实现
 */
@Service
public class IncomeExpenseServiceImpl
        extends ServiceImpl<IncomeExpenseMapper, IncomeExpense>
        implements IncomeExpenseService {

    // 🔥 注入缴费通知和车位申请 Mapper，统计系统内已支付的收入
    @Autowired
    private PaymentNotificationMapper paymentNotificationMapper;

    @Autowired
    private ParkingSpotApplicationMapper parkingSpotApplicationMapper;

    @Autowired
    private ParkingSpotMapper parkingSpotMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * ✨新建✨ 收支列表 — 合并三个数据源
     * 1. 手动记账（income_expense 表）
     * 2. 物业费/水费/电费已缴费（payment_notification 表 status='paid'）
     * 3. 车位购买已支付（parking_spot_application 表 status='paid'）
     * 系统生成的记录使用负数 ID，前端据此禁用编辑/删除
     */
    /** ✨分页✨ 分页查询收支（只查手动记账，系统记录已合并到listByType中） */
    @Override
    public IPage<IncomeExpense> pageByType(String type, int page, int pageSize) {
        // 分页只用于手动记账表
        QueryWrapper<IncomeExpense> wrapper = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) wrapper.eq("type", type);
        wrapper.orderByDesc("record_date").orderByDesc("create_time");
        // 先查总数用于合并计算
        return page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<IncomeExpense> listByType(String type) {
        boolean wantIncome = type == null || type.isEmpty() || "income".equals(type);
        boolean wantExpense = type == null || type.isEmpty() || "expense".equals(type);

        List<IncomeExpense> result = new ArrayList<>();

        // ======== 1. 手动记账（收入 + 支出） ========
        QueryWrapper<IncomeExpense> wrapper = new QueryWrapper<>();
        if (!wantIncome) wrapper.eq("type", "expense");
        if (!wantExpense) wrapper.eq("type", "income");
        wrapper.orderByDesc("record_date").orderByDesc("create_time");
        result.addAll(list(wrapper));

        // ======== 2. 已缴费的物业费/水费/电费 → 转为收入记录 ========
        if (wantIncome) {
            List<PaymentNotification> paidFees = paymentNotificationMapper.selectList(
                    new QueryWrapper<PaymentNotification>().eq("status", "paid"));
            for (PaymentNotification n : paidFees) {
                IncomeExpense item = new IncomeExpense();
                item.setId(-n.getId());                              // 负数ID，标记为系统生成
                item.setType("income");
                item.setCategory(n.getFeeTypeName() != null ? n.getFeeTypeName() : "物业费");
                item.setAmount(n.getAmount());
                item.setDescription(n.getOwnerName() + " " + n.getRoomNumber() + " 缴费");
                // payTime → recordDate
                if (n.getPayTime() != null) {
                    item.setRecordDate(n.getPayTime().toLocalDate());
                }
                item.setCreateTime(n.getPayTime());
                result.add(item);
            }
        }

        // ======== 3. 已支付的车位购买 → 转为收入记录 ========
        if (wantIncome) {
            List<ParkingSpotApplication> paidApps = parkingSpotApplicationMapper.selectList(
                    new QueryWrapper<ParkingSpotApplication>().eq("status", "paid"));
            for (ParkingSpotApplication app : paidApps) {
                ParkingSpot spot = parkingSpotMapper.selectById(app.getSpotId());
                IncomeExpense item = new IncomeExpense();
                item.setId((int) (-10000 - app.getId()));              // 负数ID，标记为系统生成
                item.setType("income");
                item.setCategory("车位费");
                item.setAmount(app.getPayAmount() != null ? app.getPayAmount() : 0.0);
                String spotCode = spot != null ? spot.getSpotCode() : "车位" + app.getSpotId();
                item.setDescription("车位购买 " + spotCode + "（申请ID:" + app.getId() + "）");
                if (app.getPayTime() != null) {
                    item.setRecordDate(app.getPayTime().toLocalDate());
                }
                item.setCreateTime(app.getPayTime());
                result.add(item);
            }
        }

        // ======== 按日期倒序排序 ========
        result.sort(Comparator.comparing(
                IncomeExpense::getRecordDate,
                Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(IncomeExpense::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        return result;
    }

    /**
     * ✨新建✨ 新增收支记录
     */
    @Override
    public void add(IncomeExpense record) {
        if (record.getType() == null || record.getType().isEmpty()) {
            throw new RuntimeException("请选择收支类型");
        }
        if (record.getCategory() == null || record.getCategory().isEmpty()) {
            throw new RuntimeException("请选择分类");
        }
        if (record.getAmount() == null || record.getAmount() <= 0) {
            throw new RuntimeException("请输入正确的金额");
        }

        record.setId(null);
        record.setCreateTime(LocalDateTime.now());
        baseMapper.insert(record);
    }

    /**
     * ✨新建✨ 修改收支记录（保留原创建时间）
     */
    @Override
    public void update(IncomeExpense record) {
        IncomeExpense exist = getById(record.getId());
        if (exist == null) {
            throw new RuntimeException("记录不存在");
        }

        // 只更新业务字段，保留创建时间
        exist.setType(record.getType());
        exist.setCategory(record.getCategory());
        exist.setAmount(record.getAmount());
        exist.setDescription(record.getDescription());
        exist.setRecordDate(record.getRecordDate());

        updateById(exist);
    }

    /**
     * ✨新建✨ 删除收支记录
     */
    @Override
    public void delete(Integer id) {
        if (!removeById(id)) {
            throw new RuntimeException("删除失败，记录不存在");
        }
    }

    /**
     * ✨Redis缓存✨ 汇总统计：总收入、总支出、结余
     * 收入 = 手动记账收入 + 物业费水费电费已缴费 + 车位购买已支付
     * 支出 = 手动记账支出
     *
     * Cache-Aside 模式：先查 Redis → 未命中查 DB → 写入 Redis
     */
    @Override
    public Map<String, Object> getStats() {
        // ==================== 1. 先查 Redis 缓存 ====================
        Map<String, Object> cached = cacheService.getIncomeExpenseStats();
        if (cached != null) {
            return cached;
        }

        // ==================== 2. 未命中，查数据库 ====================

        // ---- 2.1 手动记账收入 ----
        Double manualIncome = getBaseMapper().selectList(
                new QueryWrapper<IncomeExpense>().eq("type", "income")
        ).stream().mapToDouble(IncomeExpense::getAmount).sum();

        // ---- 2.2 物业费/水费/电费 已缴费金额 ----
        Double feeIncome = paymentNotificationMapper.selectList(
                new QueryWrapper<PaymentNotification>().eq("status", "paid")
        ).stream().mapToDouble(PaymentNotification::getAmount).sum();

        // ---- 2.3 车位购买 已支付金额 ----
        Double parkingIncome = parkingSpotApplicationMapper.selectList(
                new QueryWrapper<ParkingSpotApplication>().eq("status", "paid")
        ).stream().mapToDouble(app -> app.getPayAmount() != null ? app.getPayAmount() : 0).sum();

        // ---- 总收入 = 三项之和 ----
        Double totalIncome = manualIncome + feeIncome + parkingIncome;

        // ---- 总支出（仅手动记账） ----
        Double totalExpense = getBaseMapper().selectList(
                new QueryWrapper<IncomeExpense>().eq("type", "expense")
        ).stream().mapToDouble(IncomeExpense::getAmount).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("manualIncome", manualIncome);              // 手动记账收入
        stats.put("feeIncome", feeIncome);                    // 物业费水费电费收入
        stats.put("parkingIncome", parkingIncome);            // 车位购买收入
        stats.put("totalIncome", totalIncome);                // 总收入（三项之和）
        stats.put("totalExpense", totalExpense);              // 总支出
        stats.put("balance", totalIncome - totalExpense);     // 结余

        // ==================== 3. 写入 Redis 缓存 ====================
        cacheService.setIncomeExpenseStats(stats);

        return stats;
    }
}
