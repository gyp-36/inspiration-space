package com.is.inspirationspaceclient.work.controller;



import com.is.inspirationspaceclient.work.model.dto.TagCreateDto;
import com.is.inspirationspaceclient.work.model.vo.TagVo;
import com.is.inspirationspaceclient.work.service.WorkTagsService;
import com.is.inspirationspacecommon.annotation.RequireRole;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 作品标签 前端控制器
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/works-tags")
@Tag(name = "作品标签接口", description = "作品标签管理相关操作")
public class WorkTagsController {

    @Autowired
    private WorkTagsService worktagsService;

    @Operation(summary = "创建新标签")
    @PostMapping("/create")
    @RequireRole("work-admin")
    public ApiResponse<Boolean> createTag(@Valid @RequestBody TagCreateDto tagCreateDto) {
        return ApiResponse.ok(worktagsService.createTag(tagCreateDto));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/delete/{tagId}")
    @RequireRole("work-admin")
    public ApiResponse<Boolean> deleteTag(@PathVariable Long tagId){
        return ApiResponse.ok(worktagsService.deleteTag(tagId));
    }

    @Operation(summary = "获取标签列表")
    @GetMapping("/list")
    @RequireRole("work-admin")
    //分页
    public ApiResponse<List<TagVo>> getTagList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        List<TagVo> tagList = worktagsService.getTagList(pageNum, pageSize);
        return ApiResponse.ok(tagList);
    }


    @Operation(summary = "获取热门标签")
    @GetMapping("/popular")
    public ApiResponse<List<TagVo>> getPopularTags(
            @RequestParam(defaultValue = "10") int topN) {
        return ApiResponse.ok(worktagsService.getPopularTags(topN));
    }

    @Operation(summary = "为作品添加标签")
    @PostMapping("/{workId}/add")

    public ApiResponse<Void> addTagsToWork(
            @PathVariable Long workId,
            @RequestBody List<Integer> tagIds) {
        worktagsService.addTagsToWork(workId, tagIds);
        return ApiResponse.ok();
    }

    @Operation(summary = "移除作品标签")
    @DeleteMapping("/{workId}/remove")
    public ApiResponse<Void> removeTagsFromWork(
            @PathVariable Long workId,
            @RequestBody List<Integer> tagIds) {
        worktagsService.removeTagsFromWork(workId, tagIds);
        return ApiResponse.ok();
    }

    @Operation(summary = "获取作品标签")
    @GetMapping("/{workId}/list")
    public ApiResponse<List<TagVo>> getTagsByWorkId(
            @PathVariable Long workId) {
        return ApiResponse.ok(worktagsService.getTagsByWorkId(workId));
    }

}
