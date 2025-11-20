package com.example.lostfound.service.impl;

import com.example.lostfound.listener.ChatMessageListener;
import com.example.lostfound.mapper.ChatMapper;
import com.example.lostfound.mapper.UserContactMapper;
import com.example.lostfound.mapper.UserMapper;
import com.example.lostfound.pojo.Chat;
import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.UserContact;
import com.example.lostfound.pojo.dto.ChatDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.ChatService;
import com.example.lostfound.websocket.ChatWebSocketServer;
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
    
    @Autowired
    private UserContactMapper userContactMapper;
    
    @Autowired
    private ChatMessageListener chatMessageListener;

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
        
        // 添加联系人关系（如果不存在）
        if (userContactMapper.exists(fromUserId, chatDTO.getToUser()) == 0) {
            UserContact userContact = new UserContact();
            userContact.setUserId(fromUserId);
            userContact.setContactUserId(chatDTO.getToUser());
            userContact.setCreateTime(LocalDateTime.now());
            userContactMapper.insert(userContact);
        }
        
        // 添加反向联系人关系（如果不存在）
        if (userContactMapper.exists(chatDTO.getToUser(), fromUserId) == 0) {
            UserContact reverseContact = new UserContact();
            reverseContact.setUserId(chatDTO.getToUser());
            reverseContact.setContactUserId(fromUserId);
            reverseContact.setCreateTime(LocalDateTime.now());
            userContactMapper.insert(reverseContact);
        }
        
        // 通过WebSocket发送实时通知
        try {
            // 计算接收方的最新未读消息数量
            int receiverUnreadCount = chatMessageListener.getUnreadMessageCount(chatDTO.getToUser());
            // 计算发送方的未读消息数量（虽然发送方通常为0，但为了一致性也计算）
            int senderUnreadCount = chatMessageListener.getUnreadMessageCount(fromUserId);
            
            // 准备WebSocket消息给接收方（包含完整信息和未读数量）
            Map<String, Object> messageToReceiver = new HashMap<>();
            messageToReceiver.put("type", "CHAT");
            messageToReceiver.put("chatId", chat.getId());
            messageToReceiver.put("fromUser", fromUserId);
            messageToReceiver.put("fromUsername", fromUser.getUsername());
            messageToReceiver.put("fromUserName", fromUser.getRealName() != null ? fromUser.getRealName() : fromUser.getUsername());
            messageToReceiver.put("toUser", chatDTO.getToUser());
            messageToReceiver.put("content", chatDTO.getContent());
            messageToReceiver.put("createTime", chat.getCreateTime().toString());
            messageToReceiver.put("unreadCount", receiverUnreadCount); // 直接包含未读数量
            
            // 发送WebSocket消息给接收方
            boolean sentToReceiver = ChatWebSocketServer.sendMessageToUser(chatDTO.getToUser().intValue(), messageToReceiver);
            if (sentToReceiver) {
                log.info("已通过WebSocket发送消息通知给用户{}，未读数量: {}", chatDTO.getToUser(), receiverUnreadCount);
            }
            
            // 准备WebSocket消息给发送方（用于更新发送方的聊天列表）
            Map<String, Object> messageToSender = new HashMap<>();
            messageToSender.put("type", "CHAT");
            messageToSender.put("chatId", chat.getId());
            messageToSender.put("fromUser", fromUserId);
            messageToSender.put("fromUsername", fromUser.getUsername());
            messageToSender.put("fromUserName", fromUser.getRealName() != null ? fromUser.getRealName() : fromUser.getUsername());
            messageToSender.put("toUser", chatDTO.getToUser());
            messageToSender.put("content", chatDTO.getContent());
            messageToSender.put("createTime", chat.getCreateTime().toString());
            messageToSender.put("unreadCount", senderUnreadCount); // 发送方的未读数量
            
            // 发送给发送方本人
            boolean sentToSender = ChatWebSocketServer.sendMessageToUser(fromUserId.intValue(), messageToSender);
            if (sentToSender) {
                log.info("已通过WebSocket发送消息通知给发送方{}，未读数量: {}", fromUserId, senderUnreadCount);
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息通知失败", e);
            // 消息已保存到数据库，WebSocket通知失败不影响主流程
        }

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
    public Result<List<Map<String, Object>>> getChatUserListWithLastMessageAndUnreadCount(Long userId) {
        try {
            // 查询用户
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("获取聊天用户列表失败: 用户不存在, userId={}", userId);
                return Result.error("用户不存在");
            }

            // 查询联系人列表
            log.info("开始查询用户{}的联系人列表", userId);
            List<Long> contactUserIds = userContactMapper.selectContactUserIds(userId);
            
            // 如果联系人列表为空，则从聊天记录中获取
            if (contactUserIds.isEmpty()) {
                log.info("用户{}的联系人列表为空，从聊天记录中获取", userId);
                // 查询聊天用户列表（包含最后一条消息和未读消息数量）
                List<Map<String, Object>> chatUserList = chatMapper.selectChatUserListWithLastMessageAndUnreadCount(userId);
                
                // 自动添加到联系人表中
                for (Map<String, Object> chatUser : chatUserList) {
                    Long otherUserId = (Long) chatUser.get("otherUserId");
                    UserContact userContact = new UserContact();
                    userContact.setUserId(userId);
                    userContact.setContactUserId(otherUserId);
                    userContact.setCreateTime(LocalDateTime.now());
                    userContactMapper.insert(userContact);
                }
                
                // 重新获取联系人列表
                contactUserIds = userContactMapper.selectContactUserIds(userId);
            }
            
            // 查询用户信息和最后一条消息
            List<Map<String, Object>> result = new ArrayList<>();
            for (Long contactUserId : contactUserIds) {
                User contactUser = userMapper.selectById(contactUserId);
                if (contactUser != null) {
                    // 获取与该联系人的最后一条消息
                    List<Chat> chatHistory = chatMapper.selectChatHistory(userId, contactUserId);
                    Chat lastChat = chatHistory.isEmpty() ? null : chatHistory.get(chatHistory.size() - 1);
                    
                    // 获取未读消息数量
                    int unreadCount = chatMapper.countUnreadMessageFromUser(userId, contactUserId);
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", contactUser.getId());
                    map.put("username", contactUser.getUsername());
                    map.put("realName", contactUser.getRealName());
                    map.put("avatarUrl", contactUser.getAvatarUrl());
                    map.put("lastMessage", lastChat != null ? lastChat.getContent() : null);
                    map.put("lastMessageTime", lastChat != null ? lastChat.getCreateTime() : null);
                    map.put("unreadCount", unreadCount);
                    result.add(map);
                } else {
                    log.warn("联系人{}不存在", contactUserId);
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
            
            // 通过WebSocket发送已读状态更新通知
            try {
                // 计算当前用户的最新未读消息数量
                int unreadCount = chatMessageListener.getUnreadMessageCount(userId);
                
                // 准备WebSocket消息（包含完整信息和未读数量）
                Map<String, Object> message = new HashMap<>();
                message.put("type", "READ_STATUS");
                message.put("fromUser", fromUserId);
                message.put("toUser", userId);
                message.put("unreadCount", unreadCount); // 直接包含未读数量
                
                // 发送WebSocket消息给当前用户，更新其界面
                ChatWebSocketServer.sendMessageToUser(userId.intValue(), message);
                log.info("已通过WebSocket发送已读状态更新通知给用户{}，未读数量: {}", userId, unreadCount);
            } catch (Exception e) {
                log.error("发送WebSocket已读状态更新通知失败", e);
                // WebSocket通知失败不影响主流程
            }
            
            return Result.success("标记已读成功");
        } catch (Exception e) {
            log.error("标记消息已读异常: userId={}, fromUserId={}", userId, fromUserId, e);
            return Result.error("标记消息已读失败");
        }
    }
    
    @Override
    public Result<String> deleteContact(Long userId, Long contactUserId) {
        try {
            // 查询用户
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("删除联系人失败: 用户不存在, userId={}", userId);
                return Result.error("用户不存在");
            }
            
            // 查询联系人用户
            User contactUser = userMapper.selectById(contactUserId);
            if (contactUser == null) {
                log.error("删除联系人失败: 联系人不存在, contactUserId={}", contactUserId);
                return Result.error("联系人不存在");
            }
            
            // 从联系人表中删除关系（只删除联系人列表显示，不删除聊天记录）
            int count = userContactMapper.delete(userId, contactUserId);
            log.info("用户{}删除联系人{}, 结果: {}", userId, contactUserId, count > 0 ? "成功" : "联系人不存在");
            
            // 通过WebSocket发送联系人删除通知
            try {
                // 准备WebSocket消息
                Map<String, Object> message = new HashMap<>();
                message.put("type", "CONTACT_DELETED");
                message.put("contactUserId", contactUserId);
                
                // 发送WebSocket消息给当前用户，更新其界面
                ChatWebSocketServer.sendMessageToUser(userId.intValue(), message);
                log.info("已通过WebSocket发送联系人删除通知给用户{}", userId);
            } catch (Exception e) {
                log.error("发送WebSocket联系人删除通知失败", e);
                // WebSocket通知失败不影响主流程
            }
            
            return Result.success("删除联系人成功");
        } catch (Exception e) {
            log.error("删除联系人异常: userId={}, contactUserId={}", userId, contactUserId, e);
            return Result.error("删除联系人失败");
        }
    }
}