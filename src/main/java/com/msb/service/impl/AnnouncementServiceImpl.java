package com.msb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.mapper.AnnouncementMapper;
import com.msb.pojo.Announcement;
import com.msb.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✨新建✨ 公告 Service 实现
 */
@Service
public class AnnouncementServiceImpl
        extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    /**
     * ✨新建✨ 按发布时间倒序
     */
    @Override
    public List<Announcement> listAll() {
        return list(new QueryWrapper<Announcement>()
                .orderByDesc("create_time"));
    }

    /**
     * ✨新建✨ 发布公告
     */
    @Override
    public void publish(Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().isEmpty()) {
            throw new RuntimeException("请输入公告标题");
        }
        if (announcement.getContent() == null || announcement.getContent().isEmpty()) {
            throw new RuntimeException("请输入公告内容");
        }
        announcement.setId(null);
        announcement.setCreateTime(LocalDateTime.now());
        baseMapper.insert(announcement);
    }

    /**
     * ✨新建✨ 编辑公告
     */
    @Override
    public void update(Announcement announcement) {
        Announcement exist = getById(announcement.getId());
        if (exist == null) {
            throw new RuntimeException("公告不存在");
        }
        exist.setTitle(announcement.getTitle());
        exist.setContent(announcement.getContent());
        updateById(exist);
    }

    /**
     * ✨新建✨ 删除公告
     */
    @Override
    public void delete(Integer id) {
        if (!removeById(id)) {
            throw new RuntimeException("删除失败，公告不存在");
        }
    }
}
