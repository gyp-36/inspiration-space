<!-- App.vue: 修改为全屏布局 -->
<template>
  <div class="app-container">
    <Navigation />
    <main class="main-content">
      <router-view />
    </main>
    <Footer v-if="!isChatPage" />
  </div>

</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Footer from './components/home/Footer.vue';
import Navigation from './components/home/Navigation.vue';
import { useUserStore } from '@/stores/userStore';

const userStore = useUserStore();
const route = useRoute();

const isChatPage = computed(() => route.path === '/chat');
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Arial', sans-serif;
  width: 100%;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
