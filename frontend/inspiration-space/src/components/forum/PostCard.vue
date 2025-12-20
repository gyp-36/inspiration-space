<template>
  <div class="post-card" @click="handleCardClick">
    <!-- 头部：用户信息 -->
    <div class="post-header">
      <div class="user-section">
        <!-- 用户头像 -->
        <div class="avatar-wrapper" @click.stop="navigateToUser">
          <img 
            v-if="user.avatar" 
            :src="user.avatar" 
            class="avatar" 
            alt="用户头像"
            @error="handleAvatarError"
          >
          <div v-else class="avatar-placeholder">
            {{ user.username ? user.username.charAt(0).toUpperCase() : 'U' }}
          </div>
        </div>
        
        <!-- 用户信息 -->
        <div class="user-info">
          <h3 class="username" @click.stop="navigateToUser">{{ user.username || '加载中...' }}</h3>
          <span class="post-date">{{ formattedDate }}</span>
        </div>
      </div>
      
      <!-- 标题在用户信息下方 -->
      <h2 class="post-title">{{ title }}</h2>
    </div>
    
    <!-- 内容区域 -->
    <div class="post-content">
      <p class="content-text">{{ shortContent }}</p>
      <a v-if="link" :href="link" class="post-link" target="_blank">{{ formatLink(link) }}</a>
    </div>
    
    <!-- 底部统计信息 -->
    <div class="post-footer">
      <div class="views">
        <el-icon><View /></el-icon> {{ views }}
      </div>
      <div class="stats">
        <span @click.stop="handleToggleLike" class="stat-item like" :class="{ active: localIsLiked }">
          <span class="icon-text">{{ localIsLiked ? '❤️' : '🤍' }} {{ localLikes }}</span>
        </span>
        <span @click.stop="handleToggleFavorite" class="stat-item favorite" :class="{ active: localIsFavorited }">
          <el-icon><StarFilled v-if="localIsFavorited" /><Star v-else /></el-icon>
          {{ localFavorites }}
        </span>
        <span @click.stop="handleToggleComments" class="stat-item comment">
          <el-icon><ChatDotRound /></el-icon>
          {{ commentCount || '评论' }}
          <el-icon class="dropdown-icon" :class="{ rotated: showComments }"><CaretBottom /></el-icon>
        </span>
        <span @click.stop="handleRepost" class="stat-item share">
          <el-icon><Share /></el-icon> {{ localShares }}
        </span>
      </div>
    </div>
    
    <!-- 评论区展开 -->
    <div v-if="showComments" class="comments-section" @click.stop>
      <div class="comments-placeholder">
        <p>评论区暂未开放</p>
      </div>
    </div>
    
    <!-- 用户信息加载状态指示器 -->
    <div v-if="isLoadingUser" class="user-loading">
      <div class="skeleton-avatar"></div>
      <div class="skeleton-username"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/services/userService';
import { 
  publicApiCall,
  privateApiCall 
} from '@/services/apiClient';
import { 
  View, 
  Star, 
  StarFilled, 
  Share, 
  ChatDotRound,
  CaretBottom
} from '@element-plus/icons-vue'

// 定义组件接收的 props (与后端PostSimpleVo结构匹配)
const props = defineProps({
  id: {
    type: Number,
    required: true
  },
  userId: {
    type: Number,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  content: {
    type: String,
    required: true
  },
  createAt: {
    type: [Date, String],
    required: true
  },
  like: {
    type: Number,
    default: 0
  },
  isLiked: {
    type: Boolean,
    default: false
  },
  collect: {
    type: Number,
    default: 0
  },
  isFavorited: {
    type: Boolean,
    default: false
  },
  view: {
    type: Number,
    default: 0
  },
  repost: {
    type: Number,
    default: 0
  },
  link: {
    type: String,
    default: ''
  },
  commentCount: {
    type: Number,
    default: 0
  }
});

// 定义组件触发的自定义事件
const emit = defineEmits(['post-liked', 'post-favorited', 'card-clicked', 'reposted', 'user-loaded']);

// 响应式状态
const user = ref({
  username: '',
  avatar: ''
});
const isLoadingUser = ref(false);
const showComments = ref(false);
const router = useRouter();

// 本地状态（
const localLikes = ref(props.like);
const localIsLiked = ref(props.isLiked);
const localFavorites = ref(props.collect);
const localIsFavorited = ref(props.isFavorited);
const localShares = ref(props.repost);

// 计算属性 - 格式化日期
const formattedDate = computed(() => {
  // 检查 createAt 是否存在
  if (!props.createAt) {
    return '未知日期';
  }
  
  const date = typeof props.createAt === 'string' ? new Date(props.createAt) : props.createAt;
  
  // 检查日期是否有效
  if (!date || isNaN(date.getTime())) {
    return '无效日期';
  }
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
});

// 计算属性 - 截取内容前50字
const shortContent = computed(() => {
  let text = props.content;
  if (text.length > 50) {
    return text.slice(0, 50) + '...';
  }
  return text;
});

// 方法：格式化链接显示
const formatLink = (url) => {
  try {
    const domain = new URL(url).hostname;
    return domain.replace('www.', '');
  } catch (e) {
    return url.length > 30 ? url.substring(0, 30) + '...' : url;
  }
};

// 方法：加载用户信息
const loadUser = async () => {
    console.log('🚀 loadUser 开始执行，userId:', props.userId);
  try {
    const userProfile = await getUserInfo(props.userId);
    console.log('加载用户信息：', userProfile);
   
    user.value = {
      username: userProfile.username || 'apc',
      avatar: userProfile.avatar
    };
  
    
    // 通知父组件（仅用于调试/统计，非必须）
    emit('user-loaded', {
      postId: props.id,
      userId: props.userId,
      user: user.value
    });
  } catch (error) {
    console.warn(`用户 ${props.userId} 信息加载失败，使用默认值`);
    user.value = {
      username: 'apc',
      avatar: ''
    };
  
  }
};

// 方法：处理头像加载错误
const handleAvatarError = (e) => {
  e.target.style.display = 'none';
};

// 方法：跳转到用户主页
const navigateToUser = (e) => {
  e.stopPropagation();
  if (user.value.username) {
    router.push(`/user/${props.userId}`);
  }
};

// 优化：将乐观更新和API调用分离
const withOptimisticUpdate = async (updateFn, apiCall) => {
  const originalState = {
    likes: localLikes.value,
    isLiked: localIsLiked.value,
    favorites: localFavorites.value,
    isFavorited: localIsFavorited.value,
    shares: localShares.value
  };

  try {
    // 1. 乐观更新UI
    updateFn();
    
    // 2. 调用API
    await apiCall();
    
    return true;
  } catch (error) {
    // 3. 仅在非登录错误时回滚
    if (error.message !== '未登录') {
      // 恢复原始状态
      localLikes.value = originalState.likes;
      localIsLiked.value = originalState.isLiked;
      localFavorites.value = originalState.favorites;
      localIsFavorited.value = originalState.isFavorited;
      localShares.value = originalState.shares;
    }
    return false;
  }
};

// 方法：切换点赞状态 - 精简版
const handleToggleLike = async () => {
  await withOptimisticUpdate(
    () => {
      localIsLiked.value = !localIsLiked.value;
      localLikes.value += localIsLiked.value ? 1 : -1;
    },
    async () => {
      await privateApiCall(localIsLiked.value ? '/like' : '/unlike', 'POST', { postId: props.id });
      emit('post-liked', {
        id: props.id,
        liked: localIsLiked.value,
        newCount: localLikes.value
      });
    }
  );
};

// 方法：切换收藏状态 - 精简版
const handleToggleFavorite = async () => {
  await withOptimisticUpdate(
    () => {
      localIsFavorited.value = !localIsFavorited.value;
      localFavorites.value += localIsFavorited.value ? 1 : -1;
    },
    async () => {
      await privateApiCall(localIsFavorited.value ? '/collect' : '/uncollect', 'POST', { postId: props.id });
      emit('post-favorited', {
        id: props.id,
        favorited: localIsFavorited.value,
        newCount: localFavorites.value
      });
    }
  );
};

// 方法：处理转发 - 精简版
const handleRepost = async (e) => {
  e.stopPropagation();
  
  const success = await withOptimisticUpdate(
    () => {
      localShares.value += 1;
    },
    async () => {
      await privateApiCall('/repost', 'POST', { postId: props.id });
      emit('reposted', {
        id: props.id,
        newCount: localShares.value
      });
    }
  );
  
  if (success) {
    alert('转发成功！');
  }
};

const handleToggleComments = () => {
  showComments.value = !showComments.value;
};

// 方法：处理卡片点击
const handleCardClick = (e) => {
  // 阻止统计区域的点击冒泡
  if (e.target.closest('.stats') || e.target.closest('.views')) {
    return;
  }
  
  emit('card-clicked', props.id);
};

// 生命周期钩子
onMounted(() => {
  console.log('🔄 组件挂载，调用 loadUser');
  loadUser();
});

// 监听userId变化（例如在分页时）
watch(() => props.userId, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    loadUser();
  }
});
</script>


<style scoped>
.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { background-color: #e0e0e0; }
  50% { background-color: #f0f0f0; }
  100% { background-color: #e0e0e0; }
}
.post-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  height: auto; /* Change from fixed height to auto to accommodate comments */
  min-height: 280px; /* Minimum height for consistency */
  display: flex;
  flex-direction: column;
  background: white;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  border-color: #1890ff;
}

/* 头部区域 */
.post-header {
  margin-bottom: 12px;
}

/* Ensure content doesn't overflow when comments are hidden but maintains structure */
.post-content {
  margin-bottom: 14px;
  color: #333;
  line-height: 1.5;
  flex: 1;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
  margin-top: auto; /* Push to bottom if height is fixed, but here we use min-height */
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px; /* Increased gap for icons */
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13.5px;
}

.dropdown-icon {
  transition: transform 0.3s;
  margin-left: 2px;
}

.dropdown-icon.rotated {
  transform: rotate(180deg);
}

.comments-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee;
  animation: fadeIn 0.3s ease;
}

.comments-placeholder {
  text-align: center;
  color: #999;
  padding: 20px 0;
  font-size: 14px;
  background: #f9f9f9;
  border-radius: 8px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}

.like.active {
  color: #ff4d4f;
}

.favorite.active {
  color: #faad14;
}

/* Fix icon alignment */
.el-icon {
  vertical-align: middle;
  font-size: 16px;
}


/* 用户信息部分 - 头像和文字垂直排列 */
.user-section {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.avatar-wrapper {
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
  flex-shrink: 0;
  background-color: #f5f5f5;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-weight: bold;
  font-size: 18px;
}

.user-info {
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.username {
  margin: 0 0 2px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
  transition: color 0.2s;
}

.username:hover {
  color: #1890ff;
}

.post-date {
  font-size: 13px;
  color: #8c8c8c;
}

/* 标题在用户信息下方 */
.post-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  max-height: 42px;
}

/* 内容区域 */
.post-content {
  margin-bottom: 14px;
  color: #333;
  line-height: 1.5;
}

.content-text {
  margin: 8px 0 0;
  font-size: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.post-link {
  display: inline-block;
  font-size: 13px;
  color: #1890ff;
  text-decoration: none;
  background: #f0f7ff;
  padding: 3px 8px;
  border-radius: 4px;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 6px;
}

.post-link:hover {
  text-decoration: underline;
}

/* 底部统计信息 */
.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
  margin-top: 2px;
}

.views {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.stats {
  display: flex;
  gap: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 13.5px;
}

.stat-item:hover {
  color: #1890ff;
  background: #f0f7ff;
  transform: scale(1.05);
}

.like {
  color: v-bind('localIsLiked ? "#ff4d4f" : ""');
}

.like:hover {
  color: #ff4d4f;
}

.favorite {
  color: v-bind('localIsFavorited ? "#faad14" : ""');
}

.favorite:hover {
  color: #faad14;
}

/* 用户加载状态 */
.user-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  margin-bottom: 8px;
}

.skeleton-username {
  width: 80px;
  height: 16px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>