package com.example.lostfound.mapper;

import com.example.lostfound.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户
     */
    User selectById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    User selectByUsername(String username);

    /**
     * 插入用户
     *
     * @param user 用户
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户
     *
     * @param user 用户
     * @return 影响行数
     */
    int update(User user);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询用户列表
     *
     * @return 用户列表
     */
    List<User> selectList();
}