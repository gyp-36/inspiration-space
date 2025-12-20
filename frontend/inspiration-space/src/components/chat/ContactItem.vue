<!-- ContactItem.vue -->
<template>
  <div 
    :class="['contact-item', { active: isSelected }]"
    @click="$emit('select', contact)"
  >
    <!-- 会话头像 -->
    <div class="avatar-container">
      <img :src="avatarUrl" :alt="displayName" @error="handleAvatarError" />
      <!-- 在线状态指示器（仅私聊） -->
      <div 
        v-if="contact.sessionType === 'PRIVATE'" 
        :class="['online-indicator', { online: contact.isOnline }]"
      ></div>
      <!-- 未读消息数 -->
      <div v-if="contact.unreadCount > 0" class="unread-badge">
        {{ contact.unreadCount > 99 ? '99+' : contact.unreadCount }}
      </div>
    </div>
    
    <!-- 会话信息 -->
    <div class="info-container">
      <div class="header">
        <span class="name">{{ displayName }}</span>
        <span class="time">{{ formattedTime }}</span>
      </div>
      <div class="footer">
        <div class="last-message">
          <span v-if="contact.lastMsgType === 'IMAGE'">[图片]</span>
          <span v-else-if="contact.lastMsgType === 'FILE'">[文件]</span>
          <span v-else-if="contact.lastMsgType === 'SYSTEM'">[系统消息]</span>
          <span v-else-if="contact.lastMsgType === 'RECALL'">[消息已撤回]</span>
          <span v-else>{{ contact.lastMessage || '暂无消息' }}</span>
        </div>
        
        <!-- 状态图标 -->
        <div class="status-icons">
          <el-icon v-if="contact.isPinned" class="pin-icon" title="已置顶"><PriceTag /></el-icon>
          <el-icon v-if="contact.isMuted" class="mute-icon" title="消息免打扰"><Mute /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PriceTag, Mute } from '@element-plus/icons-vue'

const props = defineProps({
  contact: {
    type: Object,
    required: true
  },
  selectedContactId: {
    type: [String, Number],
    default: null
  }
})

defineEmits(['select'])

const isSelected = computed(() => {
  return props.contact.sessionId === props.selectedContactId || 
         props.contact.id === props.selectedContactId
})

const displayName = computed(() => {
  return props.contact.sessionName || 
         props.contact.name || 
         props.contact.targetUserName || 
         '未知会话'
})

const avatarUrl = computed(() => {
  return props.contact.avatar || 
         props.contact.sessionAvatar || 
         '/logo.png'
})

const formattedTime = computed(() => {
  const timestamp = props.contact.lastMessageTime || props.contact.timestamp
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  
  // 一周内
  const weekAgo = new Date(now)
  weekAgo.setDate(weekAgo.getDate() - 7)
  if (date > weekAgo) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()]
  }
  
  // 更早
  return `${date.getMonth() + 1}/${date.getDate()}`
})

const handleAvatarError = (e) => {
  e.target.src = '/logo.png'
}
</script>

<style scoped>
.contact-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin: 2px 8px;
  background-color: transparent;
}

.contact-item:hover {
  background-color: #f5f7fa;
}

.contact-item.active {
  background-color: #eef5fe;
}

.avatar-container {
  position: relative;
  margin-right: 12px;
  flex-shrink: 0;
}

.avatar-container img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #f0f0f0;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #909399;
  border: 2px solid #fff;
}

.online-indicator.online {
  background-color: #67c23a;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: #f56c6c;
  color: white;
  font-size: 11px;
  padding: 0 5px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
  min-width: 16px;
  text-align: center;
  box-shadow: 0 0 0 2px #fff;
}

.info-container {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.status-icons {
  display: flex;
  gap: 4px;
  margin-left: 8px;
}

.pin-icon, .mute-icon {
  font-size: 14px;
  color: #c0c4cc;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .contact-item {
    padding: 10px 12px;
    margin: 1px 4px;
  }
  
  .avatar-container img {
    width: 40px;
    height: 40px;
  }
}
</style>
