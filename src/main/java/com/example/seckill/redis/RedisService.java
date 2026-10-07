package com.example.seckill.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作封装。
 *
 * @author example
 */
@Service
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /* ---------------- 普通对象操作 ---------------- */

    public <T> boolean set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
        return true;
    }

    public <T> boolean set(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /* ---------------- String / 计数操作 ---------------- */

    public void stringSet(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String stringGet(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 以字符串形式写入长整型标记（如秒杀结果码），并可设置过期时间。
     */
    public void setLong(String key, Long value) {
        stringRedisTemplate.opsForValue().set(key, String.valueOf(value));
    }

    /**
     * 读取由 {@link #setLong} 写入的长整型标记；不存在返回 null。
     */
    public Long getLong(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Long.valueOf(value);
    }

    /**
     * 计数器自增并设置过期时间（首次访问时设置窗口）。
     */
    public Long increment(String key, long timeoutSeconds) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS);
        }
        return count;
    }

    /* ---------------- Set 操作 ---------------- */

    /**
     * 向集合中移除成员（超时关单回补一人一单标记）。
     */
    public Long removeSetMember(String key, String member) {
        return stringRedisTemplate.opsForSet().remove(key, member);
    }

    /* ---------------- Lua 脚本执行 ---------------- */

    /**
     * 执行 Lua 脚本。
     *
     * @param script lua 脚本
     * @param keys   key 列表
     * @param args   参数列表
     * @return 脚本返回值
     */
    public Long executeLua(DefaultRedisScript<Long> script, java.util.List<String> keys, String... args) {
        return stringRedisTemplate.execute(script, keys, (Object[]) args);
    }

    public Long executeLua(DefaultRedisScript<Long> script, String key, String... args) {
        return executeLua(script, Collections.singletonList(key), args);
    }
}
