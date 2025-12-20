package com.is.inspirationspaceclient.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.is.inspirationspaceclient.user.model.dto.RoleCreateDto;
import com.is.inspirationspaceclient.user.model.dto.RoleDto;
import com.is.inspirationspaceclient.user.model.entity.Role;
import com.is.inspirationspaceclient.user.service.RoleService;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

/**
 * 角色管理端控制器
 */
@RestController
@RequestMapping("/role")
@Tag(name = "角色控制器", description = "控制角色")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/get")
    @Operation(summary = "获取角色列表")
    public ApiResponse<Page<Role>> getRoleList(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Page<Role> page = new Page<>(current, size);
        return ApiResponse.ok(roleService.getRoleList(page));
    }

    @Operation(summary = "创建角色")
    @PostMapping("/add")
    public ApiResponse<Boolean> addRole(@RequestBody @Valid RoleCreateDto roleCreateDto) {
        return ApiResponse.ok(roleService.addRole(roleCreateDto));
    }

    @Operation(summary = "修改角色")
    @PutMapping("/update")
    public ApiResponse<Boolean> updateRole(@RequestBody @Valid RoleDto roleDto) {
        return ApiResponse.ok(roleService.updateRole(roleDto));
    }


    @Operation(summary = "删除角色")
    @DeleteMapping("/delete")
    //@RequireRole("super-admin")
    public ApiResponse<Boolean> deleteRole(@RequestParam Long roleId) {
        return ApiResponse.ok(roleService.deleteRole(roleId));
    }



}
