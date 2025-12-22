<!-- 登录 -->
<template>
  <div class="modal-overlay" :class="{ 'show': props.isOpen }" @click="handleOverlayClick">
    <div class="login-modal" @click.stop>
      <!-- 返回按钮 - 仅在非登录界面显示 -->
      <div v-if="props.currentView !== 'login'" class="back-button-container">
        <button class="back-button" @click="emit('switchView', 'login')" :disabled="loading">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
            <path fill-rule="evenodd"
              d="M12 8a.5.5 0 0 1-.5.5H5.707l4.147 4.146a.5.5 0 0 1-.708.708l-5-5a.5.5 0 0 1 0-.708l5-5a.5.5 0 0 1 .708.708L5.707 7.5H11.5a.5.5 0 0 1 .5.5z" />
          </svg>
        </button>
      </div>

      <div class="modal-header">
        <h3>{{ modalTitle }}</h3>
        <button class="close-button" @click="emit('close')" :disabled="loading">&times;</button>
      </div>

      <!-- 登录视图  -->
      <div v-if="props.currentView === 'login'" class="tab-container">
        <div class="tabs">
          <button class="tab active">用户登录</button>
        </div>

        <div class="tab-content">
          <form class="login-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label for="username">用户名</label>
              <input type="text" id="username" v-model="loginForm.username" placeholder="请输入用户名" required
                autocomplete="username" :disabled="loading">
            </div>

            <div class="form-group">
              <label for="password">密码</label>
              <input type="password" id="password" v-model="loginForm.password" placeholder="请输入密码" required
                autocomplete="current-password" :disabled="loading">
            </div>

            <div class="form-group captcha-group">
              <label for="captcha">验证码</label>
              <div class="captcha-container">
                <input type="text" id="captcha" v-model="loginForm.captcha" placeholder="请输入验证码" required :disabled="loading">
                <div class="captcha-image" @click="!loading && refreshCaptcha" :title="loading ? '' : '点击刷新'">{{ captchaCode }}</div>
              </div>
            </div>

            <button type="submit" class="submit-button" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>
        </div>

        <div class="modal-footer">
          <div class="footer-links">
            <a href="#" @click.prevent="!loading && emit('switchView', 'register')">注册</a>
            <a href="#" @click.prevent="!loading && emit('switchView', 'forgot')">找回密码</a>
          </div>

          <div class="third-party-login">
            <p>第三方登录</p>
            <div class="social-icons">
              <button class="social-icon wechat" :disabled="loading">微信</button>
              <button class="social-icon qq" :disabled="loading">QQ</button>
              <button class="social-icon weibo" :disabled="loading">微博</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 注册视图  -->
      <div v-if="props.currentView === 'register'" class="register-view">
        <form class="register-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="reg-username">用户名</label>
            <input type="text" id="reg-username" v-model="registerForm.username" placeholder="请输入用户名" required :disabled="loading">
          </div>

          <div class="form-group">
            <label for="reg-phone">手机号</label>
            <input type="tel" id="reg-phone" v-model="registerForm.phone" placeholder="请输入手机号" required :disabled="loading">
          </div>

          <div class="form-group">
            <label for="reg-email">邮箱</label>
            <input type="email" id="reg-email" v-model="registerForm.email" placeholder="请输入邮箱" required :disabled="loading">
          </div>

          <div class="form-group">
            <label for="reg-password">密码</label>
            <input type="password" id="reg-password" v-model="registerForm.password" placeholder="请输入密码" required :disabled="loading">
          </div>

          <div class="form-group">
            <label for="reg-confirm-password">确认密码</label>
            <input type="password" id="reg-confirm-password" v-model="registerForm.confirmPassword"
              placeholder="请再次输入密码" required :disabled="loading">
          </div>

          <div class="form-group agreement">
            <input type="checkbox" id="agreement" v-model="registerForm.agreement" required :disabled="loading">
            <label for="agreement">我已阅读并同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a></label>
          </div>

          <button type="submit" class="submit-button" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <div class="modal-footer">
          <p>已有账号？<a href="#" @click.prevent="!loading && emit('switchView', 'login')">登录</a></p>
        </div>
      </div>

      <!-- 找回密码视图-->
      <div v-if="props.currentView === 'forgot'" class="forgot-view">
        <form class="forgot-form" @submit.prevent="handleForgotPassword">
          <div class="form-group">
            <label for="forgot-email">邮箱</label>
            <input type="email" id="forgot-email" v-model="forgotForm.email" placeholder="请输入注册邮箱" required :disabled="loading">
          </div>

          <div class="form-group">
            <label for="new-password">新密码</label>
            <input type="password" id="new-password" v-model="forgotForm.newPassword" placeholder="请输入新密码" required :disabled="loading">
          </div>

          <div class="form-group">
            <label for="confirm-password">确认密码</label>
            <input type="password" id="confirm-password" v-model="forgotForm.confirmPassword" placeholder="请再次输入新密码"
              required :disabled="loading">
          </div>

          <button type="submit" class="submit-button" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? '重置中...' : '重置密码' }}
          </button>
        </form>

        <div class="modal-footer">
          <p>记得密码了？<a href="#" @click.prevent="!loading && emit('switchView', 'login')">返回登录</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits, watch } from 'vue';
import { ElMessage } from 'element-plus';

// 定义Props
const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  currentView: {
    type: String,
    default: 'login',
    validator: (val) => ['login', 'register', 'forgot'].includes(val)
  }
});

// 定义事件
const emit = defineEmits(['close', 'switchView', 'login', 'register', 'forgotPassword']);

// 加载状态
const loading = ref(false);

// 监听 isOpen 变化，关闭时重置加载状态
watch(() => props.isOpen, (newVal) => {
  if (!newVal) {
    loading.value = false;
  }
});

// 登录表单数据
const loginForm = ref({
  username: '',
  password: '',
  captcha: ''
});

// 注册表单数据
const registerForm = ref({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
});

// 找回密码表单数据
const forgotForm = ref({
  email: '',
  newPassword: '',
  confirmPassword: ''
});

// 仅保留登录相关的验证码逻辑
const captchaCode = ref('ABCD');


// 模态框标题计算属性
const modalTitle = computed(() => {
  switch (props.currentView) {
    case 'login': return '用户登录';
    case 'register': return '用户注册';
    case 'forgot': return '找回密码';
    default: return '用户登录';
  }
});

// 登录验证码刷新 - 保留
const refreshCaptcha = () => {
  // 生成随机4位验证码
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  let code = '';
  for (let i = 0; i < 4; i++) {
    code += chars[Math.floor(Math.random() * chars.length)];
  }
  captchaCode.value = code;
};



// 处理登录 
const handleLogin = async () => {
  if (loading.value) return;

  // 表单验证
  if (!loginForm.value.username) return ElMessage.warning('请输入用户名');
  if (!loginForm.value.password) return ElMessage.warning('请输入密码');
  if (loginForm.value.captcha.toUpperCase() !== captchaCode.value) {
    ElMessage.error('验证码错误');
    refreshCaptcha();
    return;
  }

  try {
    loading.value = true;
    // 触发登录事件，传递表单数据
    await emit('login', { ...loginForm.value });
    // 成功后清空表单（如果是 store 处理跳转，这里也会被销毁，但清空是好习惯）
    loginForm.value = { username: '', password: '', captcha: '' };
  } finally {
    loading.value = false;
  }
};

// 处理注册 
const handleRegister = async () => {
  if (loading.value) return;

  // 表单验证
  if (!registerForm.value.username) return ElMessage.warning('请输入用户名');
  if (!registerForm.value.phone) return ElMessage.warning('请输入手机号');
  if (!registerForm.value.email) return ElMessage.warning('请输入邮箱');
  if (registerForm.value.password.length < 6) return ElMessage.warning('密码长度不能少于6位');
  if (registerForm.value.password !== registerForm.value.confirmPassword) return ElMessage.error('两次输入的密码不一致');
  if (!registerForm.value.agreement) return ElMessage.warning('请同意用户协议和隐私政策');

  try {
    loading.value = true;
    // 触发注册事件，传递表单数据
    await emit('register', { ...registerForm.value });
    // 注册成功后的清空逻辑通常在 store 回调中或 switchView 时处理
  } finally {
    loading.value = false;
  }
};

// 处理找回密码 
const handleForgotPassword = async () => {
  if (loading.value) return;

  // 表单验证
  if (!forgotForm.value.email) return ElMessage.warning('请输入注册邮箱');
  if (forgotForm.value.newPassword.length < 6) return ElMessage.warning('新密码长度不能少于6位');
  if (forgotForm.value.newPassword !== forgotForm.value.confirmPassword) return ElMessage.error('两次输入的密码不一致');

  try {
    loading.value = true;
    // 触发找回密码事件，传递表单数据
    await emit('forgotPassword', { ...forgotForm.value });
  } finally {
    loading.value = false;
  }
};

// 点击遮罩层关闭模态框
const handleOverlayClick = () => {
  if (!loading.value) {
    emit('close');
  }
};

// 初始化登录验证码
refreshCaptcha();
</script>

<style scoped>
/* 模态框遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.modal-overlay.show {
  opacity: 1;
  visibility: visible;
}

/* 模态框主体 */
.login-modal {
  background-color: white;
  border-radius: 12px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  transform: translateY(-20px);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.modal-overlay.show .login-modal {
  transform: translateY(0);
}

/* 返回按钮 */
.back-button-container {
  position: absolute;
  top: 15px;
  left: 15px;
  z-index: 10;
}

.back-button {
  background: none;
  border: none;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
  color: #666;
}

.back-button:hover:not(:disabled) {
  background-color: #f1f5f9;
  color: #0066cc;
}

.back-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 模态框头部 */
.modal-header {
  padding: 24px 20px 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.modal-header h3 {
  margin: 0;
  font-size: 22px;
  color: #1e293b;
  font-weight: 600;
}

.close-button {
  position: absolute;
  right: 15px;
  top: 15px;
  background: none;
  border: none;
  font-size: 24px;
  color: #94a3b8;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-button:hover:not(:disabled) {
  background-color: #f1f5f9;
  color: #ef4444;
}

.close-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 登录标签容器 */
.tab-container {
  padding: 0 30px 30px;
}

.tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.tab {
  padding: 8px 16px;
  background: none;
  border: none;
  font-size: 16px;
  color: #64748b;
  font-weight: 500;
  position: relative;
}

.tab.active {
  color: #2563eb;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background-color: #2563eb;
  border-radius: 2px;
}

/* 表单通用样式 */
.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  box-sizing: border-box;
  transition: all 0.2s;
  background-color: #f8fafc;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group input:disabled {
  background-color: #f1f5f9;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 验证码组样式 */
.captcha-container {
  display: flex;
  width: 100%;
  gap: 12px;
}

.captcha-image {
  flex: 0 0 100px;
  height: 40px;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #334155;
  cursor: pointer;
  user-select: none;
  letter-spacing: 2px;
  font-style: italic;
  transition: all 0.2s;
}

.captcha-image:hover {
  border-color: #cbd5e1;
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
}

/* 提交按钮 */
.submit-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
  box-shadow: 0 4px 6px rgba(37, 99, 235, 0.2);
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.3);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
}

.submit-button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

/* 加载动画 */
.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 模态框底部 */
.modal-footer {
  padding: 20px 30px;
  background-color: #f8fafc;
  border-top: 1px solid #f1f5f9;
}

.footer-links {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.footer-links a {
  color: #2563eb;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* 第三方登录 */
.third-party-login p {
  text-align: center;
  color: #64748b;
  font-size: 13px;
  margin: 0 0 12px;
  position: relative;
}

.third-party-login p::before,
.third-party-login p::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 25%;
  height: 1px;
  background-color: #e2e8f0;
}

.third-party-login p::before { left: 0; }
.third-party-login p::after { right: 0; }

.social-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  color: white;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.social-icon:hover:not(:disabled) {
  transform: scale(1.1);
}

.social-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.wechat { background-color: #07C160; }
.qq { background-color: #1296DB; }
.weibo { background-color: #E6162D; }

/* 注册/找回密码视图 */
.register-view,
.forgot-view {
  padding: 10px 30px 30px;
}

/* 协议勾选框 */
.agreement {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.agreement input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.agreement label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 0;
  cursor: pointer;
}

.agreement a {
  color: #2563eb;
  text-decoration: none;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .login-modal {
    width: 95%;
  }
  .tab-container, .register-view, .forgot-view {
    padding: 0 20px 20px;
  }
}
</style>