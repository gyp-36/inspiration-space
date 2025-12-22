<template>
  <div class="user-profile-wrapper">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-container">
      <el-empty :description="error" />
      <el-button type="primary" @click="fetchUserData">重试</el-button>
    </div>

    <div v-else class="user-profile">
      <!-- 顶部封面与个人信息区域 -->
      <header class="profile-header-card">
        <div class="profile-banner">
          <img src="https://images.unsplash.com/photo-1557683316-973673baf926?q=80&w=2000" alt="banner" />
          <div class="banner-mask"></div>
        </div>

        <div class="header-main-content">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="140" :src="user.avatarUrl" class="user-avatar">
                <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              </el-avatar>
            </div>
          </div>

          <div class="header-info-stats">
            <!-- 用户名与简介 -->
            <div class="user-info-side">
              <div class="name-follow-row">
                <h2 class="username">{{ user.username || '匿名用户' }}</h2>
                
                <!-- 关注/取消关注按钮 -->
                <el-button 
                  v-if="!isOwnProfile"
                  :type="isFollowing ? 'info' : 'primary'" 
                  :plain="isFollowing"
                  class="follow-btn"
                  :loading="followLoading"
                  @click="handleFollowToggle"
                >
                  <el-icon v-if="!isFollowing"><Plus /></el-icon>
                  <span>{{ isFollowing ? '已关注' : '关注' }}</span>
                </el-button>
              </div>
              <p class="bio">{{ user.bio || '这个人很懒，什么都没有写~' }}</p>
            </div>

            <!-- 数据统计 -->
            <div class="user-stats-side">
              <div class="user-stats">
                <div class="stat-item">
                  <span class="count">{{ user.followingsCount || 0 }}</span>
                  <span class="label">关注</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ user.fansCount || 0 }}</span>
                  <span class="label">粉丝</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ user.likesCount || 0 }}</span>
                  <span class="label">获赞</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ user.favoritesCount || 0 }}</span>
                  <span class="label">收藏</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 用户内容区域 -->
      <main class="profile-content-layout">
        <div class="profile-tabs-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="他的灵感" name="posts">
              <div class="tab-content">
                <!-- 这里可以复用帖子列表组件，暂时放空状态 -->
                <el-empty :description="`${user.username} 还没有发布过灵感`" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getUser, getAvatar, followUser, unfollowUser, checkFollowStatus } from '@/services/userService';
import { useUserStore } from '@/stores/userStore';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const route = useRoute();
const userStore = useUserStore();

const loading = ref(true);
const followLoading = ref(false);
const error = ref(null);
const user = ref({});
const isFollowing = ref(false);
const activeTab = ref('posts');

// 响应式获取当前路由的 userId
const currentUserId = computed(() => route.params.userId);

const isOwnProfile = computed(() => {
  return String(userStore.userInfo?.userId) === String(currentUserId.value);
});

// 处理头像：如果后端返回的是文件名，则调用 API 获取完整 URL
const fetchAvatarIfNeeded = async () => {
  if (user.value.avatarUrl && !user.value.avatarUrl.startsWith('http') && !user.value.avatarUrl.startsWith('data:')) {
    try {
      const avatarUrl = await getAvatar(currentUserId.value);
      if (avatarUrl) {
        user.value.avatarUrl = avatarUrl;
      }
    } catch (error) {
      console.warn('获取用户头像失败:', error);
    }
  }
};

const fetchUserData = async () => {
  if (!currentUserId.value) return;
  try {
    loading.value = true;
    error.value = null;
    
    const userData = await getUser(currentUserId.value);
    user.value = userData;
    await fetchAvatarIfNeeded();

    // 检查关注状态 (如果已登录且不是看自己)
    if (userStore.isLogin && !isOwnProfile.value) {
      try {
        isFollowing.value = await checkFollowStatus(currentUserId.value);
      } catch (e) {
        console.warn('获取关注状态失败，可能接口未实现');
      }
    }
  } catch (err) {
    console.error('获取用户信息失败:', err);
    error.value = '用户信息加载失败';
  } finally {
    loading.value = false;
  }
};

// 监听用户 ID 变化（处理从一个用户主页跳到另一个用户主页的情况）
watch(() => currentUserId.value, (newId) => {
  if (newId) {
    fetchUserData();
  }
});

const handleFollowToggle = async () => {
  if (!userStore.isLogin) {
    userStore.openLoginModal();
    return;
  }

  try {
    followLoading.value = true;
    if (isFollowing.value) {
      await unfollowUser(currentUserId.value);
      isFollowing.value = false;
      user.value.fansCount--;
      ElMessage.success('已取消关注');
    } else {
      await followUser(currentUserId.value);
      isFollowing.value = true;
      user.value.fansCount++;
      ElMessage.success('关注成功');
    }
  } catch (err) {
    ElMessage.error(err.message || '操作失败，请重试');
  } finally {
    followLoading.value = false;
  }
};

onMounted(fetchUserData);
</script>

<style scoped>
.user-profile-wrapper {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  min-height: 100vh;
  padding: 30px 20px;
}

.user-profile {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header-card {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.profile-banner {
  height: 200px;
  position: relative;
  overflow: hidden;
}

.profile-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to top, rgba(0,0,0,0.2), transparent);
}

.header-main-content {
  padding: 0 40px 30px;
  position: relative;
  display: flex;
  align-items: flex-end;
}

.avatar-section {
  margin-right: 30px;
  z-index: 2;
}

.avatar-wrapper {
  position: relative;
  padding: 5px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  display: inline-block;
  margin-top: -60px;
}

.header-info-stats {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
}

.user-info-side {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.name-follow-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.username {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.follow-btn {
  border-radius: 20px;
  padding: 8px 24px;
  font-weight: 600;
}

.bio {
  font-size: 15px;
  color: #64748b;
  margin: 0;
  max-width: 500px;
}

.user-stats {
  display: flex;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

.stat-item .count {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.stat-item .label {
  font-size: 12px;
  color: #94a3b8;
}

.stat-divider {
  width: 1px;
  height: 20px;
  background: #e2e8f0;
}

.profile-tabs-card {
  background: #fff;
  border-radius: 20px;
  padding: 10px 40px 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.loading-container, .error-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 100px 0;
  text-align: center;
}
</style>

