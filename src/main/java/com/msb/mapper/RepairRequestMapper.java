package com.msb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.pojo.RepairRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * ✨新建✨ 报修申请 Mapper
 * 继承 BaseMapper，自带 CRUD 方法，无需自定义 SQL
 */
@Mapper
public interface RepairRequestMapper extends BaseMapper<RepairRequest> {
}
