package com.example.lostfound.interceptor;

import com.example.lostfound.mapper.AdminMapper;
import com.example.lostfound.mapper.UserMapper;
import com.example.lostfound.pojo.Admin;
import com.example.lostfound.pojo.User;
import com.example.lostfound.util.JwtUtil;
import com.example.lostfound.util.RedisUtil;
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

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AdminMapper adminMapper;

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
            // 先检查Redis中是否有token缓存
            String tokenKey = "token:" + token;
            Object cachedUserInfo = redisUtil.get(tokenKey);
            
            if (cachedUserInfo != null) {
                // 从缓存中获取用户信息
                String[] userInfo = cachedUserInfo.toString().split(":");
                String userIdStr = userInfo[0];
                String role = userInfo[1];
                
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

                // ✅ 将用户ID转换为Long类型后存入请求属性中
                request.setAttribute("userId", Long.valueOf(userIdStr));
                request.setAttribute("role", role);
                
                // 刷新缓存过期时间（24小时）
                redisUtil.expire(tokenKey, 86400);
                
                return true;
            }
            
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("token无效或已过期");
                return false;
            }

            // 获取token中的角色和用户ID
            String role = jwtUtil.getRoleFromToken(token);
            String userIdStr = jwtUtil.getUserIdFromToken(token);

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
            
            // ✅ 关键修复：在重新缓存token之前，验证用户状态
            if ("user".equals(role)) {
                User user = userMapper.selectById(Long.valueOf(userIdStr));
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("用户不存在");
                    return false;
                }
                if (user.getStatus() == 1) {
                    // 用户已被禁用，拒绝访问，不重新缓存token
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("账号已被禁用");
                    log.info("用户已被禁用，拒绝访问: userId={}", userIdStr);
                    return false;
                }
            } else if ("admin".equals(role)) {
                Admin admin = adminMapper.selectById(Long.valueOf(userIdStr));
                if (admin == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("管理员不存在");
                    return false;
                }
                if (admin.getStatus() == 1) {
                    // 管理员已被禁用，拒绝访问
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("账号已被禁用");
                    log.info("管理员已被禁用，拒绝访问: adminId={}", userIdStr);
                    return false;
                }
            }

            // ✅ 只有状态正常的用户，才重新缓存token
            // 将用户信息缓存到Redis中（24小时过期）
            redisUtil.set(tokenKey, userIdStr + ":" + role, 86400);
            
            // ✅ 建立反向索引（用于禁用时清除token）
            String tokenSetKey = "user".equals(role) ? "user:tokens:" + userIdStr : "admin:tokens:" + userIdStr;
            redisUtil.addToSet(tokenSetKey, token);
            redisUtil.expire(tokenSetKey, 86400);

            // ✅ 将用户ID转换为Long类型后存入请求属性中
            request.setAttribute("userId", Long.valueOf(userIdStr));
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