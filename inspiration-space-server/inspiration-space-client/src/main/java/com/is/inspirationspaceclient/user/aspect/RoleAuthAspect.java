package com.is.inspirationspaceclient.user.aspect;


import com.is.inspirationspaceclient.user.service.RoleService;
import com.is.inspirationspacecommon.annotation.RequireRole;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;


@Aspect
@Component
public class RoleAuthAspect {

    @Autowired
    private RoleService roleService;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {

        // 1️⃣ 从 JWT 直接获取用户 ID（无需查询用户表）
        Long userId =getUserIdFromToken();

        // 2️⃣ 查询用户角色（只查关联表+角色表）
        List<String> userRoles = roleService.getUserRoleCode(userId);

        // 3️⃣ 验证角色权限
        String[] requiredRoles = requireRole.value();
        boolean hasPermission = requireRole.requireAll()
                ? Arrays.stream(requiredRoles).allMatch(userRoles::contains)
                : Arrays.stream(requiredRoles).anyMatch(userRoles::contains);

        if (!hasPermission) {
            throw new IsServiceException(
                    ErrorCode.FORBIDDEN.getHttpStatusCode(),
                    String.format("没有权限访问此资源,当前权限:%s,需要权限:%s", userRoles, Arrays.toString(requiredRoles))
            );

        }
    }

    // 从请求头获取 JWT 并解析用户 ID
    // 修改 AOP 切面中的用户 ID 获取方式
    private Long getUserIdFromToken() {

            // 获取当前请求对象
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("Authorization");


            if (token == null || !token.startsWith("Bearer ")) {
                throw new SecurityException("未提供有效的认证令牌：缺少或格式错误的 Authorization 头");
            }

            // 提取 token（去掉 "Bearer " 前缀）
            token = token.substring(7);

            // 使用 JwtUtil 提取用户 ID（内部已包含签名验证和过期检查）
            return JwtUtil.getUserIdFromToken(token);


    }


}

