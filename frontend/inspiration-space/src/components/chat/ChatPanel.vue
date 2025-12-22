<!-- 聊天面板 -->
<template>
  <div class="chat-panel">
    <!-- 聊天顶部：显示会话信息 -->
    <div v-if="session" class="chat-header">
      <div class="session-info">
        <div class="session-avatar">
          <img :src="sessionAvatar" :alt="sessionName" @error="handleAvatarError" />
        </div>
        <div class="session-details">
          <div class="session-name">{{ sessionName }}</div>
          <div class="session-status">
            <span :class="['status-dot', { online: session.isOnline }]"></span>
            {{ sessionStatus }}
          </div>
        </div>
      </div>
      <div class="chat-actions">
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
                  <el-button link type="primary" icon="Download" @click="downloadFile(message.content)"></el-button>
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
  Bell, Delete, Remove, CircleClose, SwitchButton, Document, RefreshLeft,
  ChatDotRound, Download
} from '@element-plus/icons-vue'
import MessageInput from './MessageInput.vue'

const props = defineProps({
  session: { type: Object, required: true },
  messages: { type: Array, default: () => [] },
  sessionMembers: { type: Array, default: () => [] },
  currentUserId: { type: [String, Number], required: true },
  isGroupOwner: { type: Boolean, default: false },
  loading: { type: Object, default: () => ({ messages: false, sending: false }) },
  hasMore: { type: Boolean, default: false },
  userInfo: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['send-message', 'load-more', 'action', 'switch-session', 'show-group-members'])

const inputMessage = ref('')
const messageContainer = ref(null)
const showSearchDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)

const currentUserAvatar = computed(() => localStorage.getItem('avatar') || '/logo.png')
const sessionName = computed(() => props.session?.sessionName || props.session?.name || '未知会话')
const sessionAvatar = computed(() => props.session?.avatar || props.session?.sessionAvatar || '/logo.png')
const sessionStatus = computed(() => {
  if (!props.session) return ''
  if (props.session.sessionType === 'GROUP') return `${props.session.memberCount || 0} 位成员`
  return props.session.isOnline ? '在线' : '离线'
})

const isOwnMessage = (message) => message.senderId === props.currentUserId
const isSystemMessage = (message) => ['SYSTEM', 'GROUP_JOIN', 'GROUP_LEAVE', 'GROUP_DISSOLVE'].includes(message.msgType)

const getSenderAvatar = (senderId) => {
  if (senderId === props.currentUserId) return currentUserAvatar.value
  const member = props.sessionMembers.find(m => m.userId === senderId)
  return member?.avatar || '/logo.png'
}

const getSenderName = (senderId) => {
  if (senderId === 0) return '系统'
  if (senderId === props.currentUserId) return '我'
  const member = props.sessionMembers.find(m => m.userId === senderId)
  return member?.userName || props.userInfo?.userName || `用户${senderId}`
}

const handleAvatarError = (e) => { e.target.src = '/logo.png' }

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

const formatTimeSmall = (ts) => new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

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
  if (command === 'notice') return (showSearchDialog.value = false) // 占位
  const actions = {
    clear: { text: '确定清空聊天记录吗？', type: 'warning' },
    delete: { text: '确定删除该会话吗？', type: 'warning' },
    dissolve: { text: '确定解散该群聊吗？', type: 'danger' },
    exit: { text: '确定退出该群聊吗？', type: 'danger' }
  }
  const action = actions[command]
  if (!action) return
  ElMessageBox.confirm(action.text, '提示', { type: action.type })
    .then(() => emit('action', { type: command }))
    .catch(() => {})
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
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
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
  background-color: #f5f5f5;
  overflow: hidden;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.session-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
}

.session-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.session-status {
  font-size: 12px;
  color: #999;
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
  background-color: #f0f0f0;
  color: #333;
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f5f5;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.time-divider {
  text-align: center;
  margin: 10px 0;
}

.time-divider span {
  font-size: 12px;
  color: #999;
  background: rgba(0,0,0,0.05);
  padding: 2px 8px;
  border-radius: 4px;
}

.message-wrapper {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.own-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.other-message {
  align-self: flex-start;
}

.message-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 6px;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sender-name {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.own-message .sender-name {
  text-align: right;
  margin-right: 4px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
  word-break: break-all;
}

.other-bubble {
  background-color: #ffffff;
  color: #333;
  border-top-left-radius: 2px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.own-bubble {
  background-color: #95ec69;
  color: #000;
  border-top-right-radius: 2px;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 11px;
  color: rgba(0,0,0,0.3);
}

.own-message .message-meta {
  justify-content: flex-end;
}

.input-container {
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

/* 消息类型样式 */
.image-message {
  max-width: 300px;
}

.chat-image {
  border-radius: 4px;
  display: block;
}

.file-message {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #eee;
  min-width: 200px;
}

.file-icon {
  font-size: 32px;
  color: #409eff;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #999;
}

.system-message {
  text-align: center;
  margin: 10px 0;
}

.system-message span {
  font-size: 12px;
  color: #999;
  background: rgba(0,0,0,0.03);
  padding: 2px 10px;
  border-radius: 10px;
}

.loading-messages, .empty-messages {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 12px;
}

.load-more {
  text-align: center;
  padding: 10px;
  cursor: pointer;
}

.load-more span {
  font-size: 12px;
  color: #409eff;
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

.result-sender {
  font-weight: 600;
  font-size: 13px;
}

.result-time {
  font-size: 11px;
  color: #909399;
}

.result-body {
  font-size: 13px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.input-container {
  padding: 12px 20px 20px;
  background: #fff;
}

@media (max-width: 768px) {
  .message-wrapper { max-width: 95%; }
  .chat-header { height: 56px; padding: 0 12px; }
  .session-avatar img { width: 36px; height: 36px; }
}
</style>
