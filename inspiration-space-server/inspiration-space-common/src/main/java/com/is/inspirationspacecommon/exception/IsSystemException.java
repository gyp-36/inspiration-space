package com.is.inspirationspacecommon.exception;


import com.is.inspirationspacecommon.enums.ErrorCode;
import lombok.Data;

/**
 * 系统异常
 */
@Data
public class IsSystemException extends BaseException {

    public IsSystemException() {
        super();
    }

    public IsSystemException(String message) {
        super(message);
    }

    public IsSystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public IsSystemException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public IsSystemException(Throwable cause) {
        super(cause);
    }

    public IsSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public IsSystemException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
