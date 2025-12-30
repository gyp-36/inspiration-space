<template>
  <div class="group-member-sidebar">
    <div class="sidebar-header">
      <div class="header-title">群成员 ({{ members.length }})</div>
      <el-button 
        type="primary" 
        link 
        :icon="Plus"
        @click="$emit('invite-member')"
      >邀请</el-button>
    </div>
    
    <div class="member-list-wrapper">
      <el-scrollbar>
        <div class="member-list">
          <div 
            v-for="member in sortedMembers" 
            :key="member.userId" 
            class="member-item"
          >
            <div class="member-info" @click="goToProfile(member.userId)">
              <div class="member-avatar">
                <el-avatar :size="36" :src="member.avatar || '/logo.png'" @error="() => true">
                  <img src="/logo.png" />
                </el-avatar>
              </div>
              <div class="member-details">
                <div class="member-name-row">
                  <span class="member-name" :title="member.userName">{{ member.userName }}</span>
                  <el-tag 
                    v-if="member.role === 'OWNER'" 
                    size="small" 
                    type="warning" 
                    effect="plain"
                    class="role-tag"
                  >群主</el-tag>
                  <el-tag 
                    v-else-if="member.role === 'ADMIN'" 
                    size="small" 
                    type="success" 
                    effect="plain"
                    class="role-tag"
                  >管理员</el-tag>
                </div>
              </div>
            </div>

            <!-- 踢出操作：仅群主或管理员可见，且不能踢出自己或群主 -->
            <div class="member-actions" v-if="canKick(member)">
              <el-popconfirm
                title="确定将该成员移出群聊吗？"
                @confirm="$emit('kick-member', member.userId)"
                confirm-button-text="确定"
                cancel-button-text="取消"
              >
                <template #reference>
                  <el-button 
                    link 
                    type="danger" 
                    size="small" 
                    class="kick-btn"
                  >踢出</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  members: {
    type: Array,
    default: () => []
  },
  currentUserId: {
    type: [String, Number],
    required: true
  },
  isCurrentUserOwner: {
    type: Boolean,
    default: false
  },
  isCurrentUserAdmin: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['kick-member', 'invite-member'])
const router = useRouter()

const sortedMembers = computed(() => {
  const list = [...props.members]
  return list.sort((a, b) => {
    // OWNER 第一，ADMIN 第二，NORMAL 第三
    const roleOrder = { 'OWNER': 0, 'ADMIN': 1, 'NORMAL': 2 }
    const orderA = roleOrder[a.role] ?? 3
    const orderB = roleOrder[b.role] ?? 3
    
    if (orderA !== orderB) return orderA - orderB
    
    // 同角色按在线状态排
    if (a.isOnline !== b.isOnline) return a.isOnline ? -1 : 1
    
    // 最后按名字排
    return (a.userName || '').localeCompare(b.userName || '')
  })
})

const goToProfile = (userId) => {
  if (!userId) return
  router.push(`/user/${userId}`)
}

const canKick = (member) => {
  // 自己不能踢自己
  if (String(member.userId) === String(props.currentUserId)) return false
  
  // 群主可以踢任何人（除了自己）
  if (props.isCurrentUserOwner) return true
  
  // 管理员可以踢普通成员
  if (props.isCurrentUserAdmin && member.role === 'NORMAL') return true
  
  return false
}
</script>

<style scoped>
.group-member-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: white;
}

.sidebar-header {
  display: none; /* 在抽屉中隐藏，使用抽屉自带标题 */
}

.header-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.member-list-wrapper {
  flex: 1;
  overflow: hidden;
}

.member-list {
  padding: 8px 0;
}

.member-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  transition: all 0.2s;
}

.member-item:hover {
  background-color: #f5f7fa;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.member-details {
  flex: 1;
  min-width: 0;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.member-name {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  flex-shrink: 0;
  height: 18px;
  padding: 0 4px;
  font-size: 10px;
  line-height: 16px;
}

.member-actions {
  margin-left: 8px;
}

.kick-btn {
  font-size: 12px;
  padding: 0;
}

.kick-btn:hover {
  color: #f56c6c;
}
</style>
