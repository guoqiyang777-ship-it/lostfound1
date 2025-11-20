package com.example.lostfound.service.impl;

import com.example.lostfound.mapper.AdminMapper;
import com.example.lostfound.pojo.Admin;
import com.example.lostfound.pojo.dto.AdminLoginDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.AdminService;
import com.example.lostfound.util.JwtUtil;
import com.example.lostfound.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public Result<String> login(AdminLoginDTO loginDTO) {
        // 查询管理员
        Admin admin = adminMapper.selectByUsername(loginDTO.getUsername());
        if (admin == null) {
            return Result.error("用户名或密码错误");
        }

        // 检查密码
        String encryptPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!admin.getPassword().equals(encryptPassword)) {
            return Result.error("用户名或密码错误");
        }

        // 检查状态
        if (admin.getStatus() == 1) {
            return Result.error("账号已被禁用");
        }

        // 生成token
        String token = jwtUtil.generateToken(admin.getId().toString(), "admin");
        
        // ✅ 存储token到Redis（24小时）
        String tokenKey = "token:" + token;
        long expirationSeconds = jwtExpiration / 1000;
        redisUtil.set(tokenKey, admin.getId() + ":" + "admin", expirationSeconds);
        
        // ✅ 建立adminId到token的反向索引
        String adminTokenSetKey = "admin:tokens:" + admin.getId();
        redisUtil.addToSet(adminTokenSetKey, token);
        redisUtil.expire(adminTokenSetKey, expirationSeconds);
        
        log.info("管理员登录，token已存入Redis: key={}, value={}, expiration={}秒", tokenKey, admin.getId() + ":" + "admin", expirationSeconds);

        return Result.success(token);
    }

    @Override
    public Result<Admin> getAdminInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        // 清除敏感信息
        admin.setPassword(null);

        return Result.success(admin);
    }

    @Override
    public Result<String> logout(String token) {
        // 从Redis中删除token
        String tokenKey = "token:" + token;
        redisUtil.delete(tokenKey);
        
        // ✅ 从管理员token集合中移除
        try {
            String adminIdStr = jwtUtil.getUserIdFromToken(token);
            if (adminIdStr != null) {
                String adminTokenSetKey = "admin:tokens:" + adminIdStr;
                redisUtil.removeFromSet(adminTokenSetKey, token);
            }
        } catch (Exception e) {
            log.warn("从adminTokenSet中移除token失败", e);
        }
        
        log.info("管理员登出，token已从Redis中删除: key={}", tokenKey);
        return Result.success("登出成功");
    }
}