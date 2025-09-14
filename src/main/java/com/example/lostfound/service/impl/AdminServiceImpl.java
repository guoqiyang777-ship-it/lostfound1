package com.example.lostfound.service.impl;

import com.example.lostfound.mapper.AdminMapper;
import com.example.lostfound.pojo.Admin;
import com.example.lostfound.pojo.dto.AdminLoginDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.AdminService;
import com.example.lostfound.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
}