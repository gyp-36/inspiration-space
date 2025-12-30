package com.is.inspirationspaceclient.work.service;


import com.is.inspirationspaceclient.work.model.entity.WorkStats;
import com.is.inspirationspaceclient.work.model.vo.WorkRankVo;

import java.util.List;

public interface WorkStateService {
    void incrementViewCount(Long workId, Long userId);

    void incrementLikeCount(Long workId);

    void incrementCollectCount(Long workId);

    void incrementCommentCount(Long workId);

    void incrementPurchaseCount(Long workId);

    void toggleLike(Long workId, Long userId);

    void toggleCollect(Long workId, Long userId);

    WorkStats getWorkStats(Long workId);

    List<WorkRankVo> getWorkSalesRank(int topN);

    List<WorkRankVo> getWorkLikeRank(int topN);
}