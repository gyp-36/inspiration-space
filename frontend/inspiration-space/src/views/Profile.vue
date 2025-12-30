<template>
  <div class="profile-page-wrapper">
    <div class="profile-page">
      <!-- 1. 顶部封面与个人信息区域 -->
      <header class="profile-header-card">
        <!-- 封面图 (Banner) -->
        <div class="profile-banner">
          <img src="https://images.unsplash.com/photo-1557683316-973673baf926?q=80&w=2000" alt="banner" />
          <div class="banner-mask"></div>
        </div>

        <div class="header-main-content">
          <!-- 头像区域（上移与Banner重叠） -->
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="140" :src="avatarUrl" class="user-avatar">
                <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              </el-avatar>
              <div class="camera-trigger" @click="triggerFileInput" title="修改头像">
                <el-icon><Camera /></el-icon>
              </div>
              <input 
                type="file" 
                ref="fileInputRef" 
                accept="image/*" 
                style="display: none" 
                @change="handleAvatarUpload"
              />
            </div>
          </div>

          <div class="header-info-stats">
            <!-- 1. 用户名与简介 (紧靠头像右侧) -->
            <div class="user-info-side">
              <div class="username-row">
                <h2 class="username">{{ stats.username || profile.username || '未设置用户名' }}</h2>
                <div class="user-id-badge" @click="copyUserId" title="点击复制 ID">
                  <span class="id-text">ID: {{ profile.userId || stats.id || '-' }}</span>
                  <el-icon class="copy-icon"><CopyDocument /></el-icon>
                </div>
              </div>
              <p class="bio" :title="stats.bio || profile.bio">{{ stats.bio || profile.bio || '这个人很懒，什么都没有写~' }}</p>
            </div>

            <!-- 2. 数据统计 (保持在最右侧) -->
            <div class="user-stats-side">
              <div class="user-stats header-stats">
                <div class="stat-item">
                  <span class="count">{{ stats.followingsCount || 0 }}</span>
                  <span class="label">关注</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ stats.fansCount || 0 }}</span>
                  <span class="label">粉丝</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ stats.likesCount || 0 }}</span>
                  <span class="label">获赞</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ stats.favoritesCount || 0 }}</span>
                  <span class="label">收藏</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="count">{{ postsCount || 0 }}</span>
                  <span class="label">发布</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- 2. 下部导航与内容区域 -->
      <main class="profile-content-layout">
        <div class="profile-tabs-card">
          <el-tabs v-model="activeKey" class="profile-tabs" @tab-click="handleTabClick">
            <el-tab-pane label="个人信息" name="info">
              <div class="tab-content info-panel">
                <div class="section-group">
                  <div class="section-title flex-between">
                    <span class="title-text">基本资料</span>
                    <div class="section-actions">
                      <el-button type="primary" class="fancy-btn edit-btn" @click="openEditForm">
                        <el-icon><Edit /></el-icon> 编辑资料
                      </el-button>
                      <el-button type="info" plain class="fancy-btn password-btn" @click="handlePasswordChange">
                        <el-icon><Lock /></el-icon> 修改密码
                      </el-button>
                    </div>
                  </div>
                  <div class="info-grid">
                    <div class="info-item-box">
                      <span class="label">用户名</span>
                      <span class="value">{{ profile.username || '-' }}</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">手机号</span>
                      <span class="value">{{ profile.mobile || profile.phone || '未绑定' }}</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">邮箱</span>
                      <span class="value">{{ profile.email || '未绑定' }}</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">性别</span>
                      <span class="value">{{ formatGender(profile.gender) }}</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">生日</span>
                      <span class="value">{{ profile.birthDate || '未设置' }}</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">注册时间</span>
                      <span class="value">{{ formatDateTime(profile.registerTime) }}</span>
                    </div>
                  </div>
                </div>

                <div class="section-group mt-30">
                  <div class="section-title">
                    <span class="title-text">账户与安全</span>
                  </div>
                  <div class="info-grid">
                    <div class="info-item-box">
                      <span class="label">账户余额</span>
                      <span class="value highlight">{{ formatBalance(extend.balance) }} 元</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">信誉分</span>
                      <span class="value highlight">{{ extend.creditScore || 0 }}</span>
                    </div>
                    <div class="info-item-box">
                      <span class="label">状态</span>
                      <span class="value">
                        <el-tag :type="getStatusType(extend.userStatus)" size="small" effect="light">
                          {{ formatUserStatus(extend.userStatus) }}
                        </el-tag>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的发布" name="works">
              <div class="tab-content works-panel">
                <div v-if="userWorks.length > 0" class="works-grid">
                  <div v-for="work in userWorks" :key="work.workId" class="grid-item">
                    <WorkItem 
                      :work="work" 
                      :hideStats="true"
                    />
                  </div>
                </div>
                
                <div v-if="userWorks.length > 0" class="pagination-wrapper">
                  <el-pagination
                    v-model:current-page="worksPage"
                    :page-size="worksPageSize"
                    layout="prev, pager, next"
                    :total="worksTotal"
                    @current-change="handleWorksPageChange"
                  />
                </div>

                <el-empty v-else-if="!worksLoading" description="暂无作品，快去发布吧～" :image-size="200" />
                <div v-else class="loading-placeholder">
                   <el-skeleton :rows="5" animated />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="收藏帖子" name="collection">
              <div class="tab-content collection-panel">
                <div v-if="collectionPosts.length > 0" class="collection-grid">
                  <div v-for="post in collectionPosts" :key="post.postId" class="grid-item">
                    <PostCard 
                      v-bind="post" 
                      :id="post.postId"
                      :hideFooter="true"
                      class="mini-card"
                      @card-clicked="handleCardClick"
                    />
                  </div>
                </div>
                
                <div v-if="collectionPosts.length > 0" class="pagination-wrapper">
                  <el-pagination
                    v-model:current-page="collectionPage"
                    :page-size="collectionPageSize"
                    layout="prev, pager, next"
                    :total="collectionTotal"
                    @current-change="handleCollectionPageChange"
                  />
                </div>

                <el-empty v-else-if="!collectionLoading" description="暂无收藏内容～" :image-size="200" />
                <div v-else class="loading-placeholder">
                   <el-skeleton :rows="5" animated />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的评论" name="comment">
              <div class="tab-content">
                <el-empty description="暂无评论记录～" :image-size="200" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </main>

      <!-- 修改信息弹窗 (优化样式与交互) -->
      <el-dialog title="编辑个人资料" v-model="editDialogVisible" width="600px" destroy-on-close align-center class="custom-dialog">
        <div class="dialog-header-tip">
          完善个人资料，让更多人认识你
        </div>
        <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="100px" class="edit-form" label-position="top">
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="editForm.username" placeholder="请输入用户名">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" placeholder="请输入邮箱">
                <template #prefix><el-icon><Message /></el-icon></template>
              </el-input>
            </el-form-item>
          </div>

          <div class="form-grid">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="editForm.gender" class="gender-radio-group">
                <el-radio-button label="1"><el-icon><Male /></el-icon> 男</el-radio-button>
                <el-radio-button label="2"><el-icon><Female /></el-icon> 女</el-radio-button>
                <el-radio-button label="0">未知</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="生日" prop="birthDate">
              <el-date-picker 
                v-model="editForm.birthDate" 
                type="date" 
                placeholder="选择日期" 
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD" 
                style="width: 100%"
              >
                <template #prefix><el-icon><Calendar /></el-icon></template>
              </el-date-picker>
            </el-form-item>
          </div>

          <el-form-item label="简介" prop="bio">
            <el-input 
              v-model="editForm.bio" 
              type="textarea" 
              :rows="4" 
              placeholder="介绍一下自己吧..." 
              maxlength="200"
              show-word-limit
              resize="none"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeEditForm" class="footer-btn">取消</el-button>
            <el-button type="primary" @click="submitEditForm" :loading="submitting" class="footer-btn save-btn">保存修改</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 修改密码弹窗 -->
      <el-dialog title="修改密码" v-model="passwordDialogVisible" width="450px" destroy-on-close align-center class="custom-dialog">
        <div class="dialog-header-tip">
          为了您的账号安全，请定期修改密码
        </div>
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordFormRules" label-width="100px" label-position="top">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前使用的密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（不少于6位）">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closePasswordForm" class="footer-btn">取消</el-button>
            <el-button type="primary" @click="submitPasswordForm" :loading="submitting" class="footer-btn save-btn">确认修改</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
import { Camera, Edit, Lock, User, Message, Calendar, Postcard, Male, Female, CopyDocument } from '@element-plus/icons-vue';
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import PostCard from '@/components/forum/PostCard.vue';
import WorkItem from '@/components/home/WorkItem.vue';
import { getUser, getUserProfileInfo, getUserExtendInfo, updateUserInfo, updateAvatar, updatePassword, getAvatar } from '@/services/userService';
import { getCollectedPosts } from '@/services/forumService';
import { workService } from '@/services/workService';

// ========== 状态 ==========
const router = useRouter();
const activeKey = ref('info');
const profile = ref({});
const extend = ref({});
const stats = ref({});
const postsCount = ref(0);
const loading = ref(true);
const submitting = ref(false);
const fileInputRef = ref(null);
const fetchedAvatarUrl = ref('');

// 收藏相关
const collectionPosts = ref([]);
const collectionPage = ref(1);
const collectionTotal = ref(0);
const collectionLoading = ref(false);
const collectionPageSize = ref(15);

// 作品相关
const userWorks = ref([]);
const worksPage = ref(1);
const worksTotal = ref(0);
const worksLoading = ref(false);
const worksPageSize = ref(12);

// 头像URL
const avatarUrl = computed(() => {
  if (fetchedAvatarUrl.value) return fetchedAvatarUrl.value;
  const url = profile.value.avatarUrl || profile.value.avatar || stats.value.avatarUrl;
  if (url) return url;
  
  // 使用后端获取头像的接口
  const id = profile.value.userId || profile.value.id || stats.value.id;
  return id ? `http://localhost:8080/client/user/getAvatar/${id}?t=${new Date().getTime()}` : '';
});

// 表单相关
const editDialogVisible = ref(false);
const editFormRef = ref(null);
const editForm = reactive({
  username: '',
  email: '',
  gender: '0',
  birthDate: '',
  bio: ''
});

const editFormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ]
};

// 修改密码相关
const passwordDialogVisible = ref(false);
const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const passwordFormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// ========== API Methods ==========
const fetchAllUserInfo = async () => {
  loading.value = true;
  try {
    const userId = localStorage.getItem('userId') || 1;
    
    // 按照用户要求，上半部分调用 getUser 接口
    // 同时为了保证下半部分“基本资料”和“账户安全”正常显示，我们需要调用对应的详细信息接口
    // 新增：调用 getAvatar 接口获取头像
    const [userVo, profileInfo, extendInfo, avatarData] = await Promise.all([
      getUser(userId),
      getUserProfileInfo(userId),
      getUserExtendInfo(userId),
      getAvatar(userId)
    ]);
    
    // stats 包含：followingsCount, fansCount, likesCount, favoritesCount, postsCount
    stats.value = userVo || {};
    postsCount.value = stats.value.postsCount || 0;
    
    // profile 包含：username, email, phone, gender, birthDate, bio, avatarUrl, registerTime
    profile.value = profileInfo || {};
    
    // extend 包含：balance, creditScore, userStatus
    extend.value = extendInfo || {};

    // 设置头像
    if (avatarData) {
      fetchedAvatarUrl.value = avatarData;
    }

    if (stats.value.favoritesCount === undefined) {
       stats.value.favoritesCount = 0; 
    }

  } catch (err) {
    console.error('获取用户信息失败', err);
  } finally {
    loading.value = false;
  }
};

const triggerFileInput = () => {
  fileInputRef.value.click();
};

const copyUserId = () => {
  const id = profile.value.userId || stats.value.id;
  if (!id) return;
  navigator.clipboard.writeText(String(id)).then(() => {
    ElMessage.success('ID 已复制到剪贴板');
  }).catch(err => {
    console.error('复制失败:', err);
    ElMessage.error('复制失败，请手动复制');
  });
};

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  const isJPGPNG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPGPNG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!');
    return;
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!');
    return;
  }

  const formData = new FormData();
  formData.append('avatar', file); // 按照后端要求使用 'avatar' 作为字段名
  
  try {
    const userId = localStorage.getItem('userId') || 1;
    await updateAvatar(userId, formData);
    ElMessage.success('头像更新成功');
    fetchAllUserInfo(); // 刷新数据
  } catch (err) {
    console.error(err);
    ElMessage.error('头像上传失败');
  }
};

const openEditForm = () => {
  editForm.username = profile.value.username;
  editForm.email = profile.value.email;
  // 转换 gender 为字符串以匹配 radio
  editForm.gender = String(profile.value.gender || '0'); 
  editForm.birthDate = profile.value.birthDate;
  editForm.bio = profile.value.bio;
  editDialogVisible.value = true;
};

const closeEditForm = () => {
  editDialogVisible.value = false;
};

const submitEditForm = async () => {
  if (!editFormRef.value) return;
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const userId = localStorage.getItem('userId') || 1;
        await updateUserInfo({
          userId: Number(userId),
          ...editForm,
          gender: Number(editForm.gender) // 转回数字
        });
        ElMessage.success('保存成功');
        closeEditForm();
        fetchAllUserInfo();
      } catch (err) {
        ElMessage.error(err.msg || '保存失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

const handleTabClick = (tab) => {
  if (tab.paneName === 'collection') {
    // 切换到收藏时自动加载，如果已经有数据则不重复加载（除非需要强制刷新）
    if (collectionPosts.value.length === 0) {
      fetchCollection(1);
    }
  } else if (tab.paneName === 'works') {
    // 切换到作品时自动加载
    if (userWorks.value.length === 0) {
      fetchUserWorks(1);
    }
  }
};

const fetchUserWorks = async (page = 1) => {
  worksLoading.value = true;
  try {
    const userId = localStorage.getItem('userId') || 1;
    const res = await workService.getUserWorks(userId, page, worksPageSize.value);
    userWorks.value = res.records || [];
    worksTotal.value = res.total || 0;
    worksPage.value = page;
  } catch (err) {
    console.error('获取作品失败', err);
    ElMessage.error('获取作品列表失败');
  } finally {
    worksLoading.value = false;
  }
};

const handleWorksPageChange = (page) => {
  fetchUserWorks(page);
};

const fetchCollection = async (page = 1) => {
  collectionLoading.value = true;
  try {
    const res = await getCollectedPosts(page, collectionPageSize.value);
    collectionPosts.value = res.records || [];
    collectionTotal.value = res.total || 0;
    collectionPage.value = page;
  } catch (err) {
    console.error('获取收藏失败', err);
    ElMessage.error('获取收藏内容失败');
  } finally {
    collectionLoading.value = false;
  }
};

const handleCollectionPageChange = (page) => {
  fetchCollection(page);
};

const handleCardClick = (postId) => {
  router.push(`/forum/${postId}`);
};

const handlePasswordChange = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordDialogVisible.value = true;
};

const closePasswordForm = () => {
  passwordDialogVisible.value = false;
};

const submitPasswordForm = async () => {
  if (!passwordFormRef.value) return;
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const userId = localStorage.getItem('userId') || 1;
        await updatePassword({
          userId: Number(userId),
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        ElMessage.success('密码修改成功');
        closePasswordForm();
      } catch (err) {
        ElMessage.error(err.message || '密码修改失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// Helpers
const formatGender = (val) => {
  const map = { 'MALE': '男', 'FEMALE': '女', 'UNKNOWN': '未知', '1': '男', '2': '女', '0': '未知' };
  return map[val] || map[String(val)] || '未知';
};

const formatUserStatus = (status) => {
  if (status === undefined || status === null) return '未知';
  
  // 支持数字和字符串枚举名
  const map = { 
    0: '正常', 'NORMAL': '正常', 'normal': '正常',
    1: '禁言', 'DISABLED_SPEAKING': '禁言', 'disabled_speaking': '禁言',
    2: '封禁', 'BANNED': '封禁', 'banned': '封禁',
    3: '删除', 'DELETED': '删除', 'deleted': '删除'
  };
  return map[status] || '未知';
};

const getStatusType = (status) => {
  if (status === undefined || status === null) return 'info';
  
  const map = { 
    0: 'success', 'NORMAL': 'success', 'normal': 'success',
    1: 'warning', 'DISABLED_SPEAKING': 'warning', 'disabled_speaking': 'warning',
    2: 'danger', 'BANNED': 'danger', 'banned': 'danger',
    3: 'info', 'DELETED': 'info', 'deleted': 'info'
  };
  return map[status] || 'info';
};

const formatDateTime = (dateStr) => {
  return dateStr ? dateStr.replace('T', ' ').substring(0, 19) : '-';
};

const formatBalance = (val) => {
  if (val === undefined || val === null) return '0.00';
  return Number(val).toFixed(2);
};

onMounted(() => {
  fetchAllUserInfo();
});
</script>

<style scoped>
/* 页面背景与外层容器 */
.profile-page-wrapper {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%); /* 渐变背景 */
  min-height: 100vh;
  padding-bottom: 60px;
}

.profile-page {
  max-width: 1200px; /* 稍微放大 */
  margin: 0 auto;
  padding-top: 30px;
}

/* 顶部信息卡片 */
.profile-header-card {
  background: linear-gradient(to bottom, #ffffff, #fcfcfc); /* 淡淡的渐变 */
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

/* 封面图 */
.profile-banner {
  height: 280px;
  position: relative;
  overflow: hidden;
}

.collection-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-header {
  display: flex;
  justify-content: flex-start;
  padding-bottom: 10px;
}

.collection-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.grid-item {
  width: 100%;
}

.mini-card {
  transform: scale(0.95);
  transform-origin: top left;
  width: 105.26%; /* 1 / 0.95 to compensate for scale */
  margin-bottom: -10px; /* Offset for scale */
}

.mini-card :deep(.post-title) {
  font-size: 14px;
}

.mini-card :deep(.post-content) {
  font-size: 12px;
  -webkit-line-clamp: 2;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-bottom: 20px;
}

.loading-placeholder {
  padding: 40px;
}

.profile-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.profile-banner:hover img {
  transform: scale(1.05);
}

.banner-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(to top, rgba(0,0,0,0.3), transparent);
}

/* 头部主内容 */
.header-main-content {
  padding: 0 50px 40px; /* 增加内边距 */
  position: relative;
  display: flex;
  align-items: flex-end; /* 修改为底部对齐 */
}

/* 头像区域上移 */
.avatar-section {
  margin-bottom: -10px; /* 微调 */
  margin-right: 40px;
  z-index: 2;
}

.avatar-wrapper {
  position: relative;
  padding: 6px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  display: inline-block;
  margin-top: -80px; /* 移到这里 */
}

.user-avatar {
  border: 3px solid #fff;
}

.camera-trigger {
  position: absolute;
  right: 10px;
  bottom: 10px;
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  color: #606266;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
}

.camera-trigger:hover {
  background: #409eff;
  color: #fff;
  transform: scale(1.1);
}

/* 信息与统计区域 */
.header-info-stats {
  flex: 1;
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info-side {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 20px; /* 紧靠头像右侧 */
}

.user-stats-side {
  display: flex;
  align-items: center;
}

.username {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-id-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-size: 13px;
  border: 1px solid #e2e8f0;
}

.user-id-badge:hover {
  background: #e2e8f0;
  color: #2c3e50;
  transform: translateY(-1px);
}

.copy-icon {
  font-size: 14px;
}

.bio {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
  line-height: 1.6;
  max-width: 400px;
}

/* 头部统计项 - 去掉背景和圆形边框 */
.header-stats {
  background: transparent;
  padding: 8px 0;
  border-radius: 0;
  border: none;
}

/* 统计项样式 */
.user-stats {
  display: flex;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
  transition: transform 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-item:first-child { padding-left: 0; }

.stat-item .count {
  font-size: 20px;
  font-weight: 800;
  color: #34495e;
  margin-bottom: 2px;
}

.stat-item .label {
  font-size: 12px;
  color: #95a5a6;
}

.stat-divider {
  width: 1px;
  height: 20px;
  background-color: #ecf0f1;
}

/* 下部内容卡片 */
.profile-tabs-card {
  background: linear-gradient(to bottom, #ffffff, #fafafa);
  border-radius: 20px;
  padding: 10px 40px 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.fancy-btn {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s;
  border: none;
}

.edit-btn {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  color: white;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.password-btn {
  background: #f4f4f5;
  color: #606266;
  border: 1px solid #dcdfe6 !important;
}

.password-btn:hover {
  background: #e9e9eb;
  color: #409eff;
  border-color: #c6e2ff !important;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 2px;
  background-color: #f0f2f5;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 18px; /* 放大字号 */
  height: 60px;
  line-height: 60px;
  font-weight: 500;
}

.tab-content {
  padding: 30px 0;
}

/* 详情面板 */
.section-group {
  margin-bottom: 15px;
}

.section-title {
  margin-bottom: 25px;
}

.title-text {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  position: relative;
  padding-left: 15px;
}

.title-text::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 22px;
  background: linear-gradient(to bottom, #409eff, #70b9ff);
  border-radius: 3px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
}

.info-item-box {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f4f8 100%);
  padding: 20px 25px;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255,255,255,0.8);
}

.info-item-box:hover {
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f1ff 100%);
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0,0,0,0.05);
  border-color: #d9ecff;
}

.info-item-box .label {
  font-size: 14px;
  color: #95a5a6;
  margin-bottom: 8px;
}

.info-item-box .value {
  font-size: 17px;
  color: #2c3e50;
  font-weight: 600;
}

.info-item-box .value.highlight {
  color: #409eff;
  font-size: 20px;
}

.security-tip {
  font-size: 14px;
  color: #909399;
  font-style: italic;
}

.mt-30 { margin-top: 30px; }
.mt-20 { margin-top: 20px; }

/* 弹窗样式优化 */
.custom-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0,0,0,0.15);
}

.custom-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 25px 30px;
  background: linear-gradient(to right, #f8f9fa, #ffffff);
  border-bottom: 1px solid #f0f2f5;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  font-size: 20px;
  color: #2c3e50;
}

.dialog-header-tip {
  padding: 15px 30px;
  background-color: #f0f7ff;
  color: #409eff;
  font-size: 14px;
  margin-bottom: 25px;
}

.edit-form {
  padding: 0 30px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.gender-radio-group {
  display: flex;
  width: 100%;
}

.gender-radio-group :deep(.el-radio-button) {
  flex: 1;
}

.gender-radio-group :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.dialog-footer {
  padding: 10px 30px 25px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.footer-btn {
  padding: 12px 30px;
  border-radius: 12px;
  font-weight: 600;
}

.save-btn {
  background: linear-gradient(135deg, #409eff 0%, #1d82e6 100%);
  border: none;
}

.save-btn:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .profile-page {
    padding-left: 20px;
    padding-right: 20px;
  }
}

@media (max-width: 992px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .header-info-stats {
    flex-direction: column;
  }
  .header-actions {
    margin-top: 20px;
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 15px;
  }
  .header-main-content {
    flex-direction: column;
    align-items: center;
    padding: 0 20px 30px;
    text-align: center;
  }
  .avatar-section {
    margin-right: 0;
    margin-top: -60px;
  }
  .header-info-stats {
    align-items: center;
  }
  .user-stats {
    justify-content: center;
    flex-wrap: wrap;
    margin-top: 25px;
  }
  .stat-item {
    padding: 10px 20px;
  }
  .stat-divider { display: none; }
  .info-grid {
    grid-template-columns: 1fr;
  }
  .header-actions {
    justify-content: center;
  }
  .form-grid {
    grid-template-columns: 1fr;
  }
}

/* 收藏面板样式 */
.collection-panel, .works-panel {
  padding: 20px 0;
}

.collection-grid, .works-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.grid-item {
  display: flex;
  justify-content: center;
}

.works-grid .grid-item > * {
  width: 100%;
}

.mini-card {
  transform: scale(0.85);
  transform-origin: top center;
  width: 100%;
  margin: -20px 0; /* 抵消缩放带来的空白 */
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.loading-placeholder {
  padding: 40px 0;
}
</style>