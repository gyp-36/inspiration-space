package com.is.inspirationspaceclient.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.user.model.dto.PermissionCreateDto;
import com.is.inspirationspaceclient.user.model.dto.PermissionUpdateDto;
import com.is.inspirationspaceclient.user.model.entity.Permission;
import com.is.inspirationspaceclient.user.service.PermissionService;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permission")
@Tag(name = "权限管理")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @GetMapping("/get")
    @Operation(summary = "获取权限列表")
    public ApiResponse<Page<Permission>> getPermissionList(@RequestParam(defaultValue = "1") Integer current,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        Page<Permission> page = new Page<>(current, size);
        return ApiResponse.ok(permissionService.getPermissionList(page));
    }

    @PostMapping("/add")
    @Operation(summary = "添加权限")
    public ApiResponse<Boolean> addPermission(@RequestBody PermissionCreateDto permissionCreateDto) {
        return ApiResponse.ok(permissionService.addPermission(permissionCreateDto));
    }

    @PutMapping("/update")
    @Operation(summary = "修改权限")
    public ApiResponse<Boolean> updatePermission(@RequestBody PermissionUpdateDto permissionupdatedto) {
        return ApiResponse.ok(permissionService.updatePermission(permissionupdatedto));
    }

    @PatchMapping("/changeStatus")
    @Operation(summary = "修改权限状态")
    public ApiResponse<Boolean> changeStatus(@RequestParam Long permissionId, @RequestParam Integer status) {
        return ApiResponse.ok(permissionService.changeStatus(permissionId, status));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除权限")
    public ApiResponse<Boolean> deletePermission(@RequestParam Long permissionId) {
        return ApiResponse.ok(permissionService.deletePermission(permissionId));
    }


}
