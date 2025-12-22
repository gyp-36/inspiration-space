<!-- 左侧会话列表 -->
<template>
  <div class="contact-panel">
    <!-- 搜索和新建 -->
    <div class="header-section">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索会话/联系人"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-tooltip content="新建会话" placement="bottom">
          <el-button 
            type="primary" 
            icon="Plus" 
            circle 
            class="add-btn"
            @click="handleNewChat"
          />
        </el-tooltip>
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
          :key="session.sessionId || session.id"
          :contact="session"
          :selected-contact-id="selectedContactId"
          @select="handleSelectSession"
        />
      </div>
    </div>

    <!-- 新建会话弹窗 -->
    <el-dialog
      v-model="newChatVisible"
      title="新建会话"
      width="400px"
      append-to-body
      destroy-on-close
    >
      <div class="new-chat-options">
        <div class="option-card" @click="createPrivateChat">
          <div class="option-icon private"><el-icon><User /></el-icon></div>
          <div class="option-info">
            <div class="option-title">发起私聊</div>
            <div class="option-desc">与好友进行一对一交流</div>
          </div>
        </div>
        <div class="option-card" @click="createGroupChat">
          <div class="option-icon group"><el-icon><ChatLineRound /></el-icon></div>
          <div class="option-info">
            <div class="option-title">发起群聊</div>
            <div class="option-desc">创建多人交流群组</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Plus, User, ChatLineRound } from '@element-plus/icons-vue'
import ContactItem from './ContactItem.vue'

const props = defineProps({
  sessions: {
    type: Array,
    required: true,
    default: () => []
  },
  selectedContactId: {
    type: [String, Number],
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select-contact', 'create-private', 'create-group'])

// 搜索和过滤
const searchQuery = ref('')
const activeTab = ref('all')
const tabs = [
  { label: '全部', value: 'all' },
  { label: '私聊', value: 'private' },
  { label: '群聊', value: 'group' }
]

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
      const name = (s.sessionName || s.name || s.targetUserName || '').toLowerCase()
      const lastMsg = (s.lastMessage || '').toLowerCase()
      return name.includes(query) || lastMsg.includes(query)
    })
  }
  
  // 排序：置顶优先，然后按时间降序
  return [...list].sort((a, b) => {
    if (a.isPinned !== b.isPinned) return b.isPinned ? 1 : -1
    const timeA = a.lastMessageTime || a.timestamp || 0
    const timeB = b.lastMessageTime || b.timestamp || 0
    return timeB - timeA
  })
})

// 会话操作
const handleSelectSession = (session) => {
  emit('select-contact', session)
}

// 新建会话
const newChatVisible = ref(false)
const handleNewChat = () => {
  newChatVisible.value = true
}

const createPrivateChat = () => {
  newChatVisible.value = false
  emit('create-private')
}

const createGroupChat = () => {
  newChatVisible.value = false
  emit('create-group')
}
</script>

<style scoped>
.contact-panel {
  width: 280px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f7f7f7;
  border-right: 1px solid #e0e0e0;
}

.header-section {
  padding: 16px;
  flex-shrink: 0;
  background-color: #f7f7f7;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #e2e2e2;
  box-shadow: none !important;
  border-radius: 4px;
}

.search-input :deep(.el-input__inner) {
  height: 28px;
  font-size: 12px;
}

.add-btn {
  width: 28px !important;
  height: 28px !important;
  background-color: #e2e2e2 !important;
  border: none !important;
  color: #666 !important;
}

.add-btn:hover {
  background-color: #d1d1d1 !important;
}

.session-tabs {
  display: flex;
  gap: 16px;
  padding: 0 4px;
}

.tab-item {
  font-size: 13px;
  color: #666;
  cursor: pointer;
  padding-bottom: 4px;
  position: relative;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #333;
}

.tab-item.active {
  color: #07c160;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #07c160;
}

.list-section {
  flex: 1;
  overflow-y: auto;
}

.session-list {
  display: flex;
  flex-direction: column;
}

/* 隐藏滚动条 */
.list-section::-webkit-scrollbar {
  width: 0;
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
  padding: 16px;
  border-radius: 8px;
  background-color: #f8f9fb;
  cursor: pointer;
  transition: all 0.2s;
}

.option-card:hover {
  background-color: #f0f2f5;
  transform: translateY(-2px);
}

.option-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.option-icon.private { background: linear-gradient(135deg, #409eff, #79bbff); }
.option-icon.group { background: linear-gradient(135deg, #67c23a, #95d475); }

.option-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 12px;
  color: #909399;
}
</style>
