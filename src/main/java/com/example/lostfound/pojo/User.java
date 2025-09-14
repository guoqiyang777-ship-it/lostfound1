package com.example.lostfound.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名（唯一）
     */
    private String username;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 姓名
     */
    private String realName;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 头像URL（OSS）
     */
    private String avatarUrl;
    
    /**
     * 状态（0正常，1禁用）
     */
    private Integer status;
    
    /**
     * 注册时间
     */
    private LocalDateTime createTime;
    
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}