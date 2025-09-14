package com.example.lostfound.interceptor;

import com.example.lostfound.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        log.info("请求路径：{}", requestURI);
        
        // 对于OPTIONS请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取请求头中的token
        String token = getTokenFromRequest(request);

        // 如果token为空，返回401
        if (!StringUtils.hasText(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("未授权，请先登录");
            return false;
        }

        try {
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("token无效或已过期");
                return false;
            }

            // 获取token中的角色
            String role = jwtUtil.getRoleFromToken(token);

            // 判断请求路径和角色是否匹配
            if (requestURI.startsWith("/admin/") && !"admin".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("权限不足");
                return false;
            }

            if (requestURI.startsWith("/user/") && !"user".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("权限不足");
                return false;
            }

            // 将用户ID存入请求属性中，方便后续使用
            request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
            request.setAttribute("role", role);

            return true;
        } catch (Exception e) {
            log.error("JWT验证失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("token验证失败");
            return false;
        }
    }

    /**
     * 从请求头中获取token
     *
     * @param request 请求
     * @return token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}