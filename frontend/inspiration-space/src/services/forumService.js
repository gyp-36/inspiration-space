import { publicApiCall, privateApiCall } from './apiClient';

// 公共操作
export const getAllPosts = (sort = 'like', page = 1, size = 10) => {
  const params = new URLSearchParams({ sort, page, size });
  return publicApiCall(`/forum/getAll?${params.toString()}`, 'GET');
};

export const getPostDetail = (postId) => 
  privateApiCall(`/forum/getDetail/${postId}`, 'GET');

// 交互操作
export const likePost = (postId) => 
  privateApiCall('/forum/like', 'POST', { postId });

export const unlikePost = (postId) => 
  privateApiCall('/forum/unlike', 'POST', { postId });

export const collectPost = (postId) => 
  privateApiCall('/forum/collect', 'POST', { postId });

export const uncollectPost = (postId) => 
  privateApiCall('/forum/uncollect', 'POST', { postId });

export const repostPost = (postId) => 
  privateApiCall('/forum/repost', 'POST', { postId });

// 内容管理
export const createPost = (postData) => 
  privateApiCall('/forum/create', 'POST', postData);

export const updatePost = (postId, postData) => 
  privateApiCall(`/forum/update/${postId}`, 'PUT', postData);

export const deletePost = (postId) => 
  privateApiCall(`/forum/delete/${postId}`, 'DELETE');