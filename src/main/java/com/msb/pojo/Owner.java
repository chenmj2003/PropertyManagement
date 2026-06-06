package com.msb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Owner {
    private Integer id;
    private String name;
    private Integer age;
    private String gender;
    private String account;
    private String password;
    private String phone;
    private Integer buildingId;
    private String roomNumber;
    private String avatar;     // ✨新建✨ 头像路径
}
