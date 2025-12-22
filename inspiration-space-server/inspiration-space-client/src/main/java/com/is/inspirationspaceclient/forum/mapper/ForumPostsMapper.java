package com.is.inspirationspaceclient.forum.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.forum.model.entity.ForumPosts;
import com.is.inspirationspaceclient.forum.model.vo.PostDetailVo;
import com.is.inspirationspaceclient.forum.model.vo.PostSimpleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForumPostsMapper extends BaseMapper<ForumPosts> {

    @Select("<script>" +
            "SELECT fp.post_id as postId, fp.user_id as userId, fp.title, fp.content, fp.image_urls as imageUrls, fp.product_url as productUrl, fp.created_at as createAt, " +
            "u.username, u.avatar_url as avatar, " +
            "fps.like_count as `like`, fps.repost_count as repost, fps.comment_count as commentCount, " +
            "fps.collect_count as collect, fps.view_count as view " +
            "FROM forum_posts fp " +
            "LEFT JOIN forum_post_stats fps ON fp.post_id = fps.post_id " +
            "LEFT JOIN user_profile u ON fp.user_id = u.user_id " +
            "WHERE fp.is_deleted = 0 AND fp.visibility = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (fp.title LIKE CONCAT('%', #{keyword}, '%') OR fp.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY ${orderByColumn} DESC" +
            "</script>")
    Page<PostSimpleVo> selectPostsWithStats(Page<PostSimpleVo> page, @Param("orderByColumn") String orderByColumn, @Param("keyword") String keyword);

    @Select("SELECT fp.post_id as postId, fp.user_id as userId, fp.title, fp.content, fp.image_urls as imageUrls, fp.product_url as productUrl, fp.created_at as createAt, " +
            "u.username, u.avatar_url as avatar, " +
            "fps.like_count as `like`, fps.repost_count as repost, fps.comment_count as commentCount, " +
            "fps.collect_count as collect, fps.view_count as view " +
            "FROM forum_posts fp " +
            "JOIN forum_user_actions fua ON fp.post_id = fua.target_id " +
            "LEFT JOIN forum_post_stats fps ON fp.post_id = fps.post_id " +
            "LEFT JOIN user_profile u ON fp.user_id = u.user_id " +
            "WHERE fua.user_id = #{userId} AND fua.target_type = 3 AND fua.is_active = 1 " +
            "AND fp.is_deleted = 0 " +
            "ORDER BY fua.created_at DESC")
    Page<PostSimpleVo> selectCollectedPosts(Page<PostSimpleVo> page, @Param("userId") Long userId);

    @Select("SELECT fp.post_id as postId, fp.user_id as userId, fp.title, fp.content, fp.image_urls as imageUrls, " +
            "fp.product_url as productUrl, fp.created_at as createAt, " +
            "fps.like_count as `like`, fps.repost_count as repost, fps.comment_count as commentCount, " +
            "fps.collect_count as collect, fps.view_count as view " +
            "FROM forum_posts fp " +
            "JOIN forum_post_stats fps ON fp.post_id = fps.post_id " +
            "WHERE fp.post_id = #{postId} AND fp.is_deleted = 0")
    PostDetailVo selectPostDetail(@Param("postId") Long postId);
}