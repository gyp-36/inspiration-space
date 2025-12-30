<!-- 左侧会话列表 - 优化版 -->
<template>
  <div class="contact-panel">
    <!-- 头部信息：连接状态和搜索 -->
    <div class="header-section">
      <div class="header-top">
        <div class="status-indicator">
          <span :class="['status-dot', connectionStatus]"></span>
          <span class="status-text">{{ statusText }}</span>
        </div>
        <el-tooltip content="新建聊天" placement="bottom">
          <el-button 
            type="primary" 
            :icon="Plus" 
            circle 
            size="small"
            class="add-btn"
            @click="handleNewChat"
          />
        </el-tooltip>
      </div>
      
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索会话..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>
      
      <!-- 会话分类标签 -->
      <div class="session-tabs">
        <div 
          v-for="tab in tabs" 
          :key="tab.value"
          :class="['tab-item', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </div>
      </div>
    </div>
    
    <!-- 会话列表 -->
    <div class="list-section" v-loading="loading">
      <el-empty 
        v-if="filteredSessions.length === 0" 
        :description="searchQuery ? '未找到匹配结果' : '暂无会话'" 
        :image-size="80"
      />
      
      <div v-else class="session-list">
        <ContactItem
          v-for="session in filteredSessions"
          :key="session.sessionId"
          :contact="session"
          :selected-contact-id="selectedSessionId"
          @select="handleSelectSession"
          @pin="handlePinSession"
          @delete="handleDeleteSession"
        />
      </div>
    </div>

    <!-- 新建会话弹窗 -->
    <el-dialog
      v-model="newChatVisible"
      title="新建会话"
      width="360px"
      append-to-body
      destroy-on-close
      class="new-chat-dialog"
    >
      <div class="new-chat-options">
        <div class="option-card" @click="createPrivateChat">
          <div class="option-icon private">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="option-info">
            <div class="option-title">发起私聊</div>
            <div class="option-desc">与指定用户进行 1v1 对话</div>
          </div>
        </div>
        <div class="option-card" @click="createGroupChat">
          <div class="option-icon group">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </div>
          <div class="option-info">
            <div class="option-title">创建群聊</div>
            <div class="option-desc">邀请多位好友共同探讨</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Plus, User, ChatDotRound } from '@element-plus/icons-vue'
import ContactItem from './ContactItem.vue'

const props = defineProps({
  sessions: {
    type: Array,
    default: () => []
  },
  selectedSessionId: {
    type: [String, Number],
    default: null
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select-session', 'create-private-chat', 'create-group-chat', 'pin-session', 'delete-session'])

// 搜索和过滤
const searchQuery = ref('')
const activeTab = ref('all')
const tabs = [
  { label: '全部', value: 'all' },
  { label: '私聊', value: 'private' },
  { label: '群聊', value: 'group' }
]

const statusText = computed(() => {
  switch (props.connectionStatus) {
    case 'connected': return '服务已连接'
    case 'connecting': return '连接中...'
    case 'error': return '连接异常'
    case 'reconnecting': return '重新连接中...'
    default: return '未连接'
  }
})

const filteredSessions = computed(() => {
  let list = props.sessions
  
  // 标签过滤
  if (activeTab.value !== 'all') {
    list = list.filter(s => {
      const type = (s.sessionType || '').toUpperCase()
      return activeTab.value === 'private' ? type === 'PRIVATE' : type === 'GROUP'
    })
  }
  
  // 搜索过滤
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(s => {
      const name = (s.sessionName || '').toLowerCase()
      const lastMsg = (s.lastMessage || '').toLowerCase()
      return name.includes(query) || lastMsg.includes(query)
    })
  }
  
  return list
})

// 会话操作
const handleSelectSession = (session) => {
  emit('select-session', session)
}

const handlePinSession = (session) => {
  emit('pin-session', session)
}

const handleDeleteSession = (session) => {
  emit('delete-session', session)
}

// 新建会话
const newChatVisible = ref(false)
const handleNewChat = () => {
  newChatVisible.value = true
}

const createPrivateChat = () => {
  newChatVisible.value = false
  emit('create-private-chat')
}

const createGroupChat = () => {
  newChatVisible.value = false
  emit('create-group-chat')
}
</script>

<style scoped>
.contact-panel {
  width: 300px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border-right: 1px solid #f0f0f0;
}

.header-section {
  padding: 16px;
  background-color: #ffffff;
  border-bottom: 1px solid #f8f9fa;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #909399;
}

.status-dot.connected { background-color: #67c23a; }
.status-dot.connecting { background-color: #e6a23c; }
.status-dot.error { background-color: #f56c6c; }

.status-text {
  font-size: 12px;
  color: #606266;
}

.add-btn {
  background-color: #67c23a !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 2px 6px rgba(103, 194, 58, 0.3);
  transition: all 0.3s;
}

.add-btn:hover {
  background-color: #85ce61 !important;
  transform: scale(1.1);
  box-shadow: 0 4px 10px rgba(103, 194, 58, 0.4);
}

.search-bar {
  margin-bottom: 12px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #f4f4f5;
  box-shadow: none !important;
  border-radius: 8px;
}

.session-tabs {
  display: flex;
  gap: 20px;
}

.tab-item {
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  padding-bottom: 6px;
  position: relative;
  transition: all 0.2s;
}

.tab-item:hover { color: #303133; }

.tab-item.active {
  color: #409eff;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #409eff;
  border-radius: 2px;
}

.list-section {
  flex: 1;
  overflow-y: auto;
}

.list-section::-webkit-scrollbar {
  width: 4px;
}

.list-section::-webkit-scrollbar-thumb {
  background: #e4e7ed;
  border-radius: 4px;
}

.new-chat-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border-radius: 12px;
  background-color: #f8f9fb;
  cursor: pointer;
  transition: all 0.2s;
}

.option-card:hover {
  background-color: #ecf5ff;
}

.option-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.option-icon.private { background: linear-gradient(135deg, #409eff, #79bbff); }
.option-icon.group { background: linear-gradient(135deg, #67c23a, #95d475); }

.option-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.option-desc {
  font-size: 12px;
  color: #909399;
}
</style>
