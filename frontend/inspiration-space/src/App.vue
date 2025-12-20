<!-- App.vue: 修改为全屏布局 -->
<template>
  <div class="app-container">
    <Navigation />
    <router-view />
    <Footer />
  </div>

</template>

<script setup>
import Footer from './components/home/Footer.vue';
import Navigation from './components/home/Navigation.vue';
import { useUserStore } from '@/stores/userStore';
const userStore = useUserStore();
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Arial', sans-serif;
  width: 100%;

}
</style>
