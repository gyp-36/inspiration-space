package com.is.inspirationspacecommon.exception;


import com.is.inspirationspacecommon.enums.ErrorCode;
import com.is.inspirationspacecommon.util.ApiResponse;
import lombok.Data;

/**
 * 业务异常
 */
@Data
public class IsServiceException extends BaseException {

    public IsServiceException() {
        super();
    }

    public IsServiceException(String message) {
        super(message);
    }

    public IsServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public IsServiceException(ApiResponse apiResponse) {
        super(apiResponse.getMessage());
        this.code = apiResponse.getCode();
    }

    public IsServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public IsServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public IsServiceException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
