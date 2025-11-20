package com.example.lostfound.service;

import com.example.lostfound.pojo.Item;
import com.example.lostfound.pojo.dto.ItemDTO;
import com.example.lostfound.pojo.vo.ItemVO;
import com.example.lostfound.pojo.vo.PageResult;
import com.example.lostfound.pojo.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 失物招领信息服务接口
 */
public interface ItemService {
    /**
     * 发布信息
     *
     * @param itemDTO 信息
     * @param userId  用户ID
     * @return 结果
     */
    Result<String> publishItem(ItemDTO itemDTO, Long userId);

    /**
     * 上传物品图片
     *
     * @param file 图片文件
     * @return 结果
     */
    Result<String> uploadItemImage(MultipartFile file);

    /**
     * 获取信息列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 状态
     * @param type 类型
     * @param title 标题
     * @param location 地点
     * @return 信息列表
     */
    Result<PageResult<ItemVO>> getItemList(int pageNum, int pageSize, Integer status, String type, String title, String location, String role);

    /**
     * 获取信息详情
     *
     * @param itemId 信息ID
     * @return 结果
     */
    Result<ItemVO> getItemDetail(Long itemId, String role);

    /**
     * 获取用户发布的信息列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param title    标题
     * @param location 地点
     * @param type     类型
     * @param status   状态
     * @return 结果
     */
    Result<PageInfo<Item>> getUserItemList(Long userId, int pageNum, int pageSize, String title, String location, String type, Integer status);

    /**
     * 更新信息状态
     *
     * @param itemId 信息ID
     * @param status 状态
     * @param userId 用户ID
     * @param role   用户角色
     * @return 结果
     */
    Result<String> updateItemStatus(Long itemId, Integer status, Long userId, String role);

    /**
     * 获取待审核的信息列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 结果
     */
    Result<PageInfo<Item>> getPendingItemList(int pageNum, int pageSize);

    /**
     * 获取用户仪表盘数据
     *
     * @param userId 用户ID
     * @return 结果
     */
    Result<Map<String, Object>> getUserDashboard(Long userId);

    /**
     * 获取管理员仪表盘数据
     *
     * @return 结果
     */
    Result<Map<String, Object>> getAdminDashboard();
    
    /**
     * 获取各类型信息统计数据
     *
     * @return 结果
     */
    Result<List<Map<String, Object>>> getItemTypeStats();
    
    /**
     * 获取各状态信息统计数据
     *
     * @return 结果
     */
    Result<List<Map<String, Object>>> getItemStatusStats();
    
    /**
     * 获取最近7天每天的信息发布数量
     *
     * @return 结果
     */
    Result<List<Map<String, Object>>> getItemDailyStats();
    
    /**
     * 获取用户发布的各类型信息统计数据
     *
     * @param userId 用户ID
     * @return 结果
     */
    Result<List<Map<String, Object>>> getUserItemTypeStats(Long userId);
    
    /**
     * 获取用户发布的每日统计数据
     *
     * @param userId 用户ID
     * @return 结果
     */
    Result<List<Map<String, Object>>> getUserItemDailyStats(Long userId);
    
    /**
     * 删除信息
     *
     * @param itemId 信息ID
     * @param userId 用户ID
     * @param role   用户角色
     * @return 结果
     */
    Result<String> deleteItem(Long itemId, Long userId, String role);
    
    /**
     * 更新信息
     *
     * @param itemDTO 信息
     * @param userId  用户ID
     * @param role    用户角色
     * @return 结果
     */
    Result<String> updateItem(ItemDTO itemDTO, Long userId, String role);
    
    /**
     * 标记信息为已完成
     *
     * @param itemId 信息ID
     * @param userId 用户ID
     * @param role   用户角色
     * @return 结果
     */
    Result<String> completeItem(Long itemId, Long userId, String role);
    
    /**
     * 获取用户发布的各状态信息统计数据
     *
     * @param userId 用户ID
     * @return 结果
     */
    Result<List<Map<String, Object>>> getUserItemStatusStats(Long userId);
}