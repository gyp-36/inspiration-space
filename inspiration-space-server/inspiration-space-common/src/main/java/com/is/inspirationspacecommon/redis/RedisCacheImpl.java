package com.is.inspirationspacecommon.redis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class RedisCacheImpl implements RedisCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // ================= 基础操作 =================
    @Override
    public Boolean hasKey(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.hasKey(key.getRelKey());
    }

    @Override
    public void del(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        redisTemplate.delete(key.getRelKey());
    }

    @Override
    public void del(Collection<RedisKeyBuild> keys) {
        CacheUtil.checkNoNullElements(keys);
        List<String> keyList = CacheUtil.extractKeys(keys);
        if (!keyList.isEmpty()) {
            redisTemplate.delete(keyList);
        }
    }

    @Override
    public Boolean expire(RedisKeyBuild key, long ttl, TimeUnit unit) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.expire(key.getRelKey(), ttl, unit);
    }

    @Override
    public Long getExpire(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.getExpire(key.getRelKey());
    }

    @Override
    public Long getTokenExpire(String token) {
        return redisTemplate.getExpire(token);
    }

    @Override
    public DataType type(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.type(key.getRelKey());
    }

    // ================= String操作 =================
    @Override
    public <T> T get(RedisKeyBuild key, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Object value = redisTemplate.opsForValue().get(key.getRelKey());
        return type.cast(value);
    }

    @Override
    public <T> T get(RedisKeyBuild key, Class<T> type, Supplier<T> supplier, long ttl, TimeUnit unit) {
        CacheUtil.checkNotBlank(key);
        T value = get(key, type);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                set(key, value, ttl, unit);
            }
        }
        return value;
    }

    @Override
    public void set(RedisKeyBuild key, Object value) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        redisTemplate.opsForValue().set(key.getRelKey(), value);
    }

    @Override
    public boolean set(RedisKeyBuild key, Object value, long ttl, TimeUnit unit) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        redisTemplate.opsForValue().set(key.getRelKey(), value, ttl, unit);
        return false;
    }

    @Override
    public void updateExpire(RedisKeyBuild key, long ttl, TimeUnit unit) {
        //更新缓存过期时间
        CacheUtil.checkNotBlank(key);
        redisTemplate.opsForValue().getOperations().expire(key.getRelKey(), ttl, unit);
    }

    @Override
    public boolean setIfAbsent(RedisKeyBuild key, Object value) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key.getRelKey(), value));
    }

    @Override
    public Long incrBy(RedisKeyBuild key, long delta) {
        CacheUtil.checkNotBlank(key);
        return stringRedisTemplate.opsForValue().increment(key.getRelKey(), delta);
    }

    @Override
    public Double incrByDouble(RedisKeyBuild key, double delta) {
        CacheUtil.checkNotBlank(key);
        return stringRedisTemplate.opsForValue().increment(key.getRelKey(), delta);
    }

    // ================= Hash操作 =================

    @Override
    public Long incrHash(RedisKeyBuild key, String s, int i) {
        return redisTemplate.opsForHash().increment(key.getRelKey(), s, i);
    }

    @Override
    public <T> T getHash(RedisKeyBuild key, String field, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotBlank(field);
        Object value = redisTemplate.<String, Object>opsForHash().get(key.getRelKey(), field);
        return value != null ? type.cast(value) : null;
    }

    @Override
    public void putHash(RedisKeyBuild key, String field, Object value) {
        CacheUtil.checkNotBlank(key.getRelKey(), field);
        CacheUtil.checkNotNull(value);
        redisTemplate.opsForHash().put(key.getRelKey(), field, value);
    }

    @Override
    public void putHash(RedisKeyBuild key, String field, Object value, long ttl, TimeUnit unit) {
        putHash(key, field, value);
        expire(key, ttl, unit);
    }

    @Override
    public void putHashAll(RedisKeyBuild key, Map<String, ?> map) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNoNullElements(map.values());
        redisTemplate.opsForHash().putAll(key.getRelKey(), map);
    }

    @Override
    public void putHashAll(RedisKeyBuild key, Map<String, ?> map, long ttl, TimeUnit unit) {
        putHashAll(key, map);
        expire(key, ttl, unit);
    }

    @Override
    public Boolean putHashIfAbsent(RedisKeyBuild key, String field, Object value) {
        CacheUtil.checkNotBlank(key.getRelKey(), field);
        CacheUtil.checkNotNull(value);
        return redisTemplate.opsForHash().putIfAbsent(key.getRelKey(), field, value);
    }

    @Override
    public Boolean hasHashKey(RedisKeyBuild key, String field) {
        CacheUtil.checkNotBlank(key.getRelKey(), field);
        return redisTemplate.opsForHash().hasKey(key.getRelKey(), field);
    }

    @Override
    public Long delHash(RedisKeyBuild key, String... fields) {
        CacheUtil.checkNotBlank(key);
        if (fields == null || fields.length == 0) {
            return 0L;
        }
        return redisTemplate.opsForHash().delete(key.getRelKey(), (Object[]) fields);
    }

    @Override
    public <T> Map<String, T> getAllHash(RedisKeyBuild key, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key.getRelKey());
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toString(), type.cast(entry.getValue()));
            }
        }
        return result;
    }

    // ================= List操作 =================
    @Override
    public <T> T leftPopList(RedisKeyBuild key, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Object value = redisTemplate.opsForList().leftPop(key.getRelKey());
        return value != null ? type.cast(value) : null;
    }

    @Override
    public <T> T rightPopList(RedisKeyBuild key, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Object value = redisTemplate.opsForList().rightPop(key.getRelKey());
        return value != null ? type.cast(value) : null;
    }

    @Override
    public Long leftPushList(RedisKeyBuild key, Object value) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        return redisTemplate.opsForList().leftPush(key.getRelKey(), value);
    }

    @Override
    public Long rightPushList(RedisKeyBuild key, Object value) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        return redisTemplate.opsForList().rightPush(key.getRelKey(), value);
    }

    @Override
    public <T> List<T> rangeList(RedisKeyBuild key, long start, long end, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        List<Object> values = redisTemplate.opsForList().range(key.getRelKey(), start, end);
        return values != null ?
                values.stream().filter(Objects::nonNull).map(type::cast).collect(Collectors.toList()) :
                Collections.emptyList();
    }

    @Override
    public Long sizeList(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.opsForList().size(key.getRelKey());
    }


    // ================= Set操作 =================
    @Override
    public void createSet(RedisKeyBuild key) {
        //创建一个空集合
        CacheUtil.checkNotBlank(key);
    }

    @Override
    public Long addSet(RedisKeyBuild key, Object... values) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNoNullElements(Arrays.asList(values));
        return redisTemplate.opsForSet().add(key.getRelKey(), values);
    }

    @Override
    public Boolean isSetMember(RedisKeyBuild key, Object value) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        return redisTemplate.opsForSet().isMember(key.getRelKey(), value);
    }

    @Override
    public Long removeSet(RedisKeyBuild key, Object... values) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNoNullElements(Arrays.asList(values));
        return redisTemplate.opsForSet().remove(key.getRelKey(), values);
    }

    @Override
    public <T> Set<T> membersSet(RedisKeyBuild key, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Set<Object> members = redisTemplate.opsForSet().members(key.getRelKey());
        return members != null ?
                members.stream().filter(Objects::nonNull).map(type::cast).collect(Collectors.toSet()) :
                Collections.emptySet();
    }

    @Override
    public Long sizeSet(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.opsForSet().size(key.getRelKey());
    }

    // ================= ZSet操作 =================
    @Override
    public void addZSet(RedisKeyBuild key, Object value, double score) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        redisTemplate.opsForZSet().add(key.getRelKey(), value, score);
    }

    @Override
    public void addZSet(RedisKeyBuild key, Object value, double score, long ttl, TimeUnit unit) {
        addZSet(key, value, score);
        expire(key, ttl, unit);
    }

    @Override
    public void addZSetAll(RedisKeyBuild key, Map<Object, Double> map, long ttl, TimeUnit unit) {

            CacheUtil.checkNotBlank(key);
            Set<ZSetOperations.TypedTuple<Object>> tuples = map.entrySet().stream()
                    .map(entry -> ZSetOperations.TypedTuple.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toSet());
            redisTemplate.opsForZSet().add(key.getRelKey(), tuples);
            expire(key, ttl, unit);

    }

    @Override
    public <T> Set<T> rangeZSet(RedisKeyBuild key, long start, long end, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Set<Object> values = redisTemplate.opsForZSet().range(key.getRelKey(), start, end);
        return values != null ?
                values.stream().filter(Objects::nonNull).map(type::cast).collect(Collectors.toSet()) :
                Collections.emptySet();
    }

    @Override
    public <T> Set<T> rangeByScoreZSet(RedisKeyBuild key, double min, double max, Class<T> type) {
        CacheUtil.checkNotBlank(key);
        Set<Object> values = redisTemplate.opsForZSet().rangeByScore(key.getRelKey(), min, max);
        return values != null ?
                values.stream().filter(Objects::nonNull).map(type::cast).collect(Collectors.toSet()) :
                Collections.emptySet();
    }

    @Override
    public Long removeZSet(RedisKeyBuild key, Object... values) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNoNullElements(Arrays.asList(values));
        return redisTemplate.opsForZSet().remove(key.getRelKey(), values);
    }

    @Override
    public Double incrementScoreZSet(RedisKeyBuild key, Object value, double delta) {
        CacheUtil.checkNotBlank(key);
        CacheUtil.checkNotNull(value);
        return redisTemplate.opsForZSet().incrementScore(key.getRelKey(), value, delta);
    }

    @Override
    public Long sizeZSet(RedisKeyBuild key) {
        CacheUtil.checkNotBlank(key);
        return redisTemplate.opsForZSet().size(key.getRelKey());
    }



    // ================= 扫描操作 =================
    @Override
    public Cursor<String> scan(String pattern, ScanOptions options) {
        return redisTemplate.scan(ScanOptions.scanOptions().match(pattern).build());
    }

    @Override
    public String getCacheObject(String redisKey) {
        return stringRedisTemplate.opsForValue().get(redisKey);
    }

    // ================= 分布式锁操作 =================
    private static final ThreadLocal<Map<String, String>> LOCK_OWNERS =
            ThreadLocal.withInitial(HashMap::new);

    // 锁值生成前缀，用于识别锁的所有者
    private static final String LOCK_PREFIX = "lock:";


    @Override
    public boolean tryLock(String lockKey, int waitTime, int leastTime, TimeUnit timeUnit) {
        // 转换时间为毫秒
        long waitTimeMillis = timeUnit.toMillis(waitTime);
        long leaseTimeMillis = timeUnit.toMillis(leastTime);

        // 生成唯一锁标识 (线程ID + UUID)
        String lockValue = generateLockValue();

        // 记录开始时间用于超时控制
        long startTime = System.currentTimeMillis();
        int retryCount = 0;

        try {
            while (true) {
                // 尝试获取锁（原子操作）
                Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                        LOCK_PREFIX + lockKey,
                        lockValue,
                        leaseTimeMillis,
                        TimeUnit.MILLISECONDS
                );

                if (Boolean.TRUE.equals(locked)) {
                    // 成功获取锁，保存锁信息到ThreadLocal
                    LOCK_OWNERS.get().put(lockKey, lockValue);
                    return true;
                }

                // 检查是否已超过最大等待时间
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= waitTimeMillis) {
                    return false;
                }

                // 指数退避策略：等待时间随重试次数增加而增加，但不超过500ms
                long sleepTime = Math.min(50 * (1 << Math.min(retryCount, 4)), 500);
                Thread.sleep(sleepTime);
                retryCount++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        // 获取当前线程持有的锁值
        String lockValue = LOCK_OWNERS.get().get(lockKey);
        if (lockValue == null) {
            // 没有持有这个锁，可能是已经解锁或从未获取
            return;
        }

        // 使用Lua脚本确保原子性解锁操作
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";

        try {
            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(LOCK_PREFIX + lockKey),
                    lockValue
            );

            // 从ThreadLocal中移除锁信息
            LOCK_OWNERS.get().remove(lockKey);

            // 记录警告日志（可选）：如果result为0，表示锁已被其他客户端获取或已过期
            if (result != null && result == 0) {
                log.warn("尝试解锁一个不属于当前线程的锁，lockKey: {}, 可能已过期", lockKey);
            }
        } catch (Exception e) {
            // 解锁过程中发生异常，记录错误但不抛出（避免影响业务）
            log.error("解锁过程中发生异常，lockKey: {}", lockKey, e);
        }
    }

    /**
     * 生成唯一锁值标识
     * 格式: 线程ID:UUID
     */
    private String generateLockValue() {
        long threadId = Thread.currentThread().getId();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return threadId + ":" + uuid;
    }


}