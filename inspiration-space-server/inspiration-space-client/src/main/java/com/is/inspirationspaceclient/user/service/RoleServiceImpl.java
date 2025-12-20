package com.is.inspirationspaceclient.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.user.mapper.RoleMapper;
import com.is.inspirationspaceclient.user.mapper.UserRoleMapper;
import com.is.inspirationspaceclient.user.model.dto.RoleCreateDto;
import com.is.inspirationspaceclient.user.model.dto.RoleDto;
import com.is.inspirationspaceclient.user.model.entity.Role;
import com.is.inspirationspaceclient.user.model.entity.UserRole;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;


    @Override
    public Page<Role> getRoleList(Page<Role> page) {
        roleMapper.selectPage(page, null);
        return page;
    }

    @Override
    public Boolean addRole(RoleCreateDto roleCreateDto) {
        Role role = new Role();
        BeanUtils.copyProperties(roleCreateDto, role);
        role.setCreateTime(LocalDateTime.now());

        // 检查角色名是否已存在
        if (roleMapper.selectCount(new QueryWrapper<Role>().eq("role_code", role.getRoleCode())) > 0) {
            log.error("角色编码{}:已存在", role.getRoleCode());
            throw new IsArgumentException(ErrorCode.ROLE_ADD_FAILED.getHttpStatusCode(), "角色编码已存在") ;
        }

        roleMapper.insert(role);
        log.info("成功添加角色，角色ID:{},角色编码:{},创建时间:{}", role.getRoleId(), role.getRoleCode(), role.getCreateTime());
        return true;

    }

    @Override
    public List<String> getUserRoleCode(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 查询用户关联的角色ID列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<UserRole>().eq("user_id", userId)
        );

        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        // 提取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 根据角色ID查询角色名称
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @Override
    public Boolean updateRole(RoleDto roleDto) {
        Role role = roleMapper.selectById(roleDto.getRoleId());
        if (role == null) {
            log.error("角色ID不能为空");
            return false;
        }
        // 检查角色描述，只有非null且非空字符串时才更新
        if (roleDto.getRoleDescription() != null && !roleDto.getRoleDescription().trim().isEmpty()) {
            role.setRoleDescription(roleDto.getRoleDescription());
        }

        // 检查角色名称，只有非null且非空字符串时才更新
        if (roleDto.getRoleName() != null && !roleDto.getRoleName().trim().isEmpty()) {
            role.setRoleName(roleDto.getRoleName());
        }
        role.setUpdateTime(LocalDateTime.now());
        log.info("成功更新角色，角色ID:{},角色名称:{},角色描述:{},更新时间:{}", role.getRoleId(), role.getRoleName(), role.getRoleDescription(), role.getUpdateTime());
        return roleMapper.updateById(role) > 0;
    }

    @Override
    public Boolean deleteRole(Long roleId) {
        Long userCount = userRoleMapper.selectCount(
                new QueryWrapper<UserRole>().eq("role_id", roleId)
        );
        if (userCount > 0) {
            log.warn("角色ID:{} 存在用户关联，关联用户数量:{}", roleId, userCount);
            throw new IsServiceException(ErrorCode.ROLE_DELETE_FAILED.getHttpStatusCode(), "存在用户关联，请先解除关联关系");
        }
        // 执行删除
        int deleteCount = roleMapper.deleteById(roleId);
        if (deleteCount > 0) {
            log.info("成功删除角色，角色ID:{}", roleId);
            return true;
        } else {
            log.warn("删除角色失败，角色ID:{} ", roleId);
            throw new IsServiceException(ErrorCode.ROLE_DELETE_FAILED.getHttpStatusCode(), "删除角色失败");
        }
    }


}
