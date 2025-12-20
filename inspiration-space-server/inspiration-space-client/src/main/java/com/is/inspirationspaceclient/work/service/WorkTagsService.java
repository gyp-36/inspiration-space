package com.is.inspirationspaceclient.work.service;



import com.is.inspirationspaceclient.work.model.dto.TagCreateDto;
import com.is.inspirationspaceclient.work.model.entity.Tag;
import com.is.inspirationspaceclient.work.model.vo.TagVo;

import java.util.List;

/**
 * <p>
 * 作品标签 服务类
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
public interface WorkTagsService {

    Boolean createTag(TagCreateDto tagCreateDto);

    Boolean deleteTag(Long tagId);

    List<TagVo> getTagList(int pageNum, int pageSize);

    List<TagVo> getPopularTags(int topN);

    void addTagsToWork(Long workId, List<Integer> tagIds);

    void removeTagsFromWork(Long workId, List<Integer> tagIds);

    List<TagVo> getTagsByWorkId(Long workId);
}