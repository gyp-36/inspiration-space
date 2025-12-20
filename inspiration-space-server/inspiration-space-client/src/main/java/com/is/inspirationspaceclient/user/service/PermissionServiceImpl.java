package com.is.inspirationspaceclient.user.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.user.mapper.PermissionMapper;
import com.is.inspirationspaceclient.user.mapper.RolePermissionMapper;
import com.is.inspirationspaceclient.user.model.dto.PermissionCreateDto;
import com.is.inspirationspaceclient.user.model.dto.PermissionUpdateDto;
import com.is.inspirationspaceclient.user.model.entity.Permission;
import com.is.inspirationspaceclient.user.model.entity.RolePermission;
import com.is.inspirationspaceclient.user.model.entity.enums.PermissionStatus;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public Page<Permission> getPermissionList(Page<Permission> page) {
        permissionMapper.selectPage(page, null);
        return page;
    }

    @Override
    public Boolean addPermission(PermissionCreateDto permissionCreateDto) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionCreateDto, permission);
        permission.setCreateTime(LocalDateTime.now());
        // 检查权限码是否已存在
        if (permissionMapper.selectCount(new QueryWrapper<Permission>().eq("permission_code", permission.getPermissionCode()))>0) {
            log.error("权限码{}:已存在", permission.getPermissionCode());
           throw new IsArgumentException(ErrorCode.PERMISSION_CODE_EXIST.getHttpStatusCode(), "权限码已存在");
        }
        permissionMapper.insert(permission);
        log.info("成功添加权限，权限ID:{},权限码:{},权限名:{},创建时间:{}", permission.getPermissionId(), permission.getPermissionCode(), permission.getPermissionName(), permission.getCreateTime());
        return true;
    }

    @Override
    public Boolean updatePermission(PermissionUpdateDto permissionUpdateDto) {
        Permission permission = permissionMapper.selectById(permissionUpdateDto.getPermissionId());
        if(permission == null){
            log.error("权限ID:{}不存在", permissionUpdateDto.getPermissionId());
            throw new IsArgumentException(ErrorCode.PERMISSION_ID_NOT_EXIST.getHttpStatusCode(), "权限ID不存在");
        }
        if (permission.getPermissionId() == null) {
            log.error("权限ID不能为空");
           throw new IsArgumentException(ErrorCode.PERMISSION_ID_NOT_NULL.getHttpStatusCode(), "权限ID不能为空");
        }

        if (permission.getPermissionName() != null&&permission.getPermissionName().trim().isEmpty()) {
           permission.setPermissionName(permissionUpdateDto.getPermissionName());
        }
        if (permission.getDescription() != null&&permission.getDescription().trim().isEmpty()) {
            permission.setDescription(permissionUpdateDto.getDescription());
        }

        return permissionMapper.updateById(permission) > 0;
    }

    /**
     * 删除权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePermission(Long permissionId) {
        Long roleCount = rolePermissionMapper.selectCount(
                new QueryWrapper<RolePermission>().eq("permission_id", permissionId)
        );

        if (roleCount > 0) {
            log.warn("权限ID:{} 存在角色关联，关联角色数量:{}", permissionId, roleCount);
            throw new IsServiceException(ErrorCode.ROLE_PERMISSION_EXIST.getHttpStatusCode(), "存在角色关联，请先解除关联关系");

        }

        // 执行删除
        int deleteCount = permissionMapper.deleteById(permissionId);
        if (deleteCount > 0) {
            log.info("成功删除权限，权限ID:{}", permissionId);
            return true;
        } else {
            log.warn("删除权限失败，权限ID:{} 可能不存在", permissionId);
            throw new IsServiceException(ErrorCode.PERMISSION_DELETE_FAILED.getHttpStatusCode(), "删除权限失败");
        }
    }

    /**
     * 修改权限状态
     */
    @Override
    public Boolean changeStatus(Long permissionId, Integer status) {
        if (permissionId == null || status == null) {
            throw new IsArgumentException(ErrorCode.PERMISSION_ID_NOT_NULL.getHttpStatusCode(), "权限ID不能为空");
        }

        Permission permission = permissionMapper.selectById(permissionId);
        if (permission != null && !permission.getStatus().getCode().equals(status)) {
            permission.setStatus(PermissionStatus.getByCode(status));
            permission.setUpdateTime(LocalDateTime.now());
            return permissionMapper.updateById(permission) > 0;
        }
        log.error("权限ID:{},修改状态失败", permissionId);
        throw new IsArgumentException(ErrorCode.PERMISSION_STATUS_CHANGE_FAILED.getHttpStatusCode(), "修改状态失败");
    }

}
