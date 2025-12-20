package com.is.inspirationspaceclient.work.service;


import com.is.inspirationspaceclient.work.mapper.TagMapper;
import com.is.inspirationspaceclient.work.mapper.WorkTagsMapper;
import com.is.inspirationspaceclient.work.model.dto.TagCreateDto;
import com.is.inspirationspaceclient.work.model.entity.Tag;
import com.is.inspirationspaceclient.work.model.vo.TagVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 作品标签 服务实现类
 * </p>
 *
 * @author 
 * @since 2025-07-07
 */
@Service
public class WorkTagsServiceImpl implements WorkTagsService {

    @Autowired
    private  WorkTagsMapper workTagsMapper;

    @Autowired
    private TagMapper TagsMapper;

    @Override
    public Boolean createTag(TagCreateDto tagCreateDto) {
        if(tagCreateDto == null){
            return false;
        }

        Tag tag = new Tag();
        BeanUtils.copyProperties(tagCreateDto, tag);
        return TagsMapper.insert(tag) > 0;
    }

    @Override
    public Boolean deleteTag(Long tagId) {
        return null;
    }

    @Override
    public List<TagVo> getTagList(int pageNum, int pageSize) {
        return List.of();
    }

    @Override
    public List<TagVo> getPopularTags(int topN) {
        return List.of();
    }

    @Override
    public void addTagsToWork(Long workId, List<Integer> tagIds) {

    }

    @Override
    public void removeTagsFromWork(Long workId, List<Integer> tagIds) {

    }

    @Override
    public List<TagVo> getTagsByWorkId(Long workId) {
        return List.of();
    }
}
