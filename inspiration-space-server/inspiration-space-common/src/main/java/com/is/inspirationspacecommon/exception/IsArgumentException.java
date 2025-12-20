package com.is.inspirationspacecommon.exception;

import java.util.List;
import lombok.Data;

/**
 * 参数异常
 */
@Data
public class IsArgumentException extends BaseException {

    private List<ArgumentError> argumentErrorList;

    public IsArgumentException(Integer code, List<ArgumentError> argumentErrorList) {
        super("参数校验失败");
        this.code = code;
        this.argumentErrorList = argumentErrorList;
    }

    public IsArgumentException(String message) {
        super(message);
    }

    public IsArgumentException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public IsArgumentException(Throwable cause) {
        super(cause);
    }

    public IsArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IsArgumentException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
