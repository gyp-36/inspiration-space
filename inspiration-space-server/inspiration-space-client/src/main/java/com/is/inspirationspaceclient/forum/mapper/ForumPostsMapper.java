package com.is.inspirationspaceclient.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.forum.model.entity.ForumPosts;
import com.is.inspirationspaceclient.forum.model.vo.PostSimpleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ForumPostsMapper extends BaseMapper<ForumPosts> {

    @Select("SELECT fp.post_id, fp.user_id, fp.title, fp.content, fp.created_at as createAt, " +
            "fps.like_count as `like`, fps.repost_count as repost, fps.comment_count as collect " +
            "FROM forum_posts fp " +
            "JOIN forum_post_stats fps ON fp.post_id = fps.post_id " +
            "WHERE fp.is_deleted = 0 " +
            "ORDER BY ${orderByColumn} DESC")
    Page<PostSimpleVo> selectPostsWithStats(Page<PostSimpleVo> page, @Param("orderByColumn") String orderByColumn);


}
