<!-- 聊天面板 - 优化版 -->
<template>
  <div class="chat-panel">
    <!-- 聊天顶部：显示会话信息 -->
    <div v-if="session" class="chat-header">
      <div class="session-info">
        <div class="session-avatar">
          <img :src="sessionAvatar" :alt="sessionName" @error="handleAvatarError" />
          <div v-if="session.unreadCount > 0" class="unread-badge">
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </div>
        </div>
        <div class="session-details">
          <div class="session-name">
            <span class="name-text">{{ sessionName }}</span>
            <div v-if="session.sessionId" class="session-id-wrapper">
              <span class="session-id">({{ session.sessionId }})</span>
              <el-tooltip content="复制ID" placement="top">
                <el-icon class="copy-icon" @click.stop="copyToClipboard(session.sessionId)"><DocumentCopy /></el-icon>
              </el-tooltip>
            </div>
          </div>
          <div class="session-status">
            <span :class="['status-dot', { online: session.isOnline }]"></span>
            {{ sessionStatus }}
          </div>
        </div>
      </div>
      <div class="chat-actions">
        <el-tooltip v-if="session.sessionType === 'GROUP' && isGroupOwner" content="发布群公告" placement="bottom">
          <button class="icon-btn" @click="emit('publish-notice')">
            <el-icon><Bell /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip v-if="session.sessionType === 'GROUP'" content="群成员" placement="bottom">
          <button 
            class="icon-btn" 
            :class="{ active: showSidebar }" 
            @click="$emit('toggle-sidebar')"
          >
            <el-icon><UserFilled /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip content="搜索消息" placement="bottom">
          <button class="icon-btn" @click="showSearchDialog = true"><el-icon><Search /></el-icon></button>
        </el-tooltip>
        <el-dropdown trigger="click" @command="handleAction">
          <button class="icon-btn"><el-icon><MoreFilled /></el-icon></button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="notice" v-if="session.sessionType === 'GROUP' && isGroupOwner">
                <el-icon><Bell /></el-icon> 发布群公告
              </el-dropdown-item>
              <el-dropdown-item command="view-notice" v-if="session.sessionType === 'GROUP'">
                <el-icon><Bell /></el-icon> 查看群公告
              </el-dropdown-item>
              <el-dropdown-item command="invite" v-if="session.sessionType === 'GROUP'">
                <el-icon><Plus /></el-icon> 邀请新成员
              </el-dropdown-item>
              <el-dropdown-item command="edit" v-if="session.sessionType === 'GROUP' && isGroupAdmin">
                <el-icon><Edit /></el-icon> 修改群信息
              </el-dropdown-item>
              <el-dropdown-item command="edit-avatar" v-if="session.sessionType === 'GROUP' && isGroupAdmin">
                <el-icon><Picture /></el-icon> 修改群头像
              </el-dropdown-item>
              <el-dropdown-item command="clear">
                <el-icon><Delete /></el-icon> 清空聊天记录
              </el-dropdown-item>
              <el-dropdown-item command="delete" v-if="session.sessionType === 'PRIVATE'">
                <el-icon><Remove /></el-icon> 删除会话
              </el-dropdown-item>
              <el-dropdown-item command="dissolve" v-if="session.sessionType === 'GROUP' && isGroupOwner">
                <el-icon><CircleClose /></el-icon> 解散群聊
              </el-dropdown-item>
              <el-dropdown-item command="exit" v-if="session.sessionType === 'GROUP' && !isGroupOwner">
                <el-icon><SwitchButton /></el-icon> 退出群聊
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 消息展示区域 -->
    <div class="message-container" ref="messageContainer" @scroll="handleScroll">
      <div v-if="loading.messages" class="loading-messages">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载消息中...</span>
      </div>
      
      <div v-else-if="messages.length === 0" class="empty-messages">
        <div class="empty-state">
          <el-icon><ChatDotRound /></el-icon>
          <p>暂无消息，开始聊天吧！</p>
        </div>
      </div>
      
      <div v-else class="message-list">
        <div v-if="hasMore" class="load-more" @click="$emit('load-more')">
          <span>加载更多消息</span>
        </div>
        
        <div v-for="(message, index) in messages" :key="message.messageId || index" class="message-item">
          <!-- 时间分隔符 -->
          <div v-if="shouldShowTimeDivider(message, messages[index - 1])" class="time-divider">
            <span>{{ formatMessageTime(message.sentAt || message.timestamp) }}</span>
          </div>
          
          <!-- 系统消息 -->
          <div v-if="isSystemMessage(message)" class="system-message">
            <span>{{ message.content }}</span>
          </div>
          
          <!-- 普通消息 -->
          <div v-else :class="['message-wrapper', isOwnMessage(message) ? 'own-message' : 'other-message']" :data-message-id="message.messageId">
            <div class="message-avatar" v-if="!isOwnMessage(message)">
              <img :src="getSenderAvatar(message.senderId)" :alt="getSenderName(message.senderId)" @error="handleAvatarError" />
            </div>
            
            <div class="message-content-wrapper">
              <div class="sender-name" v-if="session.sessionType === 'GROUP' && !isOwnMessage(message)">
                {{ getSenderName(message.senderId) }}
              </div>
              
              <div class="message-bubble" :class="getMessageContentClass(message)">
                <!-- 文本消息 -->
                <div v-if="message.msgType === 'TEXT'" class="text-message">
                  {{ message.content }}
                </div>
                
                <!-- 图片消息 -->
                <div v-else-if="message.msgType === 'IMAGE'" class="image-message">
                  <el-image 
                    :src="message.content" 
                    :preview-src-list="[message.content]"
                    fit="cover"
                    class="chat-image"
                  />
                </div>
                
                <!-- 文件消息 -->
                <div v-else-if="message.msgType === 'FILE'" class="file-message">
                  <div class="file-icon">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="file-info">
                    <div class="file-name">{{ getFileName(message.content) }}</div>
                    <div class="file-size">{{ getFileSize(message.fileSize) }}</div>
                  </div>
                  <el-button link type="primary" :icon="Download" @click="downloadFile(message.content)"></el-button>
                </div>
                
                <!-- 撤回消息 -->
                <div v-else-if="message.msgType === 'RECALL'" class="recall-message">
                  <el-icon><RefreshLeft /></el-icon>
                  <span>{{ message.content }}</span>
                </div>

                <!-- 消息状态和时间 -->
                <div class="message-meta">
                  <span class="message-time-small">{{ formatTimeSmall(message.sentAt || message.timestamp) }}</span>
                  <div class="status-icons" v-if="isOwnMessage(message)">
                    <el-icon v-if="message.status === 'SENDING'" class="is-loading"><Loading /></el-icon>
                    <el-icon v-else-if="message.status === 'FAILED'" class="status-error"><Warning /></el-icon>
                    <el-icon v-else-if="message.isRead" class="status-read"><Check /></el-icon>
                    <el-icon v-else><Check /></el-icon>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="message-avatar" v-if="isOwnMessage(message)">
              <img :src="currentUserAvatar" alt="我" @error="handleAvatarError" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息输入区域 -->
    <div class="input-container">
      <MessageInput
        v-model="inputMessage"
        @send="sendMessage"
        @file-select="handleFileSelect"
        :disabled="loading.sending"
      />
    </div>

    <!-- 搜索消息对话框 -->
    <el-dialog v-model="showSearchDialog" title="搜索消息" width="500px" append-to-body destroy-on-close>
      <div class="search-dialog-content">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索聊天记录..."
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
        <div class="search-results" v-if="searched">
          <div v-if="searchResults.length > 0" class="results-list">
            <div v-for="result in searchResults" :key="result.messageId" class="result-item" @click="jumpToMessage(result)">
              <div class="result-header">
                <span class="result-sender">{{ getSenderName(result.senderId) }}</span>
                <span class="result-time">{{ formatMessageTime(result.sentAt) }}</span>
              </div>
              <div class="result-body" v-html="highlightKeyword(result.content)"></div>
            </div>
          </div>
          <el-empty v-else description="未找到相关消息" :image-size="60" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Phone, VideoCamera, Search, User, MoreFilled, Loading, Warning, Check, 
  Bell, Plus, Edit, Delete, Remove, CircleClose, SwitchButton, Document, 
  RefreshLeft, ChatDotRound, Download, UserFilled, Picture, FolderOpened,
  DocumentCopy
} from '@element-plus/icons-vue'
import MessageInput from './MessageInput.vue'

const props = defineProps({
  session: { type: Object, required: true },
  messages: { type: Array, default: () => [] },
  sessionMembers: { type: Array, default: () => [] },
  currentUserId: { type: [String, Number], required: true },
  isGroupOwner: { type: Boolean, default: false },
  isGroupAdmin: { type: Boolean, default: false },
  loading: { type: Object, default: () => ({ messages: false, sending: false }) },
  hasMore: { type: Boolean, default: false },
  userInfo: { type: Object, default: () => ({}) },
  showSidebar: { type: Boolean, default: false }
})

const emit = defineEmits([
  'send-message', 'load-more', 'action', 'switch-session', 
  'show-group-members', 'recall-message', 'mark-as-read', 
  'leave-group', 'dissolve-group', 'toggle-sidebar',
  'invite-member', 'edit-group', 'edit-avatar', 'publish-notice', 'view-notice'
])

const inputMessage = ref('')
const messageContainer = ref(null)
const showSearchDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)

const currentUserAvatar = computed(() => {
  return props.userInfo?.avatar || 
         props.userInfo?.avatarUrl || 
         localStorage.getItem('avatar') || 
         localStorage.getItem('avatarUrl') || 
         '/logo.png'
})
const sessionName = computed(() => props.session?.sessionName || props.session?.name || '未知会话')
const sessionAvatar = computed(() => props.session?.avatar || props.session?.sessionAvatar || '/logo.png')
const sessionStatus = computed(() => {
  if (!props.session) return ''
  if (props.session.sessionType === 'GROUP') {
    const count = props.sessionMembers.length || props.session.memberCount || 0
    return `${count} 位成员`
  }
  return props.session.isOnline ? '在线' : '离线'
})

const isOwnMessage = (message) => {
  if (!message || !props.currentUserId) return false
  return String(message.senderId) === String(props.currentUserId)
}
const isSystemMessage = (message) => ['SYSTEM', 'GROUP_JOIN', 'GROUP_LEAVE', 'GROUP_DISSOLVE'].includes(message.msgType)

const getSenderAvatar = (senderId) => {
  if (isOwnMessage({ senderId })) return currentUserAvatar.value
  const member = props.sessionMembers.find(m => String(m.userId) === String(senderId))
  return member?.avatar || '/logo.png'
}

const getSenderName = (senderId) => {
  if (senderId === 0 || senderId === '0') return '系统'
  if (isOwnMessage({ senderId })) return '我'
  const member = props.sessionMembers.find(m => String(m.userId) === String(senderId))
  return member?.userName || props.userInfo?.userName || `用户${senderId}`
}

const handleAvatarError = (e) => { e.target.src = '/logo.png' }

const copyToClipboard = (text) => {
  if (!text) return
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  })
}

const getMessageContentClass = (message) => ({
  'own-bubble': isOwnMessage(message),
  'other-bubble': !isOwnMessage(message),
  'status-sending': message.status === 'SENDING',
  'status-failed': message.status === 'FAILED'
})

const shouldShowTimeDivider = (curr, prev) => {
  if (!prev) return true
  return new Date(curr.sentAt || curr.timestamp) - new Date(prev.sentAt || prev.timestamp) > 5 * 60 * 1000
}

const formatMessageTime = (ts) => {
  const d = new Date(ts), now = new Date()
  if (d.toDateString() === now.toDateString()) return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const formatTimeSmall = (ts) => {
  if (!ts) return ''
  return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getFileName = (url) => url.split('/').pop() || '未知文件'
const getFileSize = (bytes) => {
  if (!bytes) return '未知'
  const sizes = ['B', 'KB', 'MB', 'GB']
  let i = 0
  while (bytes >= 1024 && i < sizes.length - 1) { bytes /= 1024; i++ }
  return `${bytes.toFixed(1)} ${sizes[i]}`
}

const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  emit('send-message', {
    content: inputMessage.value.trim(),
    msgType: 'TEXT'
  })
  inputMessage.value = ''
}

const handleFileSelect = (payload) => {
  if (!payload) return
  emit('send-message', {
    msgType: payload.type,
    file: payload.file
  })
}
const downloadFile = (url) => window.open(url, '_blank')
const showGroupMembers = () => emit('show-group-members')

const handleAction = (command) => {
  if (command === 'notice') {
    emit('publish-notice')
  } else if (command === 'view-notice') {
    emit('view-notice')
  } else if (command === 'invite') {
    emit('invite-member')
  } else if (command === 'edit') {
    emit('edit-group')
  } else if (command === 'edit-avatar') {
    emit('edit-avatar')
  } else {
    const actions = {
      clear: { text: '确定清空聊天记录吗？', type: 'warning' },
      delete: { text: '确定删除该会话吗？', type: 'warning' },
      dissolve: { text: '确定解散该群聊吗？', type: 'danger' },
      exit: { text: '确定退出该群聊吗？', type: 'danger' }
    }
    const action = actions[command]
    if (!action) return
    ElMessageBox.confirm(action.text, '提示', { type: action.type })
      .then(() => emit('action', command))
      .catch(() => {})
  }
}

const handleScroll = () => {
  const el = messageContainer.value
  if (el && el.scrollTop === 0 && props.hasMore && !props.loading.messages) emit('load-more')
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    searched.value = false
    return
  }
  const kw = searchKeyword.value.toLowerCase()
  searchResults.value = props.messages.filter(m => m.content?.toLowerCase().includes(kw))
  searched.value = true
}

const highlightKeyword = (text) => {
  if (!text || !searchKeyword.value) return text
  const kw = searchKeyword.value
  return text.replace(new RegExp(`(${kw})`, 'gi'), '<mark>$1</mark>')
}

const jumpToMessage = (msg) => {
  showSearchDialog.value = false
  nextTick(() => {
    const el = document.querySelector(`[data-message-id="${msg.messageId}"]`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      el.classList.add('highlight-flash')
      setTimeout(() => el.classList.remove('highlight-flash'), 2000)
    }
  })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageContainer.value) {
    const lastMessage = messageContainer.value.querySelector('.message-item:last-child')
    if (lastMessage) {
      lastMessage.scrollIntoView({ behavior: 'smooth', block: 'end' })
    }
  }
}

watch(() => props.messages, scrollToBottom, { deep: true })
onMounted(scrollToBottom)
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background-color: #f5f5f5;
  overflow: hidden;
}

.chat-header {
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  z-index: 10;
}

.session-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.session-avatar {
  position: relative;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}

.session-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: #ff4d4f;
  color: white;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  font-weight: bold;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
  z-index: 1;
}

.session-name {
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: -0.01em;
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 400px;
}

.name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-id-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
}

.session-id {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.copy-icon {
  font-size: 14px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}

.copy-icon:hover {
  color: #409eff;
}

.session-status {
  font-size: 12.5px;
  color: #64748b;
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ccc;
  margin-right: 6px;
}

.status-dot.online {
  background-color: #52c41a;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  background: none;
  border: none;
  padding: 8px;
  cursor: pointer;
  color: #666;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background-color: #f0f2f5;
  color: #409eff;
}

.icon-btn.active {
  color: #409eff;
  background-color: #ecf5ff;
}

/* 消息列表容器 */
.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 8px;
  background-color: #f8fafc;
  scroll-behavior: smooth;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 20px;
}

/* 消息条目父容器 */
.message-item {
  display: flex;
  flex-direction: column;
  width: 100%;
}

/* 时间分隔符 */
.time-divider {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.time-divider span {
  font-size: 12px;
  color: #94a3b8;
  background: #e2e8f0;
  padding: 2px 12px;
  border-radius: 12px;
  font-weight: 500;
}

/* 消息包装器 */
.message-wrapper {
  display: flex;
  gap: 12px;
  max-width: 85%;
  position: relative;
  transition: all 0.3s ease;
  align-self: flex-start;
}

.own-message {
  align-self: flex-end !important;
  flex-direction: row; 
  justify-content: flex-end;
}

.other-message {
  align-self: flex-start;
  justify-content: flex-start;
}

/* 头像样式 */
.message-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 2px solid #fff;
}

/* 消息内容包装器 */
.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: calc(100% - 52px);
}

.sender-name {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 2px;
  font-weight: 500;
}

.own-message .sender-name {
  text-align: right;
}

/* 消息气泡基础样式 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14.5px;
  line-height: 1.5;
  position: relative;
  word-break: break-word;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 接收方气泡 */
.other-bubble {
  background-color: #ffffff;
  color: #1e293b;
  border-bottom-left-radius: 4px;
  border: 1px solid #e2e8f0;
}

/* 发送方气泡 - 调整为淡绿色 */
.own-bubble {
  background-color: #95ec69;
  color: #1e293b;
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

/* 消息元信息 (时间、状态) */
.message-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
  font-size: 11px;
  color: #94a3b8;
}

.own-message .message-meta {
  justify-content: flex-end;
  color: #64748b;
}

.message-time-small {
  opacity: 0.8;
}

.status-icons {
  display: flex;
  align-items: center;
}

.status-read {
  color: #10b981;
}

.status-error {
  color: #ef4444;
}

/* 文本消息内容 */
.text-message {
  white-space: pre-wrap;
}

/* 聊天图片 */
.chat-image {
  border-radius: 12px;
  display: block;
  max-width: 100%;
  cursor: zoom-in;
  transition: transform 0.2s;
}

.chat-image:hover {
  transform: scale(1.02);
}

/* 文件消息 */
.file-message {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 220px;
}

.other-bubble .file-message {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.file-icon {
  font-size: 32px;
  color: #3b82f6;
}

.file-info {
  flex: 1;
  min-width: 0;
  color: inherit;
}

.file-name {
  font-weight: 600;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 11px;
  opacity: 0.7;
}

/* 系统消息 */
.system-message {
  text-align: center;
  margin: 12px 0;
}

.system-message span {
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 4px 12px;
  border-radius: 12px;
}

/* 撤回消息样式 */
.recall-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #94a3b8;
  font-size: 12px;
  font-style: italic;
  padding: 4px 0;
}

.loading-messages, .empty-messages {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  gap: 16px;
}

.empty-state {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-state .el-icon {
  font-size: 48px;
  color: #e2e8f0;
}

.load-more {
  text-align: center;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more span {
  font-size: 13px;
  color: #10b981;
  font-weight: 500;
  padding: 6px 16px;
  background: #ecfdf5;
  border-radius: 20px;
}

.load-more:hover span {
  background: #d1fae5;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.1);
}

/* 搜索弹窗样式 */
.search-dialog-content {
  flex-direction: column;
  gap: 16px;
}

.results-list {
  max-height: 400px;
  overflow-y: auto;
}

.result-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.result-item:hover {
  background: #f5f7fa;
}

.result-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}
</style>
