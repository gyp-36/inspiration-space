<!-- 消息输入组件 -->
<template>
  <div class="message-input-container">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="left-tools">
        <el-tooltip content="表情" placement="top">
          <el-button link class="tool-btn" @click="showEmoji = !showEmoji">
            <el-icon><Menu /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="图片" placement="top">
          <el-button link class="tool-btn" @click="triggerImageSelect">
            <el-icon><Picture /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="文件" placement="top">
          <el-button link class="tool-btn" @click="triggerFileSelect">
            <el-icon><FolderOpened /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="语音消息" placement="top">
          <el-button link class="tool-btn" @click="toggleRecording">
            <el-icon :class="{ 'recording-icon': isRecording }"><Microphone /></el-icon>
          </el-button>
        </el-tooltip>
      </div>

      <div class="right-tools">
        <span class="char-count" :class="{ 'over-limit': localMessage.length > 500 }">
          {{ localMessage.length }}/500
        </span>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <textarea 
        ref="textareaRef"
        v-model="localMessage"
        class="message-textarea"
        placeholder="请输入内容，回车发送 (Ctrl+Enter 换行)..."
        :rows="1"
        @keydown="handleKeyDown"
        @input="adjustHeight"
        @paste="handlePaste"
      ></textarea>
      
      <el-button 
        type="primary" 
        class="send-btn" 
        :disabled="!canSend"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>

    <!-- 隐藏的文件选择 -->
    <input type="file" ref="imageInput" hidden accept="image/*" @change="onFileChange($event, 'IMAGE')" />
    <input type="file" ref="fileInput" hidden @change="onFileChange($event, 'FILE')" />

    <!-- 表情选择器弹窗 -->
    <div v-if="showEmoji" class="emoji-picker" v-click-outside="() => showEmoji = false">
      <div class="emoji-list">
        <span 
          v-for="emoji in emojis" 
          :key="emoji" 
          class="emoji-item"
          @click="insertEmoji(emoji)"
        >
          {{ emoji }}
        </span>
      </div>
    </div>

    <!-- 录音状态 -->
    <div v-if="isRecording" class="recording-overlay">
      <div class="recording-content">
        <div class="recording-wave">
          <span></span><span></span><span></span><span></span><span></span>
        </div>
        <div class="recording-text">正在录音...</div>
        <div class="recording-actions">
          <el-button type="danger" circle icon="Close" @click="cancelRecording" />
          <el-button type="success" circle icon="Check" @click="finishRecording" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { 
  Menu, Picture, FolderOpened, Microphone, 
  Close, Check 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'send', 'file-select'])

// 状态
const localMessage = ref('')
const showEmoji = ref(false)
const isRecording = ref(false)
const textareaRef = ref(null)
const imageInput = ref(null)
const fileInput = ref(null)

// 表情数据
const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
  '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '😋',
  '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐',
  '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😔',
  '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵',
  '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓', '🧐', '😕',
  '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧'
]

const canSend = computed(() => {
  return localMessage.value.trim().length > 0 && localMessage.value.length <= 500
})

// 监听
watch(() => props.modelValue, (val) => {
  localMessage.value = val
  nextTick(adjustHeight)
})

watch(localMessage, (val) => {
  emit('update:modelValue', val)
})

// 方法
const adjustHeight = () => {
  if (!textareaRef.value) return
  textareaRef.value.style.height = 'auto'
  const scrollHeight = textareaRef.value.scrollHeight
  textareaRef.value.style.height = Math.min(scrollHeight, 150) + 'px'
}

const handleKeyDown = (e) => {
  if (e.key === 'Enter') {
    if (e.ctrlKey) {
      // Ctrl + Enter 换行
      const start = e.target.selectionStart
      const end = e.target.selectionEnd
      localMessage.value = localMessage.value.substring(0, start) + '\n' + localMessage.value.substring(end)
      nextTick(() => {
        e.target.selectionStart = e.target.selectionEnd = start + 1
        adjustHeight()
      })
    } else {
      // Enter 发送
      e.preventDefault()
      handleSend()
    }
  }
}

const handleSend = () => {
  if (!canSend.value) return
  emit('send')
  localMessage.value = ''
  nextTick(adjustHeight)
}

const insertEmoji = (emoji) => {
  const el = textareaRef.value
  const start = el.selectionStart
  const end = el.selectionEnd
  localMessage.value = localMessage.value.substring(0, start) + emoji + localMessage.value.substring(end)
  showEmoji.value = false
  nextTick(() => {
    el.focus()
    el.setSelectionRange(start + emoji.length, start + emoji.length)
    adjustHeight()
  })
}

const triggerImageSelect = () => imageInput.value.click()
const triggerFileSelect = () => fileInput.value.click()

const onFileChange = (e, type) => {
  const file = e.target.files[0]
  if (!file) return
  
  if (type === 'IMAGE' && !file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }

  emit('file-select', { file, type })
  e.target.value = '' // 重置
}

const handlePaste = (e) => {
  const items = e.clipboardData?.items
  if (!items) return
  
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        emit('file-select', { file, type: 'IMAGE' })
        e.preventDefault()
      }
    }
  }
}

// 录音相关
const toggleRecording = () => {
  if (isRecording.value) {
    finishRecording()
  } else {
    isRecording.value = true
    ElMessage.info('开始录音...')
  }
}

const cancelRecording = () => {
  isRecording.value = false
  ElMessage.info('已取消录音')
}

const finishRecording = () => {
  isRecording.value = false
  ElMessage.success('录音已发送 (演示)')
}

// 指令
const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = (event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value()
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el) {
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}

onMounted(() => {
  localMessage.value = props.modelValue
  adjustHeight()
})
</script>

<style scoped>
.message-input-container {
  padding: 12px 16px;
  background-color: #fff;
  border-top: 1px solid #f0f0f0;
  position: relative;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.left-tools {
  display: flex;
  gap: 8px;
}

.tool-btn {
  font-size: 20px;
  color: #606266;
  padding: 4px;
  transition: all 0.2s;
}

.tool-btn:hover {
  color: #409eff;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.recording-icon {
  color: #f56c6c;
  animation: pulse 1.5s infinite;
}

.char-count {
  font-size: 12px;
  color: #909399;
}

.char-count.over-limit {
  color: #f56c6c;
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.message-textarea {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  max-height: 150px;
  min-height: 36px;
  outline: none;
  transition: all 0.2s;
  background-color: #f5f7fa;
}

.message-textarea:focus {
  border-color: #409eff;
  background-color: #fff;
}

.send-btn {
  padding: 0 20px;
  height: 36px;
  border-radius: 8px;
}

/* 表情选择器 */
.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  padding: 12px;
  width: 320px;
  z-index: 100;
  margin-bottom: 8px;
}

.emoji-list {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background-color: #f5f7fa;
}

/* 录音蒙层 */
.recording-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255,255,255,0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.recording-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.recording-wave {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 30px;
}

.recording-wave span {
  width: 4px;
  height: 100%;
  background-color: #409eff;
  border-radius: 2px;
  animation: wave 1s infinite ease-in-out;
}

.recording-wave span:nth-child(2) { animation-delay: 0.2s; }
.recording-wave span:nth-child(3) { animation-delay: 0.4s; }
.recording-wave span:nth-child(4) { animation-delay: 0.6s; }
.recording-wave span:nth-child(5) { animation-delay: 0.8s; }

@keyframes wave {
  0%, 100% { transform: scaleY(0.4); }
  50% { transform: scaleY(1); }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
  100% { transform: scale(1); opacity: 1; }
}

.recording-text {
  font-size: 14px;
  color: #606266;
}

.recording-actions {
  display: flex;
  gap: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .message-input-container {
    padding: 8px 12px;
  }
  
  .emoji-picker {
    width: 280px;
    left: 8px;
  }
  
  .emoji-list {
    grid-template-columns: repeat(7, 1fr);
  }
}
</style>
