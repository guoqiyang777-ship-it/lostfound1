package com.example.lostfound.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
public class Chat {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 发送方ID（外键 → user.id）
     */
    private Long fromUser;
    
    /**
     * 接收方ID（外键 → user.id）
     */
    private Long toUser;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 发送时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否已读（0未读，1已读）
     */
    private Integer isRead;
}