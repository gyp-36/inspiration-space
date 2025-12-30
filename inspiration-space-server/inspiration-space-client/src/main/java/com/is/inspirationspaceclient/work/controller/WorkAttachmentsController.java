package com.is.inspirationspaceclient.work.controller;


import com.is.inspirationspaceclient.work.model.vo.WorkAttachmentVo;
import com.is.inspirationspaceclient.work.service.WorkAttachmentsService;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * <p>
 * 作品附件 前端控制器
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/works-attachments")
@Tag(name = "作品附件接口", description = "作品附件管理相关操作")
public class WorkAttachmentsController {

    @Autowired
    private WorkAttachmentsService workAttachmentsService;

    @Operation(summary = "上传作品附件")
    @PostMapping("/upload")
    public ApiResponse<WorkAttachmentVo> uploadWorkAttachment(
            @RequestParam("workId") Long workId,
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(workAttachmentsService.uploadWorkAttachment(token, workId, file));
    }


    @Operation(summary = "删除作品附件")
    @DeleteMapping("/delete/{attachmentId}")
    public ApiResponse<Boolean> deleteWorkAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(workAttachmentsService.deleteWorkAttachment(attachmentId, token));
    }

    @Operation(summary = "获取作品附件列表")
    @GetMapping("/list/{workId}")
    public ApiResponse<List<WorkAttachmentVo>> getWorkAttachments(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long workId) {
        return ApiResponse.ok(workAttachmentsService.getWorkAttachments(token, workId));
    }


}
