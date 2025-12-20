<!-- views/HomeView.vue: 修改为三栏布局，左侧添加用户排行榜 -->
<template>
  <div class="home-view">
    <main class="main-container">
      <div class="content-wrapper">
        <!-- 左侧栏：用户排行榜 -->
        <aside class="sidebar-left">
          <UserRanking :users="users" />
        </aside>
        
        <!-- 中间栏：作品展示 -->
        <section class="content-center">
          <WorkShowcase :works="works" />
        </section>
        
        <!-- 右侧栏：作品排行榜 -->
        <aside class="sidebar-right">
          <Ranking :ranking="ranking" />
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import WorkShowcase from '@/components/home/WorkShowcase.vue';
import Ranking from '@/components/home/Ranking.vue';
import UserRanking from '@/components/home/UserRanking.vue';

const isLogin = ref(false);

// 模拟用户数据
const users = reactive([
  { rank: 1, name: '创作者A', avatar: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', points: 1250, level: '大师' },
  { rank: 2, name: '设计师B', avatar: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', points: 1180, level: '专家' },
  { rank: 3, name: '编程C', avatar: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', points: 1050, level: '专家' },
  { rank: 4, name: '艺术D', avatar: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', points: 980, level: '高级' },
  { rank: 5, name: '写手E', avatar: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', points: 920, level: '高级' }
]);

// 模拟作品数据
const works = reactive([
  { id: 1, title: '未来城市概念设计', image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', author: 'CyberArtist', description: '探索2077年的城市形态，结合赛博朋克与生态建筑理念。', tags: ['科幻', '建筑', '概念'], likes: 2450, favorites: 1200, purchases: 45, accessType: 'paid', publishDate: '2025-05-12' },
  { id: 2, title: '极简主义UI套件', image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', author: 'DesignPro', description: '一套适用于移动端的极简风格UI组件库，包含100+图标。', tags: ['UI', '移动端', '极简'], likes: 1890, favorites: 890, purchases: 120, accessType: 'vip', publishDate: '2025-05-10' },
  { id: 3, title: '治愈系插画集', image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', author: 'WarmHeart', description: '记录日常生活中的温暖瞬间，治愈你的心灵。', tags: ['插画', '治愈', '生活'], likes: 3200, favorites: 1500, purchases: 0, accessType: 'free', publishDate: '2025-05-08' },
  { id: 4, title: '3D角色模型：战士', image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', author: 'ModelMaster', description: '高精度游戏角色模型，包含完整的骨骼绑定和贴图。', tags: ['3D', '游戏', '角色'], likes: 1560, favorites: 670, purchases: 88, accessType: 'paid', publishDate: '2025-05-05' },
  { id: 5, title: '水墨风网页模板', image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', author: 'WebNinja', description: '结合传统水墨艺术与现代网页交互的独特模板。', tags: ['网页', '水墨', '模板'], likes: 980, favorites: 450, purchases: 32, accessType: 'vip', publishDate: '2025-05-01' },
  { id: 6, title: '赛博朋克音效包', image: 'https://bailian-bmp-pre.oss-cn-hangzhou.aliyuncs.com/public/system_agent/PlaceHolder.png', author: 'SoundWave', description: '适用于科幻游戏和电影的音效素材库，包含200+音效。', tags: ['音效', '赛博朋克', '素材'], likes: 1250, favorites: 560, purchases: 65, accessType: 'paid', publishDate: '2025-04-28' }
]);

// 模拟作品排行榜数据
const ranking = reactive([
  { rank: 1, name: '未来城市概念设计', score: 98 },
  { rank: 2, name: '治愈系插画集', score: 95 },
  { rank: 3, name: '极简主义UI套件', score: 92 },
  { rank: 4, name: '3D角色模型：战士', score: 88 },
  { rank: 5, name: '赛博朋克音效包', score: 85 }
]);

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
