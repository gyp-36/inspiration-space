package com.is.inspirationspaceclient.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.user.model.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户统计信息Mapper
 * </p>
 *
 * @author :gyp
 * @since 2025-07-07
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {
}