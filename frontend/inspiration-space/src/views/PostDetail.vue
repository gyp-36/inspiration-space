<template>
  <div class="post-detail-wrapper">
    <!-- 顶部导航 -->
    <div class="nav-bar glass-effect">
      <div class="nav-content">
        <el-page-header @back="goBack">
          <template #content>
            <span class="header-title">灵感探索</span>
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

      <div v-else-if="post" class="content-layout">
        <!-- 左侧：帖子内容主体 -->
        <div class="post-column">
          <div class="post-card glass-effect">
            <!-- 作者栏 -->
            <div class="author-bar" @click="goToUser(post.userId)">
              <el-avatar :size="54" :src="authorAvatar || ''" class="author-avatar">
                {{ post.username ? post.username.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="author-meta">
                <div class="author-name">{{ post.username || '匿名用户' }}</div>
                <div class="publish-time">
                  {{ formatTime(post.createAt) }}
                  <span v-if="post.category !== null && post.category !== undefined" class="detail-category-tag">
                    {{ getCategoryLabel(post.category) }}
                  </span>
                </div>
              </div>
              <div class="flex-spacer"></div>
              <el-button 
                :type="isFollowing ? 'info' : 'primary'" 
                :plain="!isFollowing" 
                round 
                class="follow-btn"
                :loading="followLoading"
                @click.stop="handleFollow"
              >
                {{ isFollowing ? '已关注' : '关注' }}
              </el-button>
            </div>

            <!-- 帖子内容 -->
            <div class="post-body">
              <h1 class="post-title">{{ post.title }}</h1>
              <div class="post-text">{{ post.content }}</div>

              <!-- 图片九宫格/大图 -->
              <div v-if="post.imageUrls && post.imageUrls.length > 0" class="image-gallery">
                <div 
                  v-for="(url, index) in post.imageUrls" 
                  :key="index"
                  class="gallery-item"
                  :class="{ 'single-img': post.imageUrls.length === 1 }"
                >
                  <el-image 
                    :src="url" 
                    fit="cover" 
                    :preview-src-list="post.imageUrls"
                    :initial-index="index"
                    loading="lazy"
                    preview-teleported
                  />
                </div>
              </div>

              <!-- 商品/外链 -->
              <div v-if="post.productUrl" class="link-box">
                <a :href="post.productUrl" target="_blank" class="link-card">
                  <div class="link-icon-bg">
                    <el-icon><LinkIcon /></el-icon>
                  </div>
                  <div class="link-info">
                    <div class="link-label">参考链接 / 相关商品</div>
                    <div class="link-url">{{ post.productUrl }}</div>
                  </div>
                  <el-icon class="link-arrow"><ArrowRight /></el-icon>
                </a>
              </div>
            </div>

            <!-- 底部交互统计 -->
            <div class="interaction-footer">
              <div class="stat-group">
                <div class="stat-btn" :class="{ active: post.isLiked }" @click="handleLike">
                  <el-icon><Pointer /></el-icon>
                  <span class="count">{{ post.like }}</span>
                  <span class="label">点赞</span>
                </div>
                <div class="stat-btn" :class="{ active: post.isCollected }" @click="handleCollect">
                  <el-icon v-if="post.isCollected"><StarFilled /></el-icon>
                  <el-icon v-else><Star /></el-icon>
                  <span class="count">{{ post.collect }}</span>
                  <span class="label">收藏</span>
                </div>
                <div class="stat-btn" @click="handleRepost">
                  <el-icon><Share /></el-icon>
                  <span class="count">{{ post.repost }}</span>
                  <span class="label">转发</span>
                </div>
                <div class="stat-btn no-hover">
                  <el-icon><View /></el-icon>
                  <span class="count">{{ post.view }}</span>
                  <span class="label">浏览</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：评论区 -->
        <div class="comment-column">
          <div class="comment-card glass-effect sticky-top">
            <div class="comment-header">
              <span class="comment-title">全部评论</span>
              <span class="comment-count">{{ comments.length }}</span>
            </div>

            <!-- 评论输入框 -->
            <div class="comment-input-area">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="分享你的见解..."
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

            <!-- 评论列表 -->
            <div class="comment-list">
              <div v-if="comments.length === 0" class="empty-comments">
                <el-empty description="暂无评论，快来抢沙发~" :image-size="60" />
              </div>
              <div 
                v-for="comment in paginatedComments" 
                :key="comment.id" 
                class="comment-item"
              >
                <el-avatar :size="36" :src="comment.avatar" class="comment-avatar">
                  {{ comment.username ? comment.username.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
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

            <!-- 评论分页 -->
            <div v-if="comments.length > commentPageSize" class="comment-pagination">
              <el-pagination
                v-model:current-page="commentPage"
                :page-size="commentPageSize"
                :total="comments.length"
                layout="prev, pager, next"
                small
                background
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  getPostDetail, 
  likePost, 
  unlikePost, 
  collectPost, 
  uncollectPost, 
  repostPost,
  getPostComments,
  createComment,
  likeComment as likeCommentApi,
  unlikeComment as unlikeCommentApi 
} from '@/services/forumService';
import { getCategoryLabel } from '@/constants/forumConstants';
import { getUserInfo, getAvatar, followUser, unfollowUser, checkFollowStatus } from '@/services/userService';
import { ElMessage } from 'element-plus';
import { 
  Pointer, 
  Star, 
  StarFilled, 
  Share, 
  View, 
  Link as LinkIcon, 
  ArrowRight 
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const postId = route.params.postId;

const loading = ref(true);
const error = ref(null);
const post = ref(null);
const authorAvatar = ref('');
const isFollowing = ref(false);
const followLoading = ref(false);
const comments = ref([]);
const newComment = ref('');
const commentPage = ref(1);
const commentPageSize = ref(10);

const paginatedComments = computed(() => {
  const start = (commentPage.value - 1) * commentPageSize.value;
  const end = start + commentPageSize.value;
  return comments.value.slice(start, end);
});

const fetchDetail = async () => {
  try {
    loading.value = true;
    error.value = null;
    const [detailRes, commentRes] = await Promise.all([
      getPostDetail(postId),
      getPostComments(postId)
    ]);
    
    post.value = detailRes;
    comments.value = commentRes || [];
    
    // 获取作者额外信息
    if (post.value.userId) {
      const [userInfo, avatarUrl, followStatus] = await Promise.all([
        getUserInfo(post.value.userId),
        getAvatar(post.value.userId),
        checkFollowStatus(post.value.userId)
      ]);
      post.value.username = userInfo.userName;
      authorAvatar.value = avatarUrl;
      isFollowing.value = followStatus;
    }
  } catch (err) {
    console.error('获取详情失败:', err);
    error.value = '帖子详情加载失败，请重试';
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

const formatCommentTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = (now - date) / 1000;
  
  if (diff < 60) return '刚刚';
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前';
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前';
  return date.toLocaleDateString('zh-CN');
};

const handleLike = async () => {
  if (post.value.isLiked) {
    await unlikePost(postId);
    post.value.isLiked = false;
    post.value.like--;
  } else {
    await likePost(postId);
    post.value.isLiked = true;
    post.value.like++;
  }
};

const handleCollect = async () => {
  if (post.value.isCollected) {
    await uncollectPost(postId);
    post.value.isCollected = false;
    post.value.collect--;
  } else {
    await collectPost(postId);
    post.value.isCollected = true;
    post.value.collect++;
  }
};

const handleRepost = async () => {
  await repostPost(postId);
  post.value.repost++;
  ElMessage.success('转发成功');
};

const handleFollow = async () => {
  try {
    followLoading.value = true;
    if (isFollowing.value) {
      await unfollowUser(post.value.userId);
      isFollowing.value = false;
      ElMessage.success('已取消关注');
    } else {
      await followUser(post.value.userId);
      isFollowing.value = true;
      ElMessage.success('关注成功');
    }
  } catch (err) {
    ElMessage.error(err.message || '操作失败');
  } finally {
    followLoading.value = false;
  }
};

const submitComment = async () => {
  try {
    const res = await createComment({
      postId: postId,
      content: newComment.value.trim()
    });
    if (res) {
      ElMessage.success('发表成功');
      newComment.value = '';
      // 刷新评论列表
      comments.value = await getPostComments(postId);
    }
  } catch (err) {
    ElMessage.error(err.message || '发表失败');
  }
};

const likeComment = async (comment) => {
  try {
    if (comment.isLiked) {
      await unlikeCommentApi(comment.id);
      comment.isLiked = false;
      comment.likeCount--;
    } else {
      await likeCommentApi(comment.id);
      comment.isLiked = true;
      comment.likeCount++;
    }
  } catch (err) {
    ElMessage.error(err.message || '操作失败');
  }
};

onMounted(fetchDetail);
</script>

<style scoped>
.post-detail-wrapper {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100vh;
  padding-top: 80px; /* 为导航栏留位 */
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  z-index: 100;
  display: flex;
  align-items: center;
}

.nav-content {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 0 40px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.5px;
}

/* 主容器 */
.main-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px 40px;
}

.content-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

/* 帖子主体 */
.post-column {
  flex: 7;
  min-width: 0; /* 防止弹性布局溢出 */
}

.glass-effect {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
}

.post-card {
  border-radius: 28px;
  padding: 40px;
}

.author-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 40px;
  cursor: pointer;
}

.author-meta {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 17px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 4px;
}

.publish-time {
  font-size: 14px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-category-tag {
  font-size: 12px;
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.follow-btn {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 700;
  height: auto;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}

.follow-btn.el-button--info {
  box-shadow: none;
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #64748b;
}

.follow-btn.el-button--info:hover {
  background: #e2e8f0;
  color: #475569;
}

.flex-spacer {
  flex: 1;
}

.post-body {
  margin-bottom: 50px;
}

.post-title {
  font-size: 32px;
  font-weight: 900;
  color: #0f172a;
  margin-bottom: 24px;
  line-height: 1.3;
}

.post-text {
  font-size: 17px;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
  margin-bottom: 40px;
}

/* 图片展示 */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 40px;
}

.gallery-item {
  aspect-ratio: 1;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
}

.gallery-item.single-img {
  grid-template-columns: 1fr;
  max-width: 100%;
  aspect-ratio: auto;
  max-height: 600px;
}

.gallery-item :deep(.el-image) {
  width: 100%;
  height: 100%;
}

/* 链接卡片 */
.link-box {
  margin-top: 50px;
  padding-top: 30px;
  border-top: 1px dashed #e2e8f0;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 28px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1.5px solid #bae6fd;
  border-radius: 20px;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.link-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(186, 230, 253, 0.4);
}

.link-icon-bg {
  width: 48px;
  height: 48px;
  background: #fff;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0284c7;
  font-size: 22px;
  box-shadow: 0 4px 10px rgba(2, 132, 199, 0.1);
}

.link-info {
  flex: 1;
  min-width: 0;
}

.link-label {
  font-size: 13px;
  color: #0369a1;
  font-weight: 700;
  margin-bottom: 4px;
}

.link-url {
  font-size: 15px;
  color: #0284c7;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-arrow {
  color: #0284c7;
  font-size: 18px;
}

/* 底部交互 */
.interaction-footer {
  padding-top: 40px;
  border-top: 1px solid #f1f5f9;
}

.stat-group {
  display: flex;
  gap: 20px;
}

.stat-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: #f8fafc;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  color: #64748b;
  border: 1px solid #f1f5f9;
}

.stat-btn:hover:not(.no-hover) {
  background: #fff;
  border-color: #e2e8f0;
  color: #1e293b;
  transform: translateY(-2px);
}

.stat-btn.active {
  background: #eff6ff;
  color: #3b82f6;
  border-color: #bfdbfe;
}

.stat-btn .count {
  font-size: 18px;
  font-weight: 800;
  font-family: 'Inter', sans-serif;
}

.stat-btn .label {
  font-size: 14px;
  font-weight: 500;
}

.comment-like.active {
  color: #3b82f6;
  background: #eff6ff;
}

.comment-like.active .el-icon {
  color: #3b82f6;
}

/* 评论区 */
.comment-column {
  flex: 3;
  position: sticky;
  top: 94px;
}

.comment-card {
  border-radius: 28px;
  padding: 30px;
  max-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.comment-title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.comment-count {
  background: #f1f5f9;
  color: #64748b;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
}

.comment-input-area {
  margin-bottom: 30px;
}

.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 16px;
  background: #f8fafc;
  border: 1.5px solid #f1f5f9;
  padding: 15px;
  transition: all 0.3s;
}

.comment-textarea :deep(.el-textarea__inner:focus) {
  background: #fff;
  border-color: #3b82f6;
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.08);
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.send-btn {
  border-radius: 12px;
  padding: 10px 24px;
  font-weight: 700;
}

.comment-list {
  overflow-y: auto;
  flex: 1;
  padding-right: 8px;
}

.comment-list::-webkit-scrollbar {
  width: 5px;
}

.comment-list::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

.comment-item {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
}

.comment-content-box {
  flex: 1;
}

.comment-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-username {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.comment-time {
  font-size: 12px;
  color: #94a3b8;
}

.comment-text-small {
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
  margin-bottom: 8px;
}

.comment-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.comment-like {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.comment-like:hover {
  color: #3b82f6;
}

.empty-comments {
  padding: 40px 0;
}

.loading-state, .error-state {
  padding: 100px 0;
  text-align: center;
}

@media (max-width: 1100px) {
  .content-layout {
    flex-direction: column;
  }
  .post-column, .comment-column {
    flex: none;
    width: 100%;
  }
  .comment-column {
    position: static;
  }
  .comment-card {
    max-height: none;
  }
}
</style>
