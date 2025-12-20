import router from '@/router';



export class ApiError extends Error {
  constructor(message, code, data = null) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.data = data;
  }

  isNetworkError() {
    return this.code === 500 && this.message.includes('网络连接错误');
  }

  isAuthError() {
    return this.code === 401 || this.code === 403;
  }
}
const decodeBase64Url = (base64UrlStr) => {
  try {
    // 步骤1：Base64URL → 标准Base64（先替换特殊字符）
    const standardBase64 = base64UrlStr.replace(/-/g, '+').replace(/_/g, '/');
    // 步骤2：补位（针对标准Base64，补=号）
    const paddedLength = standardBase64.length + (4 - standardBase64.length % 4) % 4;
    const paddedBase64 = standardBase64.padEnd(paddedLength, '=');
    // 步骤3：解码（现代浏览器兼容atob，加try容错）
    return atob(paddedBase64);
  } catch (e) {
    throw new Error(`Base64URL解码失败：${e.message}`);
  }
};

const BASE_URL = 'http://localhost:8080/client';

const handleHttpError = async (response, responseClone, apiType) => {
  let errorMessage = `HTTP错误 [${response.status}]`;
  let errorCode = response.status;
  let errorData = null;

  try {
    const apiResponse = await responseClone.json();
    if (apiResponse && typeof apiResponse.code === 'number') {
      errorMessage = apiResponse.message || errorMessage;
      errorCode = apiResponse.code;
      errorData = apiResponse.data;
    }
  } catch (e) {
    try {
      const text = await responseClone.text();
      errorMessage = text || errorMessage;
      if (text.startsWith('<!DOCTYPE') || text.includes('<html>')) {
        errorMessage = '服务器返回了HTML内容，可能是网关错误';
        errorCode = 502;
      }
    } catch (textError) {
      errorMessage = `HTTP错误 [${response.status}] - 无法解析响应`;
    }
  }

  console.error(`${apiType} API HTTP错误:`, { status: response.status, message: errorMessage });
  throw new ApiError(errorMessage, errorCode, errorData);
};

const handleApiResponse = async (response, responseClone, apiType) => {
  let result;
  try {
    result = await response.json();
  } catch (e) {
    const responseText = await responseClone.text();
    const errorContext = responseText.substring(0, 200) + (responseText.length > 200 ? '...' : '');

    if (responseText.includes('Whitelabel Error Page')) {
      throw new ApiError('后端服务未正确启动', 500);
    }
    throw new ApiError(`服务器返回了无效响应: ${errorContext}`, 500);
  }

  if (typeof result !== 'object' || result === null) {
    throw new ApiError('服务器返回了无效的响应结构', 500);
  }

  if (result.code !== 200) {
    const error = new ApiError(result.message || `业务错误 [${result.code}]`, result.code, result.data);

    if ((result.code === 401 || result.code === 403) && apiType === 'Private') {
      localStorage.removeItem('token');
      error.message = '会话已过期，请重新登录';
    }

    throw error;
  }

  return result.data;
};

// 通用公共API调用
export const publicApiCall = async (endpoint, method = 'GET', body = null) => {
  const options = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) options.body = JSON.stringify(body);

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, options);
    const responseClone = response.clone();

    if (!response.ok) return handleHttpError(response, responseClone, 'Public');
    return await handleApiResponse(response, responseClone, 'Public');
  } catch (error) {
    const errorMessage = error.message.includes('Failed to fetch')
      ? '网络连接错误，请检查您的网络连接'
      : error.message;
    console.error(`Public API 调用失败 [${endpoint}]:`, errorMessage);
    throw new ApiError(errorMessage, 500);
  }
};

// 通用私有API调用
// 函数参数完全不变！避免所有引用报错
export const privateApiCall = async (endpoint, method = 'GET', body = null) => {
  const validateToken = () => {
    const token = localStorage.getItem('token');
    if (!token || token.trim() === '') {
      console.error('Token校验失败：无有效token');
      throw new ApiError('未登录', 401);
    }

    const jwtRegex = /^[\w-]{1,}\.[\w-]{1,}\.[\w-]{1,}$/;
    if (!jwtRegex.test(token)) {
      console.error('Token校验失败：格式错误', { token });
      localStorage.removeItem('token');
      throw new ApiError('登录状态无效（格式错误），请重新登录', 401);
    }

    let payload;
    try {
      const [, payloadBase64] = token.split('.');
      const decodedPayloadStr = decodeBase64Url(payloadBase64);
      payload = JSON.parse(decodedPayloadStr);

      console.log('===== Token过期校验详情 =====');
      console.log('原始exp值：', payload.exp, '（类型：', typeof payload.exp, '）');

      const expSeconds = Number(payload.exp);
      if (isNaN(expSeconds)) {
        throw new Error('exp字段不是有效数字');
      }
      console.log('转换为数字的exp（秒）：', expSeconds);

      const currentUtcSeconds = Math.floor(new Date().getTime() / 1000);
      const expUtcMs = expSeconds * 1000;
      const currentLocalMs = Date.now();

      console.log('当前UTC时间（秒）：', currentUtcSeconds);
      console.log('当前本地时间（毫秒）：', currentLocalMs);
      console.log('exp对应的UTC时间（毫秒）：', expUtcMs);
      console.log('exp对应的本地时间：', new Date(expUtcMs).toLocaleString());
      console.log('当前本地时间：', new Date(currentLocalMs).toLocaleString());

      const tolerance = 5 * 60;
      const isExpired = currentUtcSeconds > (expSeconds + tolerance);
      console.log('是否过期（含5分钟容错）：', isExpired);

      if (isExpired) {
        localStorage.removeItem('token');
        throw new ApiError('会话已过期，请重新登录', 401);
      }

    } catch (decodeError) {
      console.error('Token解析/过期校验失败：', decodeError);
      localStorage.removeItem('token');
      if (decodeError.message.includes('exp字段不是有效数字')) {
        throw new ApiError('登录状态无效（exp字段异常），请重新登录', 401);
      } else {
        throw new ApiError('登录状态已失效，请重新登录', 401);
      }
    }

    return token;
  };

  try {
    const token = validateToken();

    // 1. 初始化headers（不硬编码Content-Type）
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json',
      'Cache-Control': 'no-cache'
    };

    // 2. 动态设置超时：FormData（文件上传）用10秒，其他用5秒（无参数改动）
    const timeoutMs = body instanceof FormData ? 10000 : 5000;

    const requestOptions = {
      method: method.toUpperCase(),
      headers,
      signal: AbortSignal.timeout(timeoutMs), // 动态超时
      credentials: 'include' // 跨域携带Cookie（按需）
    };

    // 3. 核心修复：区分FormData和JSON处理（不修改参数）
    if (body && !['GET', 'HEAD'].includes(requestOptions.method)) {
      if (body instanceof FormData) {
        // FormData直接赋值，删除Content-Type（浏览器自动处理）
        requestOptions.body = body;
        delete headers['Content-Type'];
      } else {
        // 普通JSON数据：设置Content-Type并stringify
        headers['Content-Type'] = 'application/json;charset=UTF-8';
        try {
          requestOptions.body = JSON.stringify(body);
        } catch (e) {
          throw new ApiError('请求参数格式错误', 400);
        }
      }
    }

    const response = await fetch(`${BASE_URL}${endpoint}`, requestOptions);
    const responseClone = response.clone();

    // 4. 统一响应处理（解包并校验业务code）
    if (!response.ok) return handleHttpError(response, responseClone, 'Private');
    return await handleApiResponse(response, responseClone, 'Private');

  } catch (error) {
    console.error('API请求异常：', error);

    // 6. 增强router校验（避免未定义报错）
    if (error instanceof ApiError && error.isAuthError()) {
      localStorage.removeItem('token');
      // 先判断router是否存在（避免未引入时报错）
      const router = window.router || (typeof $router !== 'undefined' ? $router : null);
      if (router) {
        setTimeout(async () => {
          if (router.currentRoute?.path !== '/login') {
            await router.push('/login').catch(err => {
              console.warn('路由跳转失败：', err);
              window.location.href = '/login';
            });
          }
        }, 0);
      } else {
        window.location.href = '/login';
      }
    }

    throw error;
  }
};