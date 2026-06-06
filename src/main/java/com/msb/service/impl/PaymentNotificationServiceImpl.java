package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.mapper.OwnerMapper;
import com.msb.mapper.PaymentNotificationMapper;
import com.msb.pojo.Owner;
import com.msb.pojo.PaymentNotification;
import com.msb.service.PaymentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class PaymentNotificationServiceImpl extends ServiceImpl<PaymentNotificationMapper,PaymentNotification>
        implements PaymentNotificationService {

    @Autowired
    private OwnerMapper ownerMapper;

    /* 批量发送：给指定多个业主发送缴费通知 */
    @Override
    public boolean sendNotification(List<Integer> ownerIds, PaymentNotification notification) {
        List<PaymentNotification> notifications = new ArrayList<>();
        String feeTypeName = getFeeType(notification.getFeeType());
        for (Integer ownerId : ownerIds){
            Owner owner = ownerMapper.selectById(ownerId);
            if (owner == null){
                continue;
            }
            PaymentNotification n = new PaymentNotification();
            n.setOwnerId(ownerId);
            n.setOwnerName(owner.getName());
            n.setBuildingId(owner.getBuildingId());
            n.setRoomNumber(owner.getRoomNumber());
            n.setFeeType(notification.getFeeType());
            n.setFeeTypeName(feeTypeName);
            n.setAmount(notification.getAmount());
            n.setDescription(notification.getDescription());
            n.setDeadline(notification.getDeadline());
            n.setStatus("unpaid");
            n.setCreateTime(LocalDateTime.now());
            notifications.add(n);
        }
        return saveBatch(notifications);
    }

    /* 业主查询自己的缴费通知 */
    @Override
    public List<PaymentNotification> getByOwnerId(Integer ownerId) {
        QueryWrapper<PaymentNotification> wrapper = new QueryWrapper<>();
        // 按时间排序
        wrapper.eq("owner_id",ownerId).orderByDesc("create_time");
        // 封装成对象返回
        return list(wrapper);
    }

    @Override
    public boolean pay(Integer notificationId, Integer ownerId) {
        PaymentNotification notification = getById(notificationId);
        if (notification == null || !notification.getOwnerId().equals(ownerId)){
            return false;
        }
        if ("paid".equals(notification.getStatus())){
            // 已缴费
            return false;
        }
        notification.setStatus("paid");
        notification.setPayTime(LocalDateTime.now());
        return updateById(notification);
    }

    /* 管理端根据条件查询 */
    @Override
    public List<PaymentNotification> search(String ownerName, String roomNumber, String feeType, String status) {
        QueryWrapper<PaymentNotification> wrapper = new QueryWrapper<>();
        if (ownerName != null && !ownerName.isEmpty()){
            wrapper.like("owner_name",ownerName);
        }
        if (roomNumber != null && !roomNumber.isEmpty()){
            wrapper.eq("room_number",roomNumber);
        }
        if (feeType != null && !feeType.isEmpty()){
            wrapper.eq("fee_type",feeType);
        }
        if (status != null && !status.isEmpty()){
            wrapper.eq("status",status);
        }
        return list(wrapper);
    }
    private String getFeeType(String feeType){
        if ("property".equals(feeType)) return "物业费";
        if ("water".equals(feeType)) return "水费";
        if ("electric".equals(feeType)) return "电费";
        return feeType;
    }
}
