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
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  margin: 4px 12px;
  background-color: transparent;
  position: relative;
}

.contact-item:hover {
  background-color: #f0f2f5;
}

.contact-item.active {
  background-color: #e8f0fe;
}

.contact-item.active .name {
  color: #1a73e8;
}

.avatar-container {
  position: relative;
  margin-right: 14px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-container img {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.contact-item:hover .avatar-container img {
  transform: scale(1.05);
}

.online-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: #94a3b8;
  border: 2.5px solid #fff;
}

.online-indicator.online {
  background-color: #22c55e;
}

.unread-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background-color: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
  border: 2px solid #fff;
  z-index: 1;
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
  margin-bottom: 2px;
}

.name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s ease;
}

.time {
  font-size: 11px;
  color: #64748b;
  flex-shrink: 0;
  font-weight: 400;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  font-weight: 400;
}

.status-icons {
  display: flex;
  gap: 6px;
  margin-left: 8px;
  align-items: center;
}

.pin-icon, .mute-icon {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .contact-item {
    padding: 10px 12px;
    margin: 2px 8px;
  }
  
  .avatar-container img {
    width: 42px;
    height: 42px;
    border-radius: 10px;
  }
}
</style>
