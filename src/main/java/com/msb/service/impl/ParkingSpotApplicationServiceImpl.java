package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.mapper.ParkingSpotApplicationMapper;
import com.msb.mapper.ParkingSpotMapper;
import com.msb.pojo.ParkingSpot;
import com.msb.pojo.ParkingSpotApplication;
import com.msb.service.ParkingSpotApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.msb.common.BusinessException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class ParkingSpotApplicationServiceImpl extends ServiceImpl<ParkingSpotApplicationMapper,ParkingSpotApplication>
        implements ParkingSpotApplicationService {
    @Autowired
    private ParkingSpotMapper parkingSpotMapper;

    @Override
    @Transactional
    public void apply(Long spotId, Integer ownerId) {
        // 检查该业主是否已有审批中或已通过(待支付)的该车位申请
        QueryWrapper<ParkingSpotApplication> wrapper = new QueryWrapper<>();
        wrapper.eq("spot_id",spotId)
                .eq("owner_id",ownerId)
                .in("status","applying","approved");
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0){
            throw new BusinessException("已提交过该车位的申请，请勿重复提交");
        }
        ParkingSpot spot = parkingSpotMapper.selectById(spotId);
        if (spot == null){
            throw new BusinessException("车位不存在");
        }
        if (!"idle".equals(spot.getStatus())){
            throw new BusinessException("当前车位不可购买");
        }
        spot.setStatus("applying");
        parkingSpotMapper.updateById(spot);
        // 创建申请记录
        ParkingSpotApplication application = new ParkingSpotApplication();
        application.setSpotId(spotId);
        application.setOwnerId(ownerId);
        application.setStatus("applying");
        application.setApplyTime(LocalDateTime.now());
        baseMapper.insert(application);
    }

    @Override
    @Transactional
    public void approve(Long applicationId, Integer adminId) {
        ParkingSpotApplication application = baseMapper.selectById(applicationId);
        if (application == null){
            throw new BusinessException("申请不存在");
        }
        if (!"applying".equals(application.getStatus())){
            throw new BusinessException("当前申请状态无法审核");
        }
        application.setStatus("approved");
        application.setAdminId(adminId);
        application.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(application);
        // 车位状态保持"申购中"，支付完成后才变为"已售"
    }

    @Override
    @Transactional
    public void reject(Long applicationId, Integer adminId, String reason) {
        ParkingSpotApplication application = baseMapper.selectById(applicationId);
        if (application == null){
            throw new BusinessException("申请不存在");
        }
        if (!"applying".equals(application.getStatus())){
            throw new BusinessException("当前状态无法审核");
        }

        application.setStatus("rejected");
        application.setAdminId(adminId);
        application.setRejectReason(reason);
        application.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(application);
        //  拒绝之后车位恢复空闲状态
        ParkingSpot spot = parkingSpotMapper.selectById(application.getSpotId());
        if (spot != null){
            spot.setStatus("idle");
            parkingSpotMapper.updateById(spot);
        }
    }

    @Override
    @Transactional
    public void directPay(Long applicationId, Integer ownerId) {
        ParkingSpotApplication application = baseMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }

        // 校验：只能支付自己的申请
        if (!ownerId.equals(application.getOwnerId())) {
            throw new BusinessException("无权操作此申请");
        }

        if ("paid".equals(application.getStatus())) {
            throw new BusinessException("该申请已支付，无需重复支付");
        }

        if (!"approved".equals(application.getStatus())) {
            throw new BusinessException("当前申请状态不是已通过，无法支付: " + application.getStatus());
        }

        // 获取车位信息（需要价格）
        ParkingSpot spot = parkingSpotMapper.selectById(application.getSpotId());

        // 更新申请记录（写入支付金额，用于收支统计）
        application.setStatus("paid");
        application.setPayTime(LocalDateTime.now());
        application.setPayAmount(spot != null ? spot.getPrice() : 0.0);
        baseMapper.updateById(application);
        System.out.println("✅ 申请记录已更新为已支付: ID=" + applicationId);

        // 更新车位状态为"已售"
        if (spot != null) {
            spot.setStatus("sold");
            parkingSpotMapper.updateById(spot);
            System.out.println("✅ 车位已标记为已售: " + spot.getSpotCode());
        }
    }
}
