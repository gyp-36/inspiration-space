<!-- 登录 -->
<template>
  <div class="modal-overlay" :class="{ 'show': props.isOpen }" @click="handleOverlayClick">
    <div class="login-modal" @click.stop>
      <!-- 返回按钮 - 仅在非登录界面显示 -->
      <div v-if="props.currentView !== 'login'" class="back-button-container">
        <button class="back-button" @click="emit('switchView', 'login')">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
            <path fill-rule="evenodd"
              d="M12 8a.5.5 0 0 1-.5.5H5.707l4.147 4.146a.5.5 0 0 1-.708.708l-5-5a.5.5 0 0 1 0-.708l5-5a.5.5 0 0 1 .708.708L5.707 7.5H11.5a.5.5 0 0 1 .5.5z" />
          </svg>
        </button>
      </div>

      <div class="modal-header">
        <h3>{{ modalTitle }}</h3>
        <button class="close-button" @click="emit('close')">&times;</button>
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
                autocomplete="username">
            </div>

            <div class="form-group">
              <label for="password">密码</label>
              <input type="password" id="password" v-model="loginForm.password" placeholder="请输入密码" required
                autocomplete="current-password">
            </div>

            <div class="form-group captcha-group">
              <label for="captcha">验证码</label>
              <div class="captcha-container">
                <input type="text" id="captcha" v-model="loginForm.captcha" placeholder="请输入验证码" required>
                <div class="captcha-image" @click="refreshCaptcha">{{ captchaCode }}</div>
              </div>
            </div>

            <button type="submit" class="submit-button">登录</button>
          </form>
        </div>

        <div class="modal-footer">
          <div class="footer-links">
            <a href="#" @click.prevent="emit('switchView', 'register')">注册</a>
            <a href="#" @click.prevent="emit('switchView', 'forgot')">找回密码</a>
          </div>

          <div class="third-party-login">
            <p>第三方登录</p>
            <div class="social-icons">
              <button class="social-icon wechat">微信</button>
              <button class="social-icon qq">QQ</button>
              <button class="social-icon weibo">微博</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 注册视图  -->
      <div v-if="props.currentView === 'register'" class="register-view">
        <form class="register-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="reg-username">用户名</label>
            <input type="text" id="reg-username" v-model="registerForm.username" placeholder="请输入用户名" required>
          </div>

          <div class="form-group">
            <label for="reg-phone">手机号</label>
            <input type="tel" id="reg-phone" v-model="registerForm.phone" placeholder="请输入手机号" required>
          </div>

          <div class="form-group">
            <label for="reg-email">邮箱</label>
            <input type="email" id="reg-email" v-model="registerForm.email" placeholder="请输入邮箱" required>
          </div>

          <div class="form-group">
            <label for="reg-password">密码</label>
            <input type="password" id="reg-password" v-model="registerForm.password" placeholder="请输入密码" required>
          </div>

          <div class="form-group">
            <label for="reg-confirm-password">确认密码</label>
            <input type="password" id="reg-confirm-password" v-model="registerForm.confirmPassword"
              placeholder="请再次输入密码" required>
          </div>

          <div class="form-group agreement">
            <input type="checkbox" id="agreement" v-model="registerForm.agreement" required>
            <label for="agreement">我已阅读并同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a></label>
          </div>

          <button type="submit" class="submit-button">注册</button>
        </form>

        <div class="modal-footer">
          <p>已有账号？<a href="#" @click.prevent="emit('switchView', 'login')">登录</a></p>
        </div>
      </div>

      <!-- 找回密码视图-->
      <div v-if="props.currentView === 'forgot'" class="forgot-view">
        <form class="forgot-form" @submit.prevent="handleForgotPassword">
          <div class="form-group">
            <label for="forgot-email">邮箱</label>
            <input type="email" id="forgot-email" v-model="forgotForm.email" placeholder="请输入注册邮箱" required>
          </div>

          <div class="form-group">
            <label for="new-password">新密码</label>
            <input type="password" id="new-password" v-model="forgotForm.newPassword" placeholder="请输入新密码" required>
          </div>

          <div class="form-group">
            <label for="confirm-password">确认密码</label>
            <input type="password" id="confirm-password" v-model="forgotForm.confirmPassword" placeholder="请再次输入新密码"
              required>
          </div>

          <button type="submit" class="submit-button">重置密码</button>
        </form>

        <div class="modal-footer">
          <p>记得密码了？<a href="#" @click.prevent="emit('switchView', 'login')">返回登录</a></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue';

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
const handleLogin = () => {
  // 表单验证
  if (!loginForm.value.username) return alert('请输入用户名');
  if (!loginForm.value.password) return alert('请输入密码');
  if (loginForm.value.captcha.toUpperCase() !== captchaCode.value) return alert('验证码错误');

  // 触发登录事件，传递表单数据
  emit('login', { ...loginForm.value });
  // 清空表单
  loginForm.value = { username: '', password: '', captcha: '' };
};

// 处理注册 
const handleRegister = () => {
  // 表单验证
  if (!registerForm.value.username) return alert('请输入用户名');
  if (!registerForm.value.phone) return alert('请输入手机号');
  if (!registerForm.value.email) return alert('请输入邮箱');
  if (registerForm.value.password.length < 6) return alert('密码长度不能少于6位');
  if (registerForm.value.password !== registerForm.value.confirmPassword) return alert('两次输入的密码不一致');
  if (!registerForm.value.agreement) return alert('请同意用户协议和隐私政策');

  // 触发注册事件，传递表单数据
  emit('register', { ...registerForm.value });
  // 清空表单
  registerForm.value = {
    username: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreement: false
  };
};

// 处理找回密码 
const handleForgotPassword = () => {
  // 表单验证
  if (!forgotForm.value.email) return alert('请输入注册邮箱');
  if (forgotForm.value.newPassword.length < 6) return alert('新密码长度不能少于6位');
  if (forgotForm.value.newPassword !== forgotForm.value.confirmPassword) return alert('两次输入的密码不一致');

  // 触发找回密码事件，传递表单数据
  emit('forgotPassword', { ...forgotForm.value });
  // 清空表单
  forgotForm.value = { email: '', newPassword: '', confirmPassword: '' };
};

// 点击遮罩层关闭模态框
const handleOverlayClick = () => {
  emit('close');
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
  border-radius: 10px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 5px 30px rgba(0, 0, 0, 0.2);
  transform: translateY(-20px);
  transition: transform 0.3s ease;
  position: relative;
  overflow: hidden;
}

.modal-overlay.show .login-modal {
  transform: translateY(0);
}

/* 返回按钮 */
.back-button-container {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
}

.back-button {
  background: none;
  border: none;
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.back-button:hover {
  background-color: #f5f5f5;
}

/* 模态框头部 */
.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-button:hover {
  background-color: #f5f5f5;
  color: #333;
}

/* 登录标签容器 */
.tab-container {
  padding: 20px;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  background: none;
  border: none;
  font-size: 16px;
  color: #666;
  cursor: default;
}

.tab.active {
  color: #0066cc;
  border-bottom: 2px solid #0066cc;
  font-weight: 500;
}

/* 表单通用样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  box-sizing: border-box;
}

/* 验证码组样式 */
.captcha-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-container {
  display: flex;
  width: 100%;
  gap: 10px;
}

.captcha-image {
  flex: 0 0 100px;
  height: 40px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  cursor: pointer;
  user-select: none;
}

.captcha-btn {
  flex: 0 0 120px;
  height: 40px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.captcha-btn:hover {
  background-color: #eaeaea;
}

/* 提交按钮 */
.submit-button {
  width: 100%;
  padding: 12px;
  background-color: #0066cc;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-button:hover {
  background-color: #0055b3;
}

/* 模态框底部 */
.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background-color: #fafafa;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}

.footer-links {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.footer-links a {
  color: #0066cc;
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  text-decoration: underline;
}

/* 第三方登录 */
.third-party-login p {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin: 10px 0;
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.social-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

.social-icon:hover {
  transform: translateY(-2px);
}

.wechat {
  background-color: #07C160;
}

.qq {
  background-color: #1296DB;
}

.weibo {
  background-color: #E6162D;
}

/* 注册/找回密码视图 */
.register-view,
.forgot-view {
  padding: 20px;
}

/* 协议勾选框 */
.agreement {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.agreement label {
  font-size: 13px;
  color: #666;
}

.agreement a {
  color: #0066cc;
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .login-modal {
    width: 90%;
  }
}
</style>