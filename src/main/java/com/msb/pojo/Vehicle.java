package com.msb.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Vehicle {
    private Integer id;
    private String plateNumber;
    private String brand;
    private String model;
    private String color;
    private String vehicleType;
    private Integer ownerId;
    private String ownerName;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
