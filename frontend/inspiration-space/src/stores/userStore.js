// src/stores/userStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { login as userLoginApi, register as userRegisterApi, getAvatar as getAvatarApi, updateAvatar as updateAvatarApi } from '@/services/userService'
import { useRouter } from 'vue-router'

// 定义用户 Store
export const useUserStore = defineStore('user', () => {
    // ========== 状态管理 ==========
    const isLogin = ref(false) // 全局登录状态
    const userInfo = ref({
        userId: localStorage.getItem('userId') || '',
        token: localStorage.getItem('token') || '',
        avatarUrl: '/logo.png' // 默认头像
    })
    // 登录模态框相关
    const isModalOpen = ref(false)
    const currentView = ref('login') // login/register/forgot
    // 下拉菜单状态
    const isDropdownOpen = ref(false)

    const router = useRouter()

    // ========== 计算属性 ==========
    // 处理头像防缓存（拼接时间戳）
    const avatarUrlWithTimestamp = computed(() => {
        const url = userInfo.value.avatarUrl
        if (!url || url === '/logo.png') return '/logo.png'
        
        // 核心修复：如果是 MinIO 预签名 URL（包含 X-Amz-Signature），严禁添加额外参数
        // 否则会导致签名失效返回 403 Forbidden
        if (url.includes('X-Amz-Signature')) {
            return url
        }
        
        // 检查 URL 是否已经包含查询参数
        const separator = url.includes('?') ? '&' : '?'
        return `${url}${separator}t=${new Date().getTime()}`
    })

    // ========== 核心方法 ==========
    // 检查本地登录状态（初始化调用）
    const checkLoginStatus = () => {
        const token = localStorage.getItem('token')
        const userId = localStorage.getItem('userId')
        if (token && userId) {
            isLogin.value = true
            userInfo.value.token = token
            userInfo.value.userId = userId
            fetchUserAvatar() // 自动获取头像
        } else {
            resetUserState() // 清空状态
        }
    }

    // 获取用户头像
    const fetchUserAvatar = async () => {
        if (!userInfo.value.userId) return
        try {
            const res = await getAvatarApi(userInfo.value.userId)
            // res 现在是解包后的数据（头像URL字符串）
            if (res && typeof res === 'string') {
                userInfo.value.avatarUrl = res
            } else {
                userInfo.value.avatarUrl = '/logo.png'
            }
        } catch (error) {
            console.error('获取头像失败：', error)
            userInfo.value.avatarUrl = '/logo.png'
        }
    }



    // 打开文件选择器（替换原openChangeAvatarModal）
    const openChangeAvatarModal = () => {
        // 1. 创建隐藏的文件选择input
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/png,image/jpeg,image/jpg,image/gif'; // 限制图片类型
        fileInput.style.display = 'none';

        // 2. 监听文件选择事件
        fileInput.addEventListener('change', async (e) => {
            const file = e.target.files[0];
            if (!file) return;

            // 可选：文件大小限制（示例：5MB）
            const maxSize = 5 * 1024 * 1024;
            if (file.size > maxSize) {
                ElMessage.error('头像文件大小不能超过5MB');
                return;
            }

            // 3. 调用更新头像接口
            await updateUserAvatar(file);

            // 4. 清理DOM元素
            document.body.removeChild(fileInput);
        });

        // 3. 挂载到body并触发点击
        document.body.appendChild(fileInput);
        fileInput.click();
    };

    // 更新用户头像核心逻辑（完善版）
    const updateUserAvatar = async (avatarFile) => {
        if (!userInfo.value.userId) {
            ElMessage.error('用户未登录，无法更新头像');
            return false;
        }

        try {
            // 1. 创建FormData，字段名必须和后端@RequestParam("avatar")一致
            const formData = new FormData();
            formData.append('avatar', avatarFile); // 关键：字段名改为 avatar

            // 2. 调用接口：传递路径参数userId + FormData请求体
            const res = await updateAvatarApi(Number(userInfo.value.userId), formData);

            // 核心修复：apiClient已经解包了ApiResponse，成功时res即为后端返回的Boolean(true)
            if (res === true) {
                await fetchUserAvatar(); // 重新拉取最新头像
                ElMessage.success('头像更新成功！');
                return true;
            } else {
                ElMessage.error('头像更新未成功，请重试');
                return false;
            }
        } catch (error) {
            console.error('更新头像失败：', error);
            const errorMsg = error instanceof ApiError ? error.message : '头像更新失败，请重试';
            ElMessage.error(errorMsg);
            return false;
        }
    };

    // 登录逻辑
    const login = async (loginData) => {
        try {
            const res = await userLoginApi(loginData)
            const { token, userId } = res

            // 更新本地存储和全局状态
            localStorage.setItem('token', token)
            localStorage.setItem('userId', userId)
            userInfo.value.token = token
            userInfo.value.userId = userId
            isLogin.value = true

            // 同步获取头像（失败不影响登录）
            await fetchUserAvatar().catch(err => console.warn('获取头像失败:', err))

            ElMessage.success({ message: '登录成功！', duration: 1000 })
            closeLoginModal() // 关闭登录框
            return true
        } catch (error) {
            console.error('登录失败：', error)
            const errorMsg = error instanceof ApiError ? error.message : '登录失败，请重试'
            ElMessage.error(errorMsg)
            return false
        }
    }

    // 注册逻辑
    const register = async (registerData) => {
        try {
            // 1. 前端密码一致性校验
            if (registerData.password !== registerData.confirmPassword) {
                ElMessage.error('两次输入的密码不一致');
                return false;
            }

            // 2. 调用注册接口（publicApiCall 会自动处理响应/错误，抛出 ApiError）
            await userRegisterApi(registerData);

            // 3. 接口调用成功（code===200），执行成功逻辑
            ElMessage.success('注册成功！请登录');
            switchView('login'); // 切换到登录页
            return true;

        } catch (error) {
            console.error('注册失败：', error);
            ElMessage.error(error.message || '注册失败，请重试');
        }
    };

    // 退出登录
    const logout = async () => {
        try {
            await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            // 清空本地存储和全局状态
            resetUserState()
            ElMessage.success('退出登录成功')
            router.push('/') // 可选：退出后跳首页
        } catch (error) {
            ElMessage.info('已取消退出')
        }
    }

    // 重置用户状态（退出/登录失效时）
    const resetUserState = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        isLogin.value = false
        userInfo.value = {
            userId: '',
            token: '',
            avatarUrl: '/logo.png'
        }
    }

    // ========== 模态框相关方法 ==========
    const openLoginModal = () => {
        isModalOpen.value = true
        currentView.value = 'login' // 打开默认回到登录页
        // 禁止页面滚动
        document.body.style.overflow = 'hidden'
    }

    const closeLoginModal = () => {
        isModalOpen.value = false
        // 恢复页面滚动
        document.body.style.overflow = 'auto'
    }

    const switchView = (view) => {
        currentView.value = view
    }

    // ========== 下拉菜单相关 ==========
    const toggleDropdown = (status) => {
        isDropdownOpen.value = status
    }

    // ========== 导航跳转 ==========
    const navigateTo = (path) => {
        router.push(path)
    }

    return {
        // 状态
        isLogin,
        userInfo,
        isModalOpen,
        currentView,
        isDropdownOpen,
        // 计算属性
        avatarUrlWithTimestamp,
        // 方法
        checkLoginStatus,
        login,
        register,
        logout,
        fetchUserAvatar,
        openLoginModal,
        closeLoginModal,
        switchView,
        toggleDropdown,
        navigateTo,
        resetUserState,
        openChangeAvatarModal,
        updateUserAvatar
    }
})
