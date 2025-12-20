
<!-- components/UserRanking.vue: 新增的用户排行榜组件 -->
<template>
  <div class="user-ranking card-panel">
    <div class="panel-header">
      <h2>创作者榜单</h2>
      <a href="#" class="more-link">查看全部 ></a>
    </div>
    
    <div class="ranking-list">
      <div 
        v-for="user in users" 
        :key="user.rank" 
        class="ranking-item"
        :class="{ 'top-rank': user.rank <= 3 }"
      >
        <div class="rank-badge" :class="`rank-${user.rank}`">
          <span v-if="user.rank > 3">{{ user.rank }}</span>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
            <path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.589-.388 2.885-.72 3.935z"/>
          </svg>
        </div>
        
        <img :src="user.avatar" alt="用户头像" class="user-avatar" />
        
        <div class="user-details">
          <div class="user-name-row">
            <span class="user-name">{{ user.name }}</span>
            <span class="level-badge">{{ user.level }}</span>
          </div>
          <div class="user-score">积分: {{ user.points }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, onMounted } from 'vue';

const props = defineProps({
  users: {
    type: Array,
    required: true
  }
});

onMounted(() => {
  // 交互逻辑保持不变
  const items = document.querySelectorAll('.ranking-item');
  items.forEach(item => {
    item.addEventListener('click', () => {
      const userName = item.querySelector('.user-name').textContent;
      console.log(`点击了用户: ${userName}`);
    });
  });
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
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  transition: all 0.2s ease;
  cursor: pointer;
  background-color: #fff;
}

.ranking-item:hover {
  background-color: #f8f9fa;
}

.rank-badge {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 700;
  font-size: 14px;
  margin-right: 12px;
  flex-shrink: 0;
  color: #999;
  background: #f5f5f5;
}

/* 前三名样式 */
.rank-1 { color: #ffd700; background: #fffbe6; }
.rank-2 { color: #c0c0c0; background: #f9f9f9; }
.rank-3 { color: #b87333; background: #fff7e6; }

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.level-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e6f7ff, #bae7ff);
  color: #0050b3;
  font-weight: 500;
  white-space: nowrap;
}

.user-score {
  font-size: 12px;
  color: #888;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .user-ranking {
    width: 100%;
    margin-bottom: 24px;
  }
}
</style>
