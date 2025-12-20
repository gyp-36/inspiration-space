package com.is.inspirationspacecommon.redis;

import lombok.Getter;

import java.util.Objects;


/**
 * @description: Redis key构建器
 *
 */
@Getter
public final class RedisKeyBuild {
    /**
     * 实际使用的key
     */
    private final String relKey;

    private RedisKeyBuild(String relKey) {
        this.relKey = relKey;
    }

    /**
     * 构建带参数的Redis key
     * @param redisKeyManage key的枚举
     * @param args 占位符的值
     * @return RedisKeyBuild实例
     */
    public static RedisKeyBuild createRedisKey(RedisKeyManage redisKeyManage, Object... args) {
        Objects.requireNonNull(redisKeyManage, "RedisKeyManage不能为空");
        String formattedKey = String.format(redisKeyManage.getKey(), args);
        return new RedisKeyBuild(formattedKey);
    }

    /**
     * 构建无参数的Redis key
     * @param redisKeyManage key的枚举
     * @return RedisKeyBuild实例
     */
    public static RedisKeyBuild createRedisKey(RedisKeyManage redisKeyManage) {
        Objects.requireNonNull(redisKeyManage, "RedisKeyManage不能为空");
        return new RedisKeyBuild(redisKeyManage.getKey());
    }

    public static RedisKeyBuild createRedisKey(String key) {
        Objects.requireNonNull(key, "RedisKey不能为空");
        return new RedisKeyBuild(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisKeyBuild that = (RedisKeyBuild) o;
        return relKey.equals(that.relKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relKey);
    }


}
