import { publicApiCall, privateApiCall } from './apiClient';


// 公开接口
export const register = (userRegisterDto) => 
  publicApiCall('/user/register', 'POST', userRegisterDto);

export const login = (userLoginDto) => 
  publicApiCall('/user/login', 'POST', userLoginDto);

// 需要client-user角色的接口
export const getUserProfileInfo = (userId) => 
  privateApiCall(`/user/getProfile/${userId}`, 'GET');

export const getUserExtendInfo = (userId) => 
  privateApiCall(`/user/getExtend/${userId}`, 'GET');

export const getUserStatsInfo = (userId) => 
  privateApiCall(`/user/getStats/${userId}`, 'GET');

// 公开用户信息接口
export const getUser = (userId) => 
  publicApiCall(`/user/get/${userId}`, 'GET');

export const getUserInfo = (userId) => 
  publicApiCall(`/user/getUserInfo/${userId}`, 'GET');

export const getAvatar = (userId) =>
  publicApiCall(`/user/getAvatar/${userId}`, 'GET');

// 用户操作接口
export const updateUserInfo = (userUpdateDto) => 
  privateApiCall('/user/update', 'POST', userUpdateDto);

export const logout = (userIdDto) => 
  privateApiCall('/user/logout', 'POST', userIdDto);

export const updatePassword = (userChangePasswordDto) => 
  privateApiCall('/user/changePassword', 'PATCH', userChangePasswordDto);

export const deleteAccount = (userIdDto) => 
  privateApiCall('/user/delete', 'DELETE', userIdDto);

export const updateAvatar = (userId, formData) => {
  // 路径参数拼接 userId，而非 formData；请求体传递 FormData
  return privateApiCall(`/user/updateAvatar/${userId}`, 'POST', formData);
};


