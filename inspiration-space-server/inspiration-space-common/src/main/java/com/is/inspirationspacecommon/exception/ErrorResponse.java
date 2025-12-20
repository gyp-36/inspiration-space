package com.is.inspirationspacecommon.exception;


import com.is.inspirationspacecommon.enums.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 错误响应实体
 * 统一异常响应格式
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ErrorResponse {

    /**
     * 业务错误码
     */
    private final int code;

    /**
     * HTTP状态码
     */
    private final int status;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 请求路径
     */
    private final String path;

    /**
     * 时间戳
     */
    private final Instant timestamp;

    /**
     * 扩展数据
     */
    private final Map<String, Object> data;

    public ErrorResponse() {
        this(ErrorCode.SYSTEM_ERROR, null);
    }

    public ErrorResponse(ErrorCode errorCode, String path) {
        this(errorCode, path, new HashMap<>());
    }

    public ErrorResponse(ErrorCode errorCode, String path, Map<String, Object> data) {
        this.code = errorCode.getCode();
        this.status = errorCode.getHttpStatusCode();
        this.message = errorCode.getMessage();
        this.path = path;
        this.timestamp = Instant.now();
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    /**
     * 从 BaseException 创建 ErrorResponse
     */
    public ErrorResponse(BaseException ex, String path) {
        this(getErrorCodeFromException(ex), path, ex instanceof IsArgumentException ?
                Collections.singletonMap("errors", ((IsArgumentException) ex).getArgumentErrorList()) :
                new HashMap<>());
        // 如果异常中明确设置了 message，则使用它覆盖默认 message
        if (ex.getMessage() != null) {
            this.message = ex.getMessage();
        }
    }

    /**
     * 从异常中获取错误码
     */
    private static ErrorCode getErrorCodeFromException(BaseException ex) {
        if (ex instanceof IsServiceException || ex instanceof IsSystemException || ex instanceof IsArgumentException) {
            Integer code = ex.getCode();
            if (code != null) {
                // 尝试从枚举中找到对应项，否则回退到通用错误
                for (ErrorCode value : ErrorCode.values()) {
                    if (value.getCode() == code) {
                        return value;
                    }
                }
            }
        }
        // 默认映射
        if (ex instanceof IsSystemException) {
            return ErrorCode.SYSTEM_ERROR;
        } else if (ex instanceof IsArgumentException) {
            return ErrorCode.Argument_ERROR;
        } else {
            return ErrorCode.BUSINESS_ERROR;
        }
    }


    /**
     * 添加扩展数据
     */
    public ErrorResponse addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 批量添加扩展数据
     */
    public ErrorResponse addAllData(Map<String, Object> additionalData) {
        if (additionalData != null) {
            this.data.putAll(additionalData);
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "ErrorResponse{code=%d, status=%d, message='%s', path='%s', timestamp=%s, data=%s}",
                code, status, message, path, timestamp, data
        );
    }
}