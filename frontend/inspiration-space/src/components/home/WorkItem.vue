
<!-- components/WorkItem.vue: 增强版作品组件，添加描述、标签、统计信息等 -->
<template>
  <div class="work-item" ref="workItemRef" @click="handleCardClick">
    <div class="work-header">
      <img :src="work.coverUrl || defaultImage" alt="作品封面" class="work-image" @error="handleImageError" />
      <div class="access-badge" :class="accessBadgeClass" v-if="accessText">{{ accessText }}</div>
    </div>
    
    <div class="work-content">
      <div class="work-title-row">
        <h3 class="work-title">{{ work.title || '无标题' }}</h3>
        <div class="price-container" v-if="isPaid">
          <span class="title-price">¥{{ work.price }}</span>
          <button class="purchase-btn" @click.stop="handleBuy">购买</button>
        </div>
      </div>
      
      <div class="work-author-row" @click.stop="handleAuthorClick">
        <img :src="work.authorAvatar || defaultAvatar" class="author-avatar" alt="作者头像" @error="handleAvatarError"/>
        <span class="work-author">{{ work.authorName || '匿名用户' }}</span>
      </div>
      
      <p class="work-description" v-if="work.description">{{ work.description }}</p>
      
      <div class="tags-container" v-if="displayTags && displayTags.length">
        <span class="tag" v-for="(tag, index) in displayTags" :key="index">{{ tag }}</span>
      </div>
      
      <div class="stats-row">
        <div class="stat-item" title="浏览">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/>
          </svg>
          <span>{{ formatNumber(work.viewCount || 0) }}</span>
        </div>
        
        <div class="stat-item interactive" :class="{ active: work.isLiked }" title="点赞" @click.stop="handleToggleLike">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" :fill="work.isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
          </svg>
          <span>{{ formatNumber(work.likeCount || 0) }}</span>
        </div>
        
        <div class="stat-item interactive" :class="{ active: work.isCollected }" title="收藏" @click.stop="handleToggleCollect">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" :fill="work.isCollected ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
          <span>{{ formatNumber(work.collectCount || 0) }}</span>
        </div>
        
        <!-- 暂时移除评论数显示 -->
        <!-- <div class="stat-item" title="评论">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>{{ formatNumber(work.commentCount || 0) }}</span>
        </div> -->
        
        <div class="stat-item" title="购买" v-if="work.purchaseCount > 0">
           <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
             <circle cx="8" cy="21" r="1"/><circle cx="19" cy="21" r="1"/>
             <path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"/>
           </svg>
           <span>{{ formatNumber(work.purchaseCount || 0) }}</span>
        </div>
      </div>
      
      <div class="meta-info">
        <span class="publish-date">{{ formatDate(work.publishedAt || work.published_at) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { workService } from '@/services/workService';
import { useUserStore } from '@/stores/userStore';
import { ElMessage } from 'element-plus';

const defaultImage = 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png';
const defaultAvatar = 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png';
const workItemRef = ref(null);
const userStore = useUserStore();
const router = useRouter();

const props = defineProps({
  work: {
    type: Object,
    required: true,
    default: () => ({
      workId: 0,
      title: '作品标题',
      coverUrl: '',
      authorId: 0,
      authorName: '作者名称',
      authorAvatar: '',
      description: '这是一个简短的作品描述，展示了作品的主要特点和创作背景。',
      tags: [],
      likeCount: 0,
      viewCount: 0,
      collectCount: 0,
      commentCount: 0,
      purchaseCount: 0,
      price: 0,
      accessStrategy: 'FREE', // 后端对应 AccessStrategy 枚举: FREE, MEMBER_FREE, PAY
      accessType: 'free',     // 兼容旧字段
      publishedAt: new Date().toISOString(),
      isLiked: false,
      isCollected: false
    })
  }
});

// 计算属性：是否付费
const isPaid = computed(() => {
  const strategy = props.work.accessStrategy || props.work.accessType || 'FREE';
  const strategyUpper = String(strategy).toUpperCase();
  return strategyUpper === 'PAY' || strategyUpper === 'PAID';
});

// 优化：乐观更新逻辑
const withOptimisticUpdate = async (updateFn, apiCall) => {
  const originalState = {
    isLiked: props.work.isLiked,
    likeCount: props.work.likeCount,
    isCollected: props.work.isCollected,
    collectCount: props.work.collectCount
  };

  try {
    updateFn();
    await apiCall();
  } catch (error) {
    // 恢复原始状态
    props.work.isLiked = originalState.isLiked;
    props.work.likeCount = originalState.likeCount;
    props.work.isCollected = originalState.isCollected;
    props.work.collectCount = originalState.collectCount;
    
    if (error.response?.status === 401) {
      ElMessage.warning('请先登录后再操作');
    } else {
      ElMessage.error('操作失败，请重试');
    }
  }
};

// 处理点赞切换
const handleToggleLike = async (e) => {
  e.stopPropagation();
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再点赞');
    return;
  }
  
  await withOptimisticUpdate(
    () => {
      props.work.isLiked = !props.work.isLiked;
      props.work.likeCount += props.work.isLiked ? 1 : -1;
    },
    async () => {
      await workService.toggleLike(props.work.workId);
    }
  );
};

// 处理收藏切换
const handleToggleCollect = async (e) => {
  e.stopPropagation();
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再收藏');
    return;
  }
  
  await withOptimisticUpdate(
    () => {
      props.work.isCollected = !props.work.isCollected;
      props.work.collectCount += props.work.isCollected ? 1 : -1;
    },
    async () => {
      await workService.toggleCollect(props.work.workId);
    }
  );
};

// 处理用户头像点击跳转
const handleAuthorClick = (e) => {
  e.stopPropagation();
  if (props.work.authorId) {
    router.push(`/user/${props.work.authorId}`);
  }
};

const handleCardClick = () => {
  router.push(`/work/${props.work.workId}`);
};

const handleBuy = (e) => {
  e.stopPropagation();
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再购买');
    return;
  }
  // 跳转到作品详情页进行购买，或者直接在这里触发购买逻辑
  // 按照通常逻辑，点击购买建议跳转到详情页查看详情后再确认购买，或者弹出支付确认
  // 这里先实现跳转到详情页并携带购买参数，或者直接提示
  router.push(`/work/${props.work.workId}?action=buy`);
};

// 处理图片加载失败
const handleImageError = (e) => {
  e.target.src = defaultImage;
};

const handleAvatarError = (e) => {
  e.target.src = defaultAvatar;
};

// 计算属性：兼容处理标签
const displayTags = computed(() => {
  if (props.work.tags && Array.isArray(props.work.tags)) {
    return props.work.tags;
  }
  // 如果后端返回的是逗号分隔的字符串
  if (typeof props.work.tags === 'string' && props.work.tags) {
    return props.work.tags.split(',').map(t => t.trim());
  }
  return [];
});

// 计算属性：访问策略文本
const accessText = computed(() => {
  const strategy = props.work.accessStrategy || props.work.accessType || 'FREE';
  const strategyUpper = String(strategy).toUpperCase();
  
  switch(strategyUpper) {
    case 'FREE': return '免费';
    case 'MEMBER_FREE': 
    case 'VIP': return 'VIP';
    case 'PAY':
    case 'PAID': return '付费';
    default: return '免费';
  }
});

// 计算属性：访问策略样式类
const accessBadgeClass = computed(() => {
  const strategy = props.work.accessStrategy || props.work.accessType || 'FREE';
  const strategyUpper = String(strategy).toUpperCase();
  if (strategyUpper === 'MEMBER_FREE' || strategyUpper === 'VIP') return 'access-vip';
  if (strategyUpper === 'PAY' || strategyUpper === 'PAID') return 'access-paid';
  return 'access-free';
});

// 格式化数字（1000 -> 1k）
const formatNumber = (num) => {
  if (num >= 1000) {
    return (num/1000).toFixed(1) + 'k';
  }
  return num;
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  const now = new Date();
  const diffTime = Math.abs(now - date);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays === 1) return '昨天';
  if (diffDays < 7) return `${diffDays}天前`;
  if (diffDays < 30) return `${Math.floor(diffDays/7)}周前`;
  if (diffDays < 365) return `${Math.floor(diffDays/30)}月前`;
  
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric' 
  });
};

onMounted(() => {
  const element = workItemRef.value;
  
  if (!element) return;

  // 悬浮效果
  const handleMouseEnter = () => {
    element.style.transform = 'scale(1.03)';
    element.style.boxShadow = '0 4px 20px rgba(0,0,0,0.15)';
  };

  const handleMouseLeave = () => {
    element.style.transform = 'scale(1)';
    element.style.boxShadow = '0 2px 10px rgba(0,0,0,0.05)';
  };

  element.addEventListener('mouseenter', handleMouseEnter);
  element.addEventListener('mouseleave', handleMouseLeave);

  onUnmounted(() => {
    element.removeEventListener('mouseenter', handleMouseEnter);
    element.removeEventListener('mouseleave', handleMouseLeave);
  });
});
</script>

<style scoped>
.work-item {
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.work-header {
  position: relative;
}

.work-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform 0.3s;
}

.work-item:hover .work-image {
  transform: scale(1.05);
}

.access-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  z-index: 2;
}

.access-free {
  background-color: #1890ff;
}

.access-vip {
  background-color: #faad14;
}

.access-paid {
  background-color: #fa541c;
}

.work-content {
  padding: 15px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.work-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.work-title {
  font-size: 18px;
  margin: 0;
  color: #222;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 44px;
  flex: 1;
}

.title-price {
  color: #fa541c;
  font-weight: 700;
  font-size: 16px;
  white-space: nowrap;
}

.price-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.purchase-btn {
  background-color: #fa541c;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.purchase-btn:hover {
  background-color: #ff7a45;
}

.work-author-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 0;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
}

.work-author {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.work-description {
  color: #444;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 40px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  background-color: #f0f5ff;
  color: #1890ff;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.stats-row {
  display: flex;
  justify-content: flex-start;
  gap: 12px;
  padding: 12px 0;
  margin: 0;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8c8c8c;
  font-size: 12px;
  transition: all 0.2s;
}

.stat-item.interactive {
  cursor: pointer;
}

.stat-item.interactive:hover {
  color: #1890ff;
}

.stat-item.active {
  color: #ff4d4f;
}

.stat-item.active[title="收藏"] {
  color: #faad14;
}

.stat-item svg {
  width: 16px;
  height: 16px;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  color: #999;
  font-size: 12px;
}

.price-label {
  color: #fa541c;
  font-weight: 600;
  font-size: 14px;
}

.publish-date {
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 悬停效果增强 */
.work-item:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  border-color: #e8e8e8;
}
</style>
