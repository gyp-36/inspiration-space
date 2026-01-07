import { privateApiCall, publicApiCall } from './apiClient'

const unwrapResponse = (response) => {
  if (response && typeof response === 'object' && 'code' in response && 'data' in response) {
    return response.data
  }
  return response
}

// 聊天相关API服务
export const chatService = {
  // 会话管理
  async createPrivateChat(targetUserId) {
    const res = await privateApiCall(`/chat/private/${targetUserId}`, 'POST')
    return unwrapResponse(res)
  },

  async createGroupChat(createGroupRequestDto) {
    const res = await privateApiCall('/chat/group', 'POST', createGroupRequestDto)
    return unwrapResponse(res)
  },

  async uploadGroupAvatar(sessionId, avatarFile) {
    const formData = new FormData()
    formData.append('file', avatarFile)
    const res = await privateApiCall(`/chat/group/${sessionId}/upload`, 'POST', formData)
    return unwrapResponse(res)
  },

  async joinGroupChat(sessionId) {
    const res = await privateApiCall(`/chat/group/${sessionId}/join`, 'POST')
    return unwrapResponse(res)
  },

  async leaveGroupChat(sessionId) {
    const res = await privateApiCall(`/chat/group/${sessionId}/leave`, 'POST')
    return unwrapResponse(res)
  },

  async deleteChatSession(sessionId) {
    const res = await privateApiCall(`/chat/session/${sessionId}`, 'DELETE')
    return unwrapResponse(res)
  },

  async markSessionAsRead(sessionId) {
    const res = await privateApiCall(`/chat/session/${sessionId}/read`, 'PUT')
    return unwrapResponse(res)
  },

  // 会话列表
  async getChatSessions() {
    const res = await privateApiCall('/chat/sessions', 'GET')
    return unwrapResponse(res)
  },

  // 群聊管理
  async getGroupMembers(sessionId) {
    const res = await privateApiCall(`/chat/group/${sessionId}/members`, 'GET')
    return unwrapResponse(res)
  },

  async getGroupInfo(sessionId) {
    const res = await privateApiCall(`/chat/group/${sessionId}`, 'GET')
    return unwrapResponse(res)
  },

  async publishGroupNotice(sessionId, noticeContent) {
    const res = await privateApiCall(`/chat/group/${sessionId}/publish`, 'POST', noticeContent)
    return unwrapResponse(res)
  },

  async transferGroupOwner(sessionId, newOwnerId) {
    const res = await privateApiCall(
      `/chat/group/${sessionId}/transfer?newOwnerId=${newOwnerId}`,
      'POST'
    )
    return unwrapResponse(res)
  },

  async kickGroupMember(sessionId, userIds) {
    const res = await privateApiCall(`/chat/group/${sessionId}/kick`, 'DELETE', userIds)
    return unwrapResponse(res)
  },

  async dissolveGroup(sessionId) {
    const res = await privateApiCall(`/chat/group/${sessionId}/dissolve`, 'POST')
    return unwrapResponse(res)
  },

  async inviteToGroup(sessionId, userIds) {
    const res = await privateApiCall(`/chat/group/${sessionId}/invite`, 'POST', userIds)
    return unwrapResponse(res)
  },

  async updateGroupInfo(sessionId, groupData) {
    const res = await privateApiCall(`/chat/group/${sessionId}/update`, 'POST', groupData)
    return unwrapResponse(res)
  },

  async publishGroupNotice(sessionId, notice) {
    const res = await privateApiCall(`/chat/group/${sessionId}/notice`, 'POST', notice)
    return unwrapResponse(res)
  },

  // 消息管理
  async getChatMessages(sessionId, page = 1, size = 20) {
    const res = await privateApiCall(
      `/chat/messages/${sessionId}?page=${page}&size=${size}`,
      'GET'
    )
    return unwrapResponse(res)
  },

  async sendFileMessage(sessionId, fileInfo, msgType = 'FILE') {
    const res = await privateApiCall('/chat/message', 'POST', {
      sessionId,
      content: fileInfo.url,
      msgType,
      fileName: fileInfo.name,
      fileSize: fileInfo.size
    })
    return unwrapResponse(res)
  },

  async sendMessage(sessionId, content, msgType = 'TEXT') {
    const res = await privateApiCall('/chat/message', 'POST', {
      sessionId,
      content,
      msgType
    })
    return unwrapResponse(res)
  },

  // 兼容旧命名
  async sendChatMessage(sessionId, content, msgType = 'TEXT') {
    return this.sendMessage(sessionId, content, msgType)
  },

  async markMessageAsRead(messageId) {
    const res = await privateApiCall(`/chat/message/${messageId}/read`, 'PUT')
    return unwrapResponse(res)
  },

  async recallMessage(messageId) {
    const res = await privateApiCall(`/chat/message/${messageId}/recall`, 'PUT')
    return unwrapResponse(res)
  },

  // 未读消息统计
  async getUnreadCount() {
    const res = await privateApiCall('/chat/unread/count', 'GET')
    return unwrapResponse(res)
  },

  // 文件上传
  async uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await privateApiCall('/chat/file/upload', 'POST', formData)
    return unwrapResponse(res)
  }
}

// WebSocket连接管理
export class ChatWebSocket {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 1000
    this.messageHandlers = new Map()
    this.connectionStatus = 'disconnected'
  }

  connect(token) {
    return new Promise((resolve, reject) => {
      try {
        // 确保 token 被正确编码
        const encodedToken = encodeURIComponent(token)
        
        // 根据环境变量或当前 host 动态构建 WebSocket 地址
        const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
        let wsUrl
        
        if (baseUrl.startsWith('http')) {
          // 开发环境: baseUrl 通常是 http://localhost:8080/client
          wsUrl = baseUrl.replace('http', 'ws') + '/ws'
        } else {
          // 生产环境: 直接使用 Nginx 配置的 /client/ws 路径
          const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
          const host = window.location.host
          wsUrl = `${protocol}//${host}/client/ws`
        }

        console.log('正在连接 WebSocket:', wsUrl)
        this.ws = new WebSocket(`${wsUrl}?token=${encodedToken}`)
        
        this.ws.onopen = () => {
          console.log('WebSocket连接已建立')
          this.connectionStatus = 'connected'
          this.reconnectAttempts = 0
          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data)
            this.handleMessage(message)
          } catch (error) {
            console.error('解析WebSocket消息失败:', error)
          }
        }

        this.ws.onclose = () => {
          console.log('WebSocket连接已关闭')
          this.connectionStatus = 'disconnected'
          this.attemptReconnect(token)
        }

        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error)
          this.connectionStatus = 'error'
          reject(error)
        }
      } catch (error) {
        reject(error)
      }
    })
  }

  handleMessage(message) {
    // 根据消息类型分发处理
    const handler = this.messageHandlers.get(message.type)
    if (handler) {
      handler(message)
    } else {
      console.log('收到未处理的消息类型:', message.type, message)
    }
  }

  onMessage(type, handler) {
    this.messageHandlers.set(type, handler)
  }

  sendMessage(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
      return true
    } else {
      console.error('WebSocket未连接，无法发送消息')
      return false
    }
  }

  attemptReconnect(token) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect(token).catch(error => {
          console.error('重新连接失败:', error)
        })
      }, this.reconnectDelay * this.reconnectAttempts)
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.messageHandlers.clear()
    this.connectionStatus = 'disconnected'
  }

  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}
