package com.example.lostfound.mapper;

import com.example.lostfound.pojo.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper {
    /**
     * 根据ID查询管理员
     *
     * @param id 管理员ID
     * @return 管理员
     */
    Admin selectById(Long id);

    /**
     * 根据用户名查询管理员
     *
     * @param username 用户名
     * @return 管理员
     */
    Admin selectByUsername(String username);
}