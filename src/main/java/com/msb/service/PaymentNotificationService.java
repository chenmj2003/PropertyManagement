package com.msb.service;

import com.msb.pojo.PaymentNotification;

import java.util.List;

public interface PaymentNotificationService {
    /* 给指定多个业主发送缴费通知 */
    boolean sendNotification(List<Integer> ownerIds, PaymentNotification notification);

    /* 业主查询自己的缴费通知 */
    List<PaymentNotification> getByOwnerId(Integer ownerId);

    /* 业主缴费 */
    boolean pay(Integer notificationId, Integer ownerId);

    /* 管理端根据条件查询 */
    List<PaymentNotification> search(String ownerName,String roomNumber,String feeType,String status);
}