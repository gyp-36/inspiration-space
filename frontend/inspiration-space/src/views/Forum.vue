<template>
  <div class="forum-container">
    <!-- 论坛顶层标题 -->
    <header class="forum-header">
      <h1 class="welcome-title">欢迎来到知创论坛</h1>
      <p class="welcome-subtitle">开启你的探索之旅吧！</p>
    </header>

    <!-- 操作按钮 (发布新帖) - 放大并美化 -->
    <div class="action-bar">
      <el-button type="primary" @click="toggleNewPostForm" class="new-post-btn">
        <el-icon><Edit /></el-icon>
        <span>发布新帖</span>
      </el-button>
    </div>

    <!-- 顶部搜索和排序栏 -->
    <div class="search-bar-inner glass-effect">
      <el-select v-model="sortMethod" placeholder="排序" @change="handleSortChange" class="sort-select-left">
        <el-option value="time" label="最新发布">
          <div class="option-item">
            <el-icon><Clock /></el-icon>
            <span>最新发布</span>
          </div>
        </el-option>
        <el-option value="like" label="最多点赞">
          <div class="option-item">
            <el-icon><Pointer /></el-icon>
            <span>最多点赞</span>
          </div>
        </el-option>
        <el-option value="comment" label="最多评论">
          <div class="option-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>最多评论</span>
          </div>
        </el-option>
        <el-option value="view" label="最多浏览">
          <div class="option-item">
            <el-icon><ViewIcon /></el-icon>
            <span>最多浏览</span>
          </div>
        </el-option>
        <el-option value="collect" label="最多收藏">
          <div class="option-item">
            <el-icon><CollectionTag /></el-icon>
            <span>最多收藏</span>
          </div>
        </el-option>
        <el-option value="repost" label="最多转发">
          <div class="option-item">
            <el-icon><Share /></el-icon>
            <span>最多转发</span>
          </div>
        </el-option>
      </el-select>

      <el-input
        v-model="searchKeyword"
        placeholder="探索感兴趣的科技灵感..."
        class="search-input-right"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" class="search-btn">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索结果数量显示 -->
    <div v-if="!loading && totalElements >= 0" class="result-stats">
      找到 <span class="stat-count">{{ totalElements }}</span> 个相关灵感
    </div>
    
    <!-- 发布新帖对话框 -->
    <el-dialog
      v-model="showNewPostForm"
      title="分享你的灵感"
      width="600px"
      class="new-post-dialog"
      :before-close="handleDialogClose"
      destroy-on-close
      align-center
    >
      <div class="inspiration-form-container">
        <el-form :model="newPost" label-position="top" class="inspiration-form">
          <!-- 1. 标题 -->
          <el-form-item label="灵感标题" required>
            <el-input 
              v-model="newPost.title" 
              placeholder="给你的灵感起个吸睛的名字..." 
              maxlength="50"
              show-word-limit
              class="vibrant-input"
            />
          </el-form-item>
          
          <!-- 2. 内容 -->
          <el-form-item label="灵感详述" required>
            <el-input
              v-model="newPost.content"
              type="textarea"
              :rows="6"
              placeholder="在这里分享你的奇思妙想、技术细节或应用场景..."
              maxlength="2000"
              show-word-limit
              class="vibrant-input content-textarea"
            />
          </el-form-item>

          <!-- 3. 图片素材 -->
          <el-form-item label="灵感素材 (最多5张)">
            <div class="upload-section">
              <el-upload
                v-model:file-list="fileList"
                action="#"
                list-type="picture-card"
                :limit="maxImages"
                :on-success="handleImageSuccess"
                :on-remove="handleImageRemove"
                :before-upload="beforeImageUpload"
                :http-request="customUpload"
                multiple
                class="vibrant-uploader"
              >
                <div class="upload-trigger">
                  <el-icon><Plus /></el-icon>
                  <span>上传图片</span>
                </div>
                <template #tip>
                  <div class="upload-tip">
                    支持 JPG/PNG，单张不超过 5MB
                  </div>
                </template>
              </el-upload>
            </div>
          </el-form-item>

          <!-- 4. 商品链接 -->
          <el-form-item label="关联链接 (可选)">
            <el-input 
              v-model="newPost.productUrl" 
              placeholder="https:// 添加相关商品或参考链接" 
              :prefix-icon="LinkIcon"
              class="vibrant-input link-input"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showNewPostForm = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitPost" class="publish-btn">
            立即发布
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 帖子加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      正在加载帖子...
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="error">
      {{ error }}
      <button @click="loadPosts" class="retry-btn">重试</button>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="posts.length === 0" class="empty-state">
      暂时没有帖子，快来发布第一个帖子吧！
    </div>
    
    <!-- 帖子列表 - 采用瀑布流布局解决空白间距问题 -->
    <div v-else class="posts-masonry">
      <div class="masonry-column">
        <PostCard 
          v-for="post in leftColumnPosts" 
          :key="post.postId"
          :id="post.postId"
          :user-id="post.userId"
          :title="post.title"
          :content="post.content"
          :create-at="post.createAt"
          :like="post.like"
          :is-liked="post.isLiked"
          :collect="post.collect"
          :is-favorited="post.isCollected"
          :view="post.view"
          :repost="post.repost"
          :is-reposted="post.isReposted"
          :comment-count="post.commentCount"
          :product-url="post.productUrl"
          :image-urls="post.imageUrls"
          :username="post.username"
          :avatar="post.avatar"
          @card-clicked="handleCardClicked"
          @post-liked="handlePostLiked"
          @post-favorited="handlePostFavorited"
          @reposted="handleReposted"
          @user-loaded="handleUserLoaded"
        />
      </div>
      <div class="masonry-column">
        <PostCard 
          v-for="post in rightColumnPosts" 
          :key="post.postId"
          :id="post.postId"
          :user-id="post.userId"
          :title="post.title"
          :content="post.content"
          :create-at="post.createAt"
          :like="post.like"
          :is-liked="post.isLiked"
          :collect="post.collect"
          :is-favorited="post.isCollected"
          :view="post.view"
          :repost="post.repost"
          :is-reposted="post.isReposted"
          :comment-count="post.commentCount"
          :product-url="post.productUrl"
          :image-urls="post.imageUrls"
          :username="post.username"
          :avatar="post.avatar"
          @card-clicked="handleCardClicked"
          @post-liked="handlePostLiked"
          @post-favorited="handlePostFavorited"
          @reposted="handleReposted"
          @user-loaded="handleUserLoaded"
        />
      </div>
    </div>
    
    <!-- 分页控件 -->
    <div v-if="totalElements > 0" class="pagination-container">
      <div class="pagination-info">
        第 <span class="current-page">{{ currentPage }}</span> / {{ totalPages }} 页
        (共 {{ totalElements }} 条)
      </div>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalElements"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import { ElMessage } from 'element-plus';
import { 
  Search, 
  Edit, 
  Clock, 
  Pointer, 
  ChatDotRound, 
  View as ViewIcon, 
  CollectionTag, 
  Share,
  Plus,
  Link as LinkIcon
} from '@element-plus/icons-vue';
import PostCard from '@/components/forum/PostCard.vue';
import Navigation from '@/components/home/Navigation.vue';
import {  getAllPosts, 
  createPost,
  likePost,
  unlikePost,
  collectPost,
  uncollectPost,
  repostPost,
  uploadForumImage} from '@/services/forumService';


// 响应式状态
const loading = ref(true);
const submitting = ref(false);
const error = ref(null);
const showNewPostForm = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const sortMethod = ref('time');
const searchKeyword = ref('');
const posts = ref([]);
const leftColumnPosts = computed(() => posts.value.filter((_, index) => index % 2 === 0));
const rightColumnPosts = computed(() => posts.value.filter((_, index) => index % 2 !== 0));
const newPost = ref({ 
  title: '', 
  content: '',
  productUrl: '',
  imageUrls: [] 
});
const fileList = ref([]);
const maxImages = 5;
const totalPages = ref(1);
const totalElements = ref(0);

// Pinia store
const userStore = useUserStore();
const router = useRouter();

// 优化：统一API错误处理
const handleApiError = (error, action) => {
  if (error.message === '未登录' || error.status === 401) {
    userStore.openLoginModal();
    return false;
  }
  
  const messages = {
    'like': '点赞操作失败',
    'unlike': '取消点赞失败',
    'collect': '收藏操作失败',
    'uncollect': '取消收藏失败',
    'repost': '转发操作失败',
    'create': '发布帖子失败'
  };
  
  ElMessage.error(messages[action] || error.message || '操作失败，请重试');
  return true;
};

// 加载帖子列表
const loadPosts = async () => {
  try {
    loading.value = true;
    error.value = null;

    const response = await getAllPosts(
      sortMethod.value, 
      searchKeyword.value, 
      currentPage.value, 
      pageSize.value
    );
    
    // 统一处理响应
    if (response && response.records) {
      posts.value = response.records;
      totalPages.value = response.pages;
      totalElements.value = response.total;
    } else {
      throw new Error('无效的帖子数据格式');
    }
  } catch (err) {
    error.value = err.message || '加载帖子失败，请稍后重试';
    console.error('加载帖子失败:', err);
    posts.value = [];
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  loadPosts();
};

// 处理排序变化
const handleSortChange = () => {
  currentPage.value = 1;
  loadPosts();
};

// 切换新帖表单显示
const toggleNewPostForm = () => {
  if (!userStore.isLogin) {
    userStore.openLoginModal();
    return;
  }
  showNewPostForm.value = true;
};

const handleImageSuccess = (imageKey, uploadFile) => {
  if (imageKey) {
    // 存储后端返回的短路径 (Key)，发布时传给后端
    if (!newPost.value.imageUrls.includes(imageKey)) {
      newPost.value.imageUrls.push(imageKey);
    }
  } else {
    ElMessage.error('图片上传失败');
    // 如果失败，从 fileList 中移除该文件
    const index = fileList.value.findIndex(f => f.uid === uploadFile.uid);
    if (index > -1) {
      fileList.value.splice(index, 1);
    }
  }
};

const handleImageRemove = (uploadFile) => {
  // 查找并移除对应的 Key
  // 如果是通过 customUpload 上传的，uploadFile.response 就是返回的 Key 字符串
  const removedKey = typeof uploadFile.response === 'string' ? uploadFile.response : uploadFile.url;
  const index = newPost.value.imageUrls.indexOf(removedKey);
  if (index > -1) {
    newPost.value.imageUrls.splice(index, 1);
  }
};

const beforeImageUpload = (file) => {
  const isJPGPNG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isJPGPNG) {
    ElMessage.error('图片只能是 JPG/PNG 格式!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  if (newPost.value.imageUrls.length >= maxImages) {
    ElMessage.error(`最多只能上传 ${maxImages} 张图片`);
    return false;
  }
  return true;
};

const customUpload = (options) => {
  // 返回 promise，Element Plus 会自动处理成功和失败状态
  return uploadForumImage(options.file);
};

const resetForm = () => {
  newPost.value = { title: '', content: '', productUrl: '', imageUrls: [] };
  fileList.value = [];
};

// 关闭对话框前的处理
const handleDialogClose = (done) => {
  const hasContent = newPost.value.title.trim() || 
                    newPost.value.content.trim() || 
                    (newPost.value.productUrl && newPost.value.productUrl.trim()) || 
                    newPost.value.imageUrls.length > 0;

  if (hasContent) {
    if (confirm('确定要放弃正在编辑的内容吗？')) {
      resetForm();
      if (typeof done === 'function') done();
      else showNewPostForm.value = false;
    }
  } else {
    resetForm();
    if (typeof done === 'function') done();
    else showNewPostForm.value = false;
  }
};

// 提交新帖子
const submitPost = async () => {
  if (!newPost.value.title.trim()) {
    ElMessage.warning('请输入帖子标题');
    return;
  }
  if (!newPost.value.content.trim()) {
    ElMessage.warning('请输入帖子内容');
    return;
  }
  
  try {
    submitting.value = true;
    const response = await createPost({
      title: newPost.value.title.trim(),
      content: newPost.value.content.trim(),
      productUrl: newPost.value.productUrl ? newPost.value.productUrl.trim() : '',
      imageUrls: newPost.value.imageUrls
    });
    
    ElMessage.success('发布成功！');
    resetForm();
    showNewPostForm.value = false;
    await loadPosts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (error) {
    handleApiError(error, 'create');
  } finally {
    submitting.value = false;
  }
};

// 通用状态更新函数
const updatePostState = (postId, updater) => {
  const postIndex = posts.value.findIndex(p => p.postId === postId);
  if (postIndex !== -1) {
    const updatedPost = { ...posts.value[postIndex] };
    updater(updatedPost);
    posts.value = [
      ...posts.value.slice(0, postIndex),
      updatedPost,
      ...posts.value.slice(postIndex + 1)
    ];
    return true;
  }
  return false;
};
// 处理帖子卡片点击
const handleCardClicked = (postId) => {
  // 跳转到详情页
  router.push(`/forum/${postId}`);
};

// 处理帖子点赞
const handlePostLiked = ({ id, liked, newCount }) => {
  updatePostState(id, post => {
    post.isLiked = liked;
    post.like = newCount;
  });
};

// 处理帖子收藏
const handlePostFavorited = ({ id, favorited, newCount }) => {
  updatePostState(id, post => {
    post.isCollected = favorited;
    post.collect = newCount;
  });
};

// 处理帖子转发
const handleReposted = ({ id, newCount }) => {
  updatePostState(id, post => {
    post.isReposted = true;
    post.repost = newCount;
  });
};



// 分页控制
const handlePageChange = (page) => {
  currentPage.value = page;
  loadPosts();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

// 组件挂载时加载数据
onMounted(loadPosts);
</script>

<style scoped>
/* 论坛整体容器 */
.forum-container {
  max-width: 100%;
  margin: 0;
  padding: 40px 60px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%); /* 科技感渐变背景 */
  position: relative;
}

/* 论坛头部艺术字 */
.forum-header {
  text-align: center;
  margin-bottom: 30px;
}

.welcome-title {
  font-size: 42px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
  letter-spacing: -1px;
}

.welcome-subtitle {
  font-size: 18px;
  color: #64748b;
  font-weight: 500;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.new-post-btn {
  border-radius: 14px;
  padding: 16px 40px; /* 放大发布按钮 */
  font-size: 18px; /* 增大字号 */
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: auto !important;
}

.new-post-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.5);
}

/* 搜索栏 - 彻底去除内部边框，实现平整平铺 */
.search-bar-inner {
  display: flex;
  align-items: stretch; /* 强制子元素高度拉伸一致 */
  gap: 0;
  max-width: 1200px;
  margin: 0 auto 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 12px 40px rgba(31, 38, 135, 0.08);
  height: 60px;
  box-sizing: border-box; /* 确保高度包含边框 */
}

.sort-select-left {
  width: 150px;
  height: 100%; /* 占据容器全部高度 */
}

/* 彻底去除选择框内部边框，确保文字可见 */
.sort-select-left :deep(.el-select__wrapper) {
  border-radius: 0 !important;
  box-shadow: none !important;
  border: none !important;
  background-color: transparent !important;
  height: 60px !important; /* 恢复固定高度确保文字对齐 */
  padding: 0 16px;
  display: flex;
  align-items: center;
}

/* 针对较旧版本的 Element Plus */
.sort-select-left :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  box-shadow: none !important;
  border: none !important;
  background-color: transparent !important;
  height: 60px !important;
  padding: 0 16px;
  display: flex;
  align-items: center;
}

/* 移除选择框和输入框聚焦时的蓝色边框 */
.sort-select-left :deep(.el-input.is-focus .el-input__wrapper),
.sort-select-left :deep(.el-select:hover:not(.el-select--disabled) .el-select__wrapper),
.sort-select-left :deep(.el-select .el-select__wrapper.is-focused),
.search-input-right :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: none !important;
}

.sort-select-left :deep(.el-input__inner),
.sort-select-left :deep(.el-select__selected-item),
.sort-select-left :deep(.el-select__placeholder) {
  font-weight: 600;
  color: #1e293b !important;
  height: auto !important; /* 让文字高度自适应 */
  line-height: 60px !important; /* 用行高撑起并居中文字 */
}

.search-input-right {
  flex: 1;
  height: 100%;
}

/* 搜索输入框文字对齐 */
.search-input-right :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  box-shadow: none !important;
  border: none !important;
  border-left: 1px solid rgba(0, 0, 0, 0.1) !important;
  background-color: transparent !important;
  height: 60px !important;
  display: flex;
  align-items: center;
}

.search-input-right :deep(.el-input__inner) {
  height: 100%;
  line-height: 60px;
  color: #1e293b;
}

.search-input-right :deep(.el-input__inner)::placeholder {
  color: #94a3b8;
}

.search-input-right :deep(.el-input-group__append) {
  background-color: transparent;
  border: none;
  border-radius: 0;
  padding: 0;
  height: 100%;
}

.search-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  color: white !important;
  border: none !important;
  height: 100% !important; /* 占据容器全部高度 */
  padding: 0 40px !important;
  border-radius: 0 !important;
  font-weight: 700;
  margin: 0 !important;
  transition: all 0.3s;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  opacity: 0.9;
  box-shadow: inset 0 0 20px rgba(0, 0, 0, 0.1);
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}


.new-post-btn {
  border-radius: 12px;
  padding: 12px 28px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.new-post-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

/* 结果统计 */
.result-stats {
  font-size: 18px;
  font-weight: 500;
  color: #64748b;
  margin: 0 auto 16px; /* 上下间距调整，左右自动居中 */
  text-align: left;
  max-width: 1200px;
  padding: 0 20px; /* 关键：与帖子列表的 padding 保持绝对一致 */
  box-sizing: border-box;
}

.stat-count {
  color: #3b82f6;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
}

/* 帖子列表瀑布流 - 解决高度不一致导致的空白问题 */
.posts-masonry {
  display: flex;
  gap: 16px; /* 列间距 */
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
  margin-bottom: 40px;
  align-items: flex-start;
}

.masonry-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px; /* 同一列卡片之间的间距保持一致 */
}

/* 响应式调整 */
@media (max-width: 900px) {
  .posts-masonry {
    flex-direction: column; /* 小屏切换为单列 */
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .search-bar-inner {
    flex-direction: column;
    border-radius: 12px;
  }
  .sort-select-left, .search-input-right :deep(.el-input__wrapper), .search-btn {
    width: 100% !important;
    border-radius: 0 !important;
    border-bottom: 1px solid #e2e8f0 !important;
  }
  .search-btn {
    border-bottom: none !important;
  }
}


/* 发布新帖对话框美化 */
.dialog-layout {
  display: flex;
  gap: 32px;
  min-height: 480px;
}

.layout-main {
  flex: 1;
  padding-right: 8px;
}

.layout-side {
  width: 280px;
  background-color: #f8fafc;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.side-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.section-desc {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 16px;
  line-height: 1.5;
}

.side-divider {
  height: 1px;
  background-color: #e2e8f0;
  margin-bottom: 24px;
}

.side-info-card {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
}

/* 灵感发布对话框美化 */
.new-post-dialog :deep(.el-dialog) {
  border-radius: 32px; /* 更圆润的边框 */
  overflow: hidden;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.18);
}

.new-post-dialog :deep(.el-dialog__header) {
  padding: 30px 40px 10px;
  margin: 0;
}

.new-post-dialog :deep(.el-dialog__title) {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.new-post-dialog :deep(.el-dialog__body) {
  padding: 10px 40px 30px;
}

.inspiration-form-container {
  /* 移除 max-height 和 overflow，不再显示内部滚动条 */
  padding-right: 0;
}

.vibrant-input :deep(.el-input__wrapper),
.vibrant-input :deep(.el-textarea__inner) {
  border-radius: 16px;
  background-color: #f8fafc;
  border: 2px solid transparent;
  box-shadow: none !important;
  padding: 12px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.vibrant-input :deep(.el-input__wrapper:hover),
.vibrant-input :deep(.el-textarea__inner:hover) {
  background-color: #f1f5f9;
}

.vibrant-input.is-focus :deep(.el-input__wrapper),
.vibrant-input :deep(.el-textarea__inner:focus) {
  background-color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 8px 20px -6px rgba(59, 130, 246, 0.15) !important;
}

.content-textarea :deep(.el-textarea__inner) {
  font-family: inherit;
  font-size: 15px;
  line-height: 1.6;
}

/* 统一表单项间距 */
.inspiration-form :deep(.el-form-item) {
  margin-bottom: 28px;
}

.inspiration-form :deep(.el-form-item__label) {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
  margin-bottom: 12px !important;
  display: flex;
  align-items: center;
}

/* 图片上传区域 */
.vibrant-uploader :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border-radius: 20px;
  border: 2px dashed #cbd5e1;
  background-color: #f8fafc;
  transition: all 0.3s;
}

.vibrant-uploader :deep(.el-upload--picture-card:hover) {
  border-color: #3b82f6;
  background-color: #eff6ff;
  color: #3b82f6;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #64748b;
}

.upload-trigger span {
  font-size: 12px;
  font-weight: 600;
}

.vibrant-uploader :deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 20px;
  margin-right: 12px;
  border: none;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

/* 页脚按钮 */
.new-post-dialog :deep(.el-dialog__footer) {
  padding: 20px 40px 40px;
}

.dialog-footer {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

.cancel-btn {
  height: 48px !important;
  padding: 0 32px !important;
  border-radius: 14px !important;
  font-weight: 600 !important;
  color: #64748b !important;
  border: 1px solid #e2e8f0 !important;
  transition: all 0.3s !important;
}

.cancel-btn:hover {
  background-color: #f1f5f9 !important;
  color: #1e293b !important;
}

.publish-btn {
  height: 48px !important;
  padding: 0 40px !important;
  border-radius: 14px !important;
  font-weight: 700 !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3) !important;
  transition: all 0.3s !important;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 25px -5px rgba(59, 130, 246, 0.4) !important;
}

.publish-btn:active {
  transform: translateY(0);
}

/* 移除旧的对话框布局样式 */
.dialog-layout, .layout-main, .layout-side, .side-section, .side-divider, .side-info-card {
  display: none;
}

/* 分页容器样式调整 */
.pagination-container {
  margin-top: 20px;
  padding-bottom: 80px; /* 增加底部内边距，确保不被页脚或其他元素遮挡 */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
  position: relative;
  z-index: 10;
}

.pagination-info {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.current-page {
  color: #3b82f6;
  font-weight: 700;
}


:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #3b82f6 !important;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 80px 0;
  color: #64748b;
  background: white;
  border-radius: 16px;
  border: 1px dashed #e2e8f0;
}

.retry-btn {
  margin-top: 16px;
  padding: 8px 20px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>