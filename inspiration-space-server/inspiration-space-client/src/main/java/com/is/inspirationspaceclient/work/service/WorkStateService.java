package com.is.inspirationspaceclient.work.service;


import com.is.inspirationspaceclient.work.model.vo.WorkStateVo;

import java.util.List;

public interface WorkStateService {
    void incrementViewCount(Long workId);

    void incrementLikeCount(Long workId);

    void incrementCollectCount(Long workId);

    void incrementCommentCount(Long workId);

    void incrementPurchaseCount(Long workId);



    List<WorkStateVo> getWorkSalesRank(int topN);

    List<WorkStateVo> getWorkLikeRank(int topN);
}