package com.is.inspirationspaceclient.chat.service;

import com.is.inspirationspaceclient.chat.model.dto.CreateGroupRequestDto;
import com.is.inspirationspaceclient.chat.model.vo.ChatSessionVO;
import com.is.inspirationspaceclient.chat.model.vo.GroupInfoVO;
import com.is.inspirationspaceclient.chat.model.vo.GroupMemberVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatSessionService {
    Long createOrGetPrivateChat(Long targetUserId, String token);

    Long createGroupChat(CreateGroupRequestDto createGroupRequestDto, String token);

    Boolean joinGroupChat(Long sessionId, String token);

    Boolean leaveGroupChat(Long sessionId, String token);

    Boolean kickFromGroup(Long sessionId, List<Long> userIds, String token);


    Boolean inviteToGroup(Long sessionId, List<Long> userIds, String token);

    List<ChatSessionVO> getChatSessions(String token);

    Boolean deleteChatSession(Long sessionId, String token);

    List<GroupMemberVO> getGroupMembers(Long sessionId, String token);

    GroupInfoVO getGroupInfo(Long sessionId, String token);

    Boolean publishGroupNotice(Long sessionId, String groupNotice, String token);

    Boolean transferGroupOwner(Long sessionId, Long newOwnerId, String token);

    Boolean uploadGroupAvatar(Long sessionId, MultipartFile file, String token);

    Boolean dissolveGroup(Long sessionId, String token);

    Boolean updateGroupInfo(Long sessionId, CreateGroupRequestDto updateGroupRequestDto, String token);

    Boolean markSessionAsRead(Long sessionId, String token);
}
