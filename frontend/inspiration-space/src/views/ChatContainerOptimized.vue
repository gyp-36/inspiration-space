<!-- 聊天容器组件 - 优化版 -->
<template>
  <div class="chat-view">
    <div class="chat-container">
      <!-- 左侧联系人面板 -->
      <ContactPanelOptimized
        :sessions="sortedSessions" 
        :selected-session-id="currentSession?.sessionId"
        :connection-status="connectionStatus"
        :loading="storeLoading.sessions"
        @select-session="handleSelectSession"
        @create-private-chat="handleCreatePrivateChat"
        @create-group-chat="handleCreateGroupChat"
        @pin-session="handlePinSession"
        @delete-session="handleDeleteSession"
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
        <div v-else class="chat-main-area">
          <ChatPanelOptimized
            class="chat-panel-main"
            :session="currentSession" 
            :messages="currentMessages"
            :session-members="currentSessionMembers"
            :current-user-id="currentUserId"
            :is-group-owner="isCurrentUserGroupOwner"
            :is-group-admin="isCurrentUserGroupAdmin"
            :loading="storeLoading.messages || storeLoading.sending"
            :loading-members="storeLoading.members"
            :user-info="userInfo"
            :show-sidebar="showGroupSidebar"
            @send-message="handleSendMessage"
            @load-more="handleLoadMoreMessages"
            @recall-message="handleRecallMessage"
            @mark-as-read="handleMarkAsRead"
            @leave-group="handleLeaveGroup"
            @dissolve-group="handleDissolveGroup"
            @show-group-members="handleShowGroupMembers"
            @toggle-sidebar="showGroupSidebar = !showGroupSidebar"
            @action="handleChatPanelAction"
            @switch-session="handleSelectSession"
            @invite-member="showInviteDialog = true"
            @edit-group="handleOpenEditGroup"
            @edit-avatar="handleOpenEditAvatar"
            @publish-notice="showPublishNoticeDialog = true"
            @view-notice="showViewNoticeDialog = true"
          />

          <!-- 右侧群成员侧边栏（抽屉式） -->
          <el-drawer
            v-model="showGroupSidebar"
            :title="`群成员 (${currentSessionMembers.length})`"
            direction="rtl"
            size="300px"
            :with-header="true"
            v-if="currentSession?.sessionType === 'GROUP'"
            class="member-drawer"
            :modal="true"
            :append-to-body="false"
            :lock-scroll="false"
            :close-on-click-modal="true"
          >
            <GroupMemberSidebar
              :members="currentSessionMembers"
              :current-user-id="currentUserId"
              :is-current-user-owner="isCurrentUserGroupOwner"
              :is-current-user-admin="isCurrentUserGroupAdmin"
              @kick-member="handleKickMember"
              @invite-member="showInviteDialog = true"
            />
          </el-drawer>
        </div>
      </div>
    </div>

    <!-- 邀请成员对话框 -->
    <el-dialog v-model="showInviteDialog" title="邀请成员" width="400px">
      <el-form label-position="top">
        <el-form-item label="用户ID (支持输入多个，用逗号或换行分隔)">
          <el-input
            v-model="inviteUserIdsInput"
            type="textarea"
            :rows="4"
            placeholder="例如: 1001, 1002, 1003"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInviteDialog = false">取消</el-button>
        <el-button type="primary" @click="handleInviteConfirm" :loading="actionLoading.inviting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改群信息对话框 -->
    <el-dialog v-model="showEditGroupDialog" title="修改群信息" width="500px">
      <el-form :model="editGroupForm" label-width="80px">
        <el-form-item label="群名称">
          <el-input v-model="editGroupForm.groupName" />
        </el-form-item>
        <el-form-item label="群描述">
          <el-input v-model="editGroupForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="入群审核">
          <el-radio-group v-model="editGroupForm.requiredApproval">
            <el-radio :label="0">需要</el-radio>
            <el-radio :label="1">不需要</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditGroupConfirm" :loading="actionLoading.updating">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改群头像对话框 -->
    <el-dialog v-model="showEditAvatarDialog" title="修改群头像" width="400px">
      <div class="avatar-edit-container">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleAvatarChange"
        >
          <img v-if="editGroupForm.avatarUrl" :src="editGroupForm.avatarUrl" class="avatar-preview" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="avatar-tip">点击预览图更换头像</div>
      </div>
      <template #footer>
        <el-button @click="showEditAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditAvatarConfirm" :loading="actionLoading.updating">确定</el-button>
      </template>
    </el-dialog>

    <!-- 发布群公告对话框 -->
    <el-dialog v-model="showPublishNoticeDialog" title="发布群公告" width="400px">
      <el-form label-position="top">
        <el-form-item label="公告内容">
          <el-input
            v-model="noticeInput"
            type="textarea"
            :rows="6"
            placeholder="请输入群公告内容..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishNoticeDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublishNoticeConfirm">发布</el-button>
      </template>
    </el-dialog>

    <!-- 查看群公告对话框 -->
    <el-dialog v-model="showViewNoticeDialog" title="群公告" width="400px">
      <div class="notice-view-content">
        <div v-if="currentGroupNotice" class="notice-text">
          {{ currentGroupNotice }}
        </div>
        <el-empty v-else description="暂无群公告" :image-size="80" />
      </div>
      <template #footer>
        <el-button type="primary" @click="showViewNoticeDialog = false">知道了</el-button>
      </template>
    </el-dialog>

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
import { useUserStore } from '@/stores/userStore'
import ContactPanelOptimized from '../components/chat/ContactPanelOptimized.vue'
import ChatPanelOptimized from '../components/chat/ChatPanelOptimized.vue'
import GroupMemberSidebar from '../components/chat/GroupMemberSidebar.vue'
import GroupMembersDialog from '../components/chat/GroupMembersDialog.vue'
import CreatePrivateChatDialog from '../components/chat/CreatePrivateChatDialog.vue'
import CreateGroupChatDialog from '../components/chat/CreateGroupChatDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 使用聊天Store
const chatStore = useChatStore()
const userStore = useUserStore()
const {
  sortedSessions,
  currentSession,
  currentMessages,
  currentSessionMembers,
  connectionStatus,
  loading: storeLoading,
  currentUserId,
  isCurrentUserGroupOwner,
  isCurrentUserGroupAdmin,
  messagePagination
} = storeToRefs(chatStore)

const { userInfo } = storeToRefs(userStore)

// 对话框状态
const showGroupMembersDialog = ref(false)
const showCreatePrivateDialog = ref(false)
const showCreateGroupDialog = ref(false)
const showInviteDialog = ref(false)
const showEditGroupDialog = ref(false)
const showEditAvatarDialog = ref(false)
const showPublishNoticeDialog = ref(false)
const showViewNoticeDialog = ref(false)

const inviteUserIdsInput = ref('')
const noticeInput = ref('')
const editGroupForm = ref({
  groupName: '',
  description: '',
  requiredApproval: 0,
  avatarUrl: ''
})

const actionLoading = ref({
  inviting: false,
  updating: false,
  uploading: false
})

const currentGroupNotice = computed(() => {
  if (!currentSession.value) return ''
  // 查找最新的群公告消息
  const noticeMsg = currentMessages.value.findLast(m => m.msgType === 'GROUP_NOTICE')
  return noticeMsg ? noticeMsg.content : ''
})

// 处理打开修改群信息对话框
const handleOpenEditGroup = async () => {
  if (!currentSession.value) return
  
  try {
    const groupInfo = await chatStore.getGroupInfo(currentSession.value.sessionId)
    if (groupInfo) {
      editGroupForm.value = {
        groupName: groupInfo.groupName || '',
        description: groupInfo.description || '',
        // 将后端枚举值转换为前端对应的数字
        requiredApproval: groupInfo.requiredApproval === 'NEED_APPROVAL' ? 0 : 1,
        avatarUrl: groupInfo.sessionAvatar || ''
      }
      showEditGroupDialog.value = true
    }
  } catch (error) {
    console.error('获取群详情失败:', error)
    ElMessage.error('获取群详情失败')
  }
}

// 处理打开修改群头像对话框
const handleOpenEditAvatar = async () => {
  if (!currentSession.value) return
  
  try {
    const groupInfo = await chatStore.getGroupInfo(currentSession.value.sessionId)
    if (groupInfo) {
      editGroupForm.value = {
        ...editGroupForm.value,
        avatarUrl: groupInfo.sessionAvatar || ''
      }
      showEditAvatarDialog.value = true
    }
  } catch (error) {
    console.error('获取群详情失败:', error)
    ElMessage.error('获取群详情失败')
  }
}

// 处理修改头像确认
const handleEditAvatarConfirm = async () => {
  if (!currentSession.value) return
  if (!selectedAvatarFile.value) {
    ElMessage.warning('请先选择新头像')
    return
  }

  actionLoading.value.updating = true
  try {
    const avatarUrl = await chatStore.uploadGroupAvatar(currentSession.value.sessionId, selectedAvatarFile.value)
    if (avatarUrl) {
      // 同时更新群组信息中的头像
      await chatStore.updateGroupInfo(currentSession.value.sessionId, {
        ...editGroupForm.value,
        avatarUrl
      })
      ElMessage.success('群头像修改成功')
      showEditAvatarDialog.value = false
      selectedAvatarFile.value = null
    }
  } catch (error) {
    console.error('修改群头像失败:', error)
    ElMessage.error('修改群头像失败')
  } finally {
    actionLoading.value.updating = false
  }
}

const selectedAvatarFile = ref(null)
const handleAvatarChange = (file) => {
  const isJPGorPNG = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png'
  const isLt10M = file.raw.size / 1024 / 1024 < 10

  if (!isJPGorPNG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传头像图片大小不能超过 10MB!')
    return false
  }

  selectedAvatarFile.value = file.raw
  editGroupForm.value.avatarUrl = URL.createObjectURL(file.raw)
}

// 处理邀请
const handleInviteConfirm = async () => {
  if (!inviteUserIdsInput.value.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }
  
  const userIds = inviteUserIdsInput.value
    .split(/[,\n]/)
    .map(id => id.trim())
    .filter(id => id)
    .map(id => Number(id))
  
  if (userIds.some(id => isNaN(id))) {
    ElMessage.warning('请输入有效的数字ID')
    return
  }

  actionLoading.value.inviting = true
  try {
    const result = await chatStore.inviteToGroup(currentSession.value.sessionId, userIds)
    if (result) {
      showInviteDialog.value = false
      inviteUserIdsInput.value = ''
    }
  } finally {
    actionLoading.value.inviting = false
  }
}

// 处理修改群信息
const handleEditGroupConfirm = async () => {
  if (!editGroupForm.value.groupName.trim()) {
    ElMessage.warning('群名称不能为空')
    return
  }

  actionLoading.value.updating = true
  try {
    const result = await chatStore.updateGroupInfo(currentSession.value.sessionId, {
      groupName: editGroupForm.value.groupName.trim(),
      description: editGroupForm.value.description.trim(),
      requiredApproval: editGroupForm.value.requiredApproval
    })
    if (result) {
      showEditGroupDialog.value = false
      // 成功后手动同步一次，确保最新
      if (currentSession.value) {
        currentSession.value.sessionName = editGroupForm.value.groupName.trim()
        currentSession.value.description = editGroupForm.value.description.trim()
        currentSession.value.requireApproval = editGroupForm.value.requiredApproval
      }
    }
  } finally {
    actionLoading.value.updating = false
  }
}

// 处理发布公告
const handlePublishNoticeConfirm = async () => {
  if (!noticeInput.value.trim()) {
    ElMessage.warning('公告内容不能为空')
    return
  }

  const result = await chatStore.publishGroupNotice(currentSession.value.sessionId, noticeInput.value.trim())
  if (result) {
    showPublishNoticeDialog.value = false
    noticeInput.value = ''
    ElMessage.success('公告发布成功')
  }
}
const showGroupSidebar = ref(false)

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

const handlePinSession = (session) => {
  chatStore.pinSession(session.sessionId)
}

const handleDeleteSession = (session) => {
  ElMessageBox.confirm(
    '确定要删除该会话吗？删除后聊天记录将无法恢复。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    chatStore.deleteSession(session.sessionId)
  }).catch(() => {})
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

// 处理踢出群成员
const handleKickMember = async (userId) => {
  try {
    if (!currentSession.value) return
    
    await chatStore.kickFromGroup(currentSession.value.sessionId, [userId])
    ElMessage.success('移出成功')
    
    // 重新加载成员列表
    await chatStore.loadSessionMembers(currentSession.value.sessionId)
  } catch (error) {
    console.error('踢出成员失败:', error)
    ElMessage.error(error.message || '踢出成员失败')
  }
}

// 处理各种聊天面板动作
const handleChatPanelAction = async (command) => {
  try {
    const type = typeof command === 'string' ? command : command.type
    
    switch (type) {
      case 'clear':
        // 清空聊天记录
        ElMessage.success('聊天记录已清空')
        break
      case 'delete':
        // 删除会话
        if (currentSession.value) {
          await chatStore.deleteSession(currentSession.value.sessionId)
          ElMessage.success('会话已删除')
        }
        break
      case 'dissolve':
        await handleDissolveGroup()
        break
      case 'exit':
        await handleLeaveGroup()
        break
      default:
        console.log('未知的操作类型:', type)
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
  height: calc(100vh - 64px); /* 减去导航栏高度 */
  background-color: #f5f7fa;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chat-container {
  display: flex;
  width: 100%;
  max-width: 1600px;
  height: 100%;
  min-height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative; /* 关键：使 drawer 相对于此容器定位 */
}

/* 覆盖 Element Plus 抽屉样式，使其在容器内展示 */
:deep(.member-drawer) {
  position: absolute !important;
}

:deep(.el-overlay) {
  position: absolute !important;
}

.chat-main-area {
  flex: 1;
  display: flex;
  height: 100%;
  overflow: hidden;
}

.chat-panel-main {
  flex: 1;
  min-width: 0;
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

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-edit-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.notice-view-content {
  padding: 10px 0;
}

.notice-text {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}

.member-drawer :deep(.el-drawer__body) {
  padding: 0;
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