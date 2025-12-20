package com.is.inspirationspaceclient.forum.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.is.inspirationspaceclient.forum.model.dto.ForumCreateDto;
import com.is.inspirationspaceclient.forum.model.vo.PostDetailVo;
import com.is.inspirationspaceclient.forum.model.vo.PostSimpleVo;

public interface ForumService {

    Boolean deletePost(Long postId);

    Page<PostSimpleVo> getAllPosts(String sort, int page, int size);

    PostDetailVo getPostDetail(Long postId);

    Boolean like(String token, Long postId);

    Boolean unlike(String token, Long postId);

    Boolean repost(String token, Long postId);

    Boolean collect(String token, Long postId);

    Boolean uncollect(String token, Long postId);

    Boolean createPost(String token, ForumCreateDto forumCreateDto);
}
