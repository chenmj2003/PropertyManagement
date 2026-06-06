package com.msb.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Building {

    private Integer id;
    private Integer buildingNum; // 楼房号
    private Integer floorNum; // 楼层
    private Integer roomNum; // 房间号
}