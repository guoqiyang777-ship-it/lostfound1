package com.example.lostfound.service;

import com.example.lostfound.pojo.Item;
import com.example.lostfound.pojo.dto.ItemDTO;
import com.example.lostfound.pojo.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

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
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   状态
     * @param type     类型
     * @return 结果
     */
    Result<PageInfo<Item>> getItemList(int pageNum, int pageSize, Integer status, String type);

    /**
     * 获取信息详情
     *
     * @param itemId 信息ID
     * @return 结果
     */
    Result<Item> getItemDetail(Long itemId);

    /**
     * 获取用户发布的信息列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 结果
     */
    Result<PageInfo<Item>> getUserItemList(Long userId, int pageNum, int pageSize);

    /**
     * 更新信息状态
     *
     * @param itemId 信息ID
     * @param status 状态
     * @param userId 用户ID
     * @return 结果
     */
    Result<String> updateItemStatus(Long itemId, Integer status, Long userId);

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
}