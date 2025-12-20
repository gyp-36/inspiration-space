<template>
  <div class="forum-container">
    <h1 class="forum-title">技术交流论坛</h1>
    
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <div class="sort-control">
        <span class="sort-label">排序方式：</span>
        <el-select v-model="sortMethod" placeholder="选择排序" @change="loadPosts" style="width: 120px">
          <el-option label="最热" value="like" />
          <el-option label="最新" value="time" />
        </el-select>
      </div>
      <el-button type="primary" @click="toggleNewPostForm">
        {{ showNewPostForm ? '取消发布' : '发布新帖' }}
      </el-button>
    </div>
    
    <!-- 新帖表单 -->
    <div v-if="showNewPostForm" class="new-post-form">
      <input 
        v-model="newPost.title" 
        placeholder="帖子标题" 
        class="input-title"
        @keyup.enter="submitPost"
      >
      <textarea 
        v-model="newPost.content" 
        placeholder="分享你的想法..." 
        class="input-content"
        @keyup.enter.ctrl="submitPost"
      ></textarea>
      <div class="form-actions">
        <button @click="submitPost" class="submit-btn">发布</button>
      </div>
    </div>
    
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
    
    <!-- 帖子列表 -->
    <div v-else class="posts-grid">
      <PostCard 
        v-for="post in posts" 
        :key="post.postId"
        :id="post.postId"
        :user-id="post.userId"
        :title="post.title"
        :content="post.content"
        :create-at="post.createAt"
        :like="post.like"
        :is-liked="post.liked"
        :collect="post.collect"
        :is-favorited="post.collected"
        :view="post.view"
        :repost="post.repost"
        :link="post.link"
        @card-clicked="handleCardClicked"
        @post-liked="handlePostLiked"
        @post-favorited="handlePostFavorited"
        @reposted="handleReposted"
        @user-loaded="handleUserLoaded"
      />
    </div>
    
    <!-- 分页控件 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        :disabled="currentPage === 1" 
        @click="prevPage"
        class="page-btn"
      >
        上一页
      </button>
      
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      
      <button 
        :disabled="currentPage === totalPages" 
        @click="nextPage"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import PostCard from '@/components/forum/PostCard.vue';
import Navigation from '@/components/home/Navigation.vue';
import {  getAllPosts, 
  createPost,
  likePost,
  unlikePost,
  collectPost,
  uncollectPost,
  repostPost} from '@/services/forumService';


// 响应式状态
const loading = ref(true);
const error = ref(null);
const showNewPostForm = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const sortMethod = ref('like');
const posts = ref([]);
const newPost = ref({ title: '', content: '' });
const totalPages = ref(1);
const totalElements = ref(0);

// Vuex store
const store = useStore();
const router = useRouter();

// 优化：统一API错误处理
const handleApiError = (error, action) => {
  if (error.message === '未登录') {
    store.dispatch('showLoginModal');
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
  
  alert(messages[action] || '操作失败，请重试');
  return true;
};

// 加载帖子列表
const loadPosts = async () => {
  try {
    loading.value = true;
    error.value = null;

    const response = await getAllPosts(sortMethod.value, currentPage.value, pageSize.value);
    
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

// 切换新帖表单显示
const toggleNewPostForm = () => {
  if (!store.getters.isAuthenticated) {
    store.dispatch('showLoginModal');
    return;
  }
  showNewPostForm.value = !showNewPostForm.value;
  if (!showNewPostForm.value) {
    newPost.value = { title: '', content: '' };
  }
};

// 提交新帖子
const submitPost = async () => {
  if (!newPost.value.title.trim() || !newPost.value.content.trim()) {
    alert('标题和内容不能为空！');
    return;
  }
  
  try {
    const response = await createPost({
      title: newPost.value.title.trim(),
      content: newPost.value.content.trim()
    });
    
    newPost.value = { title: '', content: '' };
    showNewPostForm.value = false;
    await loadPosts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (error) {
    handleApiError(error, 'create');
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
const handlePostLiked = async ({ id, liked }) => {
  // 1. 本地乐观更新
  const success = updatePostState(id, post => {
    post.liked = liked;
    post.like += liked ? 1 : -1;
  });
  
  if (!success) return;
  
  // 2. 调用API
  try {
    await (liked ? likePost(id) : unlikePost(id));
  } catch (error) {
    // 3. API失败，回滚
    updatePostState(id, post => {
      post.liked = !liked;
      post.like += liked ? -1 : 1;
    });
    handleApiError(error, liked ? 'like' : 'unlike');
  }
};

// 处理帖子收藏
const handlePostFavorited = async ({ id, favorited }) => {
  const success = updatePostState(id, post => {
    post.collected = favorited;
    post.collect += favorited ? 1 : -1;
  });
  
  if (!success) return;
  
  try {
    await (favorited ? collectPost(id) : uncollectPost(id));
  } catch (error) {
    updatePostState(id, post => {
      post.collected = !favorited;
      post.collect += favorited ? -1 : 1;
    });
    handleApiError(error, favorited ? 'collect' : 'uncollect');
  }
};

// 处理帖子转发
const handleReposted = async ({ id }) => {
  const success = updatePostState(id, post => {
    post.repost += 1;
  });
  
  if (!success) return;
  
  try {
    await repostPost(id);
    alert('转发成功！');
  } catch (error) {
    updatePostState(id, post => {
      post.repost -= 1;
    });
    handleApiError(error, 'repost');
  }
};

// 处理用户信息加载
const handleUserLoaded = ({ userId, user }) => {
  userCache.value[userId] = user;
};



// 分页控制
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    loadPosts();
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    loadPosts();
  }
};

// 组件挂载时加载数据
onMounted(loadPosts);
</script>

<style scoped>
/* 保持与之前相同的样式 */
.forum-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.forum-title {
  text-align: center;
  color: #1a1a1a;
  margin-bottom: 20px;
  font-size: 2.2rem;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sort-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  font-size: 14px;
  color: #606266;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}


.new-post-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.new-post-btn:hover {
  background-color: #40a9ff;
}

.new-post-form {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.input-title {
  width: 100%;
  padding: 12px;
  margin-bottom: 15px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 1.1rem;
  outline: none;
}

.input-title:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.input-content {
  width: 100%;
  height: 150px;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}

.input-content:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}

.submit-btn {
  background-color: #52c41a;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 40px 0;
  color: #666;
  font-size: 1.1rem;
}

.error {
  color: #ff4d4f;
}

.retry-btn {
  margin-left: 10px;
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.spinner {
  display: inline-block;
  width: 30px;
  height: 30px;
  border: 3px solid rgba(26, 188, 156, 0.3);
  border-radius: 50%;
  border-top-color: #1abc9c;
  animation: spin 1s ease-in-out infinite;
  margin-right: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 25px;
  padding: 10px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
  border-color: #8c8c8c;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #595959;
  font-size: 0.95rem;
}
</style>