package com.is.inspirationspaceclient.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.forum.model.entity.ForumPostStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ForumPostStatsMapper extends BaseMapper<ForumPostStats> {
    @Update({
            "UPDATE forum_post_stats "+
            "SET like_count = like_count + #{delta} "+
            "WHERE post_id = #{postId}"
    })
    int updateLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    @Update({
            "UPDATE forum_post_stats "+
            "SET repost_count = repost_count + #{delta} "+
            "WHERE post_id = #{postId}"
    })
    int updateRepostCount(@Param("postId") Long postId, @Param("delta") int delta);

    @Update({
            "UPDATE forum_post_stats "+
            "SET collect_count = collect_count + #{delta} "+
            "WHERE post_id = #{postId}"
    })
    int updateCollectCount(@Param("postId") Long postId, @Param("delta") int delta);
}
