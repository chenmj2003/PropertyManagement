package com.msb.controller;

import com.msb.component.CacheService;
import com.msb.component.LoginRateLimiter;
import com.msb.mapper.BuildingMapper;
import com.msb.pojo.Admin;
import com.msb.pojo.Building;
import com.msb.pojo.Owner;
import com.msb.service.LoginService;
import com.msb.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.common.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private BuildingMapper buildingMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private LoginRateLimiter loginRateLimiter;
    @Autowired
    private CacheService cacheService;

    @PostMapping("/adminLogin")
    public Result<Map<String,Object>> adminLogin(@RequestBody Admin admin, HttpServletRequest request){
        // ==================== 登录限流检查 ====================
        String ip = LoginRateLimiter.getClientIp(request);
        String account = admin.getAccount();

        String rateLimitMsg = loginRateLimiter.checkRateLimit(ip, account);
        if (rateLimitMsg != null) {
            return Result.fail(429, rateLimitMsg);
        }

        Admin admin1 = loginService.adminLogin(admin.getAccount(), admin.getPassword());
        if (admin1 != null){
            loginRateLimiter.clearFailedAttempts(ip, account);

            String token = tokenService.createToken(admin1.getId(),"admin");
            Map<String,Object> data = new HashMap<>();
            data.put("role", "admin");
            data.put("token", token);
            return Result.success("管理员登录成功", data);
        } else {
            loginRateLimiter.recordFailedAttempt(ip, account);
            return Result.fail(400, "账号或密码出错");
        }
    }

    @PostMapping("/ownerLogin")
    public Result<Map<String,Object>> ownerLogin(@RequestBody Owner owner, HttpServletRequest request){
        String ip = LoginRateLimiter.getClientIp(request);
        String account = owner.getAccount();

        String rateLimitMsg = loginRateLimiter.checkRateLimit(ip, account);
        if (rateLimitMsg != null) {
            return Result.fail(429, rateLimitMsg);
        }

        Owner owner1 = loginService.ownerLogin(owner.getAccount(), owner.getPassword());
        if (owner1 != null){
            loginRateLimiter.clearFailedAttempts(ip, account);

            String token = tokenService.createToken(owner1.getId(),"owner");
            Map<String,Object> data = new HashMap<>();
            data.put("role", "owner");
            data.put("token", token);
            return Result.success("业主登录成功", data);
        }else {
            loginRateLimiter.recordFailedAttempt(ip, account);
            return Result.fail(400, "账号或密码出错");
        }
    }

    @GetMapping("/buildings")
    public Result<List<Building>> listBuildings(){
        return Result.success(buildingMapper.selectList(null));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody Owner owner){
        if (owner.getPhone() == null || !owner.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.fail(400, "手机号格式错误，请重新输入");
        }
        int row = loginService.registerOwner(owner);
        if(row > 0){
            cacheService.clearOwnerList();
            return Result.success("注册成功", null);
        } else if (row == 0) {
            return Result.fail(400, "账号已存在");
        } else if (row == -1) {
            return Result.fail(400, "请选择楼栋");
        } else if (row == -2) {
            return Result.fail(400, "所选楼栋不存在");
        } else if (row == -3) {
            return Result.fail(400, "房间号已被占用");
        } else {
            return Result.fail(400, "注册失败");
        }
    }

    @PostMapping("/resetPwd")
    public Result<Void> resetPwd(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String phone = params.get("phone");
        String newPassword = params.get("newPassword");
        int rows = loginService.resetOwnerPassword(account, phone, newPassword);
        if (rows > 0) {
            return Result.success("密码重置成功", null);
        } else {
            return Result.fail(400, "账号或手机号不匹配");
        }
    }

}
