package com.msb.controller;

import com.msb.common.Result;
import com.msb.pojo.PaymentNotification;
import com.msb.service.PaymentNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentNotificationController {
    @Autowired
    private PaymentNotificationService paymentNotificationService;
    // 业主端接收通知
    @GetMapping("/owner/paymentNotifications")
    public Result<List<PaymentNotification>> getOwnerPaymentNotifications(HttpServletRequest request){

        Integer userId = (Integer) request.getAttribute("userId");
        String userType = (String) request.getAttribute("userType");
        // 检验是否为业主
        if (!("owner").equals(userType)){
            return Result.fail(403,"权限不足，仅业主可看");
        }
        List<PaymentNotification> notifications = paymentNotificationService.getByOwnerId(userId);
        return Result.success(notifications);
    }
    // 业主端付钱
    @PutMapping("/owner/pay/{notificationId}")
    public Result payNotification(@PathVariable Integer notificationId,
                                  HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        String userType = (String) request.getAttribute("userType");
        if (!("owner").equals(userType)){
            return Result.fail(403,"权限不足，仅业主可操作");
        }
        boolean success = paymentNotificationService.pay(notificationId, userId);
        if (success){
            return Result.success("缴费成功");
        }else {
            return Result.fail(400,"缴费失败，通知不存在或已缴费");
        }

    }

    // 管理员端发送通知
    @PostMapping("/admin/sendNotification")
    public Result sendNotification(@RequestBody Map<String,Object> params,
                                   HttpServletRequest request){
        String userType = (String) request.getAttribute("userType");
        if (!("admin").equals(userType)){
            return Result.fail(403,"权限不足");
        }
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ownerIds = (List<Integer>) params.get("ownerIds");
            String feeType = (String) params.get("feeType");
            Object amountObj = params.get("amount");
            Double amount;
            if (amountObj instanceof Integer) {
                amount = ((Integer) amountObj).doubleValue();
            } else if (amountObj instanceof Double) {
                amount = (Double) amountObj;
            } else {
                return Result.fail(400, "请输入正确的缴费金额");
            }
            String description = (String) params.get("description");
            String deadlineStr = (String) params.get("deadline");

            if (ownerIds == null || ownerIds.isEmpty()){
                return Result.fail(400,"请至少选择一位业主");
            }
            if (feeType == null || feeType.isEmpty()){
                return Result.fail(400,"请选择缴费类型");
            }
            if (amount == null || amount <= 0){
                return Result.fail(400,"请输入正确的缴费金额");
            }
            PaymentNotification notification = new PaymentNotification();
            notification.setFeeType(feeType);
            notification.setAmount(amount);
            notification.setDescription(description);
            if (deadlineStr != null && !deadlineStr.isEmpty()) {
                // 前端传的是 "YYYY-MM-DD HH:mm:ss" 格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                notification.setDeadline(LocalDateTime.parse(deadlineStr, formatter));
            }
            // 发送通知
            boolean success = paymentNotificationService.sendNotification(ownerIds, notification);
            if (success){
                return Result.success("缴费通知发送成功");
            }else {
                return Result.fail(500,"发送失败");
            }
        }catch (Exception e){
            return Result.fail(500,"参数错误" + e.getMessage());
        }
    }

    // 管理员端查看所有缴费通知记录
    @GetMapping("/admin/paymentNotifications")
    public Result<List<PaymentNotification>> searchNotification(
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String feeType,
            @RequestParam(required = false) String status,
            HttpServletRequest request){
        String userType = (String) request.getAttribute("userType");
        if (!("admin").equals(userType)){
            return Result.fail(403,"权限不足");
        }
        List<PaymentNotification> search = paymentNotificationService.search(ownerName, roomNumber, feeType, status);
        return Result.success(search);
    }


}
