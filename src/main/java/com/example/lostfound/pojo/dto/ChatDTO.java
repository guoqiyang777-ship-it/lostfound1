package com.example.lostfound.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天消息DTO
 */
@Data
public class ChatDTO {
    /**
     * 接收方ID
     */
    @NotNull(message = "接收方ID不能为空")
    private Long toUser;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}