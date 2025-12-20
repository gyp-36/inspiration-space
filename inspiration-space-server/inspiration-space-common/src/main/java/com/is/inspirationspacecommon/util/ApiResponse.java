package com.is.inspirationspacecommon.util;



import com.is.inspirationspacecommon.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
@Data
@Schema(title="ApiResponse", description ="数据响应规范结构")

    public class ApiResponse<T> implements Serializable {

        @Schema(name ="code", type ="Integer", description ="响应码")
        private Integer code;

        @Schema(name ="message", type ="String", description ="错误信息")
        private String message;

        @Schema(name ="data", description ="响应的具体数据")
        private T data;

        private ApiResponse() {}

        public static <T> ApiResponse<T> error(Integer code, String message) {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = code;
            apiResponse.message = message;
            return apiResponse;
        }

        public static <T> ApiResponse<T> error(String message) {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = -1;
            apiResponse.message = message;
            return apiResponse;
        }

        public static <T> ApiResponse<T> error(Integer code, T data) {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = -1;
            apiResponse.data = data;
            return apiResponse;
        }

        public static <T> ApiResponse<T> error(ErrorCode baseCode) {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = baseCode.getCode();//getCode还是getHttpStatus待测试
            apiResponse.message = baseCode.getMessage();
            return apiResponse;
        }

        public static <T> ApiResponse<T> error(ErrorCode baseCode, T data) {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = baseCode.getCode();
            apiResponse.message = baseCode.getMessage();
            apiResponse.data = data;
            return apiResponse;
        }

        public static <T> ApiResponse<T> error() {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = -1;
            apiResponse.message = "系统错误，请稍后重试!";
            return apiResponse;
        }

        public static <T> ApiResponse<T> ok() {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = 200;
            apiResponse.message = "操作成功!";
            return apiResponse;
        }

        public static <T> ApiResponse<T> ok(T t) {
            ApiResponse<T> apiResponse = new ApiResponse<T>();
            apiResponse.code = 200;
            apiResponse.message = "操作成功!";
            apiResponse.setData(t);
            return apiResponse;
        }
    }
