package com.is.inspirationspacecommon.config;

import com.is.inspirationspacecommon.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器实现
 * 修复点：
 * 1. 正确处理Bearer token前缀
 * 2. 添加详细的错误处理和状态码返回
 * 3. 优化空token处理逻辑
 * 4. 增加token过期检查
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String tokenHeader = "Authorization";
    private static final String tokenHead = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头获取JWT令牌
        String authorizationHeader = request.getHeader(tokenHeader);
        String token = null;
        Long userId = null;

        // 检查Authorization头是否存在且以Bearer开头
        if (authorizationHeader != null && authorizationHeader.startsWith(tokenHead)){
            token = authorizationHeader.substring(tokenHead.length());

            try {
                // 验证令牌格式
                if (jwtUtil.validateToken(token)) {
                    // 额外检查令牌是否已过期
                    if (jwtUtil.isTokenExpired(token)) {
                        log.warn("Token已过期: {}", token);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token已过期");
                        return;
                    }

                    userId = jwtUtil.getUserIdFromToken(token);
                    log.debug("从Token中提取到用户ID: {}", userId);
                } else {
                    log.warn("无效的Token: {}", token);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效的Token");
                    return;
                }
            } catch (Exception ex) {
                log.error("Token解析失败: {}", ex.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token解析失败: " + ex.getMessage());
                return;
            }
        } else {
            log.debug("请求中未包含有效的Authorization头");
        }

        // 如果成功提取到用户ID，设置安全上下文
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                // 创建认证对象并设置到安全上下文
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("用户 {} 的安全上下文已设置", userId);
            } catch (Exception ex) {
                log.error("加载用户信息失败: {}", ex.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户信息加载失败");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查请求是否为公开访问路径
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/api/user/ranking") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/actuator/health");
    }
}
