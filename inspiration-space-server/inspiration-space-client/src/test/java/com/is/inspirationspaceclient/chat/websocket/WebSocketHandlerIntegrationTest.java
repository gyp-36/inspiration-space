package com.is.inspirationspaceclient.chat.websocket;

import com.is.inspirationspaceclient.chat.mapper.ChatMessageMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMapper;
import com.is.inspirationspaceclient.chat.mapper.ChatSessionMemberMapper;
import com.is.inspirationspacecommon.redis.RedisCache;
import com.is.inspirationspacecommon.util.JwtUtil;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocketHandler 集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
class WebSocketHandlerIntegrationTest {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private RedisCache redisCache;

    @MockitoBean
    private ConnectionManager connectionManager;

    @MockitoBean
    private ChatMessageMapper chatMessageMapper;

    @MockitoBean
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @MockitoBean
    private ChatSessionMapper chatSessionMapper;

    @MockitoBean
    private ChatSessionMemberMapper chatSessionMemberMapper;

    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        session = mock(WebSocketSession.class);
    }

    /**
     * 测试场景一：token缺失或无效 -> 应当返回错误信息并关闭连接
     */
    @Test
    void testAfterConnectionEstablished_TokenInvalid_ShouldCloseWithErrorMessage() throws Exception {
        // Arrange
        when(session.getUri()).thenReturn(new URI("ws://localhost?token=invalid_token"));

        TextMessage expectedMessage = new TextMessage("无效token");

        // Act
        webSocketHandler.afterConnectionEstablished(session);

        // Assert
        verify(session).sendMessage(expectedMessage);
        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    /**
     * 测试场景二：token合法但userId为空 -> 应当返回错误信息并关闭连接
     */
    @Test
    void testAfterConnectionEstablished_UserIdIsNull_ShouldCloseWithErrorMessage() throws Exception {
        // Arrange
        when(session.getUri()).thenReturn(new URI("ws://localhost?token=valid_token"));

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(anyString())).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(anyString())).thenReturn(null);

            // Act
            webSocketHandler.afterConnectionEstablished(session);

            // Assert
            verify(session).sendMessage(new TextMessage("userId为空"));
            verify(session).close(CloseStatus.POLICY_VIOLATION);
        }
    }

    /**
     * 测试场景三：所有条件正常 -> 成功注册会话并设置Redis在线状态
     */
    @Test
    void testAfterConnectionEstablished_ValidTokenAndUserId_ShouldRegisterAndSetOnlineStatus() throws Exception {
        // Arrange
        when(session.getUri()).thenReturn(new URI("ws://localhost?token=valid_token"));

        Map<String, Object> attributes = new HashMap<>();
        when(session.getAttributes()).thenReturn(attributes);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.validateToken(anyString())).thenReturn(true);
            jwtUtilMock.when(() -> JwtUtil.getUserIdFromToken(anyString())).thenReturn(123L);
            jwtUtilMock.when(() -> JwtUtil.getUsernameFromToken(anyString())).thenReturn("testUser");

            // Mock RedisKeyBuild behavior
            when(redisCache.set(any(), eq(123L), eq(10L), any())).thenReturn(true);

            // Act
            webSocketHandler.afterConnectionEstablished(session);

            // Assert
            assertEquals(123L, attributes.get("userId"));
            assertEquals("testUser", attributes.get("username"));
            verify(connectionManager).register(123L, session);
            verify(redisCache).set(any(), eq(123L), eq(10L), any());
        }
    }
}
