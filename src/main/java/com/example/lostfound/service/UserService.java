package com.example.lostfound.service;

import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.UserLoginDTO;
import com.example.lostfound.pojo.dto.UserRegisterDTO;
import com.example.lostfound.pojo.vo.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 结果
     */
    Result<String> register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 结果
     */
    Result<String> login(UserLoginDTO loginDTO);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 结果
     */
    Result<User> getUserInfo(Long userId);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    Result<String> updateUserInfo(User user);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 结果
     */
    Result<String> updateAvatar(Long userId, MultipartFile file);

    /**
     * 更新用户密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 结果
     */
    Result<String> updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 获取用户列表
     *
     * @return 结果
     */
    Result<List<User>> getUserList();

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    Result<String> updateStatus(Long userId, Integer status);
}