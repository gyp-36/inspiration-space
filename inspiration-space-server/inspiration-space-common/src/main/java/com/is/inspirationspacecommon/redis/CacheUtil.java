package com.is.inspirationspacecommon.redis;

import cn.hutool.core.lang.ParameterizedTypeImpl;
import com.is.inspirationspacecommon.util.StringUtil;


import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis缓存工具类
 * <p>提供缓存操作相关的辅助方法</p>
 */
public final class CacheUtil {

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 构建参数化类型
     *
     * @param types 类型数组（第一个为原始类型，后续为泛型参数）
     * @return 构建完成的参数化类型
     * @throws IllegalArgumentException 当类型数组为空时抛出
     */
    public static Type buildType(Type... types) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("类型数组不能为空");
        }

        // 单类型直接返回
        if (types.length == 1) {
            return new ParameterizedTypeImpl(new Type[]{null}, null, types[0]);
        }

        // 多类型构建参数化类型链
        ParameterizedTypeImpl result = null;
        for (int i = types.length - 1; i > 0; i--) {
            Type[] actualTypeArguments = result == null
                    ? new Type[]{types[i]}
                    : new Type[]{result};

            result = new ParameterizedTypeImpl(actualTypeArguments, null, types[i - 1]);
        }
        return result;
    }

    /**
     * 检查字符串键是否有效
     *
     * @param keys 要检查的字符串键
     * @throws IllegalArgumentException 如果有任何键为空白
     */
    public static void checkNotBlank(String... keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("Keys不能为空");
        }

        for (String key : keys) {
            if (StringUtil.isEmpty(key)) {
                throw new IllegalArgumentException("Cache不能为空");
            }
        }
    }

    /**
     * 检查Redis键构建器是否有效
     *
     * @param redisKeyBuild Redis键构建器
     * @throws IllegalArgumentException 如果键为空白
     */
    public static void checkNotBlank(RedisKeyBuild redisKeyBuild) {
        if (redisKeyBuild == null || StringUtil.isEmpty(redisKeyBuild.getRelKey())) {
            throw new IllegalArgumentException("Redis key不能为空");
        }
    }

    /**
     * 检查字符串集合是否包含空白值
     *
     * @param collection 字符串集合
     * @throws IllegalArgumentException 如果集合包含空白值
     */
    public static void checkNotBlank(Collection<String> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("集合不能为空");
        }

        for (String item : collection) {
            if (StringUtil.isEmpty(item)) {
                throw new IllegalArgumentException("Collection contains blank value");
            }
        }
    }

    /**
     * 检查集合是否包含空元素
     *
     * @param collection 要检查的集合
     * @throws IllegalArgumentException 如果集合包含空元素
     */
    public static void checkNoNullElements(Collection<?> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("集合不能为空");
        }

        for (Object item : collection) {
            if (item == null) {
                throw new IllegalArgumentException("集合包含空元素");
            }
        }
    }

    /**
     * 检查对象是否为空
     *
     * @param object 要检查的对象
     * @throws IllegalArgumentException 如果对象为空
     */
    public static void checkNotNull(Object object) {
        if (isEmpty(object)) {
            throw new IllegalArgumentException("对象不能为空");
        }
    }

    /**
     * 判断对象是否为空
     *
     * @param object 要检查的对象
     * @return true 如果对象为空，否则 false
     */
    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof String) {
            return StringUtil.isEmpty((String) object);
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        }
        return false;
    }

    /**
     * 从Redis键构建器集合中提取实际键值
     *
     * @param keyBuilders Redis键构建器集合
     * @return 实际键值列表
     */
    public static List<String> extractKeys(Collection<RedisKeyBuild> keyBuilders) {
        if (keyBuilders == null || keyBuilders.isEmpty()) {
            return Collections.emptyList();
        }
        return keyBuilders.stream()
                .map(RedisKeyBuild::getRelKey)
                .collect(Collectors.toList());
    }

    /**
     * 优化Redis返回的列表结果
     *
     * @param list 原始列表
     * @return 优化后的安全列表（不会返回null）
     */
    public static <T> List<T> optimizeListResult(List<T> list) {
        if (list == null || list.isEmpty() || list.get(0) == null) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 检查Redis返回的列表是否为空
     *
     * @param list 要检查的列表
     * @return true 如果列表为空或无效，否则 false
     */
    public static boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty() || list.get(0) == null;
    }
}