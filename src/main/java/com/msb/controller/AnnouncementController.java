package com.msb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.msb.common.Result;
import com.msb.pojo.Announcement;
import com.msb.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ==================== 业主端 ====================

    /**
     * ✨新建✨ 业主查看公告列表
     * GET /api/owner/announcements
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
        return Result.success(announcementService.pageAll(page, pageSize));
    }

    // ==================== 管理员端 ====================

    /**
     * ✨新建✨ 管理员查看公告列表
     * GET /api/admin/announcements
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
        return Result.success(announcementService.pageAll(page, pageSize));
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
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
