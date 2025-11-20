package com.example.lostfound.listener;

import com.example.lostfound.mapper.ChatMapper;
import com.example.lostfound.pojo.Chat;
import com.example.lostfound.websocket.ChatWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天消息监听器，用于处理新消息通知
 */
@Component
@Slf4j
public class ChatMessageListener {

    @Autowired
    private ChatMapper chatMapper;

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    public int getUnreadMessageCount(Long userId) {
        return chatMapper.countUnreadMessage(userId);
    }

    /**
     * 发送未读消息数量更新通知
     *
     * @param userId 用户ID
     */
    public void sendUnreadCountUpdate(Long userId) {
        try {
            int unreadCount = getUnreadMessageCount(userId);
            
            // 准备WebSocket消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "UNREAD_COUNT");
            message.put("unreadCount", unreadCount);
            
            // 发送WebSocket消息给用户
            boolean sent = ChatWebSocketServer.sendMessageToUser(userId.intValue(), message);
            if (sent) {
                log.debug("已通过WebSocket发送未读消息数量更新通知给用户{}, 未读数量: {}", userId, unreadCount);
            } else {
                // 用户未连接WebSocket，这是正常情况，不记录警告
                log.debug("用户{}的WebSocket连接不存在，未读消息数量将在下次连接时同步", userId);
            }
        } catch (Exception e) {
            log.error("发送未读消息数量更新通知失败, userId={}", userId, e);
        }
    }
}
