<template>
  <div class="work-showcase">
    <div class="showcase-header">
      <div class="header-left">
        <h2>探索创意</h2>
        <p class="subtitle">发现来自全球创作者的精彩作品</p>
      </div>
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

    <div class="works-grid">
      <WorkItem v-for="work in works" :key="work.id" :work="work" />
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref } from 'vue';
import WorkItem from '@/components/home/WorkItem.vue';

const props = defineProps({
  works: {
    type: Array,
    required: true
  }
});

const currentCategory = ref('all');

const categories = [
  { id: 'all', name: '全部' },
  { id: 'design', name: '设计' },
  { id: 'dev', name: '编程' },
  { id: 'art', name: '艺术' },
  { id: '3d', name: '3D模型' },
  { id: 'sound', name: '音效' }
];
</script>

<style scoped>
.work-showcase {
  width: 100%;
}

.showcase-header {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-left h2 {
  font-size: 28px;
  margin: 0 0 8px 0;
  color: #1a1a1a;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 15px;
}

.view-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-bottom: 4px; /* 预留阴影空间 */
}

.filter-btn {
  padding: 8px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.filter-btn:hover {
  border-color: #b0b0b0;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.05);
  color: #333;
}

.filter-btn.active {
  background: linear-gradient(135deg, #0066cc 0%, #0052a3 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(0, 102, 204, 0.25);
}

.filter-btn.active:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 102, 204, 0.35);
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  width: 100%;
}

/* 响应式调整 */
@media (min-width: 768px) {
  .showcase-header {
    flex-direction: row;
    justify-content: space-between;
    align-items: flex-end;
  }
}
</style>