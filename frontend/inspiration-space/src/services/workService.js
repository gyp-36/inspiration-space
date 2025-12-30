import { privateApiCall, publicApiCall } from './apiClient';

/**
 * 作品相关服务
 */
export const workService = {
  /**
   * 创建草稿
   * @param {Object} workData 作品数据 (WorkCreateDto)
   * @returns {Promise<Boolean>}
   */
  createDraft: (workData) => {
    return privateApiCall('/works/createDraft', 'POST', workData);
  },

  /**
   * 修改草稿
   * @param {Number|String} workId 作品ID
   * @param {Object} workData 作品数据 (WorkCreateDto)
   * @returns {Promise<Boolean>}
   */
  updateDraft: (workId, workData) => {
    return privateApiCall(`/works/updateDraft/${workId}`, 'PUT', workData);
  },

  /**
   * 发布作品
   * @param {Number|String} workId 作品ID
   * @returns {Promise<Boolean>}
   */
  publishWork: (workId) => {
    return privateApiCall(`/works/publish/${workId}`, 'POST');
  },

  /**
   * 上传封面
   * @param {File} file 封面文件
   * @param {Number|String} workId 作品ID (可选)
   * @returns {Promise<String>} 封面URL
   */
  uploadCover: (file, workId = null) => {
    const formData = new FormData();
    formData.append('file', file);
    if (workId) {
      formData.append('workId', workId);
    }
    return privateApiCall('/works/upload/cover', 'POST', formData);
  },

  /**
   * 上传作品附件
   * @param {Number|String} workId 作品ID
   * @param {File} file 附件文件
   * @returns {Promise<Object>} 附件信息 (WorkAttachmentVo)
   */
  uploadAttachment: (workId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    // 注意：后端 WorkAttachmentsController 这里用的是 @RequestBody Long workId
    // 这在 multipart 请求中比较特殊，我们尝试将其作为 part 发送
    // 如果后端无法解析，可能需要调整后端或改为 query 参数
    formData.append('workId', workId); 
    return privateApiCall('/works-attachments/upload', 'POST', formData);
  },

  /**
   * 删除作品附件
   * @param {Number|String} attachmentId 附件ID
   * @returns {Promise<Boolean>}
   */
  deleteAttachment: (attachmentId) => {
    return privateApiCall(`/works-attachments/delete/${attachmentId}`, 'DELETE');
  },

  /**
   * 获取作品附件列表
   * @param {Number|String} workId 作品ID
   * @returns {Promise<Array>} 附件列表
   */
  getWorkAttachments: (workId) => {
    return privateApiCall(`/works-attachments/list/${workId}`, 'GET');
  },

  /**
   * 获取热门标签
   * @param {Number} topN 前N个
   * @returns {Promise<Array>} 标签列表
   */
  getPopularTags: (topN = 10) => {
    return publicApiCall(`/works-tags/popular?topN=${topN}`, 'GET');
  },

  /**
   * 获取所有标签列表 (需要管理员权限，普通用户可能用不了，这里作为备选)
   */
  getTagList: (pageNum = 1, pageSize = 20) => {
    return privateApiCall(`/works-tags/list?pageNum=${pageNum}&pageSize=${pageSize}`, 'GET');
  },

  /**
   * 获取用户作品列表 (分页)
   * @param {Number|String} userId 用户ID
   * @param {Number} page 页码
   * @param {Number} size 每页数量
   * @returns {Promise<Object>} 分页结果 (Page<WorkSimpleVo>)
   */
  getUserWorks: (userId, page = 1, size = 10) => {
    return publicApiCall(`/works/user/${userId}?page=${page}&size=${size}`, 'GET');
  },

  /**
   * 获取公共作品列表 (分页)
   * @param {Number} page 页码
   * @param {Number} size 每页数量
   * @returns {Promise<Object>} 分页结果 (Page<WorkSimpleVo>)
   */
  getPublicWorks: (page = 1, size = 10) => {
    return publicApiCall(`/works/public/list?page=${page}&size=${size}`, 'GET');
  },

  /**
   * 搜索作品 (分页)
   * @param {String} keyword 关键词
   * @param {Number} page 页码
   * @param {Number} size 每页数量
   * @returns {Promise<Object>} 分页结果 (Page<WorkSimpleVo>)
   */
  searchWorks: (keyword, page = 1, size = 10) => {
    return publicApiCall(`/works/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`, 'GET');
  },

  /**
   * 获取作品排行榜 (点赞榜)
   * @param {Number} topN 前N个
   * @returns {Promise<Array>} 作品列表 (WorkStateVo)
   */
  getWorkLikeRank: (topN = 10) => {
    return publicApiCall(`/works/stats/rank?topN=${topN}`, 'GET');
  },

  /**
   * 获取作品详情 (公开)
   * @param {Number|String} workId 作品ID
   */
  getWorkDetail: (workId) => {
    return publicApiCall(`/works/public/detail/${workId}`, 'GET');
  },

  /**
   * 获取作品评论
   * @param {Number|String} workId 作品ID
   */
  getWorkComments: (workId) => {
    return publicApiCall(`/works-comments/list/${workId}`, 'GET');
  },

  /**
   * 发表作品评论
   * @param {Object} commentData 评论数据 { workId, content }
   */
  createWorkComment: (commentData) => {
    return privateApiCall('/works-comments/create', 'POST', commentData);
  },

  /**
   * 点赞/取消点赞作品评论
   * @param {Number|String} commentId 评论ID
   */
  toggleWorkCommentLike: (commentId) => {
    return privateApiCall(`/works-comments/like/${commentId}`, 'POST');
  },

  /**
   * 切换点赞状态
   * @param {Number|String} workId 作品ID
   */
  toggleLike: (workId) => {
    return privateApiCall(`/works/stats/${workId}/toggle-like`, 'POST');
  },

  /**
   * 切换收藏状态
   * @param {Number|String} workId 作品ID
   */
  toggleCollect: (workId) => {
    return privateApiCall(`/works/stats/${workId}/toggle-collect`, 'POST');
  },

  /**
   * 增加作品浏览量
   * @param {Number|String} workId 作品ID
   */
  incrementViewCount: (workId) => {
    return publicApiCall(`/works/stats/${workId}/view`, 'POST');
  }
};
