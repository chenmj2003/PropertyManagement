package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.mapper.OwnerMapper;
import com.msb.mapper.RepairRequestMapper;
import com.msb.pojo.Owner;
import com.msb.pojo.RepairRequest;
import com.msb.service.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✨新建✨ 报修管理 Service 实现
 * 继承 ServiceImpl 获得 MyBatis-Plus 内置的 CRUD 方法
 */
@Service
public class RepairRequestServiceImpl
        extends ServiceImpl<RepairRequestMapper, RepairRequest>
        implements RepairRequestService {

    @Autowired
    private OwnerMapper ownerMapper;

    /**
     * ✨新建✨ 业主提交报修
     * 从数据库获取业主姓名和房间号，确保数据一致性
     */
    @Override
    public RepairRequest submitRepair(RepairRequest repair, Integer ownerId) {
        // 校验必填字段
        if (repair.getTitle() == null || repair.getTitle().isEmpty()) {
            throw new RuntimeException("请填写报修标题");
        }
        if (repair.getDescription() == null || repair.getDescription().isEmpty()) {
            throw new RuntimeException("请填写报修描述");
        }

        // 从数据库获取业主信息
        Owner owner = ownerMapper.selectById(ownerId);
        if (owner == null) {
            throw new RuntimeException("业主信息不存在");
        }

        // 组装报修记录
        repair.setId(null);
        repair.setOwnerId(ownerId);
        repair.setOwnerName(owner.getName());
        repair.setRoomNumber(owner.getRoomNumber());
        repair.setStatus("pending");                    // 初始状态：待处理
        repair.setCreateTime(LocalDateTime.now());       // 提交时间

        baseMapper.insert(repair);
        return repair;
    }

    /**
     * ✨新建✨ 业主查询自己的报修列表（按时间倒序）
     */
    @Override
    public List<RepairRequest> getByOwnerId(Integer ownerId) {
        return list(new QueryWrapper<RepairRequest>()
                .eq("owner_id", ownerId)
                .orderByDesc("create_time"));
    }

    /**
     * ✨新建✨ 管理员查询所有报修（待处理优先，然后按时间倒序）
     */
    @Override
    public List<RepairRequest> getAllRepairs() {
        return list(new QueryWrapper<RepairRequest>()
                .orderByAsc("status")
                .orderByDesc("create_time"));
    }

    /** ✨分页✨ 管理员分页查报修，支持按状态筛选 */
    @Override
    public IPage<RepairRequest> pageAllRepairs(int page, int pageSize, String status) {
        QueryWrapper<RepairRequest> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq("status", status);
        wrapper.orderByAsc("status").orderByDesc("create_time");
        return page(new Page<>(page, pageSize), wrapper);
    }

    /** ✨分页✨ 业主分页查报修 */
    @Override
    public IPage<RepairRequest> pageByOwnerId(Integer ownerId, int page, int pageSize) {
        return page(new Page<>(page, pageSize),
                new QueryWrapper<RepairRequest>()
                        .eq("owner_id", ownerId)
                        .orderByDesc("create_time"));
    }

    /**
     * ✨新建✨ 管理员标记处理中
     */
    @Override
    public void markProcessing(Integer repairId) {
        RepairRequest repair = getById(repairId);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if (!"pending".equals(repair.getStatus())) {
            throw new RuntimeException("当前状态不是待处理，无法转为处理中");
        }
        repair.setStatus("processing");
        updateById(repair);
    }

    /**
     * ✨新建✨ 管理员标记已完成，同时记录完成时间
     */
    @Override
    public void markComplete(Integer repairId) {
        RepairRequest repair = getById(repairId);
        if (repair == null) {
            throw new RuntimeException("报修记录不存在");
        }
        if ("completed".equals(repair.getStatus())) {
            throw new RuntimeException("该报修已完成，无需重复操作");
        }
        repair.setStatus("completed");
        repair.setCompleteTime(LocalDateTime.now());
        updateById(repair);
    }
}
