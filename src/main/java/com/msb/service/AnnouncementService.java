package com.msb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.pojo.Announcement;

import java.util.List;

/**
 * ✨新建✨ 公告 Service 接口
 */
public interface AnnouncementService {

    /** 查询公告列表（按时间倒序） */
    List<Announcement> listAll();
    /** ✨新建✨ 分页查询公告 */
    IPage<Announcement> pageAll(Integer page, Integer pageSize);

    /** 发布公告 */
    void publish(Announcement announcement);

    /** 编辑公告 */
    void update(Announcement announcement);

    /** 删除公告 */
    void delete(Integer id);
}
