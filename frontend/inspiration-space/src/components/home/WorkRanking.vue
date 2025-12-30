<!-- components/Ranking.vue: 修改为全屏布局 -->
<template>
  <div class="ranking card-panel">
    <div class="panel-header">
      <h2>热门榜单</h2>
      <a href="#" class="more-link">更多 ></a>
    </div>
    
    <div class="ranking-list" v-if="!loading && ranking.length > 0">
      <div 
        v-for="item in ranking" 
        :key="item.rank" 
        class="ranking-item"
        :class="{ 'top-rank': item.rank <= 3 }"
        @click="goToDetail(item.workId)"
      >
        <div class="rank-badge" :class="`rank-${item.rank}`">
          {{ item.rank }}
        </div>
        
        <div class="work-cover">
          <img :src="item.cover || defaultImage" alt="封面" @error="handleImageError" />
        </div>
        
        <div class="work-info">
          <div class="work-name">{{ item.name }}</div>
          <div class="work-meta">
            <span class="score-label">热度</span>
            <span class="score-value">{{ item.score }}</span>
          </div>
        </div>
        
        <div class="trend-icon">
          <svg v-if="item.rank <= 2" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#f5222d" viewBox="0 0 16 16">
            <path d="M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z" transform="rotate(180 8 8)"/>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#52c41a" viewBox="0 0 16 16">
            <path d="M8 15a.5.5 0 0 0 .5-.5V2.707l3.146 3.147a.5.5 0 0 0 .708-.708l-4-4a.5.5 0 0 0-.708 0l-4 4a.5.5 0 1 0 .708.708L7.5 2.707V14.5a.5.5 0 0 0 .5.5z" transform="rotate(90 8 8)"/> <!-- Just a dash -->
             <rect x="4" y="7" width="8" height="2" rx="1" />
          </svg>
        </div>
      </div>
    </div>
    <div v-else-if="loading" class="loading-placeholder">
      正在加载排行榜...
    </div>
    <div v-else class="empty-placeholder">
      暂无排行榜数据
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { workService } from '@/services/workService';

const router = useRouter();
const ranking = ref([]);
const loading = ref(false);
const defaultImage = 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png';

const fetchRanking = async () => {
  loading.value = true;
  try {
    const res = await workService.getWorkLikeRank(5);
    if (res) {
      ranking.value = res.map((item, index) => ({
        rank: index + 1,
        name: item.title || item.name || `作品 ${item.workId || item.id || index + 1}`,
        score: item.likeCount ?? item.score ?? 0,
        workId: item.workId || item.id,
        cover: item.cover || item.coverUrl || item.cover_url || item.cover_URL || defaultImage
      }));
    }
  } catch (error) {
    console.error('获取热门榜单失败:', error);
  } finally {
    loading.value = false;
  }
};

const goToDetail = (workId) => {
  if (workId) {
    router.push(`/work/${workId}`);
  }
};

const handleImageError = (e) => {
  if (e.target.src !== defaultImage) {
    e.target.src = defaultImage;
  }
};

onMounted(() => {
  fetchRanking();
});
</script>

<style scoped>
.card-panel {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0,0,0,0.03);
  height: fit-content;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.card-panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1a1a1a;
  font-weight: 700;
}

.more-link {
  font-size: 13px;
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}

.more-link:hover {
  color: #0066cc;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 10px;
  transition: all 0.2s ease;
  cursor: pointer;
  background-color: #fff;
  border: 1px solid transparent;
}

.ranking-item:hover {
  background-color: #f8faff;
  border-color: #e6f7ff;
}

.rank-badge {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-weight: 700;
  font-size: 14px;
  margin-right: 12px;
  flex-shrink: 0;
  color: #666;
  background: #f0f0f0;
}

.work-cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
  background-color: #f5f5f5;
  border: 1px solid #f0f0f0;
}

.work-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.ranking-item:hover .work-cover img {
  transform: scale(1.1);
}

/* 前三名样式 */
.rank-1 { color: white; background: #ff4d4f; }
.rank-2 { color: white; background: #ff7a45; }
.rank-3 { color: white; background: #ffa940; }

.work-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.work-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.work-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.score-label {
  color: #999;
}

.score-value {
  color: #ff4d4f;
  font-weight: 600;
  font-family: 'Roboto', sans-serif;
}

.trend-icon {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.loading-placeholder, .empty-placeholder {
  padding: 40px 0;
  text-align: center;
  color: #8c8c8c;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .ranking {
    width: 100%;
    margin-top: 24px;
  }
}
</style>
