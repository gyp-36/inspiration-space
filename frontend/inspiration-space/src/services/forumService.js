import { publicApiCall, privateApiCall } from './apiClient';

// 公共操作
export const getAllPosts = (sort = 'time', keyword = '', page = 1, size = 10) => {
  const params = new URLSearchParams({ sort, page, size });
  if (keyword) {
    params.append('keyword', keyword);
  }
  return publicApiCall(`/forum/getAll?${params.toString()}`, 'GET');
};

export const getPostDetail = (postId) => 
  privateApiCall(`/forum/getDetail/${postId}`, 'GET');

export const getCollectedPosts = (page = 1, size = 15) =>
  privateApiCall(`/forum/getCollected?page=${page}&size=${size}`, 'GET');

export const getPostComments = (postId) =>
  privateApiCall(`/forum/getComments/${postId}`, 'GET');

// 交互操作
export const likePost = (postId) => 
  privateApiCall(`/forum/like?postId=${postId}`, 'POST');

export const unlikePost = (postId) => 
  privateApiCall(`/forum/unlike?postId=${postId}`, 'POST');

export const collectPost = (postId) => 
  privateApiCall(`/forum/collect?postId=${postId}`, 'POST');

export const uncollectPost = (postId) => 
  privateApiCall(`/forum/uncollect?postId=${postId}`, 'POST');

export const repostPost = (postId) => 
  privateApiCall(`/forum/repost?postId=${postId}`, 'POST');

export const likeComment = (commentId) =>
  privateApiCall(`/forum/likeComment?commentId=${commentId}`, 'POST');

export const unlikeComment = (commentId) =>
  privateApiCall(`/forum/unlikeComment?commentId=${commentId}`, 'POST');

// 内容管理
export const createPost = (postData) => 
  privateApiCall('/forum/create', 'POST', postData);

export const uploadForumImage = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return privateApiCall('/forum/upload', 'POST', formData);
};

export const updatePost = (postId, postData) => 
  privateApiCall(`/forum/update/${postId}`, 'PUT', postData);

export const deletePost = (postId) => 
  privateApiCall(`/forum/delete?postId=${postId}`, 'POST');

export const createComment = (commentData) =>
  privateApiCall('/forum/comment', 'POST', commentData);
