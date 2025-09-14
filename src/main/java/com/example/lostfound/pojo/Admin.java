package com.example.lostfound.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
public class Admin {
    /**
     * 管理员ID
     */
    private Long id;
    
    /**
     * 管理员账号（唯一）
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
     * 电话
     */
    private String phone;
    
    /**
     * 状态（0正常，1禁用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}