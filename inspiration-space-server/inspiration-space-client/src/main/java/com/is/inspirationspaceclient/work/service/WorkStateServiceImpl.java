package com.is.inspirationspaceclient.work.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.is.inspirationspaceclient.work.mapper.WorkStatsMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkStats;
import com.is.inspirationspaceclient.work.model.vo.WorkStateVo;
import com.is.inspirationspaceclient.forum.mapper.ForumUserActionsMapper;
import com.is.inspirationspaceclient.forum.model.entity.ForumUserActions;
import com.is.inspirationspaceclient.forum.model.entity.enums.TargetType;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private ForumUserActionsMapper userActionsMapper;

    private void incrementHashCount(Long workId, String field) {
        changeHashCount(workId, field, 1);
    }

    private void changeHashCount(Long workId, String field, int delta) {
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.WORK_COUNT);
        //如果缓存中没有该字段，从数据库导入数据
        if (!redisCache.hasHashKey(key, workId + ":" + field)) {
            WorkStats workStats = workStatsMapper.selectById(workId);
            if (workStats == null) {
                workStats = new WorkStats();
                workStats.setWorkId(workId);
                workStats.setViewCount(0);
                workStats.setLikeCount(0);
                workStats.setFavoriteCount(0);
                workStats.setCommentCount(0);
                workStats.setPurchaseCount(0);
                workStatsMapper.insert(workStats);
            }
            
            Integer value = 0;
            switch (field) {
                case "view":
                    value = workStats.getViewCount();
                    break;
                case "like":
                    value = workStats.getLikeCount();
                    break;
                case "favorite":
                    value = workStats.getFavoriteCount();
                    break;
                case "comment":
                    value = workStats.getCommentCount();
                    break;
                case "purchase":
                    value = workStats.getPurchaseCount();
                    break;
            }
            redisCache.putHash(key, workId + ":" + field, value != null ? value : 0);
            // 设置过期时间
            redisCache.expire(key, 1, TimeUnit.DAYS);
        }

        // 原子操作
        redisCache.incrHash(key, workId + ":" + field, delta);
        //更新过期时间
        redisCache.updateExpire(key, 1, TimeUnit.DAYS);
    }

    @Override
    public void incrementViewCount(Long workId, Long userId) {
        if (userId == null) {
            // 如果未登录，可以根据IP或者其他标识，这里简单处理，不计入或只计入一次
            // 为了满足“一个用户只限一次”，通常需要标识
            return;
        }

        RedisKeyBuild viewHistoryKey = RedisKeyBuild.createRedisKey(RedisKeyManage.WORK_VIEW_HISTORY, workId);
        // 检查用户是否已经看过
        Boolean hasViewed = redisCache.isSetMember(viewHistoryKey, userId);
        if (Boolean.FALSE.equals(hasViewed)) {
            // 没看过，添加记录并增加浏览量
            redisCache.addSet(viewHistoryKey, userId);
            // 设置过期时间，比如30天
            redisCache.expire(viewHistoryKey, 30, TimeUnit.DAYS);
            incrementHashCount(workId, VIEW_FIELD);
        }
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
    @Transactional
    public void toggleLike(Long workId, Long userId) {
        toggleAction(workId, userId, TargetType.LIKE, LIKE_FIELD);
    }

    @Override
    @Transactional
    public void toggleCollect(Long workId, Long userId) {
        toggleAction(workId, userId, TargetType.COLLECT, FAVORITE_FIELD);
    }

    private void toggleAction(Long workId, Long userId, TargetType type, String field) {
        QueryWrapper<ForumUserActions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("target_type", type)
                .eq("target_id", workId);
        
        ForumUserActions action = userActionsMapper.selectOne(queryWrapper);
        if (action == null) {
            // 新增
            action = new ForumUserActions();
            action.setUserId(userId);
            action.setTargetType(type);
            action.setTargetId(workId);
            action.setIsActive(1);
            userActionsMapper.insert(action);
            changeHashCount(workId, field, 1);
        } else {
            // 切换状态
            int newStatus = action.getIsActive() == 1 ? 0 : 1;
            action.setIsActive(newStatus);
            userActionsMapper.updateById(action);
            changeHashCount(workId, field, newStatus == 1 ? 1 : -1);
        }
    }


    @Override
    public WorkStats getWorkStats(Long workId) {
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.WORK_COUNT);
        WorkStats stats = new WorkStats();
        stats.setWorkId(workId);
        
        stats.setViewCount(getCountFromRedisOrDb(workId, VIEW_FIELD, key));
        stats.setLikeCount(getCountFromRedisOrDb(workId, LIKE_FIELD, key));
        stats.setFavoriteCount(getCountFromRedisOrDb(workId, FAVORITE_FIELD, key));
        stats.setCommentCount(getCountFromRedisOrDb(workId, COMMENT_FIELD, key));
        stats.setPurchaseCount(getCountFromRedisOrDb(workId, PURCHASE_FIELD, key));
        
        return stats;
    }

    private Integer getCountFromRedisOrDb(Long workId, String field, RedisKeyBuild key) {
        Object count = redisCache.getHash(key, workId + ":" + field, Object.class);
        if (count != null) {
            return ((Number) count).intValue();
        }
        
        // 如果缓存没有，则加载并返回
        WorkStats dbStats = workStatsMapper.selectById(workId);
        if (dbStats == null) {
            dbStats = new WorkStats();
            dbStats.setWorkId(workId);
            dbStats.setViewCount(0);
            dbStats.setLikeCount(0);
            dbStats.setFavoriteCount(0);
            dbStats.setCommentCount(0);
            dbStats.setPurchaseCount(0);
            workStatsMapper.insert(dbStats);
            
            redisCache.putHash(key, workId + ":" + field, 0);
            return 0;
        }
        
        Integer value = 0;
        switch (field) {
            case VIEW_FIELD -> value = dbStats.getViewCount();
            case LIKE_FIELD -> value = dbStats.getLikeCount();
            case FAVORITE_FIELD -> value = dbStats.getFavoriteCount();
            case COMMENT_FIELD -> value = dbStats.getCommentCount();
            case PURCHASE_FIELD -> value = dbStats.getPurchaseCount();
        }
        
        int finalValue = value != null ? value : 0;
        redisCache.putHash(key, workId + ":" + field, finalValue);
        return finalValue;
    }

    /**
     * 每小时同步一次 Redis 统计数据到数据库
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void syncStatsToDb() {
        RedisKeyBuild key = RedisKeyBuild.createRedisKey(RedisKeyManage.WORK_COUNT);
        Map<String, Object> allStats = redisCache.getAllHash(key, Object.class);
        if (CollectionUtils.isEmpty(allStats)) return;

        Map<Long, WorkStats> updates = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : allStats.entrySet()) {
            String fullField = entry.getKey();
            Integer count = ((Number) entry.getValue()).intValue();
            
            String[] parts = fullField.split(":");
            if (parts.length != 2) continue;
            
            Long workId = Long.parseLong(parts[0]);
            String field = parts[1];
            
            WorkStats stats = updates.computeIfAbsent(workId, id -> {
                WorkStats s = new WorkStats();
                s.setWorkId(id);
                return s;
            });
            
            switch (field) {
                case VIEW_FIELD -> stats.setViewCount(count);
                case LIKE_FIELD -> stats.setLikeCount(count);
                case FAVORITE_FIELD -> stats.setFavoriteCount(count);
                case COMMENT_FIELD -> stats.setCommentCount(count);
                case PURCHASE_FIELD -> stats.setPurchaseCount(count);
            }
        }
        
        for (WorkStats stats : updates.values()) {
            workStatsMapper.updateById(stats);
        }
        log.info("Synced {} work stats from Redis to DB", updates.size());
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
    // 定时任务预热排行榜数据
    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Async
    public void preheatHotData() {
        getWorkSalesRank(10); // 预热前10名数据
    }
}
