package com.example.lostfound.controller;

import com.example.lostfound.pojo.Item;
import com.example.lostfound.pojo.dto.ItemDTO;
import com.example.lostfound.pojo.vo.ItemVO;
import com.example.lostfound.pojo.vo.PageResult;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.ItemService;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param status    状态
     * @param type      类型
     * @param title     标题
     * @param location  地点
     * @return 结果
     */
    @GetMapping("/list")
    public Result<PageResult<ItemVO>> getItemList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                             @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                             @RequestParam(value = "status", required = false) Integer status,
                                             @RequestParam(value = "type", required = false) String type,
                                             @RequestParam(value = "title", required = false) String title,
                                             @RequestParam(value = "location", required = false) String location,
                                             HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        log.info("获取信息列表：pageNum={}, pageSize={}, status={}, type={}, title={}, location={}, role={}", 
                pageNum, pageSize, status, type, title, location, role);
        return itemService.getItemList(pageNum, pageSize, status, type, title, location, role);
    }

    /**
     * 获取信息详情
     *
     * @param itemId 信息ID
     * @return 结果
     */
    @GetMapping("/detail/{itemId}")
    public Result<ItemVO> getItemDetail(HttpServletRequest request, @PathVariable("itemId") Long itemId) {
        String role = (String) request.getAttribute("role");
        return itemService.getItemDetail(itemId, role);
    }

    /**
     * 获取用户发布的信息列表
     *
     * @param request  请求
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param title    标题
     * @param location 地点
     * @param type     类型
     * @param status   状态
     * @return 结果
     */
    @GetMapping("/user/list")
    public Result<PageInfo<Item>> getUserItemList(HttpServletRequest request,
                                                 @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                                 @RequestParam(value = "title", required = false) String title,
                                                 @RequestParam(value = "location", required = false) String location,
                                                 @RequestParam(value = "type", required = false) String type,
                                                 @RequestParam(value = "status", required = false) Integer status) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("获取用户发布的信息列表失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        log.info("获取用户发布的信息列表：userId={}, pageNum={}, pageSize={}, title={}, location={}, type={}, status={}", 
                userId, pageNum, pageSize, title, location, type, status);
        return itemService.getUserItemList(userId, pageNum, pageSize, title, location, type, status);
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
        
        // 获取用户角色
        String role = (String) request.getAttribute("role");
        
        log.info("更新信息状态：userId={}, role={}, itemId={}, status={}", userId, role, itemId, status);
        return itemService.updateItemStatus(itemId, status, userId, role);
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
    
    /**
     * 获取各类型信息统计数据
     *
     * @return 结果
     */
    @GetMapping("/stats/type")
    public Result<List<Map<String, Object>>> getItemTypeStats() {
        return itemService.getItemTypeStats();
    }
    
    /**
     * 获取各状态信息统计数据
     *
     * @return 结果
     */
    @GetMapping("/stats/status")
    public Result<List<Map<String, Object>>> getItemStatusStats() {
        return itemService.getItemStatusStats();
    }
    
    /**
     * 获取最近7天每天的信息发布数量
     *
     * @return 结果
     */
    @GetMapping("/stats/daily")
    public Result<List<Map<String, Object>>> getItemDailyStats() {
        return itemService.getItemDailyStats();
    }
    
    /**
     * 获取用户发布的各类型信息统计数据
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/user/stats/type")
    public Result<List<Map<String, Object>>> getUserItemTypeStats(HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("获取用户信息类型统计数据失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        return itemService.getUserItemTypeStats(userId);
    }
    
    /**
     * 获取用户发布的每日统计数据
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/user/stats/daily")
    public Result<List<Map<String, Object>>> getUserItemDailyStats(HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("获取用户每日统计数据失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        return itemService.getUserItemDailyStats(userId);
    }
    
    /**
     * 获取用户发布的各状态信息统计数据
     *
     * @param request 请求
     * @return 结果
     */
    @GetMapping("/user/stats/status")
    public Result<List<Map<String, Object>>> getUserItemStatusStats(HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("获取用户信息状态统计数据失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        return itemService.getUserItemStatusStats(userId);
    }
    
    /**
     * 删除信息
     *
     * @param itemId  信息ID
     * @param request 请求
     * @return 结果
     */
    @DeleteMapping("/delete/{itemId}")
    public Result<String> deleteItem(@PathVariable("itemId") Long itemId, HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("删除信息失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        
        // 获取用户角色
        String role = (String) request.getAttribute("role");
        
        log.info("删除信息：userId={}, role={}, itemId={}", userId, role, itemId);
        return itemService.deleteItem(itemId, userId, role);
    }
    
    /**
     * 更新信息
     *
     * @param itemDTO 信息
     * @param request 请求
     * @return 结果
     */
    @PutMapping("/update")
    public Result<String> updateItem(@RequestBody @Valid ItemDTO itemDTO, HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("更新信息失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        
        // 获取用户角色
        String role = (String) request.getAttribute("role");
        
        log.info("更新信息：userId={}, role={}, title={}", userId, role, itemDTO.getTitle());
        return itemService.updateItem(itemDTO, userId, role);
    }
    
    /**
     * 标记信息为已完成
     *
     * @param itemId  信息ID
     * @param request 请求
     * @return 结果
     */
    @PutMapping("/complete/{itemId}")
    public Result<String> completeItem(@PathVariable("itemId") Long itemId, HttpServletRequest request) {
        // 获取userId并进行空值检查
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            log.error("标记完成失败：userId为空");
            return Result.error("用户未登录或登录已过期");
        }
        Long userId = Long.valueOf(userIdObj.toString());
        
        // 获取用户角色
        String role = (String) request.getAttribute("role");
        
        log.info("标记完成：userId={}, role={}, itemId={}", userId, role, itemId);
        return itemService.completeItem(itemId, userId, role);
    }
}