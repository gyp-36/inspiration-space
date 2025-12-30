<!-- components/Navigation.vue: 导航组件（与userStore完全对齐版） -->
<template>
  <header class="header">
    <div class="inner-container">
      <!-- 左侧：Logo 和 标题 -->
      <div class="left-section">
        <div class="logo-container" @click="handleNavClick('/')">
          <img src="/logo.png" alt="知创空间 Logo" class="logo" />
          <span class="logo-title">知创空间</span>
        </div>
      </div>

      <!-- 中间：搜索框区域 -->
      <div class="center-section">
        <div v-if="isHomePage" class="search-container">
          <input 
            type="text" 
            placeholder="搜索作品..." 
            class="search-input" 
            ref="searchInputRef"
            @keyup.enter="handleSearch"
          />
          <button class="search-button" @click="handleSearch">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
              <path
                d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 右侧：导航菜单和用户区域 -->
      <div class="right-section">
        <nav class="inline-navbar" aria-label="主导航">
          <ul>
            <li>
              <a href="/" rel="home" title="返回网站首页" @click.prevent="handleNavClick('/')">
                <el-icon><HomeFilled /></el-icon>
                <span>首页</span>
              </a>
            </li>
            <li>
              <a href="/chat" rel="noopener" title="进入实时聊天互动平台" @click.prevent="handleNavClick('/chat')">
                <el-icon><ChatDotRound /></el-icon>
                <span>聊天室</span>
              </a>
            </li>
            <li>
              <a href="/forum" rel="noopener" title="浏览话题讨论区" @click.prevent="handleNavClick('/forum')">
                <el-icon><Postcard /></el-icon>
                <span>论坛</span>
              </a>
            </li>
            <li>
              <a href="/help" rel="noopener" title="查看使用帮助和常见问题" @click.prevent="handleNavClick('/help')">
                <el-icon><QuestionFilled /></el-icon>
                <span>帮助</span>
              </a>
            </li>
          </ul>
        </nav>

        <div class="user-section">
          <!-- 未登录状态 -->
          <template v-if="!userStore.isLogin">
            <div class="login-button-container">
              <button class="login-trigger" @click="userStore.openLoginModal">登录</button>
            </div>
          </template>

          <!-- 已登录状态 -->
          <template v-else>
            <div class="user-avatar-container" 
                 @mouseenter="userStore.toggleDropdown(true)" 
                 @mouseleave="userStore.toggleDropdown(false)">
              <img :src="userAvatar" alt="用户头像" class="user-avatar" />
              <div class="login-status">已登录</div>

              <div class="dropdown-menu" :class="{ 'show': userStore.isDropdownOpen }">
                <ul>
                  <li @click="handleItemClick('profile')">个人中心</li>
                  <li @click="handleItemClick('changeAvatar')">更换头像</li>
                  <li @click="handleItemClick('settings')">设置</li>
                  <li @click="handleItemClick('logout')">退出登录</li>
                </ul>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 引入独立的登录模态框组件 -->
    <LoginModal 
      :is-open="userStore.isModalOpen" 
      :current-view="userStore.currentView" 
      @close="userStore.closeLoginModal" 
      @switchView="userStore.switchView"
      @login="handleLogin" 
      @register="handleRegister" 
      @forgotPassword="handleForgotPassword" />
  </header>
</template>

<script setup>
import { ref, computed, defineEmits, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { HomeFilled, ChatDotRound, Postcard, QuestionFilled } from '@element-plus/icons-vue';
import LoginModal from '@/components/home/LoginModel.vue';
import { useUserStore } from '@/stores/userStore';

// 初始化Pinia Store（核心：所有状态/方法从store获取）
const userStore = useUserStore();
const route = useRoute();
const router = useRouter();

// 定义事件
const emit = defineEmits(['search']);

// 响应式数据（仅保留非store管理的局部状态）
const searchInputRef = ref(null); // 搜索框引用

// 计算属性：判断是否为首页
const isHomePage = computed(() => route.path === '/' || route.path === '/home');

// 计算属性：映射store的头像（带时间戳防缓存）
const userAvatar = computed(() => userStore.avatarUrlWithTimestamp);

// ========== 组件生命周期 ==========
onMounted(() => {
  // 初始化检查登录状态（调用store方法）
  userStore.checkLoginStatus();
});

// ========== 核心方法：全部映射到store ==========
/**
 * 导航菜单点击处理（调用store的导航方法）
 * @param {string} path 目标路径
 */
const handleNavClick = (path) => {
  userStore.navigateTo(path);
};

/**
 * 下拉菜单项点击处理
 * @param {string} action 操作类型：profile/settings/logout
 */
const handleItemClick = (action) => {
  switch (action) {
    case 'profile':
      userStore.navigateTo('/profile');
      break;
    case 'settings':
      userStore.navigateTo('/settings');
      break;
    case 'changeAvatar':
      userStore.openChangeAvatarModal();
      break;
    case 'logout':
      userStore.logout(); // 调用store的退出登录方法
      break;
  }
  // 关闭下拉菜单
  userStore.toggleDropdown(false);
};

/**
 * 搜索处理
 */
const handleSearch = () => {
  const searchValue = searchInputRef.value?.value.trim() || '';
  if (searchValue) {
    // 如果不在首页，跳转到首页并带上搜索参数
    router.push({ path: '/', query: { keyword: searchValue } });
    ElMessage.info(`正在搜索：${searchValue}`);
  } else {
    // 如果清空搜索框，返回首页
    router.push({ path: '/' });
  }
};

/**
 * 登录回调
 * @param {Object} loginData 登录表单数据
 */
const handleLogin = async (loginData) => {
  await userStore.login(loginData);
};

/**
 * 注册回调
 * @param {Object} registerData 注册表单数据
 */
const handleRegister = async (registerData) => {
  await userStore.register(registerData);
};

/**
 * 找回密码逻辑
 * @param {Object} forgotData 找回密码表单数据
 */
const handleForgotPassword = async (forgotData) => {
  if (forgotData.newPassword !== forgotData.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致');
    return;
  }
  
  try {
    ElMessage.success('密码重置成功，请使用新密码登录');
    userStore.switchView('login');
  } catch (error) {
    console.error('找回密码失败：', error);
    ElMessage.error('重置密码失败，请检查邮箱是否正确');
  }
};
</script>


<style scoped>
:deep(body) {
  background-color: #f5f7fa; /* 页面底层浅灰，突出导航栏 */
  margin: 0;
  padding: 0;
}

/* 导航栏基础样式 - 使用浅色背景，增强紧凑感 */
.header {
  background-color: #f8fafc; /* 浅蓝灰色背景 */
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 8px 0; /* 缩小上下边距 */
  width: 100%;
  box-sizing: border-box;
  position: sticky;
  top: 0;
  z-index: 999;
}

.inner-container {
  max-width: 1400px; /* 恢复稍大的宽度以适应新布局 */
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

/* 三段式布局样式 */
.left-section {
  flex: 1;
  display: flex;
  align-items: center;
}

.center-section {
  flex: 0 0 auto;
  display: flex;
  justify-content: center;
  min-width: 400px; /* 确保搜索框有足够空间且居中 */
}

.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
}

/* Logo 和 标题样式 */
.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.logo-container:hover {
  transform: translateY(-1px);
}

.logo {
  height: 36px;
  width: auto;
  object-fit: contain;
}

.logo-title {
  font-family: "STKaiti", "KaiTi", "楷体", serif;
  font-size: 26px;
  font-weight: bold;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 3px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.05);
  white-space: nowrap;
}

/* 搜索框样式 - 保持紧凑并居中 */
.search-container {
  position: relative;
  width: 100%;
  max-width: 450px;
}

.search-input {
  width: 100%;
  padding: 8px 40px 8px 16px;
  border: 1px solid #cbd5e1;
  border-radius: 20px;
  font-size: 14px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background-color: #ffffff;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.02);
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-button {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.2s ease;
}

.search-button:hover {
  background-color: #f1f5f9;
  color: #2563eb;
}

/* 导航栏样式 */
.inline-navbar {
  display: flex;
  align-items: center;
}

.inline-navbar ul {
  display: flex;
  gap: 8px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.inline-navbar a {
  text-decoration: none;
  color: #475569;
  font-weight: 500;
  font-size: 14px;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.inline-navbar a:hover {
  color: #2563eb;
  background-color: #eff6ff;
}

/* 用户区域样式 */
.user-section {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
}

.login-trigger {
  padding: 8px 20px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.login-trigger:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.3);
}

.user-avatar-container {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  background-color: rgba(255, 255, 255, 0.5);
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
  position: relative;
  cursor: pointer;
}

.user-avatar-container:hover {
  background-color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.login-status {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

/* 下拉菜单样式 */
.dropdown-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 160px;
  z-index: 1001;
  opacity: 0;
  visibility: hidden;
  transform: translateY(10px);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-menu.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-menu ul {
  list-style: none;
  padding: 6px 0;
  margin: 0;
}

.dropdown-menu li {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-size: 14px;
}

.dropdown-menu li:hover {
  background: #f8fafc;
  color: #2563eb;
  padding-left: 20px;
}

/* 响应式适配优化 */
@media (max-width: 1200px) {
  .center-section {
    min-width: 300px;
  }
  .logo-title {
    font-size: 22px;
  }
}

@media (max-width: 992px) {
  .inline-navbar {
    display: none;
  }
  .center-section {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .inner-container {
    flex-wrap: wrap;
    padding: 10px 15px;
  }
  
  .left-section {
    flex: 1;
  }
  
  .right-section {
    flex: 0 0 auto;
  }
  
  .center-section {
    order: 3;
    flex: 0 0 100%;
    min-width: 0;
    margin-top: 12px;
  }
  
  .logo-title {
    font-size: 20px;
  }
}
</style>