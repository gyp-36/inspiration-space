<!-- 消息输入组件 -->
<template>
  <div class="message-input-container">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="left-tools">
        <el-tooltip content="表情" placement="top">
          <el-button link class="tool-btn" @click="showEmoji = !showEmoji">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5zm-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11zm3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z"/>
            </svg>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="文件" placement="top">
          <el-button link class="tool-btn" @click="triggerFileSelect">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
              <path d="M20 6h-8l-2-2H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm0 12H4V8h16v10z"/>
            </svg>
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
        placeholder="请输入内容..."
        :rows="3"
        @keydown="handleKeyDown"
        @input="adjustHeight"
        @paste="handlePaste"
      ></textarea>
      
      <div class="send-action">
        <button 
          class="send-btn-new" 
          :disabled="!canSend"
          @click="handleSend"
        >
          发送(S)
        </button>
      </div>
    </div>

    <!-- 隐藏的文件选择 -->
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
  textareaRef.value.style.height = Math.max(100, Math.min(scrollHeight, 200)) + 'px'
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

const triggerFileSelect = () => fileInput.value.click()

const onFileChange = (e, type) => {
  const file = e.target.files[0]
  if (!file) return
  
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
  padding: 8px 16px 12px;
  background-color: #1a1a1a;
  border-top: 1px solid #2d2d2d;
  position: relative;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.left-tools {
  display: flex;
  gap: 12px;
}

.tool-btn {
  font-size: 20px;
  color: #a0a0a0;
  padding: 6px;
  transition: all 0.2s;
  height: auto;
}

.tool-btn:hover {
  color: #ffffff;
  background-color: #333333;
  border-radius: 4px;
}

.char-count {
  font-size: 12px;
  color: #666;
}

.char-count.over-limit {
  color: #f56c6c;
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-textarea {
  width: 100%;
  border: none;
  background-color: transparent;
  color: #e0e0e0;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  min-height: 100px;
  max-height: 200px;
  outline: none;
  padding: 4px 0;
}

.message-textarea::placeholder {
  color: #555;
}

.send-action {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
}

.send-btn-new {
  background-color: #2b2b2b;
  color: #666;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn-new:not(:disabled):hover {
  background-color: #3d3d3d;
  color: #fff;
}

.send-btn-new:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 表情选择器 */
.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 16px;
  background: #252525;
  border: 1px solid #333;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  width: 300px;
  padding: 12px;
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

.emoji-list::-webkit-scrollbar {
  width: 4px;
}

.emoji-list::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 2px;
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
  background: #333;
}
</style>
