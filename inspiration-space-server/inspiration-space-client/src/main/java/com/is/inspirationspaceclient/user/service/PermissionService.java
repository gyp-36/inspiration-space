package com.is.inspirationspaceclient.user.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.user.model.dto.PermissionCreateDto;
import com.is.inspirationspaceclient.user.model.dto.PermissionUpdateDto;
import com.is.inspirationspaceclient.user.model.entity.Permission;

public interface PermissionService {
    Page<Permission> getPermissionList(Page<Permission> page);

    Boolean addPermission(PermissionCreateDto permissionCreateDto);

    Boolean updatePermission(PermissionUpdateDto permissionUpdateDto);

    Boolean deletePermission(Long permissionId);

    Boolean changeStatus(Long permissionId, Integer status);
}