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
            <div class="author-bar" @click="goToUser(work.authorId)">
              <el-avatar :size="54" :src="processImageUrl(work.authorAvatar)" class="author-avatar">
                {{ work.authorName ? work.authorName.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="author-meta">
                <div class="author-name">{{ work.authorName || '匿名用户' }}</div>
                <div class="publish-time">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatTime(work.publishedAt) }}</span>
                </div>
              </div>
              <div class="flex-spacer"></div>
              <div class="strategy-tag" :class="strategyStyle.class">
                <el-icon class="strategy-icon"><component :is="strategyStyle.icon" /></el-icon>
                <span>{{ strategyStyle.text }}</span>
              </div>
            </div>

            <!-- 作品内容 -->
            <div class="work-body">
              <div class="work-cover-container">
                <el-image 
                  v-if="work.coverUrl"
                  :src="processImageUrl(work.coverUrl)" 
                  fit="contain" 
                  class="work-cover"
                  :preview-src-list="[processImageUrl(work.coverUrl)]"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                  <template #placeholder>
                    <div class="image-placeholder">
                      <el-skeleton-item variant="image" style="width: 100%; height: 400px" />
                    </div>
                  </template>
                </el-image>
                <div v-else class="image-empty">
                  <el-icon><Picture /></el-icon>
                  <span>暂无封面图</span>
                </div>
              </div>
              
              <h1 class="work-title">{{ work.title }}</h1>
              
              <div class="work-info-tags">
                <el-tag size="small" effect="light" class="type-tag" type="info">{{ work.type }}</el-tag>
                <div class="price-info" v-if="work.accessStrategy === 'PAY' && !work.isPurchased">
                  <span class="currency">￥</span>
                  <span class="amount">{{ work.price }}</span>
                </div>
              </div>

              <div class="work-description">{{ work.description }}</div>
              
              <div class="work-content-rich" v-html="work.content"></div>
              
              <!-- 附件资源区域 -->
              <div v-if="work.isPurchased && attachments.length > 0" class="attachments-section">
                <div class="section-header">
                  <el-icon><Document /></el-icon>
                  <span>资源附件 ({{ attachments.length }})</span>
                </div>
                <div class="attachment-list">
                  <div v-for="file in attachments" :key="file.id" class="attachment-item-card">
                    <div class="file-info">
                      <el-icon class="file-icon"><Document /></el-icon>
                      <div class="file-meta">
                        <div class="file-name">{{ file.fileName }}</div>
                        <div class="file-size">{{ formatFileSize(file.fileSize) }}</div>
                      </div>
                    </div>
                    <el-button 
                      type="primary" 
                      size="small" 
                      link 
                      @click="downloadFile(file.downloadUrl)"
                    >
                      <el-icon><Download /></el-icon>
                      <span>下载</span>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部交互统计 -->
            <div class="interaction-footer">
              <div class="stat-group">
                <div class="stat-btn" :class="{ active: work.isLiked }" @click="handleLike">
                  <el-icon><CaretTop /></el-icon>
                  <span class="count">{{ work.likeCount || 0 }}</span>
                  <span class="label">赞同</span>
                </div>
                <div class="stat-btn" :class="{ active: work.isCollected }" @click="handleCollect">
                  <el-icon><component :is="work.isCollected ? StarFilled : Star" /></el-icon>
                  <span class="count">{{ work.collectCount || 0 }}</span>
                  <span class="label">收藏</span>
                </div>
                <div class="stat-item-info">
                  <el-icon><View /></el-icon>
                  <span class="count">{{ work.viewCount || 0 }}</span>
                  <span class="label">浏览</span>
                </div>
              </div>
              
              <div class="action-group">
                <el-button 
                  type="primary" 
                  size="large" 
                  round 
                  class="purchase-btn"
                  @click="handlePurchase"
                  v-if="work.accessStrategy === 'PAY' && !work.isPurchased"
                >
                  立即购买
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
  CaretTop,
  Star, 
  StarFilled, 
  View, 
  ChatLineRound,
  Lock,
  Unlock,
  Medal,
  Calendar,
  Picture,
  Document,
  Download
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const workId = route.params.workId;

const loading = ref(true);
const error = ref(null);
const work = ref(null);
const attachments = ref([]);

const processImageUrl = (url) => {
  if (!url || url === 'null' || url === 'undefined') return '';
  
  let finalUrl = '';
  // 如果已经是完整的 http 路径
  if (url.startsWith('http')) {
    finalUrl = url;
  } else {
    // 如果是相对路径（以 / 开头或直接是 key）
    const baseUrl = 'http://localhost:8080/client';
    const path = url.startsWith('/') ? url : `/${url}`;
    finalUrl = `${baseUrl}${path}`;
  }
  
  // 生产环境下可以移除此 log
  // console.log('Processing Image URL:', { original: url, final: finalUrl });
  return finalUrl;
};

const strategyStyle = computed(() => {
  if (!work.value) return {};
  const strategy = work.value.accessStrategy;
  const map = {
    'FREE': { text: '免费', icon: Unlock, class: 'strategy-free' },
    'VIP': { text: 'VIP免费', icon: Medal, class: 'strategy-vip' },
    'PAY': { text: '付费', icon: Lock, class: 'strategy-PAY' }
  };
  return map[strategy] || map['FREE'];
});

const fetchAttachments = async () => {
  if (!work.value || !work.value.isPurchased) return;
  try {
    const res = await workService.getWorkAttachments(workId);
    attachments.value = res;
  } catch (err) {
    console.error('获取附件失败:', err);
  }
};

const fetchDetail = async () => {
  try {
    loading.value = true;
    error.value = null;
    const detailRes = await workService.getWorkDetail(workId);
    console.log('Work detail loaded:', detailRes);
    work.value = detailRes;
    if (work.value.isPurchased) {
      await fetchAttachments();
    }
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

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const downloadFile = (url) => {
  if (!url) return;
  window.open(url, '_blank');
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
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再购买');
    return;
  }
  if (work.value.isPurchased) {
    ElMessage.success('您已拥有该作品');
    return;
  }
  router.push(`/payment/${workId}`);
};

onMounted(async () => {
  await fetchDetail();
  if (route.query.action === 'buy') {
    handlePurchase();
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
  margin-bottom: 32px;
  cursor: pointer;
  padding: 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.author-bar:hover {
  background: rgba(240, 244, 248, 1);
  transform: translateX(4px);
}

.author-meta {
  margin-left: 16px;
}

.author-name {
  font-weight: 700;
  font-size: 18px;
  color: #1a1a1a;
}

.publish-time {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.publish-time .el-icon {
  font-size: 14px;
}

.flex-spacer {
  flex: 1;
}

.strategy-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.strategy-free { background: #f0f9eb; color: #67c23a; }
.strategy-vip { background: #fdf6ec; color: #e6a23c; }
.strategy-PAY { background: #fef0f0; color: #f56c6c; }

.work-body {
  margin-bottom: 40px;
}

.work-cover-container {
  width: 100%;
  min-height: 200px;
  max-height: 600px;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 32px;
  background: #f8fafc;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #edf2f7;
}

.work-cover {
  width: 100%;
  height: 100%;
}

.image-error, .image-placeholder, .image-empty {
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 12px;
  border: 1px dashed #dcdfe6;
}

.image-error .el-icon, .image-empty .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #c0c4cc;
}

.image-error span, .image-empty span {
  font-size: 14px;
}

.image-error span, .image-empty span {
  font-size: 14px;
}

.work-title {
  font-size: 32px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 20px;
  letter-spacing: -0.02em;
}

.work-info-tags {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.type-tag {
  padding: 0 12px;
  height: 28px;
  line-height: 26px;
  border-radius: 6px;
  font-weight: 500;
}

.price-info {
  display: flex;
  align-items: baseline;
  color: #f56c6c;
  font-weight: 800;
}

.currency { font-size: 16px; margin-right: 2px; }
.amount { font-size: 24px; }

.work-description {
  font-size: 16px;
  color: #475569;
  line-height: 1.8;
  margin-bottom: 32px;
  padding: 20px 24px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 5px solid #409eff;
  position: relative;
}

.work-description::before {
  content: '“';
  position: absolute;
  top: 10px;
  left: 10px;
  font-size: 40px;
  color: rgba(64, 158, 255, 0.1);
  font-family: serif;
}

.work-content-rich {
  font-size: 17px;
  color: #1e293b;
  line-height: 1.8;
  padding: 0 4px;
  margin-bottom: 40px;
}

/* 附件区域样式 */
.attachments-section {
  margin-top: 40px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.section-header .el-icon {
  color: #409eff;
  font-size: 22px;
}

.attachment-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.attachment-item-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #edf2f7;
  transition: all 0.3s ease;
}

.attachment-item-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.file-icon {
  font-size: 24px;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 8px;
  border-radius: 8px;
}

.file-meta {
  overflow: hidden;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.file-size {
  font-size: 12px;
  color: #94a3b8;
}

.interaction-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 32px;
  margin-top: 40px;
  border-top: 2px solid #f1f5f9;
}

.stat-group {
  display: flex;
  gap: 32px;
}

.stat-btn, .stat-item-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #64748b;
  position: relative;
}

.stat-item-info {
  cursor: default;
}

.stat-btn:hover { 
  color: #409eff;
  transform: translateY(-2px);
}

.stat-btn.active { 
  color: #409eff;
}

.stat-btn.active:nth-child(2) { 
  color: #e6a23c;
}

.stat-btn .el-icon, .stat-item-info .el-icon { 
  font-size: 26px; 
  margin-bottom: 6px;
  padding: 10px;
  background: #f1f5f9;
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-btn:hover .el-icon {
  background: rgba(64, 158, 255, 0.1);
}

.stat-btn.active .el-icon {
  background: rgba(64, 158, 255, 0.1);
}

.stat-btn.active:nth-child(2) .el-icon {
  background: rgba(230, 162, 60, 0.1);
}

.stat-btn .count, .stat-item-info .count { 
  font-size: 15px; 
  font-weight: 700;
  margin-bottom: 2px;
}

.stat-btn .label, .stat-item-info .label { 
  font-size: 12px;
  font-weight: 500;
  color: #94a3b8;
}

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
