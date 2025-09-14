package com.example.lostfound.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    
    /**
     * 电话
     */
    @NotBlank(message = "电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
}