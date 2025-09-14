package com.example.lostfound.service;

import com.example.lostfound.pojo.Chat;
import com.example.lostfound.pojo.dto.ChatDTO;
import com.example.lostfound.pojo.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务接口
 */
public interface ChatService {
    /**
     * 发送消息
     *
     * @param chatDTO 消息
     * @param fromUserId 发送方ID
     * @return 结果
     */
    Result<String> sendMessage(ChatDTO chatDTO, Long fromUserId);

    /**
     * 获取聊天记录
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 结果
     */
    Result<List<Chat>> getChatHistory(Long userId1, Long userId2);

    /**
     * 获取聊天用户列表
     *
     * @param userId 用户ID
     * @return 结果
     */
    Result<List<Map<String, Object>>> getChatUserList(Long userId);
    
    /**
     * 标记与某用户的消息为已读
     *
     * @param userId 当前用户ID
     * @param fromUserId 发送方用户ID
     * @return 结果
     */
    Result<String> markMessageAsRead(Long userId, Long fromUserId);
}