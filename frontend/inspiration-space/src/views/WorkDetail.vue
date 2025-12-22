<template>
  <div class="work-detail-wrapper">
    <!-- 顶部导航 -->
    <div class="nav-bar glass-effect">
      <div class="nav-content">
        <el-page-header @back="goBack">
          <template #content>
            <span class="header-title">作品详情</span>
          </template>
        </el-page-header>
      </div>
    </div>

    <div class="main-container">
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="15" animated />
      </div>

      <div v-else-if="error" class="error-state">
        <el-empty :description="error" />
        <el-button type="primary" round @click="fetchDetail">重试</el-button>
      </div>

      <div v-else-if="work" class="content-layout">
        <!-- 左侧：作品内容主体 -->
        <div class="work-column full-width">
          <div class="work-card glass-effect">
            <!-- 作者栏 -->
            <div class="author-bar" @click="goToUser(work.creatorId)">
              <el-avatar :size="54" :src="work.authorAvatar || ''" class="author-avatar">
                {{ work.authorName ? work.authorName.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="author-meta">
                <div class="author-name">{{ work.authorName || '匿名用户' }}</div>
                <div class="publish-time">{{ formatTime(work.publishedAt) }}</div>
              </div>
              <div class="flex-spacer"></div>
              <div class="strategy-tag" :class="strategyStyle.class">
                <el-icon class="strategy-icon"><component :is="strategyStyle.icon" /></el-icon>
                <span>{{ strategyStyle.text }}</span>
              </div>
            </div>

            <!-- 作品内容 -->
            <div class="work-body">
              <div class="work-cover-container" v-if="work.coverUrl">
                <el-image 
                  :src="work.coverUrl" 
                  fit="contain" 
                  class="work-cover"
                  :preview-src-list="[work.coverUrl]"
                />
              </div>
              
              <h1 class="work-title">{{ work.title }}</h1>
              
              <div class="work-info-tags">
                <el-tag size="small" effect="plain" class="type-tag">{{ work.type }}</el-tag>
                <div class="price-info" v-if="work.accessStrategy === 'PAID'">
                  <span class="currency">￥</span>
                  <span class="amount">{{ work.price }}</span>
                </div>
              </div>

              <div class="work-description">{{ work.description }}</div>
              
              <div class="work-content-rich" v-html="work.content"></div>
            </div>

            <!-- 底部交互统计 -->
            <div class="interaction-footer">
              <div class="stat-group">
                <div class="stat-btn" :class="{ active: work.isLiked }" @click="handleLike">
                  <el-icon><Pointer /></el-icon>
                  <span class="count">{{ work.likeCount || 0 }}</span>
                </div>
                <div class="stat-btn" :class="{ active: work.isCollected }" @click="handleCollect">
                  <el-icon><component :is="work.isCollected ? StarFilled : Star" /></el-icon>
                  <span class="count">{{ work.collectCount || 0 }}</span>
                </div>
                <div class="stat-item-info">
                  <el-icon><View /></el-icon>
                  <span>{{ work.viewCount || 0 }}</span>
                </div>
                <!-- 暂时移除评论数 -->
                <!-- <div class="stat-item-info">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>{{ work.commentCount || 0 }}</span>
                </div> -->
              </div>
              
              <div class="action-group">
                <el-button 
                  type="primary" 
                  size="large" 
                  round 
                  class="purchase-btn"
                  @click="handlePurchase"
                  v-if="work.accessStrategy === 'PAID'"
                >
                  立即购买
                </el-button>
                <el-button 
                  type="success" 
                  size="large" 
                  round 
                  class="download-btn"
                  v-else
                >
                  获取资源
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 暂时移除右侧：评论区 -->
        <!-- <div class="comment-column">
          <div class="comment-card glass-effect sticky-top">
            <div class="comment-header">
              <span class="comment-title">全部评论</span>
              <span class="comment-count">{{ comments.length }}</span>
            </div>

            <div class="comment-input-area">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="分享你的看法..."
                resize="none"
                class="comment-textarea"
              />
              <div class="input-footer">
                <el-button 
                  type="primary" 
                  size="small" 
                  :disabled="!newComment.trim()" 
                  @click="submitComment"
                  class="send-btn"
                >
                  发表评论
                </el-button>
              </div>
            </div>

            <div class="comment-list">
              <div v-if="comments.length === 0" class="empty-comments">
                <el-empty description="暂无评论，快来交流吧~" :image-size="60" />
              </div>
              <div 
                v-for="comment in paginatedComments" 
                :key="comment.id" 
                class="comment-item"
              >
                <el-avatar :size="36" :src="comment.avatar || ''" />
                <div class="comment-content-box">
                  <div class="comment-user-info">
                    <span class="comment-username">{{ comment.username }}</span>
                    <span class="comment-time">{{ formatCommentTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-text-small">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <div 
                      class="comment-like" 
                      :class="{ active: comment.isLiked }"
                      @click="likeComment(comment)"
                    >
                      <el-icon><Pointer /></el-icon>
                      <span>{{ comment.likeCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="comment-pagination" v-if="comments.length > commentPageSize">
              <el-pagination
                v-model:current-page="commentPage"
                :page-size="commentPageSize"
                layout="prev, pager, next"
                :total="comments.length"
                small
              />
            </div>
          </div>
        </div> -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { workService } from '@/services/workService';
import { useUserStore } from '@/stores/userStore';
import { ElMessage } from 'element-plus';
import { 
  Pointer, 
  Star, 
  StarFilled, 
  View, 
  ChatLineRound,
  Lock,
  Unlock,
  Medal
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const workId = route.params.workId;

const loading = ref(true);
const error = ref(null);
const work = ref(null);

const strategyStyle = computed(() => {
  if (!work.value) return {};
  const strategy = work.value.accessStrategy;
  const map = {
    'FREE': { text: '免费', icon: Unlock, class: 'strategy-free' },
    'VIP': { text: 'VIP免费', icon: Medal, class: 'strategy-vip' },
    'PAID': { text: '付费', icon: Lock, class: 'strategy-paid' }
  };
  return map[strategy] || map['FREE'];
});

const fetchDetail = async () => {
  try {
    loading.value = true;
    error.value = null;
    const detailRes = await workService.getWorkDetail(workId);
    
    work.value = detailRes;
  } catch (err) {
    console.error('获取详情失败:', err);
    error.value = '作品详情加载失败，请重试';
  } finally {
    loading.value = false;
  }
};

const goBack = () => router.back();
const goToUser = (userId) => router.push(`/user/${userId}`);

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

const handleLike = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再点赞');
    return;
  }
  
  const originalState = { 
    isLiked: work.value.isLiked, 
    likeCount: work.value.likeCount 
  };
  
  try {
    // 乐观更新
    work.value.isLiked = !work.value.isLiked;
    work.value.likeCount += work.value.isLiked ? 1 : -1;
    
    await workService.toggleLike(workId);
  } catch (err) {
    // 失败回滚
    work.value.isLiked = originalState.isLiked;
    work.value.likeCount = originalState.likeCount;
    ElMessage.error('操作失败，请重试');
  }
};

const handleCollect = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再收藏');
    return;
  }
  
  const originalState = { 
    isCollected: work.value.isCollected, 
    collectCount: work.value.collectCount 
  };
  
  try {
    // 乐观更新
    work.value.isCollected = !work.value.isCollected;
    work.value.collectCount += work.value.isCollected ? 1 : -1;
    
    await workService.toggleCollect(workId);
  } catch (err) {
    // 失败回滚
    work.value.isCollected = originalState.isCollected;
    work.value.collectCount = originalState.collectCount;
    ElMessage.error('操作失败，请重试');
  }
};

const handlePurchase = () => {
  ElMessage.info('购买功能正在开发中...');
};

onMounted(() => {
  fetchDetail();
  if (route.query.action === 'buy') {
    // 延迟一下确保数据加载完
    setTimeout(() => {
      handlePurchase();
    }, 500);
  }
});
</script>

<style scoped>
.work-detail-wrapper {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 40px;
}

.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-title {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

.main-container {
  max-width: 1200px;
  margin: 24px auto 0;
  padding: 0 20px;
}
.content-layout {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.work-column {
  flex: 1;
  max-width: 900px;
}

.work-column.full-width {
  max-width: 1000px;
}

@media (max-width: 992px) {
  .content-layout {
    flex-direction: column;
    align-items: center;
  }
}

.glass-effect {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.work-card {
  border-radius: 16px;
  overflow: hidden;
  padding: 24px;
}

.author-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  cursor: pointer;
}

.author-meta {
  margin-left: 12px;
}

.author-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.publish-time {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.flex-spacer {
  flex: 1;
}

.strategy-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.strategy-free { background: #f0f9eb; color: #67c23a; }
.strategy-vip { background: #fdf6ec; color: #e6a23c; }
.strategy-paid { background: #fef0f0; color: #f56c6c; }

.work-body {
  margin-bottom: 32px;
}

.work-cover-container {
  width: 100%;
  max-height: 500px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
}

.work-cover {
  max-width: 100%;
  height: auto;
}

.work-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.work-info-tags {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.price-info {
  display: flex;
  align-items: baseline;
  color: #f56c6c;
  font-weight: 700;
}

.currency { font-size: 14px; }
.amount { font-size: 20px; }

.work-description {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.work-content-rich {
  font-size: 16px;
  color: #303133;
  line-height: 1.8;
}

.interaction-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.stat-group {
  display: flex;
  gap: 24px;
}

.stat-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
}

.stat-btn:hover { color: #409eff; }
.stat-btn.active { color: #409eff; }
.stat-btn.active.stat-btn:nth-child(2) { color: #e6a23c; }

.stat-btn .el-icon { font-size: 22px; margin-bottom: 4px; }
.stat-btn .count { font-size: 14px; font-weight: 600; }
.stat-btn .label { font-size: 12px; }
.stat-btn.no-hover { cursor: default; }
.stat-btn.no-hover:hover { color: #909399; }

/* 评论区样式 */
.comment-card {
  border-radius: 16px;
  padding: 24px;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.sticky-top {
  position: sticky;
  top: 84px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.comment-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.comment-count {
  font-size: 14px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.comment-input-area {
  margin-bottom: 24px;
}

.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.comment-textarea :deep(.el-textarea__inner:focus) {
  background: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 16px;
  padding-right: 4px;
}

.comment-list::-webkit-scrollbar { width: 4px; }
.comment-list::-webkit-scrollbar-thumb { background: #e4e7ed; border-radius: 2px; }

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.comment-content-box {
  flex: 1;
}

.comment-user-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.comment-username {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text-small {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.comment-actions {
  margin-top: 8px;
}

.comment-like {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s;
}

.comment-like:hover, .comment-like.active {
  color: #409eff;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.loading-state, .error-state {
  padding: 100px 0;
  text-align: center;
}
</style>
