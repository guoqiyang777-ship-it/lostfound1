package com.example.lostfound.service.impl;

import com.example.lostfound.mapper.ChatMapper;
import com.example.lostfound.mapper.ItemMapper;
import com.example.lostfound.mapper.UserMapper;
import com.example.lostfound.pojo.Item;
import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.ItemDTO;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.ItemService;
import com.example.lostfound.util.OssUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 失物招领信息服务实现类
 */
@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private OssUtil ossUtil;

    @Override
    public Result<String> publishItem(ItemDTO itemDTO, Long userId) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 创建信息
        Item item = new Item();
        BeanUtils.copyProperties(itemDTO, item);

        // 设置时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        item.setItemTime(LocalDateTime.parse(itemDTO.getItemTime(), formatter));

        // 设置状态和用户ID
        item.setStatus(0); // 0待审核，1已通过，2已拒绝，3已解决
        item.setUserId(userId);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        // 插入信息
        itemMapper.insert(item);

        return Result.success("发布成功，等待审核");
    }

    @Override
    public Result<String> uploadItemImage(MultipartFile file) {
        try {
            // 上传图片
            String imageUrl = ossUtil.uploadItemImage(file);
            return Result.success(imageUrl);
        } catch (Exception e) {
            log.error("上传图片失败", e);
            return Result.error("上传图片失败");
        }
    }

    @Override
    public Result<PageInfo<Item>> getItemList(int pageNum, int pageSize, Integer status, String type) {
        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Item> itemList = itemMapper.selectList(status, type);
        PageInfo<Item> pageInfo = new PageInfo<>(itemList);

        return Result.success(pageInfo);
    }

    @Override
    public Result<Item> getItemDetail(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            return Result.error("信息不存在");
        }

        return Result.success(item);
    }

    @Override
    public Result<PageInfo<Item>> getUserItemList(Long userId, int pageNum, int pageSize) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Item> itemList = itemMapper.selectByUserId(userId);
        PageInfo<Item> pageInfo = new PageInfo<>(itemList);

        return Result.success(pageInfo);
    }

    @Override
    public Result<String> updateItemStatus(Long itemId, Integer status, Long userId) {
        // 查询信息
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            return Result.error("信息不存在");
        }
        
        // 验证是否为信息发布者
        if (!item.getUserId().equals(userId)) {
            log.error("更新信息状态失败：用户{}不是信息{}的发布者", userId, itemId);
            return Result.error("您不是该信息的发布者，无权操作");
        }

        // 更新状态
        itemMapper.updateStatus(itemId, status);

        return Result.success("操作成功");
    }

    @Override
    public Result<PageInfo<Item>> getPendingItemList(int pageNum, int pageSize) {
        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Item> itemList = itemMapper.selectPendingList();
        PageInfo<Item> pageInfo = new PageInfo<>(itemList);

        return Result.success(pageInfo);
    }

    @Override
    public Result<Map<String, Object>> getUserDashboard(Long userId) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 统计数据
        int totalCount = itemMapper.countByUserId(userId);
        int pendingCount = itemMapper.countPendingByUserId(userId);
        int resolvedCount = itemMapper.countResolvedByUserId(userId);
        int unreadCount = chatMapper.countUnreadMessage(userId);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("pendingCount", pendingCount);
        result.put("resolvedCount", resolvedCount);
        result.put("unreadCount", unreadCount);

        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getAdminDashboard() {
        // 统计数据
        List<User> userList = userMapper.selectList();
        int totalUserCount = userList.size();
        int bannedUserCount = (int) userList.stream().filter(user -> user.getStatus() == 1).count();
        int pendingItemCount = itemMapper.countPending();
        int todayNewItemCount = itemMapper.countTodayNew();

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalUserCount", totalUserCount);
        result.put("bannedUserCount", bannedUserCount);
        result.put("pendingItemCount", pendingItemCount);
        result.put("todayNewItemCount", todayNewItemCount);

        return Result.success(result);
    }
}