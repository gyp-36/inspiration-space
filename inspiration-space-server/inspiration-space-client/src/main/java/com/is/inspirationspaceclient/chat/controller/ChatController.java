package com.is.inspirationspaceclient.chat.controller;

import com.is.inspirationspaceclient.chat.model.dto.CreateGroupRequestDto;
import com.is.inspirationspaceclient.chat.model.dto.SendMessageRequestDto;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.vo.ChatSessionVO;
import com.is.inspirationspaceclient.chat.model.vo.GroupInfoVO;
import com.is.inspirationspaceclient.chat.model.vo.GroupMemberVO;
import com.is.inspirationspaceclient.chat.service.ChatMessageService;
import com.is.inspirationspaceclient.chat.service.ChatSessionService;
import com.is.inspirationspacecommon.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天模块")
@AllArgsConstructor
public class ChatController {

    private ChatMessageService chatMessageService;

    private ChatSessionService chatsessionService;


    // ==================== 会话管理 ====================

    @PostMapping("/private/{targetUserId}")
    @Operation(summary = "创建或获取私聊会话")
    public ApiResponse<Long> createOrGetPrivateChat(@PathVariable Long targetUserId,
                                                    @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.createOrGetPrivateChat(targetUserId, token));
    }

    @PostMapping("/group")
    @Operation(summary = "创建群聊")
    public ApiResponse<Long> createGroupChat(@RequestBody CreateGroupRequestDto createGroupRequestDto,
                                             @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.createGroupChat(createGroupRequestDto, token));
    }

    @PostMapping("/group/{sessionId}/upload")
    @Operation(summary = "上传群头像")
    public ApiResponse<Boolean> uploadGroupAvatar(@PathVariable Long sessionId,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.uploadGroupAvatar(sessionId, file, token));
    }

    @PostMapping("/group/{sessionId}/join")
    @Operation(summary = "加入群聊")
    public ApiResponse<Boolean> joinGroupChat(@PathVariable Long sessionId,
                                              @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.joinGroupChat(sessionId, token));
    }

    @PostMapping("/group/{sessionId}/leave")
    @Operation(summary = "退出群聊")
    public ApiResponse<Boolean> leaveGroupChat(@PathVariable Long sessionId,
                                               @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.leaveGroupChat(sessionId, token));
    }

    @DeleteMapping("/group/{sessionId}/kick")
    @Operation(summary = "踢出群聊")
    public ApiResponse<Boolean> kickFromGroup(@PathVariable Long sessionId,
                                              @RequestBody List<Long> userIds,
                                              @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.kickFromGroup(sessionId, userIds, token));
    }


    @PostMapping("/group/{sessionId}/invite")
    @Operation(summary = "邀请用户加入群聊")
    public ApiResponse<Boolean> inviteToGroup(@PathVariable Long sessionId,
                                              @RequestBody List<Long> userIds,
                                              @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.inviteToGroup(sessionId, userIds, token));
    }


    // ==================== 会话列表 ====================

    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表")
    public ApiResponse<List<ChatSessionVO>> getChatSessions(@RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.getChatSessions(token));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除会话")
    public ApiResponse<Boolean> deleteChatSession(@PathVariable Long sessionId,
                                                  @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.deleteChatSession(sessionId, token));
    }

    @PutMapping("/session/{sessionId}/read")
    @Operation(summary = "标记会话为已读")
    public ApiResponse<Boolean> markSessionAsRead(@PathVariable Long sessionId,
                                                 @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.markSessionAsRead(sessionId, token));
    }


    // ==================== 群聊管理 ====================

    @GetMapping("/group/{sessionId}/members")
    @Operation(summary = "获取群成员列表")
    public ApiResponse<List<GroupMemberVO>> getGroupMembers(@PathVariable Long sessionId,
                                                            @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.getGroupMembers(sessionId, token));
    }

    @GetMapping("/group/{sessionId}")
    @Operation(summary = "获取群详情")
    public ApiResponse<GroupInfoVO> getGroupInfo(@PathVariable Long sessionId,
                                                 @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.getGroupInfo(sessionId, token));
    }

   @PostMapping("/group/{sessionId}/publish")
   @Operation(summary = "发布群公告")
   public ApiResponse<Boolean> publishGroupNotice(@PathVariable Long sessionId,
                                                  @RequestBody String groupNotice,
                                                  @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.publishGroupNotice(sessionId, groupNotice, token));
    }

    @PostMapping("/group/{sessionId}/transfer")
    @Operation(summary = "转让群主")
    public ApiResponse<Boolean> transferGroupOwner(@PathVariable Long sessionId,
                                                   @RequestParam Long newOwnerId,
                                                   @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.transferGroupOwner(sessionId, newOwnerId, token));
    }

    @PostMapping("/group/{sessionId}/dissolve")
    @Operation(summary = "解散群聊")
    public ApiResponse<Boolean> dissolveGroup(@PathVariable Long sessionId,
                                           @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.dissolveGroup(sessionId, token));
    }

    @PostMapping("/group/{sessionId}/update")
    @Operation(summary = "修改群信息")
    public ApiResponse<Boolean> updateGroupInfo(@PathVariable Long sessionId,
                                                @RequestBody CreateGroupRequestDto updateGroupRequestDto,
                                                @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatsessionService.updateGroupInfo(sessionId, updateGroupRequestDto, token));
    }

    // ==================== 消息管理 ====================
    
    @GetMapping("/messages/{sessionId}")
    @Operation(summary = "获取会话消息历史")
    public ApiResponse<List<ChatMessage>> getMessageHistory(@PathVariable Long sessionId,
                                                           @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatMessageService.getMessageHistory(sessionId, token));
    }

    @PostMapping("/message")
    @Operation(summary = "发送消息")
    public ApiResponse<Long> sendMessage(@RequestBody SendMessageRequestDto sendMessageRequestDto,
                                         @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatMessageService.sendMessage(sendMessageRequestDto, token));
    }

    @PutMapping("/message/{messageId}/read")
    @Operation(summary = "标记消息为已读")
    public ApiResponse<Boolean> markMessageAsRead(@PathVariable Long messageId,
                                                 @RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatMessageService.markMessageAsRead(messageId, token));
    }

    // ==================== 其他功能 ====================

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息总数")
    public ApiResponse<Integer> getUnreadCount(@RequestHeader("Authorization") String token) {
        return ApiResponse.ok(chatMessageService.getUnreadCount(token));
    }
}