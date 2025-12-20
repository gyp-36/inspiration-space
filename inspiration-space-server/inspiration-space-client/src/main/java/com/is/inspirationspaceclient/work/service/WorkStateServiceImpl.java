package com.is.inspirationspaceclient.work.service;


import com.is.inspirationspaceclient.work.mapper.WorkStatsMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkStats;
import com.is.inspirationspaceclient.work.model.vo.WorkStateVo;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkStateServiceImpl implements WorkStateService {
    private static final String VIEW_FIELD = "view";
    private static final String LIKE_FIELD = "like";
    private static final String FAVORITE_FIELD = "favorite";
    private static final String COMMENT_FIELD = "comment";
    private static final String PURCHASE_FIELD = "purchase";

    @Autowired
    private WorkStatsMapper workStatsMapper;

    @Autowired
    private RedisCache redisCache;

    private void incrementHashCount(Long workId, String field) {
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.WORK_COUNT);
        //如果缓存中没有该字段，从数据库导入数据
        if (!redisCache.hasHashKey(key, workId + ":" + field)) {
            WorkStats workStats = workStatsMapper.selectById(workId);
            switch (field) {
                case "view":
                    redisCache.putHash(key, workId + ":" + field, workStats.getViewCount());
                    break;
                case "like":
                    redisCache.putHash(key, workId + ":" + field, workStats.getLikeCount());
                    break;
                case "favorite":
                    redisCache.putHash(key, workId + ":" + field, workStats.getFavoriteCount());
                    break;
                case "comment":
                    redisCache.putHash(key, workId + ":" + field, workStats.getCommentCount());
                    break;
                case "purchase":
                    redisCache.putHash(key, workId + ":" + field, workStats.getPurchaseCount());
                    break;
            }
            // 设置过期时间
            redisCache.expire(key, 1, TimeUnit.DAYS);
        }

        // 原子操作
        redisCache.incrHash(key, workId + ":" + field, 1);
        //更新过期时间
        redisCache.updateExpire(key, 1, TimeUnit.DAYS);
    }

    @Override
    public void incrementViewCount(Long workId) {
        incrementHashCount(workId, VIEW_FIELD);
    }

    @Override
    public void incrementLikeCount(Long workId) {
        incrementHashCount(workId, LIKE_FIELD);
    }

    @Override
    public void incrementCollectCount(Long workId) {
        incrementHashCount(workId, FAVORITE_FIELD);
    }

    @Override
    public void incrementCommentCount(Long workId) {
        incrementHashCount(workId, COMMENT_FIELD);
    }

    @Override
    public void incrementPurchaseCount(Long workId) {
        incrementHashCount(workId, PURCHASE_FIELD);
    }


    @Override
    public List<WorkStateVo> getWorkSalesRank(int topN) {
        // 1. 验证参数
        if (topN <= 0) {
            return Collections.emptyList();
        }

        // 2. 定义缓存key
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.WORK_SALES_RANK_DAY);
        String emptyKey = key.getRelKey() + ":empty"; // 空结果标记key

        try {
            // 3. 优先检查空结果标记（防止缓存穿透）
            if (redisCache.hasKey(RedisKeyBuild.createRedisKey(emptyKey))) {
                return Collections.emptyList();
            }

            // 4. 从缓存直接获取排序后的前topN条数据
            Set<WorkStateVo> cachedResult = redisCache.rangeZSet(key, 0, topN - 1, WorkStateVo.class);
            if (cachedResult != null && !cachedResult.isEmpty()) {
                return new ArrayList<>(cachedResult);
            }

            // 5. 缓存为空，使用双重检查加锁防止缓存击穿
            String lockKey = key.getRelKey() + ":lock";
            boolean locked = redisCache.set(RedisKeyBuild.createRedisKey(lockKey), 3, 10, TimeUnit.SECONDS);
            if (!locked) {
                // 获取锁失败，直接查询数据库
                return queryAndReturnFromDb(topN);
            }

            try {
                // 二次检查，避免多个线程同时重建缓存
                cachedResult = redisCache.rangeZSet(key, 0, topN - 1, WorkStateVo.class);
                if (cachedResult != null && !cachedResult.isEmpty()) {
                    return new ArrayList<>(cachedResult);
                }

                // 6. 从数据库获取作品日销量排行榜
                List<WorkStateVo> workStateVos = workStatsMapper.selectWorkSalesRank(topN);

                // 7. 处理空结果
                if (CollectionUtils.isEmpty(workStateVos)) {
                    // 设置空结果标记（短期缓存）
                    redisCache.set(RedisKeyBuild.createRedisKey(emptyKey), "1", 10, TimeUnit.MINUTES);
                    return Collections.emptyList();
                }

                // 8. 更新缓存
                updateCache(key, workStateVos, topN);
                return new ArrayList<>(workStateVos.subList(0, Math.min(topN, workStateVos.size())));

            } finally {
                redisCache.del(RedisKeyBuild.createRedisKey(lockKey));
            }
        } catch (Exception e) {
            log.error("获取作品销售排行榜异常, key: {}", key.getRelKey(), e);
            // 降级处理：直接查询数据库
            return queryAndReturnFromDb(topN);
        }
    }

    // 数据库查询+返回结果
    private List<WorkStateVo> queryAndReturnFromDb(int topN) {
        List<WorkStateVo> workStateVos = workStatsMapper.selectWorkSalesRank(topN);
        return workStateVos != null
                ? new ArrayList<>(workStateVos.subList(0, Math.min(topN, workStateVos.size())))
                : Collections.emptyList();
    }

    private void updateCache(RedisKeyBuild key, List<WorkStateVo> workStateVos, int topN) {
        try {
            // 清理旧的空结果标记
            String emptyKey = key.getRelKey() + ":empty";
            redisCache.del(RedisKeyBuild.createRedisKey(emptyKey));

            // 使用销量作为排序依据构建ZSet
            Map<Object, Double> scoreMap = workStateVos.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            vo -> (double) vo.getTotalSales() // 确保使用正确的销量字段
                    ));

            // 批量更新ZSet
            redisCache.addZSetAll(key, scoreMap, 23, TimeUnit.HOURS);


        } catch (Exception e) {
            // 缓存更新失败不应影响主流程
            log.warn("更新销售排行榜缓存失败, key: {}", key.getRelKey(), e);
        }
    }


    @Override
    public List<WorkStateVo> getWorkLikeRank(int topN) {
        return List.of();
    }


    //=========================定时任务=========================
    //设置redis同步数据库的定时任务
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Async
    public void syncWorkCounts() {

    }

    // 定时任务预热排行榜数据
    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Async
    public void preheatHotData() {
        getWorkSalesRank(10); // 预热前10名数据
    }
}
