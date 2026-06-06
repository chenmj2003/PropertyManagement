package com.msb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.pojo.ParkingSpotApplication;

public interface ParkingSpotApplicationService extends IService<ParkingSpotApplication> {
    // 业主申请购买车位
    void apply(Long spotId,Integer ownerId);
    // 管理员审核通过
    void approve(Long applicationId,Integer adminId);
    // 管理员拒绝
    void reject(Long applicationId,Integer adminId,String reason);
    // 直接支付（无需第三方）
    void directPay(Long applicationId, Integer ownerId);
}
