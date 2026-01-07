<template>
  <div 
    class="post-card" 
    :class="{ 
      'no-images': !imageUrls || imageUrls.length === 0,
      'no-product': !productUrl,
      'pure-text': (!imageUrls || imageUrls.length === 0) && !productUrl
    }"
    @click="handleCardClick"
  >
    <!-- 顶部：头像 + 用户名 + 时间 -->
    <div class="card-header">
      <div class="user-info" @click.stop="handleAvatarClick">
        <img 
          v-if="user.avatar" 
          :src="user.avatar" 
          class="user-avatar" 
          :title="user.username"
        >
        <div v-else class="user-avatar-placeholder">
          {{ user.username ? user.username.charAt(0).toUpperCase() : 'U' }}
        </div>
        <div class="user-meta">
          <span class="username">{{ user.username || '匿名用户' }}</span>
          <span class="publish-time">{{ formattedDate }}</span>
        </div>
      </div>
      <!-- 分类标签 -->
      <div v-if="category !== null && category !== undefined" class="category-tag">
        {{ categoryLabel }}
      </div>
    </div>

    <!-- 中部内容区：标题 + 内容 + 图片 + 链接 -->
    <div class="post-body">
      <h2 class="post-title">{{ title }}</h2>
      <p class="post-content">{{ shortContent }}</p>

      <!-- 图片展示 (最多3张) -->
      <div v-if="imageUrls && imageUrls.length > 0" class="post-images">
        <div 
          v-for="(url, index) in imageUrls.slice(0, 3)" 
          :key="index"
          class="image-item"
          :class="{ 'single-image': imageUrls.length === 1 }"
        >
          <el-image 
            :src="url" 
            fit="cover" 
            :preview-src-list="imageUrls"
            :initial-index="index"
            preview-teleported
            loading="lazy"
          />
        </div>
      </div>

      <!-- 商品链接展示 -->
      <div v-if="productUrl" class="product-link-wrapper" @click.stop="handleProductClick">
        <div class="product-link-card">
          <el-icon class="link-icon"><LinkIcon /></el-icon>
          <span class="link-text">{{ productUrl }}</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 分割线 -->
    <div v-if="!hideFooter" class="card-divider"></div>

    <!-- 底部：交互统计项 -->
    <div v-if="!hideFooter" class="card-footer">
      <div 
        class="footer-item action-item" 
        :class="{ active: localIsLiked }"
        @click.stop="handleToggleLike"
      >
        <el-icon><Pointer /></el-icon>
        <span class="count">{{ localLikes }}</span>
      </div>

      <div 
        class="footer-item action-item" 
        :class="{ active: localIsFavorited }"
        @click.stop="handleToggleFavorite"
      >
        <el-icon v-if="localIsFavorited"><StarFilled /></el-icon>
        <el-icon v-else><Star /></el-icon>
        <span class="count">{{ localFavorites }}</span>
      </div>

      <div class="footer-item">
        <el-icon><ChatDotRound /></el-icon>
        <span class="count">{{ commentCount || 0 }}</span>
      </div>

      <div class="footer-item">
        <el-icon><View /></el-icon>
        <span class="count">{{ view }}</span>
      </div>

      <div 
        class="footer-item action-item"
        :class="{ active: localIsReposted }"
        @click.stop="handleRepost"
      >
        <el-icon><Share /></el-icon>
        <span class="count">{{ localShares }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getCategoryLabel } from '@/constants/forumConstants';
import { 
  likePost, 
  unlikePost, 
  collectPost, 
  uncollectPost, 
  repostPost 
 } from '@/services/forumService';
import { getAvatar as getAvatarApi } from '@/services/userService';
 import { 
   View, 
  Star, 
  StarFilled, 
  ChatDotRound,
  Pointer,
  Document,
  Share,
  Link as LinkIcon,
  ArrowRight
} from '@element-plus/icons-vue'

// 定义组件接收的 props (与后端PostSimpleVo结构匹配)
const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  },
  userId: {
    type: [String, Number],
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
  imageUrls: {
    type: Array,
    default: () => []
  },
  productUrl: {
    type: String,
    default: ''
  },
  category: {
    type: [Number, Object, String],
    default: null
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
  isReposted: {
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
  },
  username: {
    type: String,
    default: ''
  },
  avatar: {
    type: String,
    default: ''
  },
  hideFooter: {
    type: Boolean,
    default: false
  }
});

// 定义组件触发的自定义事件
const emit = defineEmits(['post-liked', 'post-favorited', 'card-clicked', 'reposted']);

// 响应式状态
const user = ref({
  username: props.username || '',
  avatar: props.avatar || ''
});

// 处理头像：如果后端返回的是文件名，则调用 API 获取完整 URL
const fetchAvatarIfNeeded = async () => {
  if (user.value.avatar && !user.value.avatar.startsWith('http') && !user.value.avatar.startsWith('data:')) {
    try {
      // 如果 avatar 是文件名，调用后端 getAvatar 接口
      const avatarUrl = await getAvatarApi(props.userId);
      if (avatarUrl) {
        user.value.avatar = avatarUrl;
      }
    } catch (error) {
      console.warn('获取用户头像失败:', error);
    }
  }
};

onMounted(() => {
  fetchAvatarIfNeeded();
});

// 监听 props 变化，更新本地 user 状态
 watch(() => [props.username, props.avatar], ([newUsername, newAvatar]) => {
   user.value.username = newUsername;
   user.value.avatar = newAvatar;
   fetchAvatarIfNeeded();
 }, { immediate: true });

 // 监听 props 变化，更新本地交互状态
watch(() => props.isLiked, (newVal) => {
  localIsLiked.value = newVal;
});
watch(() => props.like, (newVal) => {
  localLikes.value = newVal;
});
watch(() => props.isFavorited, (newVal) => {
  localIsFavorited.value = newVal;
});
watch(() => props.collect, (newVal) => {
  localFavorites.value = newVal;
});
watch(() => props.repost, (newVal) => {
  localShares.value = newVal;
});
watch(() => props.isReposted, (newVal) => {
  localIsReposted.value = newVal;
});

const showComments = ref(false);
const router = useRouter();

// 本地状态
const localLikes = ref(props.like);
const localIsLiked = ref(props.isLiked);
const localFavorites = ref(props.collect);
const localIsFavorited = ref(props.isFavorited);
const localShares = ref(props.repost);
const localIsReposted = ref(props.isReposted);

const categoryLabel = computed(() => getCategoryLabel(props.category));

// 计算属性 - 格式化日期
const formattedDate = computed(() => {
  if (!props.createAt) return '未知日期';
  const date = typeof props.createAt === 'string' ? new Date(props.createAt) : props.createAt;
  if (!date || isNaN(date.getTime())) return '无效日期';

  const now = new Date();
  const diff = now - date;
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) return '刚刚';
  if (diff < hour) return Math.floor(diff / minute) + '分钟前';
  if (diff < day) return Math.floor(diff / hour) + '小时前';
  if (diff < day * 3) return Math.floor(diff / day) + '天前';

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
});

// 计算属性 - 截取内容前150字 (增加字数以支持动态布局)
const shortContent = computed(() => {
  let text = props.content || '';
  if (text.length > 150) {
    return text.slice(0, 150) + '...';
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

// 方法：处理头像加载错误
const handleAvatarError = (e) => {
  e.target.style.display = 'none';
};

// 方法：处理头像点击 (预留方法：显示用户信息)
const handleAvatarClick = (e) => {
  e.stopPropagation();
  if (props.userId) {
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
    updateFn();
    await apiCall();
    return true;
  } catch (error) {
    if (error.message !== '未登录') {
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
      if (localIsLiked.value) {
        await likePost(props.id);
      } else {
        await unlikePost(props.id);
      }
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
      if (localIsFavorited.value) {
        await collectPost(props.id);
      } else {
        await uncollectPost(props.id);
      }
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
      localIsReposted.value = true;
    },
    async () => {
      await repostPost(props.id);
      emit('reposted', {
        id: props.id,
        newCount: localShares.value,
        isReposted: true
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

const handleProductClick = () => {
  if (props.productUrl) {
    window.open(props.productUrl, '_blank');
  }
};

// 方法：处理卡片点击
const handleCardClick = (e) => {
  // 如果点击的是页脚的操作项或图片预览，不触发卡片点击
  if (e.target.closest('.card-footer') || e.target.closest('.el-image-viewer__wrapper')) {
    return;
  }
  
  emit('card-clicked', props.id);
};
</script>


<style scoped>
.post-card {
  background: rgba(255, 255, 255, 0.7); /* 半透明背景 */
  backdrop-filter: blur(8px); /* 毛玻璃效果 */
  -webkit-backdrop-filter: blur(8px);
  border-radius: 12px;
  padding: 12px 14px; /* 进一步缩小内边距，让卡片更紧凑 */
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px; /* 缩小主板块间距 */
  position: relative;
  overflow: hidden;
  height: auto; 
}

.post-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.85);
  border-color: rgba(59, 130, 246, 0.2);
}

/* 头部布局：头像左上，名时右侧 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.category-tag {
  font-size: 10px;
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px; /* 缩小头像和名称间距 */
}

.user-avatar, .user-avatar-placeholder {
  width: 28px; /* 缩小头像尺寸 */
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.user-avatar-placeholder {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 0px;
}

.username {
  font-size: 13px; 
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.publish-time {
  font-size: 10px;
  color: #94a3b8;
}

/* 标题与内容区容器 */
.post-body {
  display: flex;
  flex-direction: column;
  gap: 6px; /* 缩小内容间距 */
  /* 移除 flex-grow: 1 和 margin-bottom，彻底防止撑开空白 */
}

/* 标题与内容 */
.post-title {
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1; 
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: all 0.3s ease;
}

.no-images .post-title {
  font-size: 16px; /* 无图时标题稍微放大 */
}

.pure-text .post-title {
  -webkit-line-clamp: 2; /* 纯文字时标题允许两行 */
  font-size: 17px;
}

.post-content {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2; 
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 动态调整正文行数 */
.no-images .post-content {
  -webkit-line-clamp: 4; /* 无图时显示4行 */
}

.no-product .post-content {
  -webkit-line-clamp: 3; /* 无链接时显示3行 */
}

.no-images.no-product .post-content {
  -webkit-line-clamp: 6; /* 既无图也无链接时显示6行，填补空白 */
  font-size: 14px;
  line-height: 1.6;
}

/* 图片展示区域 */
.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px; /* 缩小图片间距 */
}

.image-item {
  aspect-ratio: 1;
  border-radius: 6px; /* 减小圆角 */
  overflow: hidden;
  background-color: #f1f5f9;
}

.image-item.single-image {
  grid-column: span 3;
  aspect-ratio: 21/9; /* 默认更扁的比例 */
  max-height: 200px; 
  transition: all 0.3s ease;
}

.no-product .image-item.single-image {
  aspect-ratio: 16/9; /* 无链接时，单图可以稍微高一点 */
  max-height: 250px;
}

.image-item :deep(.el-image) {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.image-item:hover :deep(.el-image) {
  transform: scale(1.05);
}

/* 商品链接卡片 */
.product-link-wrapper {
  /* 移除 margin-bottom，交给父级 gap 处理 */
}

.product-link-card {
  display: flex;
  align-items: center;
  padding: 6px 12px; /* 缩小内边距 */
  background: linear-gradient(90deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-link-card:hover {
  background: linear-gradient(90deg, #e0f2fe 0%, #bae6fd 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(186, 230, 253, 0.4);
}

.link-icon {
  font-size: 14px; /* 缩小图标 */
  color: #0284c7;
  margin-right: 8px;
}

.link-text {
  flex: 1;
  font-size: 12px; /* 缩小字号 */
  color: #0369a1;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
}

.arrow-icon {
  font-size: 12px;
  color: #0284c7;
}

/* 分割线 */
.card-divider {
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(0, 0, 0, 0.04), transparent);
  /* 移除 margin-bottom */
}

/* 底部交互 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 0px;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 12px; /* 缩小字号 */
  transition: all 0.2s;
  padding: 4px 8px; /* 缩小内边距 */
  border-radius: 6px;
}

.action-item {
  cursor: pointer;
}

.action-item:hover {
  background: rgba(59, 130, 246, 0.05);
  color: #3b82f6;
}

.action-item.active {
  color: #3b82f6;
  font-weight: 700;
}

.action-item.active .el-icon {
  transform: scale(1.1);
}

.count {
  font-weight: 500;
  font-family: 'Inter', sans-serif;
}
</style>
