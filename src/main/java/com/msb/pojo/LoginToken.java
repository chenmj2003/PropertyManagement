package com.msb.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginToken {
    private Integer id;
    private Integer userId;
    private String userType;
    private String token;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
