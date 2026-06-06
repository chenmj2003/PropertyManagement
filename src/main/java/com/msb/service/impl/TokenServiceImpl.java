package com.msb.service.impl;

import com.msb.mapper.TokenMapper;
import com.msb.pojo.LoginToken;
import com.msb.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenMapper tokenMapper;

    @Override
    public String createToken(int userid, String userType) {
        // userType  -->  划分是业主还是管理员的标识
        tokenMapper.deleteByUser(userid,userType);
        String token = UUID.randomUUID().toString().replace("-","");
        LoginToken loginToken = new LoginToken();
        loginToken.setUserId(userid);
        loginToken.setUserType(userType);
        loginToken.setToken(token);
        loginToken.setExpireTime(LocalDateTime.now().plusHours(24));
        tokenMapper.insert(loginToken);
        return token;
    }

    @Override
    public LoginToken getByToken(String token) {
        LoginToken loginToken = tokenMapper.selectByToken(token);
        if (loginToken != null){
            // 判断当登录token存在时，如果已经过期了，就把token从数据库删除
            if (loginToken.getExpireTime().isBefore(LocalDateTime.now())){
                tokenMapper.deleteByToken(token);
                return null;
            }
        }
        return loginToken;
    }

    @Override
    public void deleteToken(String token) {
        tokenMapper.deleteByToken(token);
    }
}
