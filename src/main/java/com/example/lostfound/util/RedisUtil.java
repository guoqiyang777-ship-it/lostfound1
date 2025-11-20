package com.example.lostfound.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取RedisTemplate实例（供其他服务使用）
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间（秒）
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -1;
    }

    // ==================== Set 操作 ====================

    /**
     * 添加元素到Set
     *
     * @param key   键
     * @param values 值
     * @return 添加成功的数量
     */
    public Long addToSet(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取Set中的所有元素
     *
     * @param key 键
     * @return Set集合
     */
    public java.util.Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 从Set中移除元素
     *
     * @param key    键
     * @param values 值
     * @return 移除成功的数量
     */
    public Long removeFromSet(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 判断元素是否在Set中
     *
     * @param key   键
     * @param value 值
     * @return 是否存在
     */
    public Boolean isMemberOfSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取Set的大小
     *
     * @param key 键
     * @return 大小
     */
    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ==================== Sorted Set 操作 ====================

    /**
     * 添加元素到Sorted Set
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return 是否添加成功
     */
    public Boolean addToSortedSet(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 从Sorted Set中移除元素
     *
     * @param key    键
     * @param values 值
     * @return 移除成功的数量
     */
    public Long removeFromSortedSet(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取Sorted Set指定范围的元素（按分数从小到大）
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public java.util.Set<Object> getSortedSetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取Sorted Set指定范围的元素（按分数从大到小）
     *
     * @param key   键
     * @param start 起始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public java.util.Set<Object> getSortedSetReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取Sorted Set的大小
     *
     * @param key 键
     * @return 大小
     */
    public Long getSortedSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取元素在Sorted Set中的分数
     *
     * @param key   键
     * @param value 值
     * @return 分数
     */
    public Double getSortedSetScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // ==================== Hash 操作 ====================

    /**
     * 设置Hash中的字段值
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取Hash中的字段值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    public Object getHashField(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 批量设置Hash字段
     *
     * @param key 键
     * @param map 字段-值映射
     */
    public void setHashFields(String key, java.util.Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取Hash的所有字段和值
     *
     * @param key 键
     * @return 字段-值映射
     */
    public java.util.Map<Object, Object> getHashAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Hash中的字段
     *
     * @param key    键
     * @param fields 字段
     * @return 删除成功的数量
     */
    public Long deleteHashFields(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断Hash中是否存在字段
     *
     * @param key   键
     * @param field 字段
     * @return 是否存在
     */
    public Boolean hasHashField(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量删除键（使用scan而非keys命令）
     *
     * @param pattern 模式
     * @return 删除的数量
     */
    public Long deleteByPattern(String pattern) {
        java.util.Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.delete(keys);
        }
        return 0L;
    }

    /**
     * 批量获取
     *
     * @param keys 键列表
     * @return 值列表
     */
    public java.util.List<Object> multiGet(java.util.Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量设置
     *
     * @param map 键-值映射
     */
    public void multiSet(java.util.Map<String, Object> map) {
        redisTemplate.opsForValue().multiSet(map);
    }
}