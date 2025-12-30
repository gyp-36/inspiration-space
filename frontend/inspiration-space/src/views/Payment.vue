<template>
  <div class="payment-container">
    <div class="payment-card glass-effect">
      <div class="payment-header">
        <el-page-header @back="goBack" content="确认订单" />
      </div>

      <div v-if="loading" class="loading-box">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="orderPreview" class="order-content">
        <div class="work-brief">
          <img :src="processImageUrl(orderPreview.coverUrl)" class="work-cover" />
          <div class="work-info">
            <h2 class="work-title">{{ orderPreview.title }}</h2>
            <p class="work-desc">{{ orderPreview.description }}</p>
          </div>
          <div class="price-box">
            <span class="currency">￥</span>
            <span class="amount">{{ orderPreview.totalPrice }}</span>
          </div>
        </div>

        <div class="payment-methods">
          <h3>选择支付方式</h3>
          <div class="method-list">
            <div 
              class="method-item" 
              :class="{ active: selectedMethod === 2 }"
              @click="selectedMethod = 2"
            >
              <img src="https://img.alicdn.com/tfs/TB19S6_m7T2gK0jSZFkXXcIQFXa-200-200.png" class="method-icon" />
              <span>支付宝</span>
              <el-icon v-if="selectedMethod === 2" class="check-icon"><Check /></el-icon>
            </div>
            <div 
              class="method-item" 
              :class="{ active: selectedMethod === 1 }"
              @click="selectedMethod = 1"
            >
              <img src="https://pay.weixin.qq.com/wiki/doc/api/img/logo.png" class="method-icon" />
              <span>微信支付</span>
              <el-icon v-if="selectedMethod === 1" class="check-icon"><Check /></el-icon>
            </div>
            <div 
              class="method-item disabled" 
              title="暂未开通"
            >
              <el-icon class="method-icon"><CreditCard /></el-icon>
              <span>银行卡</span>
            </div>
          </div>
        </div>

        <div class="payment-actions">
          <div class="total-bar">
            <span>实付金额：</span>
            <span class="total-amount">￥{{ orderPreview.totalPrice }}</span>
          </div>
          <el-button type="primary" size="large" :loading="paying" @click="handlePay">
            {{ paying ? '正在发起支付...' : '确认并支付' }}
          </el-button>
        </div>
      </div>

      <!-- 支付二维码弹窗 -->
      <el-dialog
        v-model="showQrDialog"
        title="扫码支付"
        width="300px"
        center
        :close-on-click-modal="false"
        :show-close="false"
      >
        <div class="qr-container">
          <img :src="paymentInfo.base64Image" class="qr-code" v-if="paymentInfo.base64Image" />
          <p class="qr-tip">请使用支付宝扫码支付</p>
          <p class="qr-timer">订单有效期：15分钟</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelPay">取消支付</el-button>
            <el-button type="primary" @click="checkPaymentStatus">我已完成支付</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { paymentService } from '@/services/paymentService';
import { workService } from '@/services/workService';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Check, CreditCard } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const workId = route.params.workId;

const loading = ref(true);
const paying = ref(false);
const orderPreview = ref(null);
const selectedMethod = ref(2); // 2: ALIPAY, 1: WECHAT
const showQrDialog = ref(false);
const paymentInfo = ref({});
const draftId = ref('');

const processImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return `http://localhost:8080/client/${url.startsWith('/') ? url.substring(1) : url}`;
};

const goBack = () => router.back();

const fetchPreview = async () => {
  try {
    loading.value = true;
    
    // 1. 先校验是否已购买
    const workDetail = await workService.getWorkDetail(workId);
    if (workDetail.isPurchased) {
      ElMessage.success('您已拥有该作品，无需重复购买');
      router.replace(`/work/${workId}`);
      return;
    }

    // 2. 获取订单预览
    const res = await paymentService.previewOrder(workId);
    orderPreview.value = res;
  } catch (err) {
    ElMessage.error('获取订单信息失败');
    goBack();
  } finally {
    loading.value = false;
  }
};

const handlePay = async () => {
  try {
    paying.value = true;
    
    // 1. 创建草稿
    const draftRes = await paymentService.createOrderDraft(workId);
    draftId.value = draftRes.draftId;

    // 2. 发起支付
    const payRes = await paymentService.pay(draftId.value, {
      subject: orderPreview.value.title,
      amount: orderPreview.value.totalPrice,
      paymentMethod: selectedMethod.value
    });

    paymentInfo.value = payRes;
    showQrDialog.value = true;
  } catch (err) {
    console.error('支付发起失败', err);
    ElMessage.error(err.message || '发起支付失败');
  } finally {
    paying.value = false;
  }
};

const checkPaymentStatus = async () => {
  try {
    // 轮询或手动检查。这里我们调用 updateStatus 实际上在后端会触发同步或检查
    await paymentService.checkStatus(paymentInfo.value.transactionNo);
    
    ElMessage.success('支付成功！');
    showQrDialog.value = false;
    // 跳转回作品详情页，详情页会自动刷新并显示附件
    router.push(`/work/${workId}`);
  } catch (err) {
    ElMessage.info('未检测到支付成功，请稍后再试');
  }
};

const cancelPay = () => {
  ElMessageBox.confirm('确定要取消支付吗？', '提示', {
    type: 'warning'
  }).then(() => {
    showQrDialog.value = false;
  });
};

onMounted(() => {
  fetchPreview();
});
</script>

<style scoped>
.payment-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.payment-card {
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.work-brief {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
  margin: 24px 0;
}

.work-cover {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.work-info {
  flex: 1;
}

.work-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
}

.work-desc {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-box {
  color: #f56c6c;
  font-weight: 800;
}

.amount {
  font-size: 28px;
}

.payment-methods {
  margin: 32px 0;
}

.method-list {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.method-item:hover {
  border-color: #409eff;
}

.method-item.active {
  border-color: var(--el-color-primary);
  background-color: rgba(64, 158, 255, 0.1);
}

.method-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #f5f7fa;
}

.method-icon {
  width: 24px;
  height: 24px;
}

.check-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  padding: 2px;
  font-size: 12px;
}

.payment-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 32px;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #e2e8f0;
}

.total-amount {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 800;
}

.qr-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin-bottom: 16px;
}

.qr-tip {
  font-weight: 600;
  margin-bottom: 8px;
}

.qr-timer {
  font-size: 12px;
  color: #94a3b8;
}
</style>
