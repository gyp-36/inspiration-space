package com.is.inspirationspaceclient.user.service;

import com.is.inspirationspaceclient.user.model.dto.*;
import com.is.inspirationspaceclient.user.model.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author
 * @since 2025-07-07
 */
public interface UserService {


    Boolean register(UserRegisterDto userRegisterDto);

    UserLoginVo login(UserLoginDto userloginDto);

    UserProfileVo getUserProfile(Long userId);

    UserExtendVo getUserExtendInfo(Long userId);

    UserStatsVo getUserStatsInfo(Long userId);

    UserVo getUser(Long userId);

    Boolean updateUser(UserUpdateDto userUpdateDto);

    Boolean logout(UserIdDto userIdDto);

    Boolean updatePassword(UserChangePasswordDto userChangePasswordDto);

    Boolean removeById(UserIdDto userIdeDto);

    UserInfoVo getUserInfo(Long userId);

    String getAvatar(Long userId);

    Boolean updateAvatar(Long userId, MultipartFile avatar);

    /**
     * 关注用户
     */
    Boolean followUser(Long targetUserId);

    /**
     * 取消关注用户
     */
    Boolean unfollowUser(Long targetUserId);

    /**
     * 判断是否已关注
     */
    Boolean isFollowing(Long targetUserId);

    /**
     * 获取用户排行榜
     * @param topN 前N名
     * @return 用户列表
     */
    List<UserVo> getUserRanking(int topN);
}