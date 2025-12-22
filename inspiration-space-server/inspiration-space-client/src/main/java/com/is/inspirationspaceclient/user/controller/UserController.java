package com.is.inspirationspaceclient.user.controller;


import com.is.inspirationspaceclient.user.model.dto.*;
import com.is.inspirationspaceclient.user.model.vo.*;
import com.is.inspirationspaceclient.user.service.UserService;
import com.is.inspirationspacecommon.annotation.RequireRole;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 用户表 前端控制器
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口", description = "用户接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")

    public ApiResponse<Boolean> register(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        return ApiResponse.ok(userService.register(userRegisterDto));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<UserLoginVo> login(@Valid @RequestBody UserLoginDto userloginDto) {
        return ApiResponse.ok(userService.login(userloginDto));
    }


    @Operation(summary = "获取用户基本信息")
    @GetMapping("/getProfile/{userId}")
    @RequireRole("client-user")
    public ApiResponse<UserProfileVo> getUserProfileInfo(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUserProfile(userId));
    }

    @Operation(summary = "获取用户扩展信息")
    @GetMapping("/getExtend/{userId}")
    @RequireRole("client-user")
    public ApiResponse<UserExtendVo> getUserExtendInfo(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUserExtendInfo(userId));
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/getStats/{userId}")
    @RequireRole("client-user")
    public ApiResponse<UserStatsVo> getUserStatsInfo(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUserStatsInfo(userId));
    }

    @Operation(summary = "展示用户信息")
    @GetMapping("/get/{userId}")

    public ApiResponse<UserVo> getUser(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUser(userId));
    }

    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public ApiResponse<Boolean> updateUserInfo(@RequestBody UserUpdateDto userUpdateDto) {
        return ApiResponse.ok(userService.updateUser(userUpdateDto));
    }


    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@RequestBody UserIdDto userIdDto) {
        return ApiResponse.ok(userService.logout(userIdDto));
    }



    @Operation(summary = "用户界面修改密码")
    @PatchMapping("/changePassword")
    public ApiResponse<Boolean> updatePassword(@RequestBody UserChangePasswordDto userChangePasswordDto) {
        return ApiResponse.ok(userService.updatePassword(userChangePasswordDto));
    }

    @Operation(summary = "注销账户")
    @DeleteMapping("/delete")
    public ApiResponse<Boolean> deleteAccount(@RequestBody UserIdDto userIdeDto) {
        return ApiResponse.ok(userService.removeById(userIdeDto));
    }

    @Operation(summary = "获取用户信息(渲染帖子)")
    @GetMapping("/getUserInfo/{userId}")
    public ApiResponse<UserInfoVo> getUserInfo(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUserInfo(userId));
    }

    @Operation(summary = "获取头像")
    @GetMapping("/getAvatar/{userId}")
    public ApiResponse<String> getAvatar(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getAvatar(userId));
    }

    @Operation(summary = "更新头像")
    @PostMapping("/updateAvatar/{userId}")
    public ApiResponse<Boolean> updateAvatar(@PathVariable Long userId, @RequestParam("avatar") MultipartFile avatar) {
        return ApiResponse.ok(userService.updateAvatar(userId, avatar));
    }

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{userId}")
    public ApiResponse<Boolean> followUser(@PathVariable Long userId) {
        return ApiResponse.ok(userService.followUser(userId));
    }

    @Operation(summary = "取消关注用户")
    @PostMapping("/unfollow/{userId}")
    public ApiResponse<Boolean> unfollowUser(@PathVariable Long userId) {
        return ApiResponse.ok(userService.unfollowUser(userId));
    }

    @Operation(summary = "是否已关注用户")
    @GetMapping("/isFollowing/{userId}")
    public ApiResponse<Boolean> isFollowing(@PathVariable Long userId) {
        return ApiResponse.ok(userService.isFollowing(userId));
    }

    @Operation(summary = "获取用户排行榜")
    @GetMapping("/ranking")
    public ApiResponse<List<UserVo>> getUserRanking(@RequestParam(defaultValue = "10") int topN) {
        return ApiResponse.ok(userService.getUserRanking(topN));
    }
}