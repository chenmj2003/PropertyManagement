package com.msb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.common.Result;
import com.msb.component.CacheService;
import com.msb.pojo.Announcement;
import com.msb.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ✨新建✨ 社区公告控制器
 * 业主端：查看公告列表
 * 管理员端：发布/编辑/删除公告
 */
@RestController
@RequestMapping("/api")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private CacheService cacheService;

    // ==================== 业主端 ====================

    /**
     * ✨Redis缓存✨ 业主查看公告列表（分页）
     * Cache-Aside：缓存完整 IPage（含 records + total），写操作时清除
     */
    @GetMapping("/owner/announcements")
    public Result<IPage<Announcement>> ownerList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"owner".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        // 1. 查 Redis 缓存
        IPage<Announcement> cached = cacheService.getAnnouncementPage(page, pageSize, Announcement.class);
        if (cached != null) {
            return Result.success(cached);
        }
        // 2. 查数据库
        IPage<Announcement> result = announcementService.pageAll(page, pageSize);
        // 3. 写入 Redis（存完整 Page 对象，含 total）
        cacheService.setAnnouncementPage(page, pageSize, result);
        return Result.success(result);
    }

    // ==================== 管理员端 ====================

    /**
     * ✨Redis缓存✨ 管理员查看公告列表（分页）
     */
    @GetMapping("/admin/announcements")
    public Result<IPage<Announcement>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        // 1. 查 Redis 缓存（业主和管理员共用同一份 key）
        IPage<Announcement> cached = cacheService.getAnnouncementPage(page, pageSize, Announcement.class);
        if (cached != null) {
            return Result.success(cached);
        }
        // 2. 查数据库
        IPage<Announcement> result = announcementService.pageAll(page, pageSize);
        // 3. 写入 Redis
        cacheService.setAnnouncementPage(page, pageSize, result);
        return Result.success(result);
    }

    /**
     * ✨新建✨ 管理员发布公告
     * POST /api/admin/announcements
     */
    @PostMapping("/admin/announcements")
    public Result<Void> publish(@RequestBody Announcement announcement,
                                HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        try {
            announcementService.publish(announcement);
            // 公告变更 → 清除所有分页缓存，下次访问自动重建
            cacheService.clearAnnouncements();
            return Result.success("公告发布成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 管理员编辑公告
     * PUT /api/admin/announcements/{id}
     */
    @PutMapping("/admin/announcements/{id}")
    public Result<Void> update(@PathVariable Integer id,
                               @RequestBody Announcement announcement,
                               HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        try {
            announcement.setId(id);
            announcementService.update(announcement);
            // 公告变更 → 清除所有分页缓存
            cacheService.clearAnnouncements();
            return Result.success("修改成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * ✨新建✨ 管理员删除公告
     * DELETE /api/admin/announcements/{id}
     */
    @DeleteMapping("/admin/announcements/{id}")
    public Result<Void> delete(@PathVariable Integer id,
                               HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"admin".equals(userType)) {
            return Result.fail(403, "权限不足");
        }
        try {
            announcementService.delete(id);
            // 公告变更 → 清除所有分页缓存
            cacheService.clearAnnouncements();
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
