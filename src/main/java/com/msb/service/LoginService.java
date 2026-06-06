package com.msb.service;

import com.msb.pojo.Admin;
import com.msb.pojo.Owner;

public interface LoginService {
    // 管理员登录接口
    Admin adminLogin(String account,String password);
    // 业主登录接口
    Owner ownerLogin(String account,String password);
    //返回带id的对象
    Admin adminLoginReturnId(String account,String password);
    Owner ownerLoginReturnId(String account,String password);
    // 注册
    int registerOwner(Owner owner);
    // 重置密码
    int resetOwnerPassword(String account,String phone,String newPassword);
}
