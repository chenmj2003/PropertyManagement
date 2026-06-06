package com.msb.service;

import com.msb.pojo.LoginToken;

public interface TokenService {
    String createToken(int userid,String userType);
    LoginToken getByToken(String token);
    void deleteToken(String token);

}
