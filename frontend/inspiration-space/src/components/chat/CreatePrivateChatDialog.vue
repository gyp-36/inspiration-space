<template>
  <div class="create-private-dialog">
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="用户 ID" prop="userId">
        <el-input 
          v-model.number="form.userId" 
          placeholder="请输入对方的用户 ID" 
          class="custom-input"
        />
      </el-form-item>
    </el-form>
    
    <div class="dialog-footer">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">发起私聊</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['create-chat', 'cancel'])

const formRef = ref(null)
const form = reactive({
  userId: ''
})

const rules = {
  userId: [
    { required: true, message: '请输入用户 ID', trigger: 'blur' },
    { type: 'number', message: '用户 ID 必须为数字', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      emit('create-chat', form.userId)
    }
  })
}
</script>

<style scoped>
.create-private-dialog {
  padding: 10px 5px;
}

.custom-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
  padding: 8px 12px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.dialog-footer {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
}
</style>
