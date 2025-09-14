package com.example.lostfound.service.impl;

import com.example.lostfound.mapper.ChatMapper;
import com.example.lostfound.mapper.UserMapper;
import com.example.lostfound.pojo.Chat;
import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.ChatDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<String> sendMessage(ChatDTO chatDTO, Long fromUserId) {
        // 查询发送方
        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) {
            return Result.error("发送方不存在");
        }

        // 查询接收方
        User toUser = userMapper.selectById(chatDTO.getToUser());
        if (toUser == null) {
            return Result.error("接收方不存在");
        }

        // 创建消息
        Chat chat = new Chat();
        chat.setFromUser(fromUserId);
        chat.setToUser(chatDTO.getToUser());
        chat.setContent(chatDTO.getContent());
        chat.setCreateTime(LocalDateTime.now());
        chat.setIsRead(0); // 设置为未读

        // 插入消息
        chatMapper.insert(chat);

        return Result.success("发送成功");
    }

    @Override
    public Result<List<Chat>> getChatHistory(Long userId1, Long userId2) {
        // 查询用户1
        User user1 = userMapper.selectById(userId1);
        if (user1 == null) {
            return Result.error("用户1不存在");
        }

        // 查询用户2
        User user2 = userMapper.selectById(userId2);
        if (user2 == null) {
            return Result.error("用户2不存在");
        }

        // 查询聊天记录
        List<Chat> chatList = chatMapper.selectChatHistory(userId1, userId2);

        return Result.success(chatList);
    }

    @Override
    public Result<List<Map<String, Object>>> getChatUserList(Long userId) {
        try {
            // 查询用户
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("获取聊天用户列表失败: 用户不存在, userId={}", userId);
                return Result.error("用户不存在");
            }

            // 查询聊天用户列表
            log.info("开始查询用户{}的聊天列表", userId);
            List<Long> userIdList = chatMapper.selectChatUserList(userId);
            log.info("用户{}的聊天列表查询结果: {}", userId, userIdList);

            // 查询用户信息
            List<Map<String, Object>> result = new ArrayList<>();
            for (Long otherUserId : userIdList) {
                User otherUser = userMapper.selectById(otherUserId);
                if (otherUser != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", otherUser.getId());
                    map.put("username", otherUser.getUsername());
                    map.put("realName", otherUser.getRealName());
                    map.put("avatarUrl", otherUser.getAvatarUrl());
                    result.add(map);
                } else {
                    log.warn("聊天用户{}不存在", otherUserId);
                }
            }

            log.info("用户{}的聊天用户列表获取成功, 共{}个联系人", userId, result.size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取聊天用户列表异常: userId={}", userId, e);
            return Result.error("获取聊天用户列表失败");
        }
    }
    
    @Override
    public Result<String> markMessageAsRead(Long userId, Long fromUserId) {
        try {
            // 查询用户
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("标记消息已读失败: 用户不存在, userId={}", userId);
                return Result.error("用户不存在");
            }
            
            // 查询发送方用户
            User fromUser = userMapper.selectById(fromUserId);
            if (fromUser == null) {
                log.error("标记消息已读失败: 发送方用户不存在, fromUserId={}", fromUserId);
                return Result.error("发送方用户不存在");
            }
            
            // 更新消息已读状态 - 参数顺序为(toUserId, fromUserId)
            int count = chatMapper.updateMessageReadStatus(userId, fromUserId);
            log.info("用户{}标记来自用户{}的消息为已读, 共{}条", userId, fromUserId, count);
            
            return Result.success("标记已读成功");
        } catch (Exception e) {
            log.error("标记消息已读异常: userId={}, fromUserId={}", userId, fromUserId, e);
            return Result.error("标记消息已读失败");
        }
    }
}