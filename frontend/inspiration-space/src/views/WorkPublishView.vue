<template>
  <div class="publish-container">
    <div class="publish-header">
      <div class="header-left">
        <el-button link icon="Back" @click="handleBack" class="back-btn" />
        <h2 class="page-title">发布新作品</h2>
      </div>
      <div class="header-actions">
        <el-button type="danger" class="btn-deep" @click="handleCancel">取消</el-button>
        <el-button type="info" class="btn-deep" :loading="draftLoading" @click="handleSaveDraft">暂存草稿</el-button>
        <el-button type="success" class="btn-deep" :loading="publishLoading" @click="handlePublish">正式发布</el-button>
      </div>
    </div>

    <div class="publish-content">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="publish-form">
        <el-row :gutter="24" class="main-row">
          <el-col :span="15" class="left-column">
            <div class="card-section info-section">
              <h4 class="section-title">基础信息</h4>
              <el-form-item label="作品标题" prop="title">
                <el-input 
                  v-model="form.title" 
                  placeholder="好的标题能吸引更多目光..." 
                  maxlength="100" 
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="作品描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  rows="2"
                  placeholder="详细介绍一下你的作品..."
                  maxlength="1000"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="详细内容" prop="content" class="content-form-item">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  rows="6"
                  placeholder="在此输入作品的详细内容..."
                />
              </el-form-item>
            </div>

            <div class="card-section attachment-section">
              <h4 class="section-title">
                作品附件 
                <span class="title-tip">(最多9个，每个不超过 100MB)</span>
                <span class="attachment-count" :class="{ 'at-limit': attachmentFiles.length >= 9 }">
                  {{ attachmentFiles.length }}/9
                </span>
              </h4>
              
              <div class="custom-file-list">
                <!-- 已上传的文件 -->
                <div 
                  v-for="(file, index) in attachmentFiles" 
                  :key="index" 
                  class="file-item"
                >
                  <div class="file-icon-wrapper" :class="getFileInfo(file.name).color">
                    <el-icon class="file-type-icon">
                      <component :is="getFileInfo(file.name).icon" />
                    </el-icon>
                  </div>
                  <div class="file-info">
                    <span class="file-name" :title="file.name">{{ file.name }}</span>
                  </div>
                  <div class="file-delete" @click.stop="removeAttachment(index)">
                    <el-icon><Close /></el-icon>
                  </div>
                </div>

                <!-- 上传触发器：当文件少于9个时显示 -->
                <div v-if="attachmentFiles.length < 9" class="file-item upload-trigger-item">
                  <el-upload
                    action="#"
                    multiple
                    :auto-upload="false"
                    :on-change="handleAttachmentChange"
                    :show-file-list="false"
                    :file-list="attachmentFiles"
                    class="mini-attachment-uploader"
                  >
                    <div class="upload-trigger-content">
                      <el-icon class="plus-icon"><Plus /></el-icon>
                      <span>继续添加</span>
                    </div>
                  </el-upload>
                </div>
              </div>
            </div>
          </el-col>

          <el-col :span="9" class="right-column">
            <div class="card-section cover-section">
              <h4 class="section-title">作品封面 <span class="title-tip">(JPG/PNG，不超过 10MB)</span></h4>
              <el-form-item prop="coverUrl" class="center-form-item">
                <el-upload
                  class="cover-uploader"
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleCoverChange"
                >
                  <div v-if="form.coverUrl" class="cover-preview-container">
                    <img :src="form.coverUrl" class="cover-preview" />
                    <div class="cover-mask">
                      <el-icon><Edit /></el-icon>
                      <span>更换封面</span>
                    </div>
                  </div>
                  <div v-else class="cover-placeholder">
                    <el-icon class="cover-uploader-icon"><Plus /></el-icon>
                    <span>上传封面</span>
                  </div>
                </el-upload>
              </el-form-item>
            </div>

            <div class="card-section side-settings-section">
              <h4 class="section-title">分类与标签</h4>
              <el-form-item label="作品类型" prop="type">
                <el-select v-model="form.type" placeholder="请选择作品类型" style="width: 100%">
                  <el-option label="平面设计" value="design" />
                  <el-option label="摄影" value="photography" />
                  <el-option label="插画" value="illustration" />
                  <el-option label="UI/UX" value="ui" />
                  <el-option label="3D/渲染" value="3d" />
                  <el-option label="代码/算法" value="code" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>

              <el-form-item label="作品标签">
                <el-select
                  v-model="form.tags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="选择或输入标签"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in popularTags"
                    :key="item.id"
                    :label="item.name"
                    :value="item.name"
                  />
                </el-select>
              </el-form-item>
            </div>

            <div class="card-section side-settings-section">
              <h4 class="section-title">发布设置</h4>
              <el-form-item label="访问策略" prop="accessStrategy">
                <el-radio-group v-model="form.accessStrategy" class="access-strategy-group">
                  <el-radio-button :label="0">免费</el-radio-button>
                  <el-radio-button :label="1">会员</el-radio-button>
                  <el-radio-button :label="2">付费</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item v-if="form.accessStrategy === 2" label="作品价格" prop="price">
                <el-input-number
                  v-model="form.price"
                  :min="0"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>

              <el-form-item label="可见性" prop="visibility">
                <el-select v-model="form.visibility" style="width: 100%">
                  <el-option label="公开发布" :value="0" />
                  <el-option label="仅自己可见" :value="1" />
                  <el-option label="凭密码访问" :value="2" />
                </el-select>
              </el-form-item>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Back, UploadFilled, Document, Picture, VideoCamera, Headset, Files, Close } from '@element-plus/icons-vue';
import { workService } from '@/services/workService';

const router = useRouter();
const route = useRoute();

const formRef = ref(null);
const draftLoading = ref(false);
const publishLoading = ref(false);
const attachmentFiles = ref([]);
const popularTags = ref([]);
const workId = ref(route.query.id || null);
const pendingCoverFile = ref(null);

// 获取文件图标和颜色类名
const getFileInfo = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase();
  const types = {
    // PDF
    'pdf': { icon: Document, color: 'icon-pdf' },
    // Excel
    'xls': { icon: Document, color: 'icon-excel' },
    'xlsx': { icon: Document, color: 'icon-excel' },
    'csv': { icon: Document, color: 'icon-excel' },
    // Word
    'doc': { icon: Document, color: 'icon-word' },
    'docx': { icon: Document, color: 'icon-word' },
    // PPT
    'ppt': { icon: Document, color: 'icon-ppt' },
    'pptx': { icon: Document, color: 'icon-ppt' },
    // 图片
    'jpg': { icon: Picture, color: 'icon-image' },
    'jpeg': { icon: Picture, color: 'icon-image' },
    'png': { icon: Picture, color: 'icon-image' },
    'gif': { icon: Picture, color: 'icon-image' },
    'webp': { icon: Picture, color: 'icon-image' },
    // 视频
    'mp4': { icon: VideoCamera, color: 'icon-video' },
    'webm': { icon: VideoCamera, color: 'icon-video' },
    'mov': { icon: VideoCamera, color: 'icon-video' },
    // 音频
    'mp3': { icon: Headset, color: 'icon-audio' },
    'wav': { icon: Headset, color: 'icon-audio' },
    // 压缩包
    'zip': { icon: Files, color: 'icon-zip' },
    'rar': { icon: Files, color: 'icon-zip' },
    '7z': { icon: Files, color: 'icon-zip' }
  };
  return types[ext] || { icon: Document, color: 'icon-default' };
};

const form = reactive({
  title: '',
  type: '',
  description: '',
  coverUrl: '',
  content: '',
  price: 0,
  accessStrategy: 0,
  visibility: 0,
  attachmentIds: [],
  tags: []
});

const rules = {
  title: [{ required: true, message: '请输入作品标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择作品类型', trigger: 'change' }],
  accessStrategy: [{ required: true, message: '请选择访问策略', trigger: 'change' }],
  visibility: [{ required: true, message: '请选择可见性', trigger: 'change' }],
  content: [{ required: true, message: '请输入作品内容', trigger: 'blur' }]
};

onMounted(async () => {
  fetchPopularTags();
  if (workId.value) {
    // 如果是编辑模式，加载作品数据
    loadWorkData(workId.value);
  }
});

const fetchPopularTags = async () => {
  try {
    const tags = await workService.getPopularTags();
    popularTags.value = tags || [];
  } catch (error) {
    console.error('获取标签失败:', error);
  }
};

const loadWorkData = async (id) => {
  try {
    // 假设 workService 有获取详情的方法
    const data = await workService.getWorkDetail(id);
    Object.assign(form, data);
  } catch (error) {
    ElMessage.error('加载作品数据失败');
  }
};

const handleBack = () => {
  if (form.title || form.content) {
    ElMessageBox.confirm('内容尚未保存，确定退出吗？', '提示', {
      type: 'warning'
    }).then(() => {
      router.back();
    }).catch(() => {});
  } else {
    router.back();
  }
};

const handleCoverChange = async (file) => {
  // 检查文件大小 (10MB)
  const isLt10M = file.raw.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('封面图片大小不能超过 10MB!');
    return false;
  }

  // 如果已有 workId，直接上传
  if (workId.value) {
    try {
      const url = await workService.uploadCover(file.raw, workId.value);
      form.coverUrl = url;
      ElMessage.success('封面上传成功');
    } catch (error) {
      ElMessage.error('封面上传失败: ' + error.message);
    }
  } else {
    // 如果没有 workId，先本地预览，待保存时上传
    pendingCoverFile.value = file.raw;
    form.coverUrl = URL.createObjectURL(file.raw);
  }
};

const handleAttachmentChange = (file, fileList) => {
  if (fileList.length > 9) {
    ElMessage.warning('最多只能上传 9 个附件');
    attachmentFiles.value = fileList.slice(0, 9);
    return;
  }
  attachmentFiles.value = fileList;
};

const removeAttachment = (index) => {
  attachmentFiles.value.splice(index, 1);
};

const uploadAttachments = async (id) => {
  const ids = [];
  for (let i = 0; i < attachmentFiles.value.length; i++) {
    const file = attachmentFiles.value[i];
    if (file.raw) {
      try {
        const res = await workService.uploadAttachment(id, file.raw);
        if (res && res.id) {
          ids.push(res.id);
          // 上传成功后，更新列表中的文件对象，标记为已上传并存入 ID
          attachmentFiles.value[i] = {
            ...file,
            id: res.id,
            raw: null // 清除 raw 标记，防止重复上传
          };
        }
      } catch (error) {
        console.error('附件上传失败:', file.name, error);
      }
    } else if (file.id) {
      ids.push(file.id);
    }
  }
  return ids;
};

const handleCancel = () => {
  handleBack();
};

const handleSaveDraft = async () => {
  try {
    draftLoading.value = true;
    let currentId = workId.value;
    
    if (currentId) {
      // 即使已有 ID，也需要检查并上传新添加的附件
      if (attachmentFiles.value.some(f => f.raw)) {
        const uploadedIds = await uploadAttachments(currentId);
        form.attachmentIds = uploadedIds;
      }
      await workService.updateDraft(currentId, form);
    } else {
      const newId = await workService.createDraft(form);
      currentId = newId;
      workId.value = newId;
      
      if (attachmentFiles.value.length > 0) {
        const uploadedIds = await uploadAttachments(currentId);
        form.attachmentIds = uploadedIds;
        await workService.updateDraft(currentId, form);
      }
    }
    
    ElMessage.success('草稿已暂存');
  } catch (error) {
    ElMessage.error('暂存失败: ' + error.message);
  } finally {
    draftLoading.value = false;
  }
};

const handlePublish = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        publishLoading.value = true;
        let currentId = workId.value;
        
        if (!currentId) {
          // 新建发布
          // 1. 处理 Blob 封面
          const tempCoverUrl = form.coverUrl;
          const isBlobCover = tempCoverUrl && tempCoverUrl.startsWith('blob:');
          if (isBlobCover) {
            form.coverUrl = '';
          }

          // 2. 创建草稿
          currentId = await workService.createDraft(form);
          workId.value = currentId;

          // 3. 上传封面
          let coverUpdated = false;
          if (pendingCoverFile.value) {
            const coverUrl = await workService.uploadCover(pendingCoverFile.value, currentId);
            form.coverUrl = coverUrl;
            pendingCoverFile.value = null;
            coverUpdated = true;
          }

          // 4. 上传附件
          let attachmentsUpdated = false;
          if (attachmentFiles.value.length > 0) {
            const uploadedIds = await uploadAttachments(currentId);
            form.attachmentIds = uploadedIds;
            attachmentsUpdated = true;
          }
          
          // 5. 更新草稿
          if (coverUpdated || attachmentsUpdated) {
             await workService.updateDraft(currentId, form);
          }
        } else {
            // 已有ID
            // 1. 上传待传封面
            let coverUpdated = false;
            if (pendingCoverFile.value) {
                const coverUrl = await workService.uploadCover(pendingCoverFile.value, currentId);
                form.coverUrl = coverUrl;
                pendingCoverFile.value = null;
                coverUpdated = true;
            }

            // 2. 检查附件
            let attachmentsUpdated = false;
            if (attachmentFiles.value.some(f => f.raw)) {
                const uploadedIds = await uploadAttachments(currentId);
                form.attachmentIds = uploadedIds;
                attachmentsUpdated = true;
            }

            // 3. 只有当有新上传的内容时才需要调 updateDraft，或者为了保险起见总是调一次
            // 因为用户可能改了标题等信息，所以总是要调的
            await workService.updateDraft(currentId, form);
        }
        
        await workService.publishWork(currentId);
        
        ElMessage.success('作品已正式发布！');
        router.push('/');
      } catch (error) {
        ElMessage.error('发布失败: ' + error.message);
      } finally {
        publishLoading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.publish-container {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 20px;
}

.publish-header {
  background: transparent;
  padding: 10px 0;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.btn-deep {
  font-weight: 600;
  padding: 8px 24px;
  border: none;
  transition: all 0.3s;
  height: 36px;
}

.btn-deep.el-button--success {
  background-color: #28a745 !important;
  color: #fff;
}
.btn-deep.el-button--success:hover {
  background-color: #218838 !important;
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

.btn-deep.el-button--danger {
  background-color: #dc3545 !important;
  color: #fff;
}
.btn-deep.el-button--danger:hover {
  background-color: #c82333 !important;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.btn-deep.el-button--info {
  background-color: #343a40 !important;
  color: #fff;
}
.btn-deep.el-button--info:hover {
  background-color: #23272b !important;
  box-shadow: 0 4px 12px rgba(52, 58, 64, 0.3);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.back-btn {
  font-size: 20px;
  color: #1a1a1a;
}

.publish-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.main-row {
  display: flex;
  align-items: stretch;
}

.left-column, .right-column {
  display: flex;
  flex-direction: column;
}

.info-section {
  flex-shrink: 0;
}

.attachment-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.attachment-section .center-form-item {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.attachment-count {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.attachment-count.at-limit {
  color: #f56c6c;
}

.custom-file-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 4px;
}

.file-item {
  position: relative;
  background: #fdfdfd;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  aspect-ratio: 1.4 / 1;
}

.file-item:hover {
  border-color: #0066cc;
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 102, 204, 0.08);
}

.file-icon-wrapper {
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  border-radius: 8px;
  transition: transform 0.3s;
}

/* 特定文件类型颜色 */
.icon-pdf { color: #f5222d; background: #fff1f0; }
.icon-excel { color: #52c41a; background: #f6ffed; }
.icon-word { color: #1890ff; background: #e6f7ff; }
.icon-ppt { color: #fa8c16; background: #fff7e6; }
.icon-image { color: #722ed1; background: #f9f0ff; }
.icon-video { color: #13c2c2; background: #e6fffb; }
.icon-audio { color: #eb2f96; background: #fff0f6; }
.icon-zip { color: #faad14; background: #fffbe6; }
.icon-default { color: #8c8c8c; background: #f5f5f5; }

.file-item:hover .file-icon-wrapper {
  transform: scale(1.1);
}

.file-info {
  width: 100%;
  text-align: center;
}

.file-name {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  padding: 0 6px;
}

/* 上传触发器样式 */
.upload-trigger-item {
  border: 1px dashed #dcdfe6;
  background: #fafafa;
  cursor: pointer;
}

.upload-trigger-item:hover {
  border-style: solid;
  border-color: #0066cc;
  background: #f0f7ff;
}

.mini-attachment-uploader {
  width: 100%;
  height: 100%;
}

.mini-attachment-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
}

.upload-trigger-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
}

.upload-trigger-item:hover .upload-trigger-content {
  color: #0066cc;
}

.plus-icon {
  font-size: 24px;
}

.upload-trigger-content span {
  font-size: 13px;
}

.file-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 22px;
  height: 22px;
  background: rgba(245, 108, 108, 0.9);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.2s;
  z-index: 10;
}

.file-item:hover .file-delete {
  opacity: 1;
  transform: scale(1);
}

.file-delete:hover {
  background: #f56c6c;
  transform: scale(1.1);
}

.attachment-section :deep(.el-form-item__content) {
  height: auto;
}

.right-column {
  justify-content: flex-start;
}

.publish-form :deep(.el-form-item__label) {
  padding-bottom: 2px;
  line-height: 1.2;
  font-weight: 500;
}

.content-form-item :deep(.el-textarea__inner) {
  min-height: 100px !important;
  max-height: 220px;
  overflow-y: auto;
}

.card-section {
  background: #fff;
  border-radius: 12px;
  padding: 14px 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid #f0f0f0;
  box-sizing: border-box;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
}

.title-tip {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 6px;
}

.center-form-item :deep(.el-form-item__content) {
  justify-content: center;
}

.section-title::before {
  content: '';
  width: 3px;
  height: 14px;
  background: linear-gradient(to bottom, #0066cc, #00b4ff);
  margin-right: 8px;
  border-radius: 2px;
}

.cover-uploader {
  width: 100%;
  display: flex;
  justify-content: center;
}

.cover-placeholder {
  width: 100%;
  min-height: 180px;
  background: #fafafa;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
  color: #909399;
  cursor: pointer;
  transition: all 0.3s;
}

.cover-placeholder:hover {
  border-color: #0066cc;
  color: #0066cc;
  background: #f5f7fa;
}

.cover-uploader-icon {
  font-size: 28px;
}

.cover-placeholder span {
  font-size: 13px;
}

.cover-preview-container {
  width: 100%;
  min-height: 180px;
  max-height: 400px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
}

.cover-preview {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.cover-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview-container:hover .cover-mask {
  opacity: 1;
}

.attachment-uploader {
  width: 100%;
}

.attachment-uploader :deep(.el-upload-dragger) {
  border-radius: 8px;
  padding: 20px;
  width: 100%;
}

.attachment-uploader :deep(.el-icon--upload) {
  font-size: 28px;
  margin-bottom: 4px;
  color: #0066cc;
}

.attachment-uploader :deep(.el-upload__text) {
  font-size: 13px;
}

.access-strategy-group {
  width: 100%;
  display: flex;
}

.access-strategy-group :deep(.el-radio-button) {
  flex: 1;
}

.access-strategy-group :deep(.el-radio-button__inner) {
  width: 100%;
  height: 32px;
  line-height: 32px;
  padding: 0;
}

:deep(.el-input__wrapper), 
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 6px !important;
  box-shadow: 0 0 0 1px #e0e0e0 inset !important;
}

:deep(.el-input__wrapper:hover), 
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #0066cc inset !important;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #0066cc inset !important;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #0066cc 0%, #00b4ff 100%);
  border: none;
}
</style>
