package com.is.inspirationspacecommon.interceptor;



import com.is.inspirationspacecommon.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 移除 "Bearer " 前缀

            try {
                // 解析 token
                Long userId = JwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    // 将 userId 存入请求属性中
                    request.setAttribute("currentUserId", userId);
                    return true;
                }
            } catch (Exception e) {
                response.setStatus(401);
                return false;
            }
        }

        response.setStatus(401);
        return false;
    }
}

