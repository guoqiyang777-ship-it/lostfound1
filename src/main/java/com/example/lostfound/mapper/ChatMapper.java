package com.example.lostfound.mapper;

import com.example.lostfound.pojo.Chat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper接口
 */
@Mapper
public interface ChatMapper {
    /**
     * 插入消息
     *
     * @param chat 消息
     * @return 影响行数
     */
    int insert(Chat chat);

    /**
     * 查询两个用户之间的聊天记录
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 聊天记录
     */
    List<Chat> selectChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询用户的聊天列表（去重）
     *
     * @param userId 用户ID
     * @return 聊天列表
     */
    List<Long> selectChatUserList(Long userId);

    /**
     * 统计用户未读消息数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countUnreadMessage(Long userId);
    
    /**
     * 将指定用户发送的消息标记为已读
     *
     * @param toUserId 接收方ID
     * @param fromUserId 发送方ID
     * @return 影响行数
     */
    int updateMessageReadStatus(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId);
}