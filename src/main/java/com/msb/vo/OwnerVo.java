package com.msb.vo;

import lombok.Data;

@Data
public class OwnerVo {
    private Integer id;
    private String name;
    private Integer age;
    private String gender;
    private String account;
    private String phone;
    private String buildingName;  // 楼栋名称
    private String roomNumber;
}
