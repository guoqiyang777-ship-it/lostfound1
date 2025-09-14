package com.example.lostfound.mapper;

import com.example.lostfound.pojo.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 失物招领信息Mapper接口
 */
@Mapper
public interface ItemMapper {
    /**
     * 根据ID查询信息
     *
     * @param id 信息ID
     * @return 信息
     */
    Item selectById(Long id);

    /**
     * 插入信息
     *
     * @param item 信息
     * @return 影响行数
     */
    int insert(Item item);

    /**
     * 更新信息
     *
     * @param item 信息
     * @return 影响行数
     */
    int update(Item item);

    /**
     * 更新信息状态
     *
     * @param id     信息ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询信息列表
     *
     * @param status 状态
     * @param type   类型
     * @return 信息列表
     */
    List<Item> selectList(@Param("status") Integer status, @Param("type") String type);

    /**
     * 查询用户发布的信息列表
     *
     * @param userId 用户ID
     * @return 信息列表
     */
    List<Item> selectByUserId(Long userId);

    /**
     * 查询待审核的信息列表
     *
     * @return 信息列表
     */
    List<Item> selectPendingList();

    /**
     * 统计用户发布的信息数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countByUserId(Long userId);

    /**
     * 统计用户发布的待审核信息数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countPendingByUserId(Long userId);

    /**
     * 统计用户发布的已解决信息数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countResolvedByUserId(Long userId);

    /**
     * 统计待审核信息数量
     *
     * @return 数量
     */
    int countPending();

    /**
     * 统计今日新增信息数量
     *
     * @return 数量
     */
    int countTodayNew();
}