package com.is.inspirationspacecommon.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;



@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    /**
     * 处理所有自定义异常
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex, request.getRequestURI());
        HttpStatus httpStatus = HttpStatus.valueOf(errorResponse.getStatus());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), httpStatus);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(IsServiceException.class)
    public ResponseEntity<?> handleBusinessException(IsServiceException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(IsArgumentException.class)
    public ResponseEntity<?> handleArgumentException(IsArgumentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(IsSystemException.class)
    public ResponseEntity<?> handleSystemException(IsSystemException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}