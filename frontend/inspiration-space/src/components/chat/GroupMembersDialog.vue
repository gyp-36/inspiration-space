<template>
  <el-dialog
    v-model="visible"
    :title="`群成员 (${members.length})`"
    :width="dialogWidth"
    destroy-on-close
    append-to-body
    class="group-members-dialog"
  >
    <div class="dialog-content">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索成员..."
          prefix-icon="Search"
          clearable
        />
        <el-button 
          v-if="isOwner" 
          type="primary" 
          icon="Plus"
          @click="$emit('add-member')"
        >
          邀请成员
        </el-button>
      </div>

      <div class="member-list-container">
        <el-scrollbar max-height="400px">
          <div v-if="filteredMembers.length > 0" class="member-list">
            <div 
              v-for="member in filteredMembers" 
              :key="member.id" 
              class="member-item"
            >
              <div class="member-info">
                <el-avatar :size="40" :src="member.avatar || '/logo.png'" @error="() => true">
                  <img src="/logo.png" />
                </el-avatar>
                <div class="member-details">
                  <div class="member-name-row">
                    <span class="member-name">{{ member.nickname || member.username }}</span>
                    <el-tag 
                      v-if="member.role === 'OWNER'" 
                      size="small" 
                      type="warning" 
                      effect="dark"
                      class="role-tag"
                    >群主</el-tag>
                    <el-tag 
                      v-else-if="member.role === 'ADMIN'" 
                      size="small" 
                      type="success" 
                      effect="dark"
                      class="role-tag"
                    >管理员</el-tag>
                  </div>
                  <div class="member-status">
                    <span :class="['status-dot', member.online ? 'online' : 'offline']"></span>
                    {{ member.online ? '在线' : '离线' }}
                  </div>
                </div>
              </div>

              <div class="member-actions" v-if="canManage(member)">
                <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, member)">
                  <el-button icon="More" circle size="small" />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="isOwner && member.role !== 'OWNER'" command="transfer">
                        转让群主
                      </el-dropdown-item>
                      <el-dropdown-item 
                        v-if="canRemove(member)" 
                        command="remove" 
                        class="delete-item"
                      >
                        移出群聊
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
          <el-empty v-else description="未找到匹配成员" :image-size="100" />
        </el-scrollbar>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Search, Plus, More } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  members: {
    type: Array,
    default: () => []
  },
  currentUserId: [String, Number],
  isOwner: Boolean
})

const emit = defineEmits(['update:modelValue', 'remove-member', 'transfer-owner', 'add-member'])

const searchQuery = ref('')
const windowWidth = ref(window.innerWidth)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dialogWidth = computed(() => {
  if (windowWidth.value < 768) return '90%'
  if (windowWidth.value < 1200) return '500px'
  return '600px'
})

const sortedMembers = computed(() => {
  return [...props.members].sort((a, b) => {
    // Owner first, then online status, then nickname
    if (a.role === 'OWNER' && b.role !== 'OWNER') return -1
    if (a.role !== 'OWNER' && b.role === 'OWNER') return 1
    if (a.online && !b.online) return -1
    if (!a.online && b.online) return 1
    return (a.nickname || a.username).localeCompare(b.nickname || b.username)
  })
})

const filteredMembers = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) return sortedMembers.value
  return sortedMembers.value.filter(m => 
    (m.nickname || '').toLowerCase().includes(query) || 
    (m.username || '').toLowerCase().includes(query)
  )
})

const canManage = (member) => {
  if (member.id === props.currentUserId) return false
  return props.isOwner || (member.role !== 'OWNER' && member.role !== 'ADMIN')
}

const canRemove = (member) => {
  if (member.role === 'OWNER') return false
  return props.isOwner
}

const handleCommand = (command, member) => {
  if (command === 'remove') {
    ElMessageBox.confirm(
      `确定要将 ${member.nickname || member.username} 移出群聊吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      emit('remove-member', member.id)
    }).catch(() => {})
  } else if (command === 'transfer') {
    ElMessageBox.confirm(
      `确定要将群主转让给 ${member.nickname || member.username} 吗？转让后你将失去群主权限。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      emit('transfer-owner', member.id)
    }).catch(() => {})
  }
}

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
}

.member-list-container {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}

.member-list {
  display: flex;
  flex-direction: column;
}

.member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  transition: all 0.3s;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.member-item:last-child {
  border-bottom: none;
}

.member-item:hover {
  background-color: var(--el-fill-color-light);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.role-tag {
  height: 18px;
  padding: 0 4px;
  font-size: 10px;
}

.member-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background-color: var(--el-color-success);
  box-shadow: 0 0 4px var(--el-color-success);
}

.status-dot.offline {
  background-color: var(--el-text-color-disabled);
}

.delete-item {
  color: var(--el-color-danger);
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}

@media (max-width: 768px) {
  .member-item {
    padding: 10px 12px;
  }
  
  .member-name {
    font-size: 13px;
  }
}
</style>
