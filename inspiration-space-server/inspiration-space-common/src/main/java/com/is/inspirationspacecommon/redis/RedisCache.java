package com.is.inspirationspacecommon.redis;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis缓存操作接口
 */

public interface RedisCache {

    // ================= 基础操作 =================

    /**
     *
     * @param lockKey
     */
    void unlock(String lockKey);


    boolean tryLock(String lockKey, int waitTime, int leastTime, TimeUnit timeUnit);
    /**
     * 判断键是否存在
     *
     * @param key 键构造器对象
     * @return true 存在，false 不存在
     */
    Boolean hasKey(RedisKeyBuild key);

    /**
     * 删除一个键
     *
     * @param key 键构造器对象
     */
    void del(RedisKeyBuild key);

    /**
     * 批量删除键
     *
     * @param keys 键构造器对象集合
     */
    void del(Collection<RedisKeyBuild> keys);

    /**
     * 设置键的过期时间
     *
     * @param key  键构造器对象
     * @param ttl  过期时间数值
     * @param unit 时间单位
     * @return true 设置成功，false 键不存在或设置失败
     */
    Boolean expire(RedisKeyBuild key, long ttl, TimeUnit unit);

    /**
     * 获取键的剩余过期时间
     *
     * @param key 键构造器对象
     * @return 剩余时间（单位：毫秒），-1 表示永久有效，-2 表示键不存在
     */
    Long getExpire(RedisKeyBuild key);

    /**
     * 获取token的剩余过期时间
     *
     * @param token  token
     * @return 剩余时间（单位：毫秒），-1 键失效
     */
    Long getTokenExpire(String token);

    /**
     * 获取键存储的数据类型
     *
     * @param key 键构造器对象
     * @return 数据类型枚举（STRING, LIST, SET, ZSET, HASH 等）
     */
    DataType type(RedisKeyBuild key);

    // ================= String操作 =================

    /**
     * 获取缓存值（缓存不存在时返回null）
     *
     * @param key  键构造器对象
     * @param type 返回值类型
     * @param <T>  泛型类型
     * @return 缓存值或null
     */
    <T> T get(RedisKeyBuild key, Class<T> type);

    /**
     * 获取或加载缓存（缓存不存在时通过supplier加载数据并写入缓存）
     *
     * @param key      键构造器对象
     * @param type     返回值类型
     * @param supplier 数据加载函数
     * @param ttl      写入缓存时的过期时间
     * @param unit     时间单位
     * @param <T>      泛型类型
     * @return 缓存值或supplier返回的数据
     */
    <T> T get(RedisKeyBuild key, Class<T> type, Supplier<T> supplier, long ttl, TimeUnit unit);

    /**
     * 设置缓存（永不过期）
     *
     * @param key   键构造器对象
     * @param value 缓存值
     */
    void set(RedisKeyBuild key, Object value);

    /**
     * 设置带过期时间的缓存
     *
     * @param key   键构造器对象
     * @param value 缓存值
     * @param ttl   过期时间数值
     * @param unit  时间单位
     * @return
     */
    boolean set(RedisKeyBuild key, Object value, long ttl, TimeUnit unit);

    /**
     * 当键存在时，更新缓存时间
     *
     */
    void updateExpire(RedisKeyBuild key, long ttl, TimeUnit unit);

    /**
     * 仅当键不存在时设置缓存
     *
     * @param key   键构造器对象
     * @param value 缓存值
     * @return true 设置成功，false 键已存在
     */
    boolean setIfAbsent(RedisKeyBuild key, Object value);

    /**
     * 对整数值进行原子增加
     *
     * @param key   键构造器对象
     * @param delta 增量（可为负）
     * @return 增加后的值
     */
    Long incrBy(RedisKeyBuild key, long delta);

    /**
     * 对浮点数值进行原子增加
     *
     * @param key   键构造器对象
     * @param delta 增量（可为负）
     * @return 增加后的值
     */
    Double incrByDouble(RedisKeyBuild key, double delta);

    // ================= Hash操作 =================

    /**
     * Hash结构中的字段值原子增加
     * @param key
     * @param s
     * @param i
     * @return
     */
    Long incrHash(RedisKeyBuild key, String s, int i);

    /**
     * 获取Hash结构中指定字段的值
     *
     * @param key   键构造器对象
     * @param field 字段名
     * @param type  返回值类型
     * @param <T>   泛型类型
     * @return 字段值或null
     */
    <T> T getHash(RedisKeyBuild key, String field, Class<T> type);

    /**
     * 设置Hash结构中的字段值
     *
     * @param key   键构造器对象
     * @param field 字段名
     * @param value 字段值
     */
    void putHash(RedisKeyBuild key, String field, Object value);

    /**
     * 设置Hash字段值并指定整个键的过期时间
     *
     * @param key   键构造器对象
     * @param field 字段名
     * @param value 字段值
     * @param ttl   过期时间数值
     * @param unit  时间单位
     */
    void putHash(RedisKeyBuild key, String field, Object value, long ttl, TimeUnit unit);

    /**
     * 批量设置Hash结构的字段值
     *
     * @param key 键构造器对象
     * @param map 字段-值映射表
     */
    void putHashAll(RedisKeyBuild key, Map<String, ?> map);

    /**
     * 批量设置Hash字段值并指定整个键的过期时间
     *
     * @param key  键构造器对象
     * @param map  字段-值映射表
     * @param ttl  过期时间数值
     * @param unit 时间单位
     */
    void putHashAll(RedisKeyBuild key, Map<String, ?> map, long ttl, TimeUnit unit);

    /**
     * 仅当字段不存在时设置Hash字段值
     *
     * @param key   键构造器对象
     * @param field 字段名
     * @param value 字段值
     * @return true 设置成功，false 字段已存在
     */
    Boolean putHashIfAbsent(RedisKeyBuild key, String field, Object value);

    /**
     * 判断Hash结构中是否存在指定字段
     *
     * @param key   键构造器对象
     * @param field 字段名
     * @return true 存在，false 不存在
     */
    Boolean hasHashKey(RedisKeyBuild key, String field);

    /**
     * 删除Hash结构中的指定字段
     *
     * @param key    键构造器对象
     * @param fields 要删除的字段名（可变参数）
     * @return 成功删除的字段数量
     */
    Long delHash(RedisKeyBuild key, String... fields);

    /**
     * 获取整个Hash结构的所有字段和值
     *
     * @param key  键构造器对象
     * @param type 值类型
     * @param <T>  泛型类型
     * @return 字段-值映射表（可能为空）
     */
    <T> Map<String, T> getAllHash(RedisKeyBuild key, Class<T> type);

    // ================= List操作 =================

    /**
     * 从列表左侧弹出元素
     *
     * @param key  键构造器对象
     * @param type 返回值类型
     * @param <T>  泛型类型
     * @return 弹出的元素或null（列表为空时）
     */
    <T> T leftPopList(RedisKeyBuild key, Class<T> type);

    /**
     * 从列表右侧弹出元素
     *
     * @param key  键构造器对象
     * @param type 返回值类型
     * @param <T>  泛型类型
     * @return 弹出的元素或null（列表为空时）
     */
    <T> T rightPopList(RedisKeyBuild key, Class<T> type);

    /**
     * 向列表左侧插入元素
     *
     * @param key   键构造器对象
     * @param value 插入值
     * @return 插入后列表的长度
     */
    Long leftPushList(RedisKeyBuild key, Object value);

    /**
     * 向列表右侧插入元素
     *
     * @param key   键构造器对象
     * @param value 插入值
     * @return 插入后列表的长度
     */
    Long rightPushList(RedisKeyBuild key, Object value);

    /**
     * 获取列表指定范围内的元素
     *
     * @param key   键构造器对象
     * @param start 起始索引（0表示第一个）
     * @param end   结束索引（-1表示最后一个）
     * @param type  元素类型
     * @param <T>   泛型类型
     * @return 元素列表（可能为空）
     */
    <T> List<T> rangeList(RedisKeyBuild key, long start, long end, Class<T> type);

    /**
     * 获取列表长度
     *
     * @param key 键构造器对象
     * @return 列表长度（键不存在时返回0）
     */
    Long sizeList(RedisKeyBuild key);

    // ================= Set操作 =================

    /**
     * 创建集合
     */
    void createSet(RedisKeyBuild key);

    /**
     * 向集合添加元素
     *
     * @param key    键构造器对象
     * @param values 要添加的元素（可变参数）
     * @return 成功添加的元素数量（忽略重复元素）
     */
    Long addSet(RedisKeyBuild key, Object... values);



    /**
     * 判断元素是否在集合中
     *
     * @param key   键构造器对象
     * @param value 查询值
     * @return true 存在，false 不存在
     */
    Boolean isSetMember(RedisKeyBuild key, Object value);

    /**
     * 从集合中移除元素
     *
     * @param key    键构造器对象
     * @param values 要移除的元素（可变参数）
     * @return 成功移除的元素数量
     */
    Long removeSet(RedisKeyBuild key, Object... values);

    /**
     * 获取集合所有成员
     *
     * @param key  键构造器对象
     * @param type 元素类型
     * @param <T>  泛型类型
     * @return 成员集合（可能为空）
     */
    <T> Set<T> membersSet(RedisKeyBuild key, Class<T> type);

    /**
     * 获取集合大小
     *
     * @param key 键构造器对象
     * @return 集合元素数量（键不存在时返回0）
     */
    Long sizeSet(RedisKeyBuild key);

    // ================= ZSet操作 =================

    /**
     * 向有序集合添加成员
     *
     * @param key   键构造器对象
     * @param value 成员值
     * @param score 分数（用于排序）
     */
    void addZSet(RedisKeyBuild key, Object value, double score);

    /**
     * 向有序集合添加成员并设置键的过期时间
     *
     * @param key   键构造器对象
     * @param value 成员值
     * @param score 分数
     * @param ttl   过期时间数值
     * @param unit  时间单位
     */
    void addZSet(RedisKeyBuild key, Object value, double score, long ttl, TimeUnit unit);

    /**
     * 向有序集合批量添加成员并设置键的过期时间
     * @param key   键构造器对象
     * @param map   成员-分数映射表
     * @param ttl   过期时间数值
     * @param unit  时间单位
     */
    void addZSetAll(RedisKeyBuild key, Map<Object, Double> map, long ttl, TimeUnit unit);

    /**
     * 按索引范围获取有序集合成员（按分数升序）
     *
     * @param key   键构造器对象
     * @param start 起始索引（0表示第一个）
     * @param end   结束索引（-1表示最后一个）
     * @param type  成员类型
     * @param <T>   泛型类型
     * @return 成员集合（可能为空）
     */
    <T> Set<T> rangeZSet(RedisKeyBuild key, long start, long end, Class<T> type);



    /**
     * 按分数范围获取有序集合成员
     *
     * @param key  键构造器对象
     * @param min  最小分数（包含）
     * @param max  最大分数（包含）
     * @param type 成员类型
     * @param <T>  泛型类型
     * @return 成员集合（可能为空）
     */
    <T> Set<T> rangeByScoreZSet(RedisKeyBuild key, double min, double max, Class<T> type);

    /**
     * 从有序集合中移除成员
     *
     * @param key    键构造器对象
     * @param values 要移除的成员（可变参数）
     * @return 成功移除的成员数量
     */
    Long removeZSet(RedisKeyBuild key, Object... values);

    /**
     * 增加有序集合成员的分数
     *
     * @param key   键构造器对象
     * @param value 成员值
     * @param delta 增量
     * @return 增加后的新分数
     */
    Double incrementScoreZSet(RedisKeyBuild key, Object value, double delta);

    /**
     * 获取有序集合大小
     *
     * @param key 键构造器对象
     * @return 成员数量（键不存在时返回0）
     */
    Long sizeZSet(RedisKeyBuild key);

    // ================= 扫描操作 =================

    /**
     * 使用模式匹配扫描键
     *
     * @param pattern 键的模式（如 "user:*"）
     * @param options 扫描选项（包含COUNT等参数）
     * @return 游标对象，用于迭代匹配的键
     */
    Cursor<String> scan(String pattern, ScanOptions options);


    /**
     * 获取缓存对象
     *
     * @param redisKey 缓存键
     * @return 缓存对象
     */
    String getCacheObject(String redisKey);




}