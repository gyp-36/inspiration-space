package com.is.inspirationspaceclient.user.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;


import com.is.inspirationspaceclient.user.mapper.*;
import com.is.inspirationspaceclient.user.model.dto.*;
import com.is.inspirationspaceclient.user.model.entity.*;
import com.is.inspirationspaceclient.user.model.entity.enums.FollowType;
import com.is.inspirationspaceclient.user.model.entity.enums.UserStatus;
import com.is.inspirationspaceclient.user.model.vo.*;
import com.is.inspirationspaceclient.user.rabbitmq.UserMessageProducer;
import com.is.inspirationspacecommon.config.StorageService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.exception.IsServiceException;
import com.is.inspirationspacecommon.exception.IsSystemException;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.redis.RedisKeyBuild;
import com.is.inspirationspacecommon.redis.RedisKeyManage;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.StringUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;

    private RoleMapper roleMapper;

    private UserRoleMapper userRoleMapper;

    private UserExpansionMapper userExpansionMapper;

    private UserStatsMapper userStatsMapper;

    private PasswordEncoder passwordEncoder;

    private SnowflakeIdGenerator snowflakeIdGenerator;

    private RedisCache redisCache;

    private UserMessageProducer userMessageProducer;

    private StorageService storageService;

    private UserRelationshipMapper userRelationshipMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean register(@Valid UserRegisterDto userRegisterDto) {
        log.info("[注册开始] username={}, email={}, phone={}",
                userRegisterDto.getUsername(),
                userRegisterDto.getEmail(),
                userRegisterDto.getPhone()
        );

        // 1. 批量检查唯一性
        List<User> existingUsers = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, userRegisterDto.getUsername())
                .or()
                .eq(User::getEmail, userRegisterDto.getEmail())
                .or()
                .eq(User::getPhone, userRegisterDto.getPhone()));

        //验证码检查


        for (User user : existingUsers) {
            if (user.getUsername().equals(userRegisterDto.getUsername())) {
                throw new IsArgumentException(ErrorCode.USER_EXISTS.getHttpStatusCode(), "用户名已存在");
            }
            if (user.getEmail() != null && user.getEmail().equals(userRegisterDto.getEmail())) {
                throw new IsArgumentException(ErrorCode.EMAIL_EXISTS.getHttpStatusCode(), "邮箱已存在");
            }
            if (user.getPhone() != null && user.getPhone().equals(userRegisterDto.getPhone())) {
                throw new IsArgumentException(ErrorCode.PHONE_EXISTS.getHttpStatusCode(), "手机号已存在");
            }
        }

        // 2. 生成ID
        Long userId = snowflakeIdGenerator.nextId();

        // 3. 初始化基础用户
        User user = new User();
        BeanUtils.copyProperties(userRegisterDto, user, "password");
        user.setUserId(userId);
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));

        // 4. 初始化扩展信息（设置默认值）
        UserExpansion userExpansion = new UserExpansion();
        userExpansion.setUserId(userId);
        userExpansion.setLastLoginTime(LocalDateTime.now());

        // 5. 初始化统计信息
        UserStats userStats = new UserStats();
        userStats.setUserId(userId);

        // 6. 初始化角色
        String defaultRoleCode = "client-user";
        Role defaultRole = roleMapper.selectOne(Wrappers.<Role>lambdaQuery()
                .eq(Role::getRoleCode, defaultRoleCode));
        if (defaultRole == null) {
            log.error("默认角色 {} 不存在，请检查系统配置", defaultRoleCode);
            throw new IsSystemException(ErrorCode.SYSTEM_ERROR.getHttpStatusCode(), "系统角色配置错误");
        }

        //7.建立用户-角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(defaultRole.getRoleId());

        // 8. 批量插入
        userMapper.insert(user);
        userExpansionMapper.insert(userExpansion);
        userStatsMapper.insert(userStats);
        userRoleMapper.insert(userRole);

        //9.发送消息
        userMessageProducer.sendFastMessage("user", String.format("[注册成功] userId=%d, username=%s", userId, user.getUsername()));
        log.info("[注册成功] userId={}, username={}", userId, user.getUsername());
        return true;
    }


    @Override
    public UserLoginVo login(UserLoginDto userloginDto) {
        log.info("[用户登录开始] username={}", userloginDto.getUsername());

        // ==========  初始化对象 ==========
        UserLoginVo userLoginVo = new UserLoginVo();
        String captcha = userloginDto.getCaptcha();       // 验证码
        String username = userloginDto.getUsername();   // 用户名
        String password = userloginDto.getPassword(); // 密码

        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
            throw new IsArgumentException(ErrorCode.USER_PASSWORD_NULL_ERROR.getHttpStatusCode(), "用户名或密码不能为空");
        }
        //==========  比对验证码 ==========


        // ==========  验证用户密码 ==========
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IsArgumentException(ErrorCode.USER_PASSWORD_ERROR_MESSAGE.getHttpStatusCode(), "用户名或密码错误");
        }

        // ==========  验证用户状态 ==========
        UserExpansion userExpansion = userExpansionMapper.selectById(user.getUserId());
        if (userExpansion.getUserStatus().equals(UserStatus.BANNED) || userExpansion.getUserStatus().equals(UserStatus.DELETED)) {
            throw new IsArgumentException(ErrorCode.USER_STATUS_ERROR.getHttpStatusCode(), "用户状态异常");
        }


        // ========== 登录成功处理 ==========
        userExpansion.setLastLoginTime(LocalDateTime.now());
        userExpansionMapper.updateById(userExpansion);


        // ========== Token处理 ==========
        // 构建loginVo
        userLoginVo.setUserId(user.getUserId());

        // 检查用户是否已有token
        RedisKeyBuild userTokensKey = RedisKeyBuild.createRedisKey(
                RedisKeyManage.USER_TOKENS,
                user.getUserId()
        );

        Set<String> existingTokens = redisCache.membersSet(userTokensKey, String.class);

        // 如果已有token复用
        if (existingTokens != null && !existingTokens.isEmpty()) {
            // 1. 删除所有旧Token在Redis中的状态记录
            for (String oldToken : existingTokens) {
                RedisKeyBuild oldTokenStatusKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, oldToken, "STATUS");
                redisCache.del(oldTokenStatusKey);
            }
            // 2. 清空用户Token集合（旧Token全部失效）
            redisCache.del(userTokensKey);
            log.info("用户{}登录，已清理所有旧Token", username);

        }

        // 【核心修改2：无论是否有旧Token，都生成新Token（更新exp字段）】
        newToken(user, username, userLoginVo, userTokensKey);


        //发送消息
        userMessageProducer.sendFastMessage("user", String.format("[用户登录成功] userId=%d, username=%s", user.getUserId(), username));
        log.info("[用户登录成功] userId={}, username={}", user.getUserId(), username);
        return userLoginVo;

    }

    /**
     * 从请求头 Token 获取当前登录用户 ID
     */
    private Long getCurrentUserIdFromToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "无效的token:" + token);
        }

        RedisKeyBuild tokenKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, token, "STATUS");
        if (!redisCache.hasKey(tokenKey)) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "未登录");
        }
        UserLoginVo userLoginVo = redisCache.get(tokenKey, UserLoginVo.class);
        if (userLoginVo == null || userLoginVo.getUserId() == null) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "登录状态已失效");
        }
        return userLoginVo.getUserId();
    }

    private void newToken(User user, String username, UserLoginVo userLoginVo, RedisKeyBuild userTokensKey) {
        String newToken = JwtUtil.generateToken(user.getUserId(), username);
        userLoginVo.setToken(newToken);

        // 存储登录状态
        RedisKeyBuild redisKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, newToken, "STATUS");
        redisCache.set(redisKey, userLoginVo, 3, TimeUnit.HOURS);

        // 存储用户ID到token的映射关系
        redisCache.addSet(userTokensKey, newToken);
        // 设置集合过期时间（与token相同）
        redisCache.expire(userTokensKey, 3, TimeUnit.HOURS);

        log.info("用户{}创建新token", username);
    }

    @Override
    public UserProfileVo getUserProfile(Long userId) {

        UserProfileVo userProfileVo = new UserProfileVo();

        if (userId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }

        // 1. 从请求头获取当前token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "无效的token:" + token);
        }


        // 2. 检查token是否有效（在Redis中有对应登录状态）
        RedisKeyBuild tokenKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, token, "STATUS");
        if (!redisCache.hasKey(tokenKey)) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "未登录");
        }
        UserLoginVo userLoginVo = redisCache.get(tokenKey, UserLoginVo.class);
        // 4. 权限验证
        Long currentUserId = userLoginVo.getUserId();


        if (!currentUserId.equals(userId)) {
            throw new IsArgumentException(ErrorCode.FORBIDDEN_ERROR.getHttpStatusCode(), "没有权限访问该用户信息");
        }


        User user = userMapper.selectById(userId);
        BeanUtils.copyProperties(user, userProfileVo, "password");

        log.info("成功获取用户信息，用户ID:{}", userId);
        return userProfileVo;
    }

    @Override
    public UserExtendVo getUserExtendInfo(Long userId) {

        UserExtendVo userExtendVo = new UserExtendVo();

        if (userId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }

        // 1. 从请求头获取当前token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "无效的token:" + token);
        }


        // 2. 检查token是否有效（在Redis中有对应登录状态）
        RedisKeyBuild tokenKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, token, "STATUS");
        if (!redisCache.hasKey(tokenKey)) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "未登录");
        }
        UserLoginVo userLoginVo = redisCache.get(tokenKey, UserLoginVo.class);
        // 4. 权限验证
        Long currentUserId = userLoginVo.getUserId();


        if (!currentUserId.equals(userId)) {
            throw new IsArgumentException(ErrorCode.FORBIDDEN_ERROR.getHttpStatusCode(), "没有权限访问该用户信息");
        }


        UserExpansion userExpansion = userExpansionMapper.selectById(userId);
        BeanUtils.copyProperties(userExpansion, userExtendVo, "last_login_time", "update_time", "deleted");

        log.info("成功获取用户扩展信息，用户ID:{}", userId);
        return userExtendVo;
    }

    @Override
    public UserStatsVo getUserStatsInfo(Long userId) {
        UserStatsVo userStatsVo = new UserStatsVo();

        if (userId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }

        // 1. 从请求头获取当前token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "无效的token:" + token);
        }


        // 2. 检查token是否有效（在Redis中有对应登录状态）
        RedisKeyBuild tokenKey = RedisKeyBuild.createRedisKey(RedisKeyManage.USER_LOGIN, token, "STATUS");
        if (!redisCache.hasKey(tokenKey)) {
            throw new IsArgumentException(ErrorCode.USER_TOKEN_ERROR.getHttpStatusCode(), "未登录");
        }
        UserLoginVo userLoginVo = redisCache.get(tokenKey, UserLoginVo.class);
        // 4. 权限验证
        Long currentUserId = userLoginVo.getUserId();


        if (!currentUserId.equals(userId)) {
            throw new IsArgumentException(ErrorCode.FORBIDDEN_ERROR.getHttpStatusCode(), "没有权限访问该用户信息");
        }


        UserStats userStats = userStatsMapper.selectById(userId);
        BeanUtils.copyProperties(userStats, userStatsVo, "update_time");

        log.info("成功获取用户统计信息，用户ID:{}", userId);
        return userStatsVo;
    }

    @Override
    public UserVo getUser(Long userId) {
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }

        UserVo userVo = new UserVo();

        //1.查询基本信息
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("user_id", "username", "avatar_url", "bio")
                .eq("user_id", userId));
        if (user == null) {
            throw new IsArgumentException(ErrorCode.USER_NOT_FOUND_ERROR.getHttpStatusCode(), "用户不存在");
        }
        BeanUtils.copyProperties(user, userVo);

        //2.查询扩展信息
        UserStats userStats = userStatsMapper.selectById(userId);
        if (userStats == null) {
            throw new IsArgumentException(ErrorCode.USER_STATS_NOT_FOUND_ERROR.getHttpStatusCode(), "用户统计信息不存在");
        }
        BeanUtils.copyProperties(userStats, userVo, "update_time", "user_id");

        log.info("成功获取用户信息，用户ID:{}", userId);
        return userVo;
    }

    @Override
    public Boolean updateUser(UserUpdateDto userUpdateDto) {
        if (userUpdateDto.getUserId() == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }
        User user = userMapper.selectById(userUpdateDto.getUserId());
        BeanUtils.copyProperties(userUpdateDto, user, getNullPropertyNames(userUpdateDto));

        //发送消息
        userMessageProducer.sendFastMessage("user", String.format("用户信息更新成功，用户ID=%s", userUpdateDto.getUserId()));
        log.info("成功更新用户信息，用户ID:{}", userUpdateDto.getUserId());
        return userMapper.updateById(user) > 0;

    }

    @Override
    public Boolean logout(UserIdDto userIdDto) {
        return null;
    }

    @Override
    public Boolean updatePassword(UserChangePasswordDto userChangePasswordDto) {
        if (userChangePasswordDto.getUserId() == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }
        User user = userMapper.selectById(userChangePasswordDto.getUserId());
        if (!passwordEncoder.matches(userChangePasswordDto.getOldPassword(), user.getPassword())) {
            throw new IsArgumentException(ErrorCode.USER_PASSWORD_ERROR.getHttpStatusCode(), "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(userChangePasswordDto.getNewPassword()));

        userMessageProducer.sendFastMessage("user", String.format("用户密码更新成功，用户ID=%s", userChangePasswordDto.getUserId()));
        log.info("成功更新用户密码，用户ID:{}", userChangePasswordDto.getUserId());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public Boolean removeById(UserIdDto userIdeDto) {
        return null;
    }

    @Override
    public UserInfoVo getUserInfo(Long userId) {
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID不能为空");
        }
        UserInfoVo userInfoVo = new UserInfoVo();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IsArgumentException(ErrorCode.USER_NOT_EXISTS.getHttpStatusCode(), "用户不存在");
        }
        userInfoVo.setUserName(user.getUsername());
        userInfoVo.setAvatar(user.getAvatarUrl());
        return userInfoVo;
    }

    @Override
    /**
     * 获取用户头像URL
     */
    public String getAvatar(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || StringUtil.isEmpty(user.getAvatarUrl())) {
            // 返回默认头像或空字符串
            return "";
        }
        // 如果avatarUrl已经是完整的URL，直接返回
        if (user.getAvatarUrl().startsWith("http")) {
            return user.getAvatarUrl();
        }
        // 否则生成预签名URL（有效期1小时）
        try {
            return storageService.getPreSignedUrl("avatars", user.getAvatarUrl(), 3, TimeUnit.HOURS);
        } catch (Exception e) {
            // 如果生成预签名URL失败，返回原始URL或空字符串

            return "";
        }
    }

    /**
     * 更新用户头像
     */
    @Override
    public Boolean updateAvatar(Long userId, MultipartFile avatar) {
        try {
            // 1. 验证用户存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new IsArgumentException(ErrorCode.USER_NOT_EXISTS.getHttpStatusCode(), "用户不存在");
            }

            // 2. 验证文件类型
            String contentType = avatar.getContentType();
            if (!isValidImageType(contentType)) {
                throw new IsArgumentException(ErrorCode.INVALID_FILE_TYPE.getHttpStatusCode(), "文件类型错误");
            }

            // 3. 验证文件大小（限制为10MB）
            if (avatar.getSize() > 10 * 1024 * 1024) {
                throw new IsArgumentException(ErrorCode.FILE_TOO_LARGE.getHttpStatusCode(), "文件过大");
            }

            // 4. 保存旧头像的对象键，以便后续删除
            String oldAvatarObjectKey = user.getAvatarUrl();

            // 5. 生成唯一的对象键（避免文件名冲突）
            String fileExtension = getFileExtension(avatar.getOriginalFilename());
            String objectKey = generateObjectKey(userId, fileExtension);

            // 6. 上传到Minio
            String storedObjectKey = storageService.upload(avatar, "avatars", objectKey);

            // 7. 更新数据库（存储对象键，不存储完整URL）
            user.setAvatarUrl(storedObjectKey);
            userMapper.updateById(user);

            // 8. 删除旧头像文件（如果存在）
            if (oldAvatarObjectKey != null && !oldAvatarObjectKey.isEmpty() && !oldAvatarObjectKey.equals(storedObjectKey)) {
                try {
                    storageService.delete("avatars", oldAvatarObjectKey);
                    log.info("成功删除旧头像文件: {}", oldAvatarObjectKey);
                } catch (Exception e) {
                    log.warn("删除旧头像文件失败: {}", oldAvatarObjectKey, e);
                }
            }

            return true;
        } catch (IsArgumentException e) {
            // 保留业务逻辑异常（如文件过大、类型错误）
            throw e;
        } catch (Exception e) {
            log.error("头像上传系统异常，用户ID: {}, 文件名: {}, 文件大小: {}",
                    userId, avatar.getOriginalFilename(), avatar.getSize(), e);
            throw new IsServiceException(ErrorCode.FILE_UPLOAD_FAILED.getHttpStatusCode(), "文件上传系统异常: " + e.getMessage());
        }
    }

    /**
     * 验证图片类型
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/jpg")
        );
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 生成唯一的对象键
     */
    private String generateObjectKey(Long userId, String extension) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int) (Math.random() * 1000));
        return String.format("avatar_%s_%s_%s.%s", userId, timestamp, random, extension);
    }

    // 获取对象中值为null或空字符串的属性名(搭配BeanUtils.copyProperties方法使用)
    private String[] getNullPropertyNames(Object source) {
        final org.springframework.beans.BeanWrapper src = new org.springframework.beans.BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            // 同时忽略null值和空字符串
            if (srcValue == null || (srcValue instanceof String && ((String) srcValue).isEmpty())) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }

    @Override
    public Boolean followUser(Long targetUserId) {
        if (targetUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "目标用户ID不能为空");
        }
        Long currentUserId = getCurrentUserIdFromToken();
        if (currentUserId.equals(targetUserId)) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "不能关注自己");
        }

        UserRelationship relationship = userRelationshipMapper.selectOne(Wrappers.<UserRelationship>lambdaQuery()
                .eq(UserRelationship::getFollowerId, currentUserId)
                .eq(UserRelationship::getFolloweeId, targetUserId));

        boolean alreadyFollowing = relationship != null && relationship.getFollowType() == FollowType.FOLLOW;
        if (!alreadyFollowing) {
            LocalDateTime now = LocalDateTime.now();
            if (relationship == null) {
                relationship = new UserRelationship();
                relationship.setFollowerId(currentUserId);
                relationship.setFolloweeId(targetUserId);
                relationship.setFollowType(FollowType.FOLLOW);
                relationship.setFollowTime(now);
                relationship.setUpdateTime(now);
                userRelationshipMapper.insert(relationship);
            } else {
                relationship.setFollowType(FollowType.FOLLOW);
                relationship.setUpdateTime(now);
                userRelationshipMapper.update(relationship, Wrappers.<UserRelationship>lambdaQuery()
                        .eq(UserRelationship::getFollowerId, currentUserId)
                        .eq(UserRelationship::getFolloweeId, targetUserId));
            }
            // 更新统计：当前用户关注数 +1，被关注者粉丝数 +1
            adjustFollowStats(currentUserId, targetUserId, 1);
        }
        return true;
    }

    @Override
    public Boolean unfollowUser(Long targetUserId) {
        if (targetUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "目标用户ID不能为空");
        }
        Long currentUserId = getCurrentUserIdFromToken();
        if (currentUserId.equals(targetUserId)) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "不能取关自己");
        }

        UserRelationship relationship = userRelationshipMapper.selectOne(Wrappers.<UserRelationship>lambdaQuery()
                .eq(UserRelationship::getFollowerId, currentUserId)
                .eq(UserRelationship::getFolloweeId, targetUserId));

        boolean wasFollowing = relationship != null && relationship.getFollowType() == FollowType.FOLLOW;
        if (relationship != null && relationship.getFollowType() != FollowType.UNFOLLOW) {
            relationship.setFollowType(FollowType.UNFOLLOW);
            relationship.setUpdateTime(LocalDateTime.now());
            userRelationshipMapper.update(relationship, Wrappers.<UserRelationship>lambdaQuery()
                    .eq(UserRelationship::getFollowerId, currentUserId)
                    .eq(UserRelationship::getFolloweeId, targetUserId));
        }
        if (wasFollowing) {
            adjustFollowStats(currentUserId, targetUserId, -1);
        }
        return true;
    }

    @Override
    public Boolean isFollowing(Long targetUserId) {
        if (targetUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "目标用户ID不能为空");
        }
        Long currentUserId = getCurrentUserIdFromToken();
        UserRelationship relationship = userRelationshipMapper.selectOne(Wrappers.<UserRelationship>lambdaQuery()
                .eq(UserRelationship::getFollowerId, currentUserId)
                .eq(UserRelationship::getFolloweeId, targetUserId));
        return relationship != null && relationship.getFollowType() == FollowType.FOLLOW;
    }

    /**
     * 调整关注/粉丝计数，delta 可为 1 或 -1
     */
    private void adjustFollowStats(Long followerId, Long followeeId, int delta) {
        UserStats followerStats = userStatsMapper.selectById(followerId);
        if (followerStats != null) {
            int newFollowings = Math.max(0, Optional.ofNullable(followerStats.getFollowingsCount()).orElse(0) + delta);
            followerStats.setFollowingsCount(newFollowings);
            userStatsMapper.updateById(followerStats);
        }

        UserStats followeeStats = userStatsMapper.selectById(followeeId);
        if (followeeStats != null) {
            int newFans = Math.max(0, Optional.ofNullable(followeeStats.getFansCount()).orElse(0) + delta);
            followeeStats.setFansCount(newFans);
            userStatsMapper.updateById(followeeStats);
        }
    }

    @Override
    public List<UserVo> getUserRanking(int topN) {
        // 1. 查询统计信息排名前 N 的用户 ID
        List<UserStats> topStats = userStatsMapper.selectList(new QueryWrapper<UserStats>()
                .orderByDesc("likes_count")
                .last("LIMIT " + topN));

        if (CollectionUtils.isEmpty(topStats)) {
            return Collections.emptyList();
        }

        // 2. 批量查询用户信息并组合
        return topStats.stream().map(stats -> {
            UserVo vo = new UserVo();
            User user = userMapper.selectById(stats.getUserId());
            if (user != null) {
                vo.setId(user.getUserId());
                vo.setUsername(user.getUsername());
                vo.setAvatarUrl(user.getAvatarUrl());
                vo.setBio(user.getBio());
            }
            BeanUtils.copyProperties(stats, vo, "update_time", "user_id");
            return vo;
        }).collect(Collectors.toList());
    }
}
