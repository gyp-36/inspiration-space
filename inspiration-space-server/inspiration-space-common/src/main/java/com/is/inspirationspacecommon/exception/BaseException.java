package com.is.inspirationspacecommon.exception;

import lombok.Data;

/**
 * 自定义异常基类
 */
@Data
public class BaseException extends RuntimeException {
    protected Integer code;

    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }


}