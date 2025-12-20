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
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-right: 1px solid #f0f0f0;
}

.header-section {
  padding: 16px 16px 8px;
  flex-shrink: 0;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid transparent;
}

.search-input :deep(.el-input__wrapper):hover {
  background-color: #ebeef5;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #409eff;
}

.add-btn {
  flex-shrink: 0;
}

.session-tabs {
  display: flex;
  gap: 20px;
  padding: 0 4px;
}

.tab-item {
  font-size: 14px;
  color: #909399;
  cursor: pointer;
  padding-bottom: 8px;
  position: relative;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #303133;
}

.tab-item.active {
  color: #409eff;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #409eff;
  border-radius: 1px;
}

.list-section {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.session-list {
  display: flex;
  flex-direction: column;
}

/* 滚动条美化 */
.list-section::-webkit-scrollbar {
  width: 5px;
}

.list-section::-webkit-scrollbar-thumb {
  background: #e4e7ed;
  border-radius: 10px;
}

.list-section::-webkit-scrollbar-thumb:hover {
  background: #dcdfe6;
}

/* 新建会话弹窗 */
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
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-card:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
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
}

.option-icon.private {
  background-color: #ecf5ff;
  color: #409eff;
}

.option-icon.group {
  background-color: #f0f9eb;
  color: #67c23a;
}

.option-info {
  flex: 1;
}

.option-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 13px;
  color: #909399;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-section {
    padding: 12px 12px 4px;
  }
  
  .search-bar {
    gap: 8px;
    margin-bottom: 12px;
  }
  
  .session-tabs {
    gap: 16px;
  }
}
</style>
