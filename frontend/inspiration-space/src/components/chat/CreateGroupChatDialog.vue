<template>
  <div class="create-group-container">
    <div class="dialog-header">
      <div class="header-icon">
        <el-icon><ChatDotRound /></el-icon>
      </div>
      <div class="header-text">
        <h3>创建新群聊</h3>
        <p>开启属于你们的精彩讨论空间</p>
      </div>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="custom-form">
      <el-form-item label="群聊名称" prop="groupName">
        <el-input 
          v-model="form.groupName" 
          placeholder="给群聊起个响亮的名字..." 
          maxlength="50" 
          show-word-limit
          class="modern-input"
        >
          <template #prefix>
            <el-icon><EditPen /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      
      <el-form-item label="群聊描述" prop="description">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          placeholder="简单介绍一下群聊的主题或规则（可选）" 
          maxlength="200" 
          show-word-limit
          :rows="3"
          class="modern-textarea"
        />
      </el-form-item>
      
      <el-form-item label="加入方式" prop="requiredApproval">
        <div class="approval-options">
          <div 
            :class="['approval-card', { active: form.requiredApproval === 'NEED_NOT_APPROVAL' }]"
            @click="form.requiredApproval = 'NEED_NOT_APPROVAL'"
          >
            <div class="card-icon open">
              <el-icon><Unlock /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">公开加入</div>
              <div class="card-desc">任何人都可以直接加入群聊</div>
            </div>
            <div class="card-check" v-if="form.requiredApproval === 'NEED_NOT_APPROVAL'">
              <el-icon><Check /></el-icon>
            </div>
          </div>

          <div 
            :class="['approval-card', { active: form.requiredApproval === 'NEED_APPROVAL' }]"
            @click="form.requiredApproval = 'NEED_APPROVAL'"
          >
            <div class="card-icon lock">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">审核加入</div>
              <div class="card-desc">新成员加入需经群主审核同意</div>
            </div>
            <div class="card-check" v-if="form.requiredApproval === 'NEED_APPROVAL'">
              <el-icon><Check /></el-icon>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>
    
    <div class="footer-actions">
      <el-button @click="$emit('cancel')" class="btn-cancel">取消</el-button>
      <el-button 
        type="primary" 
        :loading="loading" 
        @click="handleSubmit"
        class="btn-submit"
      >
        立即开启
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ChatDotRound, EditPen, Unlock, Lock, Check } from '@element-plus/icons-vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['create-group', 'cancel'])

const formRef = ref(null)
const form = reactive({
  groupName: '',
  description: '',
  requiredApproval: 'NEED_NOT_APPROVAL'
})

const rules = {
  groupName: [
    { required: true, message: '群聊名称是必填项哦', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度请保持在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述内容太长啦，请精简到 200 字以内', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      emit('create-group', { ...form })
    }
  })
}
</script>

<style scoped>
.create-group-container {
  padding: 0 10px;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #67c23a, #95d475);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
}

.header-text h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  padding-bottom: 10px;
}

.modern-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 8px 12px;
  transition: all 0.3s;
}

.modern-input :deep(.el-input__wrapper:hover) {
  background-color: #f0f2f5;
  border-color: #dcdfe6;
}

.modern-input :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #67c23a;
  box-shadow: 0 0 0 1px #67c23a inset !important;
}

.modern-textarea :deep(.el-textarea__inner) {
  background-color: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 12px;
  transition: all 0.3s;
  resize: none;
}

.modern-textarea :deep(.el-textarea__inner:hover) {
  background-color: #f0f2f5;
  border-color: #dcdfe6;
}

.modern-textarea :deep(.el-textarea__inner:focus) {
  background-color: #ffffff;
  border-color: #67c23a;
  box-shadow: 0 0 0 1px #67c23a inset !important;
}

/* 入群审核卡片样式 */
.approval-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.approval-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  background-color: #f8f9fb;
  border: 1.5px solid transparent;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.approval-card:hover {
  background-color: #f0f2f5;
}

.approval-card.active {
  background-color: #f0f9eb;
  border-color: #67c23a;
}

.card-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.card-icon.open {
  background-color: #e1f3d8;
  color: #67c23a;
}

.card-icon.lock {
  background-color: #fef0f0;
  color: #f56c6c;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 0;
}

.card-desc {
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-check {
  position: absolute;
  right: 8px;
  top: 8px;
  color: #67c23a;
  font-size: 14px;
  font-weight: bold;
}

.footer-actions {
  margin-top: 36px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 500;
}

.btn-submit {
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #67c23a, #529b2e) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.btn-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
}

.btn-submit:active {
  transform: translateY(0);
}

/* 表单项间距 */
.el-form-item {
  margin-bottom: 24px;
}
</style>
