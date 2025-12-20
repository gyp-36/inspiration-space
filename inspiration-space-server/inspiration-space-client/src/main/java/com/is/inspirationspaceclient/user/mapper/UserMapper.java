package com.is.inspirationspaceclient.user.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.is.inspirationspaceclient.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户基本信息Mapper
 * </p>
 *
 * @author :gyp
 * @since 2025-07-07
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
