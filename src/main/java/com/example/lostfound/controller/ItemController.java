package com.example.lostfound.controller;

import com.example.lostfound.pojo.Item;
import com.example.lostfound.pojo.dto.ItemDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.ItemService;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 失物招领信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 发布信息
     *
     * @param itemDTO 信息
     * @param request 请求
     * @return 结果
     */
    @PostMapping("/publish")
    public Result<String> publishItem(@RequestBody @Valid ItemDTO itemDTO, HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("发布信息失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        log.info("发布信息：userId={}, title={}", userId, itemDTO.getTitle());
        return itemService.publishItem(itemDTO, userId);
    }

    /**
     * 上传物品图片
     *
     * @param file 图片文件
     * @return 结果
     */
    @PostMapping("/upload")
    public Result<String> uploadItemImage(@RequestParam("file") MultipartFile file) {
        return itemService.uploadItemImage(file);
    }

    /**
     * 获取信息列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   状态
     * @param type     类型
     * @return 结果
     */
    @GetMapping("/list")
    public Result<PageInfo<Item>> getItemList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                             @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                             @RequestParam(value = "status", required = false) Integer status,
                                             @RequestParam(value = "type", required = false) String type) {
        return itemService.getItemList(pageNum, pageSize, status, type);
    }

    /**
     * 获取信息详情
     *
     * @param itemId 信息ID
     * @return 结果
     */
    @GetMapping("/detail/{itemId}")
    public Result<Item> getItemDetail(@PathVariable("itemId") Long itemId) {
        return itemService.getItemDetail(itemId);
    }

    /**
     * 获取用户发布的信息列表
     *
     * @param request  请求
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 结果
     */
    @GetMapping("/user/list")
    public Result<PageInfo<Item>> getUserItemList(HttpServletRequest request,
                                                 @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("获取用户发布的信息列表失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        return itemService.getUserItemList(userId, pageNum, pageSize);
    }

    /**
     * 更新信息状态
     *
     * @param itemId  信息ID
     * @param status  状态
     * @param request 请求
     * @return 结果
     */
    @PutMapping("/status")
    public Result<String> updateItemStatus(@RequestParam("itemId") Long itemId,
                                          @RequestParam("status") Integer status,
                                          HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("更新信息状态失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        log.info("更新信息状态：userId={}, itemId={}, status={}", userId, itemId, status);
        return itemService.updateItemStatus(itemId, status, userId);
    }

    /**
     * 获取待审核的信息列表（管理员）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 结果
     */
    @GetMapping("/admin/pending")
    public Result<PageInfo<Item>> getPendingItemList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return itemService.getPendingItemList(pageNum, pageSize);
    }

    /**
     * 获取用户仪表盘数据
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/user/dashboard")
    public Result<Map<String, Object>> getUserDashboard(HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("获取用户仪表盘数据失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        return itemService.getUserDashboard(userId);
    }

    /**
     * 获取管理员仪表盘数据
     *
     * @return 结果
     */
    @GetMapping("/admin/dashboard")
    public Result<Map<String, Object>> getAdminDashboard() {
        return itemService.getAdminDashboard();
    }
}