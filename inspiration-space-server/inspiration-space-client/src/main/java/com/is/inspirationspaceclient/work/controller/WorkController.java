package com.is.inspirationspaceclient.work.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.work.model.dto.WorkCreateDto;
import com.is.inspirationspaceclient.work.model.dto.WorkUpdateDto;
import com.is.inspirationspaceclient.work.model.vo.WorkDetailVo;
import com.is.inspirationspaceclient.work.model.vo.WorkSimpleVo;
import com.is.inspirationspaceclient.work.service.WorkService;
import com.is.inspirationspacecommon.annotation.RequireRole;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 作品表 前端控制器
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/works")
@Tag(name = "作品接口", description = "作品管理相关操作")
public class WorkController {

    @Autowired
    private WorkService workService;

    @Operation(summary = "创建草稿")
    @PostMapping("/createDraft")
    @RequireRole("client-user")
    public ApiResponse<Boolean> createDraft(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody WorkCreateDto workCreateDto) {
        return ApiResponse.ok(workService.createDraft(token, workCreateDto));
    }

    @Operation(summary = "上传封面")
    @PostMapping("/upload/cover")
    @RequireRole("client-user")
    public ApiResponse<String> uploadCover(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(workService.uploadCover(file, token));
    }


    @Operation(summary = "修改草稿")
    @PutMapping("/updateDraft/{workId}")
    @RequireRole("client-user")
    public ApiResponse<Boolean> updateDraft(
            @PathVariable Long workId,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody WorkCreateDto workCreateDto) {
        return ApiResponse.ok(workService.updateDraft(workId, token, workCreateDto));
    }


    @Operation(summary = "发布作品")
    @PostMapping("/publish/{workId}")
    @RequireRole("client-user")
    public ApiResponse<Boolean> publishWork(
            @RequestHeader("Authorization") String token,
            @Valid @PathVariable Long workId) {
        return ApiResponse.ok(workService.publishWork(token, workId));
    }


    @Operation(summary = "删除作品")
    @DeleteMapping("/delete/{workId}")
    @RequireRole("client-user")
    public ApiResponse<Boolean> deleteWork(
            @PathVariable Long workId,
            @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(workService.deleteWork(workId, token));
    }

    @Operation(summary = "修改作品")
    @PutMapping("/update/{workId}")
    @RequireRole("client-user")
    public ApiResponse<Boolean> updateWork(
            @PathVariable Long workId,
            @RequestHeader("Authorization") String token,
            @RequestBody WorkUpdateDto workUpdateDto) {
        return ApiResponse.ok(workService.updateWork(workId, token, workUpdateDto));
    }

    @Operation(summary = "获取作品信息(个人)")
    @GetMapping("/get/{workId}")
    public ApiResponse<WorkDetailVo> getWorkDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long workId) {
        return ApiResponse.ok(workService.getWorkDetail(token, workId));
    }

    @Operation(summary = "获取用户作品列表(所有人)")
    @GetMapping("/get/{userId}")
    public ApiResponse<Page<WorkSimpleVo>> getUserWorks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(workService.getUserWorks(userId, page, size));
    }

//    @Operation(summary = "审核")
//    @PostMapping("/audit")
//    public ApiResponse<Boolean> auditWork(@RequestBody WorkIdDto workIdDto) {
//        return workService.auditWork(workIdDto);
//    }


}
