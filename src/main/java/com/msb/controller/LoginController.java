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
    public Map<String,Object> adminLogin(@RequestBody Admin admin, HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();

        // ==================== 登录限流检查 ====================
        String ip = LoginRateLimiter.getClientIp(request);
        String account = admin.getAccount();

        // 检查是否被限流锁定
        String rateLimitMsg = loginRateLimiter.checkRateLimit(ip, account);
        if (rateLimitMsg != null) {
            result.put("code", 429);
            result.put("message", rateLimitMsg);
            return result;
        }

        Admin admin1 = loginService.adminLogin(admin.getAccount(), admin.getPassword());
        if (admin1 != null){
            // 登录成功 → 清除失败记录
            loginRateLimiter.clearFailedAttempts(ip, account);

            // 生成token
            String token = tokenService.createToken(admin1.getId(),"admin");
            result.put("code",200);
            result.put("message","管理员登录成功");
            result.put("role","admin");
            result.put("token",token);
        } else {
            // 登录失败 → 记录一次失败尝试
            loginRateLimiter.recordFailedAttempt(ip, account);

            result.put("code",400);
            result.put("message","账号或密码出错");
        }
        return result;
    }

    @PostMapping("/ownerLogin")
    public Map<String,Object> ownerLogin(@RequestBody Owner owner, HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();

        // ==================== 登录限流检查 ====================
        String ip = LoginRateLimiter.getClientIp(request);
        String account = owner.getAccount();

        // 检查是否被限流锁定
        String rateLimitMsg = loginRateLimiter.checkRateLimit(ip, account);
        if (rateLimitMsg != null) {
            result.put("code", 429);
            result.put("message", rateLimitMsg);
            return result;
        }

        Owner owner1 = loginService.ownerLogin(owner.getAccount(), owner.getPassword());
        if (owner1 != null){
            // 登录成功 → 清除失败记录
            loginRateLimiter.clearFailedAttempts(ip, account);

            String token = tokenService.createToken(owner1.getId(),"owner");
            result.put("code",200);
            result.put("message","业主登录成功");
            result.put("role","owner");
            result.put("token",token);
        }else {
            // 登录失败 → 记录一次失败尝试
            loginRateLimiter.recordFailedAttempt(ip, account);

            result.put("code",400);
            result.put("message","账号或密码出错");
        }
        return result;
    }

    @GetMapping("/buildings")
    public Map<String,Object> listBuildings(){
        List<Building> list = buildingMapper.selectList(null);
        Map<String,Object> result = new HashMap<>();
        result.put("code",200);
        result.put("data",list);
        return result;
    }

    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody Owner owner){
        Map<String,Object> result = new HashMap<>();
        if (owner.getPhone() == null || !owner.getPhone().matches("^1[3-9]\\d{9}$")) {
            result.put("code", 400);
            result.put("message", "手机号格式错误，请重新输入");
            return result;
        }
        int row = loginService.registerOwner(owner);
        if(row > 0){
            // 注册成功 → 清除业主列表缓存，下次访问自动重建
            cacheService.clearOwnerList();
            result.put("code",200);
            result.put("message","注册成功");
        } else if (row == 0) {
            result.put("code",400);
            result.put("message","账号已存在");
        } else if (row == -1) {
            result.put("code",400);
            result.put("message","请选择楼栋");
        } else if (row == -2) {
            result.put("code",400);
            result.put("message","所选楼栋不存在");
        } else if (row == -3) {
            result.put("code",400);
            result.put("message","房间号已被占用");
        }else {
            result.put("code",400);
            result.put("message","注册失败");
        }
        return result;
    }

    @PostMapping("/resetPwd")
    public Map<String, Object> resetPwd(@RequestBody Map<String, String> params) {
        String account = params.get("account");
        String phone = params.get("phone");
        String newPassword = params.get("newPassword");
        Map<String, Object> result = new HashMap<>();
        int rows = loginService.resetOwnerPassword(account, phone, newPassword);
        if (rows > 0) {
            result.put("code", 200);
            result.put("message", "密码重置成功");
        } else {
            result.put("code", 400);
            result.put("message", "账号或手机号不匹配");
        }
        return result;
    }

}
