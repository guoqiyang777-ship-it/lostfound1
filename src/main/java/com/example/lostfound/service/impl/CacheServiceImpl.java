package com.example.lostfound.service.impl;

import com.example.lostfound.pojo.vo.ItemVO;
import com.example.lostfound.pojo.vo.PageResult;
import com.example.lostfound.service.CacheService;
import com.example.lostfound.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;

/**
 * 缓存服务实现类（优化版）
 * 
 * 优化点：
 * 1. 分层缓存架构：用户端和管理端分离
 * 2. Tag机制：精准缓存失效
 * 3. 过期时间：防止缓存堆积和雪崩
 * 4. 状态索引：快速过滤不同状态
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisUtil redisUtil;

    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    
    // 缓存键前缀
    private static final String USER_ITEM_LIST_PREFIX = "user:item:list:";      // 用户端列表缓存
    private static final String ADMIN_ITEM_LIST_PREFIX = "admin:item:list:";    // 管理端列表缓存
    private static final String USER_ITEM_DETAIL_PREFIX = "user:item:detail:";  // 用户端详情缓存
    private static final String ITEM_STATUS_INDEX_PREFIX = "item:status:";      // 状态索引
    private static final String ITEM_TAG_PREFIX = "item:tag:";                  // 缓存标签
    
    // 缓存过期时间（秒）
    private static final long LIST_CACHE_EXPIRE = 300;      // 5分钟
    private static final long DETAIL_CACHE_EXPIRE = 600;    // 10分钟
    private static final long STATUS_INDEX_EXPIRE = 3600;   // 1小时
    private static final int EXPIRE_RANDOM_OFFSET = 60;     // 随机偏移量，防止缓存雪崩
    
    // 构造函数中配置ObjectMapper
    public CacheServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void cacheItemList(int pageNum, int pageSize, Integer status, String type, 
                             String title, String location, PageResult<ItemVO> pageResult) {
        String cacheKey = "";
        try {
            // 构建用户端缓存键
            cacheKey = buildUserListCacheKey(pageNum, pageSize, status, type, title, location);
            
            // ✅ 强制控制台输出（确保能看到）
            System.out.println("\n========================================");
            System.out.println("💾 [写入缓存] 开始");
            System.out.println("缓存键: " + cacheKey);
            System.out.println("数据总数: " + pageResult.getTotal());
            System.out.println("========================================\n");
            
            // ✅ 手动序列化为JSON字符串（因为RedisTemplate现在使用StringSerializer）
            String jsonValue = objectMapper.writeValueAsString(pageResult);
            
            // 设置缓存，带随机过期时间防止雪崩
            long expireTime = LIST_CACHE_EXPIRE + random.nextInt(EXPIRE_RANDOM_OFFSET);
            redisUtil.set(cacheKey, jsonValue, expireTime);
            
            System.out.println("✅ Redis写入成功，过期时间: " + expireTime + "秒\n");
            
            // 为列表中的每个itemId添加Tag关联
            if (pageResult.getList() != null) {
                for (ItemVO item : pageResult.getList()) {
                    addCacheTag(item.getId(), cacheKey);
                }
            }
            
            log.info("💾 [写入缓存] 用户端列表: total={}, expire={}s, key={}", pageResult.getTotal(), expireTime, cacheKey.substring(0, Math.min(50, cacheKey.length())) + "...");
        } catch (Exception e) {
            System.err.println("❌ 缓存写入异常: " + e.getMessage());
            e.printStackTrace();
            log.error("🔴 [错误] 缓存信息列表失败", e);
        }
    }

    @Override
    public PageResult<ItemVO> getCachedItemList(int pageNum, int pageSize, Integer status, 
                                               String type, String title, String location) {
        String cacheKey = "";
        try {
            cacheKey = buildUserListCacheKey(pageNum, pageSize, status, type, title, location);
            
            // ✅ 强制控制台输出
            System.out.println("\n========================================");
            System.out.println("🔍 [读取缓存] 开始");
            System.out.println("缓存键: " + cacheKey);
            
            Object cachedValue = redisUtil.get(cacheKey);
            
            if (cachedValue == null) {
                System.out.println("❌ 缓存值: null (未命中)");
                System.out.println("========================================\n");
                log.info("❌ [缓存未命中] key={}", cacheKey.substring(0, Math.min(50, cacheKey.length())) + "...");
                return null;
            }
            
            System.out.println("✅ 缓存值: 存在 (命中!)");
            System.out.println("对象类型: " + cachedValue.getClass().getName());
            System.out.println("========================================\n");
            
            // ✅ 从JSON字符串反序列化
            String jsonValue = cachedValue.toString();
            PageResult<ItemVO> result = objectMapper.readValue(jsonValue, 
                new TypeReference<PageResult<ItemVO>>() {});
            log.info("📚 [读取缓存] 用户端列表: total={}, key={}", result.getTotal(), cacheKey.substring(0, Math.min(50, cacheKey.length())) + "...");
            return result;
        } catch (Exception e) {
            System.err.println("❌ 缓存读取异常: " + e.getMessage());
            e.printStackTrace();
            log.error("🔴 [错误] 缓存操作异常", e);
        }
        return null;
    }

    @Override
    public void cacheItemDetail(Long itemId, ItemVO itemVO) {
        try {
            String userDetailKey = USER_ITEM_DETAIL_PREFIX + itemId;
            long expireTime = DETAIL_CACHE_EXPIRE + random.nextInt(EXPIRE_RANDOM_OFFSET);
            
            // 序列化为JSON字符串
            String jsonValue = objectMapper.writeValueAsString(itemVO);
            redisUtil.set(userDetailKey, jsonValue, expireTime);
            
            // 添加Tag关联
            addCacheTag(itemId, userDetailKey);
            
            log.info("💾 [写入缓存] 详情: itemId={}, expire={}s", itemId, expireTime);
        } catch (Exception e) {
            log.error("缓存信息详情失败", e);
        }
    }

    @Override
    public ItemVO getCachedItemDetail(Long itemId) {
        try {
            String cacheKey = USER_ITEM_DETAIL_PREFIX + itemId;
            Object cachedValue = redisUtil.get(cacheKey);
            
            if (cachedValue != null) {
                String jsonValue = cachedValue.toString();
                ItemVO result = objectMapper.readValue(jsonValue, ItemVO.class);
                log.info("📚 [读取缓存] 详情: itemId={}", itemId);
                return result;
            }
        } catch (Exception e) {
            log.error("从缓存获取信息详情失败", e);
        }
        return null;
    }

    @Override
    public void clearItemCache(Long itemId) {
        if (itemId == null) {
            return;
        }
        
        // 使用Tag机制精准清除所有相关缓存
        String tagKey = ITEM_TAG_PREFIX + itemId;
        Set<Object> cacheKeys = redisUtil.getSetMembers(tagKey);
        
        if (cacheKeys != null && !cacheKeys.isEmpty()) {
            for (Object key : cacheKeys) {
                redisUtil.delete(key.toString());
            }
            redisUtil.delete(tagKey);
            log.info("🗑️ [清除缓存] 精准清除: itemId={}, 清除{}个键", itemId, cacheKeys.size());
        }
    }

    @Override
    public void clearItemListCache() {
        try {
            // 清除用户端列表缓存
            long userCount = redisUtil.deleteByPattern(USER_ITEM_LIST_PREFIX + "*");
            // 清除管理端列表缓存
            long adminCount = redisUtil.deleteByPattern(ADMIN_ITEM_LIST_PREFIX + "*");
            
            log.info("🗑️ [清除缓存] 列表缓存: 用户端{}个, 管理端{}个", userCount, adminCount);
        } catch (Exception e) {
            log.error("清除信息列表缓存失败", e);
        }
    }

    /**
     * 添加状态索引
     */
    public void addToStatusIndex(Long itemId, Integer status, LocalDateTime createTime) {
        try {
            String indexKey = ITEM_STATUS_INDEX_PREFIX + status;
            double score = createTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            redisUtil.addToSortedSet(indexKey, itemId.toString(), score);
            redisUtil.expire(indexKey, STATUS_INDEX_EXPIRE);
            log.info("📈 [状态索引] 添加: status={}, itemId={}", status, itemId);
        } catch (Exception e) {
            log.error("添加状态索引失败", e);
        }
    }

    /**
     * 从状态索引中移除
     */
    public void removeFromStatusIndex(Long itemId, Integer status) {
        try {
            String indexKey = ITEM_STATUS_INDEX_PREFIX + status;
            redisUtil.removeFromSortedSet(indexKey, itemId.toString());
            log.info("📉 [状态索引] 移除: status={}, itemId={}", status, itemId);
        } catch (Exception e) {
            log.error("从状态索引移除失败", e);
        }
    }

    /**
     * 更新状态索引（状态变更时调用）
     */
    public void updateStatusIndex(Long itemId, Integer oldStatus, Integer newStatus, LocalDateTime createTime) {
        if (oldStatus != null) {
            removeFromStatusIndex(itemId, oldStatus);
        }
        if (newStatus != null) {
            addToStatusIndex(itemId, newStatus, createTime);
        }
    }

    /**
     * 添加缓存标签（记录itemId与缓存键的关联）
     */
    private void addCacheTag(Long itemId, String cacheKey) {
        try {
            String tagKey = ITEM_TAG_PREFIX + itemId;
            redisUtil.addToSet(tagKey, cacheKey);
            redisUtil.expire(tagKey, DETAIL_CACHE_EXPIRE + 300);
        } catch (Exception e) {
            log.error("添加缓存标签失败: itemId={}, cacheKey={}", itemId, cacheKey, e);
        }
    }

    /**
     * 缓存管理端信息列表
     */
    public void cacheAdminItemList(int pageNum, int pageSize, Integer status, String type, 
                                   String title, String location, PageResult<ItemVO> pageResult) {
        try {
            String cacheKey = buildAdminListCacheKey(pageNum, pageSize, status, type, title, location);
            
            long expireTime = LIST_CACHE_EXPIRE + random.nextInt(EXPIRE_RANDOM_OFFSET);
            // 序列化为JSON字符串
            String jsonValue = objectMapper.writeValueAsString(pageResult);
            redisUtil.set(cacheKey, jsonValue, expireTime);
            
            if (pageResult.getList() != null) {
                for (ItemVO item : pageResult.getList()) {
                    addCacheTag(item.getId(), cacheKey);
                }
            }
            
            log.info("💾 [写入缓存] 管理端列表: total={}, expire={}s", pageResult.getTotal(), expireTime);
        } catch (Exception e) {
            log.error("缓存管理端信息列表失败", e);
        }
    }

    /**
     * 获取缓存的管理端信息列表
     */
    public PageResult<ItemVO> getCachedAdminItemList(int pageNum, int pageSize, Integer status, 
                                                     String type, String title, String location) {
        try {
            String cacheKey = buildAdminListCacheKey(pageNum, pageSize, status, type, title, location);
            Object cachedValue = redisUtil.get(cacheKey);
            
            if (cachedValue != null) {
                String jsonValue = cachedValue.toString();
                PageResult<ItemVO> result = objectMapper.readValue(jsonValue, 
                    new TypeReference<PageResult<ItemVO>>() {});
                log.info("📚 [读取缓存] 管理端列表: total={}", result.getTotal());
                return result;
            }
        } catch (Exception e) {
            log.error("从管理端缓存获取信息列表失败", e);
        }
        return null;
    }

    /**
     * 构建用户端列表缓存键
     */
    private String buildUserListCacheKey(int pageNum, int pageSize, Integer status, 
                                        String type, String title, String location) {
        String params = String.format("%d_%d_%s_%s_%s_%s", 
            pageNum, pageSize, 
            status == null ? "null" : status,
            type == null || type.isEmpty() ? "null" : type,
            title == null || title.isEmpty() ? "null" : title,
            location == null || location.isEmpty() ? "null" : location);
        
        String hash = generateMD5Hash(params);
        return USER_ITEM_LIST_PREFIX + hash;
    }

    /**
     * 构建管理端列表缓存键
     */
    private String buildAdminListCacheKey(int pageNum, int pageSize, Integer status, 
                                         String type, String title, String location) {
        String params = String.format("%d_%d_%s_%s_%s_%s", 
            pageNum, pageSize, 
            status == null ? "null" : status,
            type == null || type.isEmpty() ? "null" : type,
            title == null || title.isEmpty() ? "null" : title,
            location == null || location.isEmpty() ? "null" : location);
        
        String hash = generateMD5Hash(params);
        return ADMIN_ITEM_LIST_PREFIX + hash;
    }
    
    /**
     * 生成MD5哈希值
     */
    private String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成MD5哈希失败", e);
            // 如果MD5不可用，使用原始参数的哈希码
            return String.valueOf(input.hashCode());
        }
    }
}