package com.is.inspirationspaceclient.chat.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspaceclient.chat.model.dto.CreateGroupRequestDto;
import com.is.inspirationspaceclient.chat.model.entity.ChatMessage;
import com.is.inspirationspaceclient.chat.model.entity.ChatSession;
import com.is.inspirationspaceclient.chat.model.entity.ChatSessionMember;
import com.is.inspirationspaceclient.chat.model.entity.enums.*;
import com.is.inspirationspaceclient.chat.model.vo.ChatSessionVO;
import com.is.inspirationspaceclient.chat.model.vo.GroupMemberVO;
import com.is.inspirationspaceclient.chat.websocket.ConnectionManager;

import com.is.inspirationspaceclient.user.mapper.UserMapper;
import com.is.inspirationspaceclient.user.model.entity.User;
import com.is.inspirationspaceclient.user.model.vo.UserInfoVo;
import com.is.inspirationspaceclient.user.service.UserServiceImpl;
import com.is.inspirationspacecommon.config.StorageService;
import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.exception.IsArgumentException;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserServiceImpl userServiceImpl;
    private final ChatSessionMapper chatSessionMapper;

    private final ChatSessionMemberMapper chatSessionMemberMapper;

    private SnowflakeIdGenerator snowflakeIdGenerator;

    private StorageService storageService;
    private final UserMapper userMapper;
    private final ConnectionManager connectionManager;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 验证用户token并获取用户ID
     */
    private Long validateAndGetUserId(String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效的用户token");
        }
        return userId;
    }

    /**
     * 验证用户是否是会话成员
     */
    private ChatSessionMember validateSessionMember(Long sessionId, Long userId) {
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", userId)
        );
        if (member == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户不是会话成员");
        }
        return member;
    }

    /**
     * 验证用户是否是群主
     */
    private void validateGroupOwner(Long sessionId, Long userId) {
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .select("role")
                        .eq("session_id", sessionId)
                        .eq("user_id", userId)
        );
        if (member == null || member.getRole() != MsgRole.OWNER) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "只有群主才能执行此操作");
        }
    }

    @Transactional
    @Override
    public Long createOrGetPrivateChat(Long targetUserId, String token) {
        Long currentUserId = validateAndGetUserId(token);
        
        // 检查是否尝试与自己创建私聊
        if (currentUserId.equals(targetUserId)) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "不能与自己创建私聊会话");
        }

        // 查询当前用户参与的所有私聊会话ID
        List<Long> userSessions = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("session_id")
                        .eq("user_id", currentUserId)
        ).stream().map(ChatSessionMember::getSessionId).collect(Collectors.toList());

        if (!userSessions.isEmpty()) {
            // 检查目标用户是否也在这些私聊会话中
            ChatSession existingSession = chatSessionMapper.selectOne(
                    new QueryWrapper<ChatSession>()
                            .eq("session_type", SessionType.PRIVATE)
                            .eq("session_status", SessionStatus.NORMAL)
                            .in("session_id", userSessions)
                            .exists("SELECT 1 FROM chat_session_members WHERE session_id = chat_sessions.session_id AND user_id = " + targetUserId)
            );

            if (existingSession != null) {
                return existingSession.getSessionId();
            }
        }

        // 创建新私聊会话
        Long sessionId = snowflakeIdGenerator.nextId();
        ChatSession newSession = new ChatSession();
        newSession.setSessionId(sessionId);
        newSession.setSessionType(SessionType.PRIVATE);
        newSession.setSessionName("私聊会话");
        newSession.setSessionAvatar("");
        newSession.setCreatedBy(currentUserId);
        newSession.setSessionStatus(SessionStatus.NORMAL);
        newSession.setCreatedAt(LocalDateTime.now());
        chatSessionMapper.insert(newSession);

        // 添加会话成员
        ChatSessionMember member1 = new ChatSessionMember();
        member1.setSessionId(sessionId);
        member1.setUserId(currentUserId);
        member1.setRole(MsgRole.NORMAL);
        member1.setJoinedAt(LocalDateTime.now());
        chatSessionMemberMapper.insert(member1);

        ChatSessionMember member2 = new ChatSessionMember();
        member2.setSessionId(sessionId);
        member2.setUserId(targetUserId);
        member2.setRole(MsgRole.NORMAL);
        member2.setJoinedAt(LocalDateTime.now());
        chatSessionMemberMapper.insert(member2);

        return sessionId;
    }

    @Transactional
    @Override
    public Long createGroupChat(CreateGroupRequestDto createGroupRequestDto, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        // 1.创建群聊会话
        Long sessionId = snowflakeIdGenerator.nextId();
        ChatSession group = new ChatSession();
        BeanUtils.copyProperties(createGroupRequestDto, group);
        group.setSessionId(sessionId);
        group.setSessionType(SessionType.GROUP);
        group.setCreatedBy(currentUserId);
        group.setSessionStatus(SessionStatus.NORMAL);
        chatSessionMapper.insert(group);

        // 2.添加群聊成员
        ChatSessionMember member = new ChatSessionMember();
        member.setSessionId(sessionId);
        member.setUserId(currentUserId);
        member.setRole(MsgRole.OWNER);
        member.setJoinedAt(LocalDateTime.now());
        chatSessionMemberMapper.insert(member);


        return sessionId;
    }

    @Transactional
    @Override
    public Boolean joinGroupChat(Long sessionId, String token) {
        Long currentUserId = validateAndGetUserId(token);
        
        //1.判断群聊是否有效
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || session.getSessionType() != SessionType.GROUP || session.getSessionStatus() != SessionStatus.NORMAL) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "群聊不存在或已失效");
        }
        
        //2.检查是否已经是群成员
        ChatSessionMember existingMember = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (existingMember != null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "已经是群成员");
        }

        //3.是否需要审核
        if (session.getRequireApproval() == IsApproval.NEED_APPROVAL) {
            // 创建入群申请消息
            ChatMessage applyMessage = new ChatMessage();
            applyMessage.setMessageId(snowflakeIdGenerator.nextId());
            applyMessage.setSessionId(sessionId);
            applyMessage.setSenderId(currentUserId);
            applyMessage.setContent("申请加入群聊");
            applyMessage.setMsgType(MsgType.GROUP_APPLY);
            applyMessage.setStatus(MsgStatus.NORMAL);
            chatMessageMapper.insert(applyMessage);
            
            // 发送消息到消息队列，通知群主和管理员审核
            try {
                rabbitTemplate.convertAndSend("group.apply", JSON.toJSONString(applyMessage));
                log.info("群聊申请消息已发送到消息队列: {}", applyMessage.getMessageId());
            } catch (Exception e) {
                log.error("发送群聊申请消息到消息队列失败: {}", e.getMessage());
            }
            
            return true; // 申请已提交，等待审核
        } else {
            // 直接添加群聊成员
            ChatSessionMember newMember = new ChatSessionMember();
            newMember.setSessionId(sessionId);
            newMember.setUserId(currentUserId);
            newMember.setRole(MsgRole.NORMAL);
            newMember.setJoinedAt(LocalDateTime.now());
            chatSessionMemberMapper.insert(newMember);

            // 发送入群欢迎消息
            ChatMessage welcomeMessage = new ChatMessage();
            welcomeMessage.setMessageId(snowflakeIdGenerator.nextId());
            welcomeMessage.setSessionId(sessionId);
            welcomeMessage.setSenderId(currentUserId);
            welcomeMessage.setContent("加入了群聊");
            welcomeMessage.setMsgType(MsgType.GROUP_JOIN);
            welcomeMessage.setStatus(MsgStatus.NORMAL);
            chatMessageMapper.insert(welcomeMessage);
            
            // 广播消息给所有群成员
            broadcastMessageToSession(sessionId, welcomeMessage);
            
            return true;
        }
    }

    @Transactional
    @Override
    public Boolean leaveGroupChat(Long sessionId, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        //1.判断是否群成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }
        //2.判断是否群主
        if (member.getRole() == MsgRole.OWNER) {
            //清空所有群成员
            chatSessionMemberMapper.delete(
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
            );
            //删除群聊
            chatSessionMapper.deleteById(sessionId);
            //批量删除消息(可异步)
            chatMessageMapper.delete(
                    new QueryWrapper<ChatMessage>()
                            .eq("session_id", sessionId)
            );
        } else {
            //删除群成员
            chatSessionMemberMapper.delete(
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
                            .eq("user_id", currentUserId)
            );
            //批量删除消息(可异步)
            chatMessageMapper.delete(
                    new QueryWrapper<ChatMessage>()
                            .eq("session_id", sessionId)
                            .eq("sender_id", currentUserId)
            );
        }


        return true;
    }

    @Transactional
    @Override
    public Boolean kickFromGroup(Long sessionId, List<Long> userIds, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }

        //1.判断是否群成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null || member.getRole() == MsgRole.NORMAL) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }

        //2.执行删除操作
        chatSessionMemberMapper.delete(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .in("user_id", userIds)
        );

        return true;
    }


    @Transactional
    @Override
    public Boolean inviteToGroup(Long sessionId, List<Long> userIds, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        //1.判断是否群成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }
        //2.批量执行添加操作
        chatSessionMemberMapper.batchInsertMembers(sessionId, userIds);

        return true;
    }

    @Override
    public List<ChatSessionVO> getChatSessions(String token) {
        Long currentUserId = validateAndGetUserId(token);

        // 获取用户参与的所有会话ID
        List<Long> sessionIds = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("session_id")
                        .eq("user_id", currentUserId)
        ).stream().map(ChatSessionMember::getSessionId).collect(Collectors.toList());

        if (sessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询这些会话的详细信息
        List<ChatSession> sessions = chatSessionMapper.selectList(
                new QueryWrapper<ChatSession>()
                        .in("session_id", sessionIds)
                        .eq("session_status", SessionStatus.NORMAL)
                        .orderByDesc("created_at")  // 按创建时间倒序排列
        );

        return sessions.stream().map(session -> {
            ChatSessionVO sessionVO = new ChatSessionVO();
            sessionVO.setSessionId(session.getSessionId());
            sessionVO.setSessionName(session.getSessionName());
            sessionVO.setSessionAvatar(session.getSessionAvatar());
            return sessionVO;
        }).collect(Collectors.toList());
    }
    
    private String getLastMessage(Long sessionId) {
        ChatMessage lastMessage = chatMessageMapper.selectOne(
                new QueryWrapper<ChatMessage>()
                        .eq("session_id", sessionId)
                        .orderByDesc("sent_at")
                        .last("LIMIT 1")
        );
        return lastMessage != null ? lastMessage.getContent() : "";
    }
    
    private Integer getUnreadCountForSession(Long sessionId, Long userId) {
        Long count = chatMessageMapper.selectCount(
                new QueryWrapper<ChatMessage>()
                        .eq("session_id", sessionId)
                        .ne("sender_id", userId)
                        .eq("status", MsgStatus.DELIVERED)
        );
        return count.intValue();
    }

    @Transactional
    @Override
    public Boolean deleteChatSession(Long sessionId, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }

        //1.判断是否是会话成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "不是会话成员，无法删除");
        }

        //2.获取会话信息
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "会话不存在");
        }

        //3.根据会话类型处理删除逻辑
        if (session.getSessionType() == SessionType.PRIVATE) {
            // 私聊会话：删除成员记录
            chatSessionMemberMapper.delete(
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
                            .eq("user_id", currentUserId)
            );
            
            // 如果会话没有其他成员，删除整个会话
            Long remainingMembers = chatSessionMemberMapper.selectCount(
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
            );
            
            if (remainingMembers == 0) {
                // 删除会话
                chatSessionMapper.deleteById(sessionId);
                // 删除该会话的所有消息（可异步处理）
                chatMessageMapper.delete(
                        new QueryWrapper<ChatMessage>()
                                .eq("session_id", sessionId)
                );
            }
            
        } else if (session.getSessionType() == SessionType.GROUP) {
            // 群聊会话：检查权限
            if (member.getRole() == MsgRole.OWNER) {
                throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "群主不能直接删除会话，需要先转让群主或解散群聊");
            }
            
            // 普通成员退出群聊
            chatSessionMemberMapper.delete(
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
                            .eq("user_id", currentUserId)
            );
            
            // 发送退出群聊消息
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setMessageId(snowflakeIdGenerator.nextId());
            leaveMessage.setSessionId(sessionId);
            leaveMessage.setSenderId(currentUserId);
            leaveMessage.setContent("退出了群聊");
            leaveMessage.setMsgType(MsgType.GROUP_LEAVE);
            leaveMessage.setStatus(MsgStatus.NORMAL);
            chatMessageMapper.insert(leaveMessage);
            
            // 广播退出消息给其他群成员
            broadcastMessageToSession(sessionId, leaveMessage);
        }
        
        return true;
    }

    @Override
    public List<GroupMemberVO> getGroupMembers(Long sessionId, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        //1.判断是否是群成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }
        //2.获取群成员信息
        List<ChatSessionMember> members = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("user_id", "role")
                        .eq("session_id", sessionId)
        );

        // 提取所有用户ID并批量查询用户信息
        List<Long> userIds = members.stream()
                .map(ChatSessionMember::getUserId)
                .collect(Collectors.toList());

        List<User> allUsers = userMapper.selectList(
                new QueryWrapper<User>()
                        .select("user_id", "username", "avatar_url")
                        .in("user_id", userIds)
        );

        // 将用户信息转换为Map便于查找
        Map<Long, User> userMap = allUsers.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        // 组装返回数据
        List<GroupMemberVO> membersVO = members.stream().map(allmembers -> {
            User user = userMap.get(allmembers.getUserId());
            GroupMemberVO memberVO = new GroupMemberVO();
            memberVO.setUserId(allmembers.getUserId());
            memberVO.setRole(allmembers.getRole());
            if (user != null) {
                memberVO.setUserName(user.getUsername());
                memberVO.setAvatar(user.getAvatarUrl());
            }
            return memberVO;
        }).collect(Collectors.toList());

        return membersVO;
    }

    @Override
    public Boolean publishGroupNotice(Long sessionId, String groupNotice, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        //1.判断是否是群成员
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .select("role")
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null || member.getRole() != MsgRole.OWNER) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }
        //2.发布群公告
        ChatMessage message = new ChatMessage();
        message.setMessageId(snowflakeIdGenerator.nextId());
        message.setSessionId(sessionId);
        message.setSenderId(currentUserId);
        message.setContent(groupNotice);
        message.setMsgType(MsgType.GROUP_NOTICE);
        message.setStatus(MsgStatus.NORMAL);
        chatMessageMapper.insert(message);
        //3.发送消息入队列

        return true;
    }

    @Override
    public Boolean transferGroupOwner(Long sessionId, Long newOwnerId, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        //1.判断是否是群主
        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .select("role")
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null || member.getRole() != MsgRole.OWNER) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }
        //2.判断新群主是否是群成员
        ChatSessionMember newOwnerMember = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .select("role")
                        .eq("session_id", sessionId)
                        .eq("user_id", newOwnerId)
        );
        if (newOwnerMember == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "新群主不是群成员");
        }
        //3.更新信息(旧的变成普通成员，新的成群主)
        try {
            // 获取新旧群主的用户信息
            User oldOwnerUser = userMapper.selectById(currentUserId);
            User newOwnerUser = userMapper.selectById(newOwnerId);
            
            // 更新原群主为普通成员
            ChatSessionMember oldOwner = new ChatSessionMember();
            oldOwner.setRole(MsgRole.NORMAL);
            chatSessionMemberMapper.update(oldOwner,
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
                            .eq("user_id", currentUserId)
            );

            // 更新新群主为群主
            ChatSessionMember newOwner = new ChatSessionMember();
            newOwner.setRole(MsgRole.OWNER);
            chatSessionMemberMapper.update(newOwner,
                    new QueryWrapper<ChatSessionMember>()
                            .eq("session_id", sessionId)
                            .eq("user_id", newOwnerId)
            );
            
            // 发送群主转让通知消息
            ChatMessage transferMessage = new ChatMessage();
            transferMessage.setMessageId(snowflakeIdGenerator.nextId());
            transferMessage.setSessionId(sessionId);
            transferMessage.setSenderId(currentUserId);
            String transferContent = String.format("%s 将群主转让给 %s", 
                oldOwnerUser != null ? oldOwnerUser.getUsername() : "原群主",
                newOwnerUser != null ? newOwnerUser.getUsername() : "新群主");
            transferMessage.setContent(transferContent);
            transferMessage.setMsgType(MsgType.GROUP_TRANSFER);
            transferMessage.setStatus(MsgStatus.NORMAL);
            chatMessageMapper.insert(transferMessage);
            
            // 广播群主转让消息给所有群成员
            broadcastMessageToSession(sessionId, transferMessage);
            
            return true;
        } catch (Exception e) {
            throw new IsArgumentException(ErrorCode.INTERNAL_ERROR.getHttpStatusCode(), "转让群主失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean uploadGroupAvatar(Long sessionId, MultipartFile file, String token) {
        Long currentUserId = JwtUtil.getUserIdFromToken(token);
        if (currentUserId == null) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "用户ID为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "文件为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "只支持图片文件上传");
        }

        // 验证文件大小(例如限制为5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "文件大小不能超过5MB");
        }

        ChatSessionMember member = chatSessionMemberMapper.selectOne(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
                        .eq("user_id", currentUserId)
        );
        if (member == null || member.getRole() == MsgRole.NORMAL) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "无效操作");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "group_avatar_" + sessionId + "_" + System.currentTimeMillis() + fileExtension;

            // 上传到 MinIO
            String bucketName = "group-avatars"; // 指定存储桶名称
            String objectKey = "groups/" + fileName; // 对象键，可包含路径

            // 上传文件到 MinIO
            storageService.upload(file, bucketName, objectKey);

            // 生成访问URL
            String avatarUrl = storageService.getPreSignedUrl(bucketName, objectKey, 31, TimeUnit.DAYS);


            // 更新群聊头像
            ChatSession session = new ChatSession();
            session.setSessionId(sessionId);
            session.setSessionAvatar(avatarUrl);
            chatSessionMapper.updateById(session);

            return true;
        } catch (Exception e) {
            throw new IsArgumentException(ErrorCode.INTERNAL_ERROR.getHttpStatusCode(), "文件上传失败: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public Boolean dissolveGroup(Long sessionId, String token) {
        Long currentUserId = validateAndGetUserId(token);
        
        // 验证用户是否是群主
        validateGroupOwner(sessionId, currentUserId);
        
        // 获取群聊信息
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || session.getSessionType() != SessionType.GROUP) {
            throw new IsArgumentException(ErrorCode.INVALID_PARAMETER_ERROR.getHttpStatusCode(), "群聊不存在");
        }
        
        // 获取群主信息
        User ownerUser = userMapper.selectById(currentUserId);
        
        // 发送群解散通知消息
        ChatMessage dissolveMessage = new ChatMessage();
        dissolveMessage.setMessageId(snowflakeIdGenerator.nextId());
        dissolveMessage.setSessionId(sessionId);
        dissolveMessage.setSenderId(currentUserId);
        String dissolveContent = String.format("%s 解散了群聊", 
            ownerUser != null ? ownerUser.getUsername() : "群主");
        dissolveMessage.setContent(dissolveContent);
        dissolveMessage.setMsgType(MsgType.GROUP_DISSOLVE);
        dissolveMessage.setStatus(MsgStatus.NORMAL);
        chatMessageMapper.insert(dissolveMessage);
        
        // 广播解散消息给所有群成员
        broadcastMessageToSession(sessionId, dissolveMessage);
        
        // 删除所有群成员
        chatSessionMemberMapper.delete(
                new QueryWrapper<ChatSessionMember>()
                        .eq("session_id", sessionId)
        );
        
        // 将会话状态设置为已解散
        ChatSession updatedSession = new ChatSession();
        updatedSession.setSessionId(sessionId);
        updatedSession.setSessionStatus(SessionStatus.DISSOLVED);
        chatSessionMapper.updateById(updatedSession);
        
        // TODO: 异步删除群聊消息（可以放到消息队列中处理）
        // rabbitTemplate.convertAndSend("group.dissolve", sessionId);
        
        return true;
    }


    /**
     * 广播消息给会话的所有成员
     * @param sessionId 会话ID
     * @param message 要广播的消息
     */
    private void broadcastMessageToSession(Long sessionId, ChatMessage message) {
        // 获取会话的所有成员
        List<ChatSessionMember> members = chatSessionMemberMapper.selectList(
                new QueryWrapper<ChatSessionMember>()
                        .select("user_id")
                        .eq("session_id", sessionId)
        );
        
        if (members == null || members.isEmpty()) {
            return;
        }
        
        // 将消息转换为JSON字符串
        String messageJson = JSON.toJSONString(message);
        
        // 向每个在线成员发送消息
        for (ChatSessionMember member : members) {
            Long memberId = member.getUserId();
            // 不发送给消息发送者自己（避免重复通知）
            if (!memberId.equals(message.getSenderId())) {
                connectionManager.sendIfOnline(memberId, session -> {
                    try {
                        session.sendMessage(new org.springframework.web.socket.TextMessage(messageJson));
                        log.info("消息已发送给用户 {}: {}", memberId, message.getContent());
                    } catch (Exception e) {
                        log.error("发送消息给用户 {} 失败: {}", memberId, e.getMessage());
                    }
                });
            }
        }
    }
}

