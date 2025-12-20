
<!-- components/WorkItem.vue: 增强版作品组件，添加描述、标签、统计信息等 -->
<template>
  <div class="work-item" :id="`work-item-${work.id}`">
    <div class="work-header">
      <img :src="work.image" alt="作品封面" class="work-image" />
      <div class="access-badge" :class="accessBadgeClass">{{ accessText }}</div>
    </div>
    
    <div class="work-content">
      <h3 class="work-title">{{ work.title }}</h3>
      
      <p class="work-author">作者: {{ work.author }}</p>
      
      <p class="work-description" v-if="work.description">{{ work.description }}</p>
      
      <div class="tags-container" v-if="work.tags && work.tags.length">
        <span class="tag" v-for="(tag, index) in work.tags" :key="index">{{ tag }}</span>
      </div>
      
      <div class="stats-row">
        <div class="stat-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
            <path d="m8 2.748-.717-.737C5.6.281 2.514.878 1.4 3.053c-.523 1.023-.641 2.5.314 4.385.92 1.815 2.834 3.989 6.286 6.357 3.452-2.368 5.365-4.542 6.286-6.357.955-1.886.838-3.362.314-4.385C13.486.878 10.4.28 8.717 2.01L8 2.748zM8 15C-1 8.5 3.5 1.75 7.5 1.75S14 8.5 8 15z"/>
          </svg>
          <span>{{ formatNumber(work.likes) }}</span>
        </div>
        <div class="stat-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
            <path d="M2 5.5a.5.5 0 0 1 .5-.5h11a.5.5 0 0 1 0 1h-11a.5.5 0 0 1-.5-.5zm0 4a.5.5 0 0 1 .5-.5h11a.5.5 0 0 1 0 1h-11a.5.5 0 0 1-.5-.5zm0 4a.5.5 0 0 1 .5-.5h11a.5.5 0 0 1 0 1h-11a.5.5 0 0 1-.5-.5z"/>
          </svg>
          <span>{{ formatNumber(work.favorites) }}</span>
        </div>
        <div class="stat-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
            <path d="M0 1.5A.5.5 0 0 1 .5 1H2a.5.5 0 0 1 .485.379L2.89 3H14.5a.5.5 0 0 1 .49.598l-1 5a.5.5 0 0 1-.465.401l-9.397.471L4.415 11.1H13a.5.5 0 0 1 0 1H4a.5.5 0 0 1-.491-.408L2.01 3.607 1.61 2H.5a.5.5 0 0 1-.5-.5zM3.102 4l.84 4.479 9.144-.459L13.89 4H3.102zM5 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm7 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-7 1a1 1 0 1 1 0 2 1 1 0 0 1 0-2zm7 0a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
          </svg>
          <span>{{ formatNumber(work.purchases) }}</span>
        </div>
      </div>
      
      <div class="meta-info">
        <span class="publish-date">{{ formatDate(work.publishDate) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, computed, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  work: {
    type: Object,
    required: true,
    default: () => ({
      id: 0,
      title: '作品标题',
      image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png',
      author: '作者名称',
      description: '这是一个简短的作品描述，展示了作品的主要特点和创作背景。',
      tags: ['设计', '创意', '艺术'],
      likes: 1250,
      favorites: 845,
      purchases: 240,
      accessType: 'free', // 'free', 'vip', 'paid'
      publishDate: new Date().toISOString()
    })
  }
});

// 计算属性：访问策略文本
const accessText = computed(() => {
  switch(props.work.accessType) {
    case 'free': return '免费';
    case 'vip': return 'VIP';
    case 'paid': return '付费';
    default: return '免费';
  }
});

// 计算属性：访问策略样式类
const accessBadgeClass = computed(() => {
  return `access-${props.work.accessType}`;
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
  const element = document.getElementById(`work-item-${props.work.id}`);
  
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

.work-title {
  font-size: 18px;
  margin: 0 0 8px 0;
  color: #222;
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 44px;
}

.work-author {
  color: #666;
  font-size: 14px;
  margin: 0 0 10px 0;
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
  justify-content: space-between;
  padding: 10px 0;
  margin: 0 -15px;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 15px;
  color: #666;
  font-size: 13px;
}

.stat-item svg {
  width: 14px;
  height: 14px;
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
