package com.example.lostfound.service.impl;

import com.example.lostfound.mapper.UserMapper;
import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.UserLoginDTO;
import com.example.lostfound.pojo.dto.UserRegisterDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.UserService;
import com.example.lostfound.util.JwtUtil;
import com.example.lostfound.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OssUtil ossUtil;

    @Override
    public Result<String> register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);

        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));

        // 设置状态和时间
        user.setStatus(0); // 0正常，1禁用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 插入用户
        userMapper.insert(user);

        return Result.success("注册成功");
    }

    @Override
    public Result<String> login(UserLoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 检查密码
        String encryptPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!user.getPassword().equals(encryptPassword)) {
            return Result.error("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == 1) {
            return Result.error("账号已被禁用");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId().toString(), "user");

        return Result.success(token);
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 清除敏感信息
        user.setPassword(null);

        return Result.success(user);
    }

    @Override
    public Result<String> updateUserInfo(User user) {
        // 查询用户
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }

        // 设置更新时间
        user.setUpdateTime(LocalDateTime.now());

        // 更新用户
        userMapper.update(user);

        return Result.success("更新成功");
    }

    @Override
    public Result<String> updateAvatar(Long userId, MultipartFile file) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        try {
            // 上传头像
            String avatarUrl = ossUtil.uploadAvatar(file);

            // 更新用户头像
            user.setAvatarUrl(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.update(user);

            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return Result.error("上传头像失败");
        }
    }

    @Override
    public Result<String> updatePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 检查旧密码
        String encryptOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptOldPassword)) {
            return Result.error("旧密码错误");
        }

        // 更新密码
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);

        return Result.success("修改成功");
    }

    @Override
    public Result<List<User>> getUserList() {
        List<User> userList = userMapper.selectList();

        // 清除敏感信息
        userList.forEach(user -> user.setPassword(null));

        return Result.success(userList);
    }

    @Override
    public Result<String> updateStatus(Long userId, Integer status) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新状态
        userMapper.updateStatus(userId, status);

        return Result.success("操作成功");
    }
}