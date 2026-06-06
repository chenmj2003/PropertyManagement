package com.msb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.pojo.RepairRequest;

import java.util.List;

/**
 * ✨新建✨ 报修管理 Service 接口
 */
public interface RepairRequestService {

    /** 业主提交报修申请 */
    RepairRequest submitRepair(RepairRequest repair, Integer ownerId);

    /** 业主查看自己的报修列表 */
    List<RepairRequest> getByOwnerId(Integer ownerId);

    /** 管理员查看所有报修 */
    List<RepairRequest> getAllRepairs();
    /** ✨分页✨ 管理员分页查报修 */
    IPage<RepairRequest> pageAllRepairs(int page, int pageSize, String status);
    /** ✨分页✨ 业主分页查报修 */
    IPage<RepairRequest> pageByOwnerId(Integer ownerId, int page, int pageSize);

    /** 管理员标记处理中 */
    void markProcessing(Integer repairId);

    /** 管理员标记已完成 */
    void markComplete(Integer repairId);
}
