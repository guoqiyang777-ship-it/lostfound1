package com.example.lostfound.controller;

import com.example.lostfound.pojo.Admin;
import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.AdminLoginDTO;
import com.example.lostfound.pojo.vo.PageResult;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.AdminService;
import com.example.lostfound.service.UserService;
import com.example.lostfound.util.CaptchaUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;

import java.util.List;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    /**
     * 管理员登录
     *
     * @param loginDTO 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid AdminLoginDTO loginDTO) {
        log.info("管理员登录：{}", loginDTO.getUsername());
        return adminService.login(loginDTO);
    }

    /**
     * 获取管理员信息
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/info")
    public Result<Admin> getAdminInfo(HttpServletRequest request) {
        Long adminId = Long.valueOf(request.getAttribute("userId").toString());
        return adminService.getAdminInfo(adminId);
    }

    /**
     * 获取用户列表
     *
     * @return 结果
     */
    @GetMapping("/user/list")
    public Result<List<User>> getUserList() {
        return userService.getUserList();
    }

    /**
     * 分页获取用户列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param realName 真实姓名（可选）
     * @param studentNo 学号（可选）
     * @return 分页结果
     */
    @GetMapping("/user/list/paging")
    public Result<PageResult<User>> getUserListPaging(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String studentNo) {
        return userService.getUserListWithPaging(pageNum, pageSize, username, realName, studentNo);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    @PutMapping("/user/status")
    public Result<String> updateUserStatus(Long userId,
                                          Integer status) {
        log.info("更新用户状态：userId={}, status={}", userId, status);
        return userService.updateStatus(userId, status);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        return adminService.logout(token);
    }
    
    /**
     * 获取验证码
     *
     * @param session HttpSession
     * @return 验证码图片
     */
    @GetMapping("/captcha")
    public Result<String> getCaptcha(HttpSession session) {
        CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
        if (captchaResult != null) {
            // 将验证码文本存储在session中
            session.setAttribute("captcha", captchaResult.getText());
            
            // 返回Base64编码的图片
            return Result.success(captchaResult.getImage());
        } else {
            return Result.error("验证码生成失败");
        }
    }
}