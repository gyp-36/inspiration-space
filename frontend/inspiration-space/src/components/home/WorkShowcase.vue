<template>
  <div class="work-showcase">
    <div class="showcase-header">
      <div class="title-section">
        <h1 class="art-title">探索创意</h1>
        <p class="subtitle">发现来自全球创作者的精彩作品</p>
      </div>
      
      <div class="action-section">
        <button class="publish-btn" @click="handlePublishClick">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
            <path d="M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4z"/>
          </svg>
          发布作品
        </button>
      </div>

      <div class="filter-section">
        <div class="view-options">
          <button 
            v-for="category in categories" 
            :key="category.id"
            class="filter-btn" 
            :class="{ active: currentCategory === category.id }"
            @click="currentCategory = category.id"
          >
            {{ category.name }}
          </button>
        </div>
      </div>
    </div>

    <div class="works-container" v-if="!loading && works.length > 0">
      <div class="works-grid">
        <WorkItem v-for="work in works" :key="work.workId" :work="work" />
      </div>
      
      <!-- 分页控制 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
    
    <div v-else-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>正在加载精彩作品...</p>
    </div>
    
    <div v-else class="empty-state">
      <p>暂无相关作品</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import WorkItem from '@/components/home/WorkItem.vue';
import { workService } from '@/services/workService';
import { useUserStore } from '@/stores/userStore';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const works = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const totalPages = ref(1);
const pageSize = 10; // 2x5 布局，每页10个

const handlePublishClick = () => {
  router.push('/publish');
};

const currentCategory = ref('all');

// TODO: 后续对接分类筛选接口，目前仅作为示例
const categories = [
  { id: 'all', name: '全部' },
  { id: 'design', name: '设计' },
  { id: 'dev', name: '编程' },
  { id: 'art', name: '艺术' },
  { id: '3d', name: '3D模型' },
  { id: 'sound', name: '音效' }
];

const fetchWorks = async (page = 1) => {
  loading.value = true;
  const keyword = route.query.keyword;
  
  try {
    let res;
    if (keyword) {
      res = await workService.searchWorks(keyword, page, pageSize);
    } else {
      res = await workService.getPublicWorks(page, pageSize);
    }
    
    if (res) {
      works.value = res.records || [];
      currentPage.value = res.current || 1;
      totalPages.value = res.pages || 1;
    }
  } catch (error) {
    console.error('获取作品列表失败:', error);
  } finally {
    loading.value = false;
  }
};

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    fetchWorks(page);
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

// 监听分类变化（目前仅重置分页，后续可加筛选逻辑）
watch(currentCategory, () => {
  currentPage.value = 1;
  fetchWorks(1);
});

// 监听路由参数变化（特别是搜索关键词）
watch(() => route.query.keyword, () => {
  currentPage.value = 1;
  fetchWorks(1);
});

// 监听登录状态变化，自动刷新列表（以获取最新的点赞、收藏状态）
watch(() => userStore.isLogin, () => {
  console.log('检测到登录状态变化，正在刷新作品列表...');
  fetchWorks(currentPage.value);
});

// 监听路由路径变化，如果进入首页则刷新
watch(() => route.path, (newPath) => {
  if (newPath === '/' || newPath === '/home') {
    fetchWorks(1);
  }
});

onMounted(() => {
  fetchWorks();
});
</script>

<style scoped>
.work-showcase {
  width: 100%;
  padding: 10px 0;
}

.showcase-header {
  margin-bottom: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.title-section {
  margin-bottom: 4px;
}

.art-title {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #1a1a1a 0%, #4a4a4a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
  position: relative;
  display: inline-block;
}

.art-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background: #0066cc;
  border-radius: 2px;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 15px;
  font-weight: 400;
}

.action-section {
  margin-bottom: 4px;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  background: linear-gradient(135deg, #0066cc 0%, #0052a3 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 102, 204, 0.2);
}

.publish-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 15px rgba(0, 102, 204, 0.3);
}

.publish-btn:active {
  transform: translateY(0);
}

.filter-section {
  width: 100%;
  display: flex;
  justify-content: center;
}

.view-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  padding: 2px;
}

.filter-btn {
  padding: 6px 18px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.3s ease;
  font-weight: 500;
}

.filter-btn:hover {
  border-color: #0066cc;
  color: #0066cc;
  background: #f0f7ff;
}

.filter-btn.active {
  background: #0066cc;
  color: white;
  border-color: #0066cc;
  box-shadow: 0 4px 10px rgba(0, 102, 204, 0.15);
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 默认2列 */
  gap: 24px;
  width: 100%;
}

@media (min-width: 1024px) {
    .works-grid {
        grid-template-columns: repeat(2, 1fr); /* 保持2列 */
    }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .art-title {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 14px;
  }
  
  .works-grid {
    grid-template-columns: 1fr; /* 手机端单列 */
    gap: 16px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 15px;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f5f5;
}

.page-btn:not(:disabled):hover {
  border-color: #0066cc;
  color: #0066cc;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 40px 0;
  color: #666;
}

.loader {
  border: 3px solid #f3f3f3;
  border-radius: 50%;
  border-top: 3px solid #0066cc;
  width: 30px;
  height: 30px;
  -webkit-animation: spin 1s linear infinite; /* Safari */
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>