package com.is.inspirationspaceclient.user.service;

import com.is.inspirationspaceclient.user.model.dto.*;
import com.is.inspirationspaceclient.user.model.vo.*;
import org.springframework.web.multipart.MultipartFile;
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
}