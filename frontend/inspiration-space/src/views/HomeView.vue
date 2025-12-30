<!-- views/HomeView.vue: 修改为三栏布局，左侧添加用户排行榜 -->
<template>
  <div class="home-view">
    <main class="main-container">
      <div class="content-wrapper">
        <!-- 左侧栏：用户排行榜 -->
        <aside class="sidebar-left">
          <UserRanking />
        </aside>
        
        <!-- 中间栏：作品展示 -->
        <section class="content-center">
          <WorkShowcase />
        </section>
        
        <!-- 右侧栏：作品排行榜 -->
        <aside class="sidebar-right">
          <Ranking />
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import WorkShowcase from '@/components/home/WorkShowcase.vue';
import Ranking from '@/components/home/WorkRanking.vue';
import UserRanking from '@/components/home/UserRanking.vue';

const isLogin = ref(false);

// 监听登录状态切换事件
const handleToggleLogin = (newStatus) => {
  console.log('父组件收到登录状态更新:', newStatus);
  isLogin.value = newStatus; // 关键：更新父组件的isLogin状态
};
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa; /* 柔和的背景色 */
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.main-container {
  flex: 1;
  width: 100%;
  max-width: 1440px; /* 限制最大宽度，保持大屏美观 */
  margin: 0 auto;
  padding: 24px 20px;
  box-sizing: border-box;
}
/* Rest of the styles remain the same */
.content-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start; /* 顶部对齐 */
}

.sidebar-left,
.sidebar-right {
  flex: 0 0 300px; /* 固定侧边栏宽度 */
  position: sticky;
  top: 24px; /* 简单的粘性定位 */
}

.content-center {
  flex: 1;
  min-width: 0; /* 防止flex子项溢出 */
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .content-wrapper {
    flex-wrap: wrap;
  }
  
  .sidebar-left {
    display: none; /* 中等屏幕隐藏左侧，或调整位置 */
  }

  .sidebar-right {
    display: none; /* 暂时隐藏右侧，或调整 */
  }
  
  .content-center {
    flex: 0 0 100%;
  }
}

/* 如果需要更精细的响应式，比如在大屏显示三栏，中屏显示两栏 */
@media (max-width: 1400px) and (min-width: 1000px) {
  .content-wrapper {
    justify-content: center;
  }
  .sidebar-left {
    display: none;
  }
  .sidebar-right {
    flex: 0 0 280px;
  }
}
</style>
