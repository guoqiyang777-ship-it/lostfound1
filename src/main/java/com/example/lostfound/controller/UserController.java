package com.example.lostfound.controller;

import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.UserLoginDTO;
import com.example.lostfound.pojo.dto.UserRegisterDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        log.info("用户注册：{}", registerDTO.getUsername());
        return userService.register(registerDTO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO.getUsername());
        return userService.login(loginDTO);
    }

    /**
     * 获取用户信息
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return userService.getUserInfo(userId);
    }

    /**
     * 更新用户信息
     *
     * @param user    用户信息
     * @param request 请求
     * @return 结果
     */
    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        user.setId(userId);
        return userService.updateUserInfo(user);
    }

    /**
     * 更新用户头像
     *
     * @param file    头像文件
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/avatar")
    public Result<String> updateAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return userService.updateAvatar(userId, file);
    }

    /**
     * 更新用户密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param request     请求
     * @return 结果
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestParam("oldPassword") String oldPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        HttpServletRequest request) {
        Long userId = Long.valueOf(request.getAttribute("userId").toString());
        return userService.updatePassword(userId, oldPassword, newPassword);
    }
}