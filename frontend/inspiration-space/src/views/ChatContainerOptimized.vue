<!-- 聊天容器组件 - 优化版 -->
<template>
  <div class="chat-view">
    <div class="chat-container">
      <!-- 左侧联系人面板 -->
      <ContactPanelOptimized
        :sessions="sortedSessions" 
        :selected-session-id="currentSession?.sessionId"
        :connection-status="connectionStatus"
        :loading="loading.sessions"
        @select-session="handleSelectSession"
        @create-private-chat="handleCreatePrivateChat"
        @create-group-chat="handleCreateGroupChat"
      />

      <!-- 右侧聊天面板（条件渲染） -->
      <div class="chat-panel-wrapper">
        <!-- 未选择会话时的提示 -->
        <div v-if="!currentSession" class="empty-chat">
          <div class="empty-chat-content">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
            </svg>
            <p>{{ getEmptyChatMessage() }}</p>
          </div>
        </div>

        <!-- 选中会话后的聊天面板 -->
        <ChatPanelOptimized
          v-else
          :session="currentSession" 
          :messages="currentMessages"
          :session-members="currentSessionMembers"
          :current-user-id="currentUserId"
          :is-group-owner="isCurrentUserGroupOwner"
          :loading="loading.messages || loading.sending"
          :loading-members="loading.members"
          :user-info="{ userId: currentUserId, userName: '当前用户' }"
          @send-message="handleSendMessage"
          @load-more="handleLoadMoreMessages"
          @recall-message="handleRecallMessage"
          @mark-as-read="handleMarkAsRead"
          @leave-group="handleLeaveGroup"
          @dissolve-group="handleDissolveGroup"
          @show-group-members="handleShowGroupMembers"
          @action="handleChatPanelAction"
          @switch-session="handleSelectSession"
        />
      </div>
    </div>

    <!-- 群成员对话框 -->
    <el-dialog
      v-model="showGroupMembersDialog"
      title="群成员"
      width="400px"
      :close-on-click-modal="false"
    >
      <GroupMembersDialog
        :members="currentSessionMembers"
        :current-user-id="currentUserId"
        :is-group-owner="isCurrentUserGroupOwner"
        @kick-member="handleKickMember"
        @transfer-ownership="handleTransferOwnership"
      />
    </el-dialog>

    <!-- 创建私聊对话框 -->
    <el-dialog
      v-model="showCreatePrivateDialog"
      title="创建私聊"
      width="400px"
      :close-on-click-modal="false"
    >
      <CreatePrivateChatDialog
        @create-chat="handleCreatePrivateChatConfirm"
        @cancel="showCreatePrivateDialog = false"
      />
    </el-dialog>

    <!-- 创建群聊对话框 -->
    <el-dialog
      v-model="showCreateGroupDialog"
      title="创建群聊"
      width="500px"
      :close-on-click-modal="false"
    >
      <CreateGroupChatDialog
        @create-group="handleCreateGroupChatConfirm"
        @cancel="showCreateGroupDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useChatStore } from '@/stores/chatStoreOptimized'
import ContactPanelOptimized from '../components/chat/ContactPanelOptimized.vue'
import ChatPanelOptimized from '../components/chat/ChatPanelOptimized.vue'
import GroupMembersDialog from '../components/chat/GroupMembersDialog.vue'
import CreatePrivateChatDialog from '../components/chat/CreatePrivateChatDialog.vue'
import CreateGroupChatDialog from '../components/chat/CreateGroupChatDialog.vue'
import { ElMessage } from 'element-plus'

// 使用聊天Store
const chatStore = useChatStore()
const { 
  sortedSessions, 
  currentSession, 
  currentMessages, 
  currentSessionMembers,
  connectionStatus, 
  loading,
  currentUserId,
  isCurrentUserGroupOwner
} = storeToRefs(chatStore)

// 对话框状态
const showGroupMembersDialog = ref(false)
const showCreatePrivateDialog = ref(false)
const showCreateGroupDialog = ref(false)

// 组件挂载时初始化
onMounted(async () => {
  try {
    console.log('初始化聊天界面...')
    
    // 检查用户是否已登录
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.error('请先登录')
      return
    }
    
    // 初始化WebSocket连接
    const wsConnected = await chatStore.initializeWebSocket()
    if (!wsConnected) {
      ElMessage.error('聊天服务连接失败')
      return
    }
    
    // 加载会话列表
    await chatStore.loadSessions()
    
    console.log('聊天界面初始化完成')
    
  } catch (error) {
    console.error('初始化聊天界面失败:', error)
    ElMessage.error('初始化聊天界面失败')
  }
})

// 组件卸载时清理
onUnmounted(() => {
  console.log('清理聊天资源...')
  chatStore.clearState()
})

// 监听连接状态变化
watch(connectionStatus, (newStatus) => {
  console.log('连接状态变化:', newStatus)
  if (newStatus === 'error') {
    ElMessage.error('聊天服务连接异常')
  }
})

// 获取空聊天界面的提示消息
const getEmptyChatMessage = () => {
  switch (connectionStatus.value) {
    case 'connecting':
      return '聊天服务连接中...'
    case 'error':
      return '聊天服务连接失败，请重试'
    case 'disconnected':
      return '聊天服务未连接'
    default:
      return '请选择联系人开始聊天'
  }
}

// 处理选择会话
const handleSelectSession = async (session) => {
  try {
    console.log('选择会话:', session.sessionName)
    await chatStore.selectSession(session)
  } catch (error) {
    console.error('选择会话失败:', error)
    ElMessage.error('选择会话失败')
  }
}

// 处理发送消息
const handleSendMessage = async (messageData) => {
  try {
    const { content, msgType, fileInfo } = messageData
    await chatStore.sendMessage(content, msgType, fileInfo)
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 处理加载更多消息
const handleLoadMoreMessages = async () => {
  try {
    await chatStore.loadMoreMessages()
  } catch (error) {
    console.error('加载更多消息失败:', error)
    ElMessage.error('加载更多消息失败')
  }
}

// 处理撤回消息
const handleRecallMessage = async (messageId) => {
  try {
    await chatStore.recallMessage(messageId)
    ElMessage.success('消息撤回成功')
  } catch (error) {
    console.error('撤回消息失败:', error)
    ElMessage.error('撤回消息失败')
  }
}

// 处理标记已读
const handleMarkAsRead = async (messageId) => {
  try {
    await chatStore.markAsRead(messageId)
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 处理离开群聊
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

// 处理解散群聊
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

// 处理聊天面板的各种操作
const handleChatPanelAction = async (action) => {
  try {
    switch (action.type) {
      case 'publish-notice':
        if (currentSession.value) {
          await chatStore.publishGroupNotice(currentSession.value.sessionId, action.content)
        }
        break
      case 'clear':
        // 清空聊天记录
        ElMessage.success('聊天记录已清空')
        break
      case 'delete':
        // 删除会话
        ElMessage.success('会话已删除')
        break
      case 'dissolve':
        await handleDissolveGroup()
        break
      case 'exit':
        await handleLeaveGroup()
        break
      default:
        console.log('未知的操作类型:', action.type)
    }
  } catch (error) {
    console.error('处理聊天面板操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 处理显示群成员
const handleShowGroupMembers = () => {
  showGroupMembersDialog.value = true
}

// 处理踢出成员
const handleKickMember = async (userId) => {
  if (!currentSession.value) return
  
  try {
    await chatStore.kickGroupMember(currentSession.value.sessionId, userId)
  } catch (error) {
    console.error('踢出成员失败:', error)
    ElMessage.error('踢出成员失败')
  }
}

// 处理转让群主
const handleTransferOwnership = async (newOwnerId) => {
  if (!currentSession.value) return
  
  try {
    await chatStore.transferGroupOwner(currentSession.value.sessionId, newOwnerId)
  } catch (error) {
    console.error('转让群主失败:', error)
    ElMessage.error('转让群主失败')
  }
}

// 处理创建私聊
const handleCreatePrivateChat = () => {
  showCreatePrivateDialog.value = true
}

// 处理创建私聊确认
const handleCreatePrivateChatConfirm = async (targetUserId) => {
  try {
    const sessionId = await chatStore.createPrivateChat(targetUserId)
    if (sessionId) {
      showCreatePrivateDialog.value = false
      ElMessage.success('私聊创建成功')
    }
  } catch (error) {
    console.error('创建私聊失败:', error)
    ElMessage.error('创建私聊失败')
  }
}

// 处理创建群聊
const handleCreateGroupChat = () => {
  showCreateGroupDialog.value = true
}

// 处理创建群聊确认
const handleCreateGroupChatConfirm = async (groupData) => {
  try {
    const sessionId = await chatStore.createGroupChat(groupData)
    if (sessionId) {
      showCreateGroupDialog.value = false
      ElMessage.success('群聊创建成功')
    }
  } catch (error) {
    console.error('创建群聊失败:', error)
    ElMessage.error('创建群聊失败')
  }
}
</script>

<style scoped>
.chat-view {
  height: 100vh;
  background-color: #f5f5f5;
}

.chat-container {
  display: flex;
  height: 100%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-panel-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.empty-chat-content {
  text-align: center;
  color: #999;
}

.empty-chat-content svg {
  margin-bottom: 16px;
}

.empty-chat-content p {
  font-size: 16px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    border-radius: 0;
  }
  
  .chat-panel-wrapper {
    position: relative;
  }
}
</style>