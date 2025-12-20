package com.is.inspirationspaceclient.user.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.user.model.dto.RoleCreateDto;
import com.is.inspirationspaceclient.user.model.dto.RoleDto;
import com.is.inspirationspaceclient.user.model.entity.Role;

import java.util.List;

public interface RoleService {
    Page<Role> getRoleList(Page<Role> page);

    Boolean updateRole(RoleDto roleDto);

    Boolean deleteRole(Long roleId);

    Boolean addRole(RoleCreateDto roleCreateDto);

    List<String> getUserRoleCode(Long userId);
}