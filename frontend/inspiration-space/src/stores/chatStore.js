import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatService, ChatWebSocket } from '@/services/chatService'
import { ElMessage } from 'element-plus'

// 定义聊天 Store
export const useChatStore = defineStore('chat', () => {
  // ========== 状态管理 ==========
  
  // 会话列表
  const sessions = ref([])
  
  // 当前选中的会话
  const currentSession = ref(null)
  
  // 会话消息映射 (sessionId -> messages[])
  const sessionMessages = ref(new Map())
  
  // WebSocket连接
  const websocket = ref(null)
  
  // 连接状态
  const connectionStatus = ref('disconnected')
  
  // 未读消息总数
  const totalUnreadCount = ref(0)
  
  // 正在加载的状态
  const loading = ref({
    sessions: false,
    messages: false,
    sending: false
  })

  // ========== 计算属性 ==========
  
  // 过滤后的会话列表（按最后消息时间排序）
  const sortedSessions = computed(() => {
    return [...sessions.value].sort((a, b) => {
      const timeA = new Date(a.lastMessageTime || 0).getTime()
      const timeB = new Date(b.lastMessageTime || 0).getTime()
      return timeB - timeA // 最新的在前
    })
  })
  
  // 当前会话的消息列表
  const currentMessages = computed(() => {
    if (!currentSession.value) return []
    return sessionMessages.value.get(currentSession.value.sessionId) || []
  })
  
  // 当前会话的未读消息数
  const currentSessionUnreadCount = computed(() => {
    if (!currentSession.value) return 0
    return currentSession.value.unreadCount || 0
  })

  // ========== 核心方法 ==========
  
  // 初始化WebSocket连接
  const initializeWebSocket = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.error('请先登录')
        return
      }
      
      websocket.value = new ChatWebSocket()
      
      // 注册消息处理器
      websocket.value.onMessage('CHAT_MESSAGE', handleIncomingMessage)
      websocket.value.onMessage('READ_RECEIPT', handleReadReceipt)
      websocket.value.onMessage('MESSAGE_RECALL', handleMessageRecall)
      websocket.value.onMessage('GROUP_JOIN', handleGroupJoin)
      websocket.value.onMessage('GROUP_LEAVE', handleGroupLeave)
      websocket.value.onMessage('GROUP_DISSOLVE', handleGroupDissolve)
      
      await websocket.value.connect(token)
      connectionStatus.value = 'connected'
      
      ElMessage.success('聊天服务连接成功')
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      connectionStatus.value = 'error'
      ElMessage.error('聊天服务连接失败')
    }
  }
  
  // 加载会话列表
  const loadSessions = async () => {
    loading.value.sessions = true
    try {
      const response = await chatService.getChatSessions()
      sessions.value = response || []
      
      // 计算总未读数
      totalUnreadCount.value = sessions.value.reduce((sum, session) => {
        return sum + (session.unreadCount || 0)
      }, 0)
      
    } catch (error) {
      console.error('加载会话列表失败:', error)
      ElMessage.error('加载会话列表失败')
    } finally {
      loading.value.sessions = false
    }
  }
  
  // 选择会话
  const selectSession = async (session) => {
    currentSession.value = session
    
    // 加载该会话的消息
    await loadSessionMessages(session.sessionId)
    
    // 清空未读消息
    if (session.unreadCount > 0) {
      session.unreadCount = 0
      totalUnreadCount.value = Math.max(0, totalUnreadCount.value - session.unreadCount)
    }
  }
  
  // 加载会话消息
  const loadSessionMessages = async (sessionId, page = 1) => {
    loading.value.messages = true
    try {
      const response = await chatService.getChatMessages(sessionId, page)
      const messages = response.records || []
      
      // 存储到消息映射中
      if (!sessionMessages.value.has(sessionId)) {
        sessionMessages.value.set(sessionId, [])
      }
      
      const existingMessages = sessionMessages.value.get(sessionId)
      if (page === 1) {
        // 第一页，替换所有消息
        sessionMessages.value.set(sessionId, messages)
      } else {
        // 后续页，追加到前面（历史消息）
        sessionMessages.value.set(sessionId, [...messages, ...existingMessages])
      }
      
    } catch (error) {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败')
    } finally {
      loading.value.messages = false
    }
  }
  
  // 发送消息
  const sendMessage = async (content, msgType = 'TEXT') => {
    if (!currentSession.value) {
      ElMessage.error('请先选择一个会话')
      return
    }
    
    if (!content.trim()) {
      ElMessage.error('消息内容不能为空')
      return
    }
    
    loading.value.sending = true
    try {
      // 创建本地消息对象（乐观更新）
      const localMessage = {
        messageId: Date.now(), // 临时ID
        sessionId: currentSession.value.sessionId,
        senderId: localStorage.getItem('userId'),
        content: content.trim(),
        msgType: msgType,
        status: 'SENDING',
        timestamp: new Date().toISOString()
      }
      
      // 添加到本地消息列表
      const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
      messages.push(localMessage)
      sessionMessages.value.set(currentSession.value.sessionId, messages)
      
      // 发送到服务器
      const response = await chatService.sendMessage(
        currentSession.value.sessionId,
        content.trim(),
        msgType
      )
      
      // 更新消息ID
      localMessage.messageId = response
      localMessage.status = 'SENT'
      
      // 更新会话的最后消息
      currentSession.value.lastMessage = content.trim()
      currentSession.value.lastMessageTime = new Date().toISOString()
      
      // 通过WebSocket发送实时消息（如果已连接）
      if (websocket.value && websocket.value.isConnected()) {
        websocket.value.sendMessage({
          type: 'CHAT_MESSAGE',
          sessionId: currentSession.value.sessionId,
          content: content.trim(),
          msgType: msgType
        })
      }
      
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败')
      
      // 标记消息为失败
      const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
      const lastMessage = messages[messages.length - 1]
      if (lastMessage && lastMessage.status === 'SENDING') {
        lastMessage.status = 'FAILED'
      }
    } finally {
      loading.value.sending = false
    }
  }
  
  // 标记消息已读
  const markAsRead = async (messageId) => {
    try {
      await chatService.markMessageAsRead(messageId)
      
      // 通过WebSocket发送已读回执
      if (websocket.value && websocket.value.isConnected()) {
        websocket.value.sendMessage({
          type: 'READ_RECEIPT',
          messageId: messageId
        })
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 撤回消息
  const recallMessage = async (messageId) => {
    try {
      await chatService.recallMessage(messageId)
      
      // 更新本地消息状态
      const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
      const message = messages.find(msg => msg.messageId === messageId)
      if (message) {
        message.msgType = 'RECALL'
        message.content = '消息已撤回'
      }
      
    } catch (error) {
      console.error('撤回消息失败:', error)
      ElMessage.error('撤回消息失败')
    }
  }
  
  // 创建私聊会话
  const createPrivateChat = async (targetUserId) => {
    try {
      const sessionId = await chatService.createPrivateChat(targetUserId)
      
      // 重新加载会话列表
      await loadSessions()
      
      // 选中新创建的会话
      const newSession = sessions.value.find(s => s.sessionId === sessionId)
      if (newSession) {
        await selectSession(newSession)
      }
      
      return sessionId
    } catch (error) {
      console.error('创建私聊失败:', error)
      ElMessage.error('创建私聊失败')
      throw error
    }
  }
  
  // 创建群聊
  const createGroupChat = async (groupData) => {
    try {
      const sessionId = await chatService.createGroupChat(groupData)
      
      // 重新加载会话列表
      await loadSessions()
      
      // 选中新创建的群聊
      const newSession = sessions.value.find(s => s.sessionId === sessionId)
      if (newSession) {
        await selectSession(newSession)
      }
      
      return sessionId
    } catch (error) {
      console.error('创建群聊失败:', error)
      ElMessage.error('创建群聊失败')
      throw error
    }
  }

  // ========== WebSocket消息处理器 ==========
  
  // 处理新消息
  const handleIncomingMessage = (message) => {
    const { sessionId, data } = message
    
    // 如果当前会话就是消息所属的会话，添加到消息列表
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      const messages = sessionMessages.value.get(sessionId) || []
      messages.push(data)
      sessionMessages.value.set(sessionId, messages)
      
      // 自动标记已读
      markAsRead(data.messageId)
    } else {
      // 否则增加未读计数
      const session = sessions.value.find(s => s.sessionId === sessionId)
      if (session) {
        session.unreadCount = (session.unreadCount || 0) + 1
        totalUnreadCount.value++
      }
    }
    
    // 更新会话的最后消息
    const session = sessions.value.find(s => s.sessionId === sessionId)
    if (session) {
      session.lastMessage = data.content
      session.lastMessageTime = data.timestamp
    }
  }
  
  // 处理已读回执
  const handleReadReceipt = (message) => {
    const { messageId } = message
    
    // 更新消息状态为已读
    if (currentSession.value) {
      const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
      const msg = messages.find(m => m.messageId === messageId)
      if (msg) {
        msg.isRead = true
      }
    }
  }
  
  // 处理消息撤回
  const handleMessageRecall = (message) => {
    const { messageId } = message
    
    if (currentSession.value) {
      const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
      const msg = messages.find(m => m.messageId === messageId)
      if (msg) {
        msg.msgType = 'RECALL'
        msg.content = '消息已撤回'
      }
    }
  }
  
  // 处理群成员加入
  const handleGroupJoin = (message) => {
    const { sessionId, userId, userName } = message
    
    // 显示系统消息
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      const joinMessage = {
        messageId: Date.now(),
        sessionId: sessionId,
        senderId: 0, // 系统消息
        content: `${userName} 加入了群聊`,
        msgType: 'SYSTEM',
        timestamp: new Date().toISOString()
      }
      
      const messages = sessionMessages.value.get(sessionId) || []
      messages.push(joinMessage)
      sessionMessages.value.set(sessionId, messages)
    }
  }
  
  // 处理群成员离开
  const handleGroupLeave = (message) => {
    const { sessionId, userId, userName } = message
    
    // 显示系统消息
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      const leaveMessage = {
        messageId: Date.now(),
        sessionId: sessionId,
        senderId: 0, // 系统消息
        content: `${userName} 离开了群聊`,
        msgType: 'SYSTEM',
        timestamp: new Date().toISOString()
      }
      
      const messages = sessionMessages.value.get(sessionId) || []
      messages.push(leaveMessage)
      sessionMessages.value.set(sessionId, messages)
    }
  }
  
  // 处理群解散
  const handleGroupDissolve = (message) => {
    const { sessionId } = message
    
    // 从会话列表中移除
    const index = sessions.value.findIndex(s => s.sessionId === sessionId)
    if (index !== -1) {
      sessions.value.splice(index, 1)
    }
    
    // 清空消息缓存
    sessionMessages.value.delete(sessionId)
    
    // 如果当前会话就是被解散的群，清空当前会话
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      currentSession.value = null
      ElMessage.warning('当前群聊已被解散')
    }
  }

  return {
    // 状态
    sessions,
    currentSession,
    sessionMessages,
    connectionStatus,
    totalUnreadCount,
    loading,
    
    // 计算属性
    sortedSessions,
    currentMessages,
    currentSessionUnreadCount,
    
    // 方法
    initializeWebSocket,
    loadSessions,
    selectSession,
    loadSessionMessages,
    sendMessage,
    markAsRead,
    recallMessage,
    createPrivateChat,
    createGroupChat
  }
})