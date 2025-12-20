package com.is.inspirationspaceclient.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.work.model.entity.WorkStats;
import com.is.inspirationspaceclient.work.model.vo.WorkStateVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 作品统计表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
@Mapper
public interface WorkStatsMapper extends BaseMapper<WorkStats> {

    List<WorkStateVo> selectWorkSalesRank(int topN);
}
