
<!-- 聊天主界面（接入后端会话与消息） -->
<template>
  <div class="chat-view">
    <div class="chat-container">
      <ContactPanel
        :sessions="sortedSessions"
        :selected-contact-id="currentSession?.sessionId"
        :loading="loading.sessions"
        @select-contact="handleSelectSession"
        @create-private="handleCreatePrivateChat"
        @create-group="handleCreateGroupChat"
      />

      <div class="chat-main">
        <div v-if="!currentSession" class="empty-chat">
          <div class="empty-chat-content">
            <svg
              width="64"
              height="64"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#ccc"
              stroke-width="1"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path
                d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"
              ></path>
            </svg>
            <p>{{ getEmptyChatMessage() }}</p>
          </div>
        </div>

        <template v-else>
          <div class="chat-content">
            <ChatPanel
              :session="currentSession"
              :messages="currentMessages"
              :session-members="currentSessionMembers"
              :current-user-id="currentUserId"
              :is-group-owner="isCurrentUserGroupOwner"
              :loading="{ messages: loading.messages, sending: loading.sending }"
              :has-more="hasMoreMessages"
              :user-info="{ userId: currentUserId, userName: '当前用户' }"
              @send-message="handleSendMessage"
              @load-more="handleLoadMoreMessages"
              @action="handleChatPanelAction"
              @show-group-members="handleShowGroupMembers"
              @switch-session="handleSelectSession"
            />
          </div>

          <!-- 群聊侧边栏 -->
          <div v-if="currentSession.sessionType === 'GROUP'" class="group-sidebar">
            <div class="sidebar-header">
              群成员 ({{ currentSessionMembers.length }})
            </div>
            <div class="member-list">
              <el-scrollbar>
                <div 
                  v-for="member in currentSessionMembers" 
                  :key="member.userId" 
                  class="member-item"
                >
                  <el-avatar :size="36" :src="member.avatar || '/logo.png'" @error="() => true">
                    <img src="/logo.png" />
                  </el-avatar>
                  <div class="member-info">
                    <span class="member-name">{{ member.userName || member.nickname }}</span>
                    <el-tag 
                      v-if="member.role === 'OWNER'" 
                      size="small" 
                      type="warning" 
                      effect="plain"
                      class="role-tag"
                    >群主</el-tag>
                  </div>
                </div>
              </el-scrollbar>
            </div>
          </div>
        </template>
      </div>
    </div>

    <GroupMembersDialog
      v-model="showGroupMembersDialog"
      :members="currentSessionMembers"
      :current-user-id="currentUserId"
      :is-owner="isCurrentUserGroupOwner"
      @remove-member="handleKickMember"
      @transfer-owner="handleTransferOwnership"
      @add-member="handleAddGroupMember"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useChatStore } from '@/stores/chatStoreOptimized'
import ContactPanel from '../components/chat/ContactPanel.vue'
import ChatPanel from '../components/chat/ChatPanel.vue'
import GroupMembersDialog from '../components/chat/GroupMembersDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const chatStore = useChatStore()
const {
  sortedSessions,
  currentSession,
  currentMessages,
  currentSessionMembers,
  connectionStatus,
  loading,
  currentUserId,
  isCurrentUserGroupOwner,
  messagePagination
} = storeToRefs(chatStore)

const showGroupMembersDialog = ref(false)

const hasMoreMessages = computed(() => {
  if (!currentSession.value) return false
  const pagination = messagePagination.value.get(currentSession.value.sessionId)
  return pagination?.hasMore || false
})

onMounted(async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }

    const wsConnected = await chatStore.initializeWebSocket()
    if (!wsConnected) {
      ElMessage.error('聊天服务连接失败')
      return
    }

    await chatStore.loadSessions()
  } catch (error) {
    console.error('初始化聊天界面失败:', error)
    ElMessage.error('初始化聊天界面失败')
  }
})

onUnmounted(() => {
  chatStore.clearState()
})

watch(connectionStatus, (newStatus) => {
  if (newStatus === 'error') {
    ElMessage.error('聊天服务连接异常')
  }
})

const getEmptyChatMessage = () => {
  switch (connectionStatus.value) {
    case 'connecting':
      return '聊天服务连接中...'
    case 'error':
      return '聊天服务连接失败，请重试'
    case 'disconnected':
      return '聊天服务未连接'
    default:
      return '请选择会话开始聊天'
  }
}

const handleSelectSession = async (session) => {
  try {
    await chatStore.selectSession(session)
  } catch (error) {
    console.error('选择会话失败:', error)
    ElMessage.error('选择会话失败')
  }
}

const handleSendMessage = async (payload) => {
  try {
    if (!payload) return

    // 兼容旧的纯文本字符串
    if (typeof payload === 'string') {
      const text = payload.trim()
      if (!text) return
      await chatStore.sendMessage(text, 'TEXT')
      return
    }

    const { content = '', msgType = 'TEXT', file, fileInfo } = payload

    // 文件或图片消息：先上传文件再发送
    if ((msgType === 'IMAGE' || msgType === 'FILE') && file) {
      const uploaded = await chatStore.uploadFileAndGetInfo(file)
      if (!uploaded) return
      await chatStore.sendMessage(uploaded.url, msgType, uploaded)
      return
    }

    // 文本消息或带现成 fileInfo 的消息
    const text = content || ''
    if (!text.trim() && msgType === 'TEXT') return
    await chatStore.sendMessage(text, msgType, fileInfo || null)
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

const handleLoadMoreMessages = async () => {
  try {
    await chatStore.loadMoreMessages()
  } catch (error) {
    console.error('加载更多消息失败:', error)
    ElMessage.error('加载更多消息失败')
  }
}

const handleChatPanelAction = async (action) => {
  try {
    switch (action.type) {
      case 'clear':
        ElMessage.success('聊天记录已清空（仅本地效果待实现）')
        break
      case 'delete':
        ElMessage.success('会话删除功能待与后端联调')
        break
      case 'dissolve':
        await handleDissolveGroup()
        break
      case 'exit':
        await handleLeaveGroup()
        break
      default:
        break
    }
  } catch (error) {
    console.error('处理聊天面板操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleShowGroupMembers = () => {
  showGroupMembersDialog.value = true
}

const handleKickMember = async (userId) => {
  if (!currentSession.value) return
  try {
    await chatStore.kickGroupMember(currentSession.value.sessionId, userId)
  } catch (error) {
    console.error('踢出成员失败:', error)
    ElMessage.error('踢出成员失败')
  }
}

const handleTransferOwnership = async (newOwnerId) => {
  if (!currentSession.value) return
  try {
    await chatStore.transferGroupOwner(currentSession.value.sessionId, newOwnerId)
  } catch (error) {
    console.error('转让群主失败:', error)
    ElMessage.error('转让群主失败')
  }
}

const handleAddGroupMember = () => {
  ElMessage.info('邀请成员功能开发中')
}

const handleLeaveGroup = async () => {
  if (!currentSession.value) return
  try {
    const result = await chatStore.leaveGroupChat(currentSession.value.sessionId)
    if (result) {
      ElMessage.success('已退出群聊')
      showGroupMembersDialog.value = false
    }
  } catch (error) {
    console.error('退出群聊失败:', error)
    ElMessage.error('退出群聊失败')
  }
}

const handleDissolveGroup = async () => {
  if (!currentSession.value) return
  try {
    const result = await chatStore.dissolveGroupChat(currentSession.value.sessionId)
    if (result) {
      ElMessage.success('群聊已解散')
      showGroupMembersDialog.value = false
    }
  } catch (error) {
    console.error('解散群聊失败:', error)
    ElMessage.error('解散群聊失败')
  }
}

const handleCreatePrivateChat = async () => {
  try {
    const { value, action } = await ElMessageBox.prompt(
      '请输入要发起私聊的用户ID',
      '创建私聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[0-9]+$/,
        inputErrorMessage: '请输入有效的数字ID'
      }
    )
    if (action === 'confirm' && value) {
      await chatStore.createPrivateChat(Number(value))
      ElMessage.success('私聊创建成功')
    }
  } catch (error) {}
}

const handleCreateGroupChat = async () => {
  try {
    const { value, action } = await ElMessageBox.prompt('请输入群聊名称', '创建群聊', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '群聊名称不能为空'
    })
    if (action === 'confirm' && value) {
      await chatStore.createGroupChat({ name: value })
      ElMessage.success('群聊创建成功')
    }
  } catch (error) {}
}
</script>

<style scoped>
.chat-view {
  height: calc(100vh - 64px); /* 减去导航栏高度 */
  background-color: #f0f2f5;
  padding: 20px;
  box-sizing: border-box;
}

.chat-container {
  display: flex;
  height: 100%;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  max-width: 1400px;
  margin: 0 auto;
}

.chat-main {
  flex: 1;
  display: flex;
  background-color: #fff;
  position: relative;
  min-width: 0;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  border-right: 1px solid #f0f0f0;
}

.group-sidebar {
  width: 260px;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-left: 1px solid #eef0f2;
}

.sidebar-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 1px solid #eef0f2;
  background-color: #fff;
}

.member-list {
  flex: 1;
  overflow: hidden;
  padding: 8px 0;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  gap: 12px;
  transition: all 0.2s ease;
  cursor: default;
}

.member-item:hover {
  background-color: #f8fafc;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 2px;
}

.member-name {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  align-self: flex-start;
  font-size: 10px;
  height: 18px;
  padding: 0 6px;
  line-height: 16px;
  border-radius: 4px;
  font-weight: 500;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.empty-chat-content {
  text-align: center;
  color: #999;
}

.empty-chat-content p {
  margin-top: 16px;
  font-size: 14px;
}

@media (max-width: 1024px) {
  .group-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .chat-view {
    padding: 0;
    height: 100vh;
  }
  .chat-container {
    border-radius: 0;
  }
}
</style>
