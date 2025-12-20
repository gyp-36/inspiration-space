import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatService, ChatWebSocket } from '@/services/chatService'
import { ElMessage } from 'element-plus'

// 聊天状态管理 - 优化版（更准确地反映后端数据模型）
export const useChatStore = defineStore('chat', () => {
  // ========== 状态管理 ==========
  
  // 会话列表 - 严格按照后端ChatSessionVO结构
  const sessions = ref([])
  
  // 当前选中的会话
  const currentSession = ref(null)
  
  // 会话成员映射 (sessionId -> members[])
  const sessionMembers = ref(new Map())
  
  // 会话消息映射 (sessionId -> messages[])
  const sessionMessages = ref(new Map())
  
  // 消息分页信息映射 (sessionId -> {page, size, total, hasMore})
  const messagePagination = ref(new Map())
  
  // WebSocket连接
  const websocket = ref(null)
  
  // 连接状态: 'disconnected', 'connecting', 'connected', 'error', 'reconnecting'
  const connectionStatus = ref('disconnected')
  
  // 未读消息总数
  const totalUnreadCount = ref(0)
  
  // 用户在线状态映射 (userId -> isOnline)
  const userOnlineStatus = ref(new Map())
  
  // 正在加载的状态
  const loading = ref({
    sessions: false,
    messages: false,
    sending: false,
    members: false
  })

  // ========== 计算属性 ==========
  
  // 排序后的会话列表（按最后消息时间倒序）
  const sortedSessions = computed(() => {
    return [...sessions.value].sort((a, b) => {
      const timeA = new Date(a.lastMessageTime || a.updatedAt || 0).getTime()
      const timeB = new Date(b.lastMessageTime || b.updatedAt || 0).getTime()
      return timeB - timeA // 最新的在前
    })
  })
  
  // 私聊会话列表
  const privateSessions = computed(() => {
    return sessions.value.filter(session => session.sessionType === 'PRIVATE')
  })
  
  // 群聊会话列表
  const groupSessions = computed(() => {
    return sessions.value.filter(session => session.sessionType === 'GROUP')
  })
  
  // 当前会话的消息列表
  const currentMessages = computed(() => {
    if (!currentSession.value) return []
    return sessionMessages.value.get(currentSession.value.sessionId) || []
  })
  
  // 当前会话的成员列表
  const currentSessionMembers = computed(() => {
    if (!currentSession.value) return []
    return sessionMembers.value.get(currentSession.value.sessionId) || []
  })
  
  // 当前会话的未读消息数
  const currentSessionUnreadCount = computed(() => {
    if (!currentSession.value) return 0
    return currentSession.value.unreadCount || 0
  })
  
  // 当前用户ID
  const currentUserId = computed(() => {
    return localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null
  })
  
  // 当前用户是否为群主
  const isCurrentUserGroupOwner = computed(() => {
    if (!currentSession.value || currentSession.value.sessionType !== 'GROUP') return false
    return currentSession.value.createdBy === currentUserId.value
  })

  // ========== 核心方法 ==========
  
  // 初始化WebSocket连接
  const initializeWebSocket = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        ElMessage.error('请先登录')
        return false
      }
      
      connectionStatus.value = 'connecting'
      websocket.value = new ChatWebSocket()
      
      // 注册消息处理器
      websocket.value.onMessage('CHAT_MESSAGE', handleIncomingMessage)
      websocket.value.onMessage('READ_RECEIPT', handleReadReceipt)
      websocket.value.onMessage('MESSAGE_RECALL', handleMessageRecall)
      websocket.value.onMessage('GROUP_JOIN', handleGroupJoin)
      websocket.value.onMessage('GROUP_LEAVE', handleGroupLeave)
      websocket.value.onMessage('GROUP_DISSOLVE', handleGroupDissolve)
      websocket.value.onMessage('GROUP_NOTICE', handleGroupNotice)
      websocket.value.onMessage('USER_ONLINE', handleUserOnline)
      websocket.value.onMessage('USER_OFFLINE', handleUserOffline)
      
      await websocket.value.connect(token)
      connectionStatus.value = 'connected'
      
      ElMessage.success('聊天服务连接成功')
      return true
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      connectionStatus.value = 'error'
      ElMessage.error('聊天服务连接失败')
      return false
    }
  }
  
  // 加载会话列表
  const loadSessions = async () => {
    loading.value.sessions = true
    try {
      const response = await chatService.getChatSessions()
      
      // 严格按照后端返回的数据结构
      if (response && Array.isArray(response)) {
        sessions.value = response.map(session => ({
          sessionId: session.sessionId,
          sessionName: session.sessionName,
          sessionAvatar: session.sessionAvatar,
          sessionType: session.sessionType, // PRIVATE or GROUP
          description: session.description,
          requireApproval: session.requireApproval,
          createdBy: session.createdBy,
          sessionStatus: session.sessionStatus,
          createdAt: session.createdAt,
          updatedAt: session.updatedAt,
          // 前端扩展字段
          lastMessage: session.lastMessage || '',
          lastMessageTime: session.lastMessageTime,
          lastMessageType: session.lastMessageType || 'TEXT',
          unreadCount: session.unreadCount || 0,
          isOnline: session.isOnline || false
        }))
        
        // 计算总未读数
        totalUnreadCount.value = sessions.value.reduce((sum, session) => {
          return sum + (session.unreadCount || 0)
        }, 0)
      } else {
        sessions.value = []
        totalUnreadCount.value = 0
      }
      
    } catch (error) {
      console.error('加载会话列表失败:', error)
      ElMessage.error('加载会话列表失败')
      sessions.value = []
      totalUnreadCount.value = 0
    } finally {
      loading.value.sessions = false
    }
  }
  
  // 选择会话
  const selectSession = async (session) => {
    if (!session || !session.sessionId) {
      console.error('无效的会话对象', session)
      return
    }
    
    currentSession.value = session
    
    // 并行加载消息和成员
    await Promise.all([
      loadSessionMessages(session.sessionId),
      loadSessionMembers(session.sessionId)
    ])
    
    // 清空未读消息
    if (session.unreadCount > 0) {
      await markSessionAsRead(session.sessionId)
    }
  }
  
  // 加载会话成员
  const loadSessionMembers = async (sessionId) => {
    if (!sessionId) return
    
    loading.value.members = true
    try {
      const response = await chatService.getGroupMembers(sessionId)
      
      if (response && Array.isArray(response)) {
        sessionMembers.value.set(sessionId, response.map(member => ({
          userId: member.userId,
          userName: member.userName,
          userAvatar: member.userAvatar,
          role: member.role, // NORMAL, ADMIN, OWNER
          joinedAt: member.joinedAt,
          isOnline: member.isOnline || false
        })))
      } else {
        sessionMembers.value.set(sessionId, [])
      }
      
    } catch (error) {
      console.error('加载会话成员失败:', error)
      sessionMembers.value.set(sessionId, [])
    } finally {
      loading.value.members = false
    }
  }
  
  // 加载会话消息
  const loadSessionMessages = async (sessionId, page = 1, size = 20) => {
    if (!sessionId) return
    
    loading.value.messages = true
    try {
      const response = await chatService.getChatMessages(sessionId, page, size)
      
      if (response && response.records) {
        const messages = response.records.map(msg => ({
          messageId: msg.messageId,
          sessionId: msg.sessionId,
          senderId: msg.senderId,
          content: msg.content,
          msgType: msg.msgType, // TEXT, IMAGE, FILE, SYSTEM, GROUP_NOTICE
          fileUrl: msg.fileUrl,
          fileName: msg.fileName,
          fileSize: msg.fileSize,
          status: msg.status, // NORMAL, RECALLED, DELIVERED
          sentAt: msg.sentAt,
          updatedAt: msg.updatedAt,
          // 前端扩展字段
          isRead: msg.isRead || false,
          senderName: msg.senderName,
          senderAvatar: msg.senderAvatar
        }))
        
        // 初始化消息映射
        if (!sessionMessages.value.has(sessionId)) {
          sessionMessages.value.set(sessionId, [])
        }
        
        // 更新分页信息
        messagePagination.value.set(sessionId, {
          page: response.current || page,
          size: response.size || size,
          total: response.total || 0,
          hasMore: (response.current * response.size) < response.total
        })
        
        const existingMessages = sessionMessages.value.get(sessionId)
        if (page === 1) {
          // 第一页，替换所有消息
          sessionMessages.value.set(sessionId, messages)
        } else {
          // 后续页，追加到前面（历史消息）
          sessionMessages.value.set(sessionId, [...messages, ...existingMessages])
        }
        
      } else {
        sessionMessages.value.set(sessionId, [])
        messagePagination.value.set(sessionId, {
          page: 1,
          size: size,
          total: 0,
          hasMore: false
        })
      }
      
    } catch (error) {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败')
      
      // 确保有默认值
      if (!sessionMessages.value.has(sessionId)) {
        sessionMessages.value.set(sessionId, [])
      }
    } finally {
      loading.value.messages = false
    }
  }
  
  // 加载更多消息
  const loadMoreMessages = async () => {
    if (!currentSession.value) return
    
    const sessionId = currentSession.value.sessionId
    const pagination = messagePagination.value.get(sessionId)
    
    if (!pagination || !pagination.hasMore) return
    
    const nextPage = pagination.page + 1
    await loadSessionMessages(sessionId, nextPage)
  }
  
  // 发送消息
  const sendMessage = async (content, msgType = 'TEXT', fileInfo = null) => {
    if (!currentSession.value) {
      ElMessage.error('请先选择一个会话')
      return null
    }
    
    if (!content || !content.trim()) {
      if (msgType === 'TEXT') {
        ElMessage.error('消息内容不能为空')
        return null
      }
    }
    
    loading.value.sending = true
    
    try {
      // 创建本地消息对象（乐观更新）
      const tempMessageId = Date.now()
      const localMessage = {
        messageId: tempMessageId,
        sessionId: currentSession.value.sessionId,
        senderId: currentUserId.value,
        content: content ? content.trim() : (fileInfo ? fileInfo.url : ''),
        msgType: msgType,
        fileUrl: fileInfo ? fileInfo.url : null,
        fileName: fileInfo ? fileInfo.name : null,
        fileSize: fileInfo ? fileInfo.size : null,
        status: 'NORMAL',
        sentAt: new Date().toISOString(),
        // 前端状态
        localStatus: 'SENDING',
        isOwnMessage: true
      }
      
      // 添加到本地消息列表
      if (!sessionMessages.value.has(currentSession.value.sessionId)) {
        sessionMessages.value.set(currentSession.value.sessionId, [])
      }
      const messages = sessionMessages.value.get(currentSession.value.sessionId)
      messages.push(localMessage)
      
      // 发送到服务器
      let response
      if (msgType === 'TEXT') {
        response = await chatService.sendMessage(
          currentSession.value.sessionId,
          content.trim(),
          msgType
        )
      } else if (msgType === 'IMAGE' || msgType === 'FILE') {
        response = await chatService.sendFileMessage(
          currentSession.value.sessionId,
          fileInfo,
          msgType
        )
      }
      
      // 更新消息ID
      const serverMessageId = response
      const messageIndex = messages.findIndex(msg => msg.messageId === tempMessageId)
      if (messageIndex !== -1) {
        messages[messageIndex].messageId = serverMessageId
        messages[messageIndex].localStatus = 'SENT'
      }
      
      // 更新会话的最后消息
      currentSession.value.lastMessage = content ? content.trim() : `[${msgType}]`
      currentSession.value.lastMessageTime = new Date().toISOString()
      currentSession.value.lastMessageType = msgType
      
      return serverMessageId
      
    } catch (error) {
      console.error('发送消息失败:', error)
      ElMessage.error('发送消息失败')
      
      // 标记消息为失败
      const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
      const failedMessage = messages.find(msg => msg.localStatus === 'SENDING')
      if (failedMessage) {
        failedMessage.localStatus = 'FAILED'
      }
      
      return null
    } finally {
      loading.value.sending = false
    }
  }

  // 上传文件并返回文件信息
  const uploadFileAndGetInfo = async (file) => {
    if (!file) return null
    try {
      const url = await chatService.uploadFile(file)
      if (!url) {
        ElMessage.error('文件上传失败')
        return null
      }
      return {
        url,
        name: file.name,
        size: file.size
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      ElMessage.error('文件上传失败')
      return null
    }
  }
  
  // 标记会话已读
  const markSessionAsRead = async (sessionId) => {
    if (!sessionId) return
    
    try {
      // 获取该会话的所有未读消息
      const messages = sessionMessages.value.get(sessionId) || []
      const unreadMessages = messages.filter(msg => 
        msg.senderId !== currentUserId.value && 
        msg.status === 'DELIVERED' && 
        !msg.isRead
      )
      
      if (unreadMessages.length === 0) return
      
      // 批量标记已读
      for (const message of unreadMessages) {
        await markAsRead(message.messageId)
      }
      
      // 更新会话未读数
      const session = sessions.value.find(s => s.sessionId === sessionId)
      if (session) {
        const previousUnreadCount = session.unreadCount || 0
        session.unreadCount = 0
        totalUnreadCount.value = Math.max(0, totalUnreadCount.value - previousUnreadCount)
      }
      
    } catch (error) {
      console.error('标记会话已读失败:', error)
    }
  }
  
  // 标记消息已读
  const markAsRead = async (messageId) => {
    if (!messageId) return
    
    try {
      await chatService.markMessageAsRead(messageId)
      
      // 更新本地消息状态
      if (currentSession.value) {
        const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
        const message = messages.find(msg => msg.messageId === messageId)
        if (message) {
          message.isRead = true
          message.status = 'DELIVERED'
        }
      }
      
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
    if (!messageId) return
    
    try {
      await chatService.recallMessage(messageId)
      
      // 更新本地消息状态
      if (currentSession.value) {
        const messages = sessionMessages.value.get(currentSession.value.sessionId) || []
        const message = messages.find(msg => msg.messageId === messageId)
        if (message) {
          message.msgType = 'RECALL'
          message.content = '消息已撤回'
          message.status = 'RECALLED'
        }
      }
      
    } catch (error) {
      console.error('撤回消息失败:', error)
      ElMessage.error('撤回消息失败')
    }
  }
  
  // 创建私聊会话
  const createPrivateChat = async (targetUserId) => {
    if (!targetUserId) {
      ElMessage.error('目标用户ID不能为空')
      return null
    }
    
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
    if (!groupData || !groupData.name) {
      ElMessage.error('群聊名称不能为空')
      return null
    }
    
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
  
  // 加入群聊
  const joinGroupChat = async (sessionId) => {
    if (!sessionId) return false
    
    try {
      const result = await chatService.joinGroupChat(sessionId)
      
      if (result) {
        // 重新加载会话列表和成员
        await Promise.all([
          loadSessions(),
          loadSessionMembers(sessionId)
        ])
        
        ElMessage.success('加入群聊成功')
      }
      
      return result
    } catch (error) {
      console.error('加入群聊失败:', error)
      ElMessage.error('加入群聊失败')
      return false
    }
  }
  
  // 退出群聊
  const leaveGroupChat = async (sessionId) => {
    if (!sessionId) return false
    
    try {
      const result = await chatService.leaveGroupChat(sessionId)
      
      if (result) {
        // 从会话列表中移除
        const index = sessions.value.findIndex(s => s.sessionId === sessionId)
        if (index !== -1) {
          sessions.value.splice(index, 1)
        }
        
        // 清空相关缓存
        sessionMessages.value.delete(sessionId)
        sessionMembers.value.delete(sessionId)
        messagePagination.value.delete(sessionId)
        
        // 如果当前会话就是这个，清空当前会话
        if (currentSession.value && currentSession.value.sessionId === sessionId) {
          currentSession.value = null
        }
        
        ElMessage.success('退出群聊成功')
      }
      
      return result
    } catch (error) {
      console.error('退出群聊失败:', error)
      ElMessage.error('退出群聊失败')
      return false
    }
  }
  
  // 解散群聊
  const dissolveGroupChat = async (sessionId) => {
    if (!sessionId) return false
    
    try {
      const result = await chatService.dissolveGroup(sessionId)
      
      if (result) {
        // 从会话列表中移除
        const index = sessions.value.findIndex(s => s.sessionId === sessionId)
        if (index !== -1) {
          sessions.value.splice(index, 1)
        }
        
        // 清空相关缓存
        sessionMessages.value.delete(sessionId)
        sessionMembers.value.delete(sessionId)
        messagePagination.value.delete(sessionId)
        
        // 如果当前会话就是这个，清空当前会话
        if (currentSession.value && currentSession.value.sessionId === sessionId) {
          currentSession.value = null
          ElMessage.warning('当前群聊已被解散')
        }
      }
      
      return result
    } catch (error) {
      console.error('解散群聊失败:', error)
      ElMessage.error('解散群聊失败')
      return false
    }
  }

  // 踢出群成员
  const kickGroupMember = async (sessionId, userId) => {
    if (!sessionId || !userId) return false
    
    try {
      const result = await chatService.kickGroupMember(sessionId, [userId])
      
      if (result) {
        // 重新加载成员列表
        await loadSessionMembers(sessionId)
        ElMessage.success('成员已踢出')
      }
      
      return result
    } catch (error) {
      console.error('踢出成员失败:', error)
      ElMessage.error('踢出成员失败')
      return false
    }
  }

  // 转让群主
  const transferGroupOwner = async (sessionId, newOwnerId) => {
    if (!sessionId || !newOwnerId) return false
    
    try {
      const result = await chatService.transferGroupOwner(sessionId, newOwnerId)
      
      if (result) {
        // 重新加载会话信息和成员列表
        await Promise.all([
          loadSessions(),
          loadSessionMembers(sessionId)
        ])
        ElMessage.success('群主已转让')
      }
      
      return result
    } catch (error) {
      console.error('转让群主失败:', error)
      ElMessage.error('转让群主失败')
      return false
    }
  }

  // 发布群公告
  const publishGroupNotice = async (sessionId, noticeContent) => {
    if (!sessionId || !noticeContent || !noticeContent.trim()) {
      ElMessage.error('公告内容不能为空')
      return false
    }
    
    try {
      const result = await chatService.publishGroupNotice(sessionId, noticeContent.trim())
      
      if (result) {
        ElMessage.success('群公告发布成功')
        
        // 添加系统消息到当前会话
        if (currentSession.value && currentSession.value.sessionId === sessionId) {
          const noticeMessage = {
            messageId: Date.now(),
            sessionId: sessionId,
            senderId: 0, // 系统消息
            content: noticeContent.trim(),
            msgType: 'GROUP_NOTICE',
            sentAt: new Date().toISOString()
          }
          
          if (!sessionMessages.value.has(sessionId)) {
            sessionMessages.value.set(sessionId, [])
          }
          const messages = sessionMessages.value.get(sessionId)
          messages.push(noticeMessage)
        }
      }
      
      return result
    } catch (error) {
      console.error('发布群公告失败:', error)
      ElMessage.error('发布群公告失败')
      return false
    }
  }

  // ========== WebSocket消息处理器 ==========
  
  // 处理新消息
  const handleIncomingMessage = (message) => {
    const { sessionId, data } = message
    
    if (!sessionId || !data) return
    
    // 标准化消息格式
    const normalizedMessage = {
      messageId: data.messageId,
      sessionId: data.sessionId,
      senderId: data.senderId,
      content: data.content,
      msgType: data.msgType,
      fileUrl: data.fileUrl,
      fileName: data.fileName,
      fileSize: data.fileSize,
      status: data.status || 'NORMAL',
      sentAt: data.sentAt || new Date().toISOString(),
      senderName: data.senderName,
      senderAvatar: data.senderAvatar,
      isRead: data.isRead || false
    }
    
    // 如果当前会话就是消息所属的会话，添加到消息列表
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      if (!sessionMessages.value.has(sessionId)) {
        sessionMessages.value.set(sessionId, [])
      }
      const messages = sessionMessages.value.get(sessionId)
      messages.push(normalizedMessage)
      
      // 自动标记已读（因为是当前会话）
      if (normalizedMessage.senderId !== currentUserId.value) {
        markAsRead(normalizedMessage.messageId)
      }
    } else {
      // 否则增加未读计数
      const session = sessions.value.find(s => s.sessionId === sessionId)
      if (session) {
        session.unreadCount = (session.unreadCount || 0) + 1
        totalUnreadCount.value++
        
        // 播放提示音或显示通知
        playMessageSound()
      }
    }
    
    // 更新会话的最后消息信息
    const session = sessions.value.find(s => s.sessionId === sessionId)
    if (session) {
      session.lastMessage = normalizedMessage.content
      session.lastMessageTime = normalizedMessage.sentAt
      session.lastMessageType = normalizedMessage.msgType
    }
  }
  
  // 处理已读回执
  const handleReadReceipt = (message) => {
    const { messageId, sessionId } = message
    
    // 更新消息状态为已读
    if (sessionMessages.value.has(sessionId)) {
      const messages = sessionMessages.value.get(sessionId)
      const msg = messages.find(m => m.messageId === messageId)
      if (msg) {
        msg.isRead = true
        msg.status = 'DELIVERED'
      }
    }
  }
  
  // 处理消息撤回
  const handleMessageRecall = (message) => {
    const { messageId, sessionId } = message
    
    if (sessionMessages.value.has(sessionId)) {
      const messages = sessionMessages.value.get(sessionId)
      const msg = messages.find(m => m.messageId === messageId)
      if (msg) {
        msg.msgType = 'RECALL'
        msg.content = '消息已撤回'
        msg.status = 'RECALLED'
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
        sentAt: new Date().toISOString()
      }
      
      if (!sessionMessages.value.has(sessionId)) {
        sessionMessages.value.set(sessionId, [])
      }
      const messages = sessionMessages.value.get(sessionId)
      messages.push(joinMessage)
    }
    
    // 重新加载成员列表
    loadSessionMembers(sessionId)
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
        sentAt: new Date().toISOString()
      }
      
      if (!sessionMessages.value.has(sessionId)) {
        sessionMessages.value.set(sessionId, [])
      }
      const messages = sessionMessages.value.get(sessionId)
      messages.push(leaveMessage)
    }
    
    // 重新加载成员列表
    loadSessionMembers(sessionId)
  }
  
  // 处理群解散
  const handleGroupDissolve = (message) => {
    const { sessionId } = message
    
    // 从会话列表中移除
    const index = sessions.value.findIndex(s => s.sessionId === sessionId)
    if (index !== -1) {
      sessions.value.splice(index, 1)
    }
    
    // 清空相关缓存
    sessionMessages.value.delete(sessionId)
    sessionMembers.value.delete(sessionId)
    messagePagination.value.delete(sessionId)
    
    // 如果当前会话就是这个，清空当前会话
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      currentSession.value = null
      ElMessage.warning('当前群聊已被解散')
    }
  }
  
  // 处理群公告
  const handleGroupNotice = (message) => {
    const { sessionId, notice } = message
    
    // 显示系统消息
    if (currentSession.value && currentSession.value.sessionId === sessionId) {
      const noticeMessage = {
        messageId: Date.now(),
        sessionId: sessionId,
        senderId: 0, // 系统消息
        content: `群公告: ${notice}`,
        msgType: 'GROUP_NOTICE',
        sentAt: new Date().toISOString()
      }
      
      if (!sessionMessages.value.has(sessionId)) {
        sessionMessages.value.set(sessionId, [])
      }
      const messages = sessionMessages.value.get(sessionId)
      messages.push(noticeMessage)
    }
  }
  
  // 处理用户上线
  const handleUserOnline = (message) => {
    const { userId } = message
    userOnlineStatus.value.set(userId, true)
    
    // 更新会话中的在线状态
    sessions.value.forEach(session => {
      if (session.sessionType === 'PRIVATE' && 
          (session.targetUserId === userId || session.otherUserId === userId)) {
        session.isOnline = true
      }
    })
  }
  
  // 处理用户下线
  const handleUserOffline = (message) => {
    const { userId } = message
    userOnlineStatus.value.set(userId, false)
    
    // 更新会话中的在线状态
    sessions.value.forEach(session => {
      if (session.sessionType === 'PRIVATE' && 
          (session.targetUserId === userId || session.otherUserId === userId)) {
        session.isOnline = false
      }
    })
  }
  
  // 播放消息提示音
  const playMessageSound = () => {
    // 这里可以添加实际的提示音播放逻辑
    // 例如：new Audio('/sounds/message.mp3').play()
  }

  // ========== 工具方法 ==========
  
  // 获取会话类型文本
  const getSessionTypeText = (sessionType) => {
    const typeMap = {
      'PRIVATE': '私聊',
      'GROUP': '群聊'
    }
    return typeMap[sessionType] || '未知'
  }
  
  // 获取消息类型文本
  const getMessageTypeText = (msgType) => {
    const typeMap = {
      'TEXT': '文本',
      'IMAGE': '图片',
      'FILE': '文件',
      'SYSTEM': '系统',
      'GROUP_NOTICE': '群公告',
      'RECALL': '撤回'
    }
    return typeMap[msgType] || '未知'
  }
  
  // 获取用户角色文本
  const getUserRoleText = (role) => {
    const roleMap = {
      'NORMAL': '成员',
      'ADMIN': '管理员',
      'OWNER': '群主'
    }
    return roleMap[role] || '成员'
  }
  
  // 清理状态
  const clearState = () => {
    sessions.value = []
    currentSession.value = null
    sessionMembers.value.clear()
    sessionMessages.value.clear()
    messagePagination.value.clear()
    userOnlineStatus.value.clear()
    totalUnreadCount.value = 0
    connectionStatus.value = 'disconnected'
    
    if (websocket.value) {
      websocket.value.disconnect()
      websocket.value = null
    }
  }

  return {
    // 状态
    sessions,
    currentSession,
    sessionMembers,
    sessionMessages,
    messagePagination,
    connectionStatus,
    totalUnreadCount,
    userOnlineStatus,
    loading,
    
    // 计算属性
    sortedSessions,
    privateSessions,
    groupSessions,
    currentMessages,
    currentSessionMembers,
    currentSessionUnreadCount,
    currentUserId,
    isCurrentUserGroupOwner,
    
    // 核心方法
    initializeWebSocket,
    loadSessions,
    selectSession,
    loadSessionMessages,
    loadMoreMessages,
    loadSessionMembers,
    sendMessage,
    uploadFileAndGetInfo,
    markAsRead,
    recallMessage,
    createPrivateChat,
    createGroupChat,
    joinGroupChat,
    leaveGroupChat,
    dissolveGroupChat,
    kickGroupMember,
    transferGroupOwner,
    publishGroupNotice,
    markSessionAsRead,
    
    // 工具方法
    getSessionTypeText,
    getMessageTypeText,
    getUserRoleText,
    clearState
  }
})
