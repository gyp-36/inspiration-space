package com.is.inspirationspacecommon.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 错误码
 * 采用MTN格式
 * M - 模块(1-用户，2-作品，3-聊天，4-论坛，5-支付，6-通知)
 * T - 错误类型（0-系统，1-参数，2-业务，3-认证，4-其他）
 * N - 错误序号
 */
@Getter
public enum ErrorCode {

    // ========== 特殊状态码 ==========
    SUCCESS(200, HttpStatus.OK, "操作成功"),
    SYSTEM_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请稍后重试"),
    Argument_ERROR(400, HttpStatus.BAD_REQUEST, "参数异常，请重试"),
    BUSINESS_ERROR(429, HttpStatus.UNPROCESSABLE_ENTITY, "业务异常，请稍后重试"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "未授权"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "权限不足"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "接口不存在"),
    METHOD_NOT_ALLOWED(405, HttpStatus.METHOD_NOT_ALLOWED, "请求方法不允许"),
    REQUEST_TIMEOUT(408, HttpStatus.REQUEST_TIMEOUT, "请求超时"),

    // ========== (10XXX) ==========
    USER_NOT_FOUND(10001, HttpStatus.NOT_FOUND, "用户不存在"),
    USER_EXISTS(10002, HttpStatus.CONFLICT, "用户已存在"),
    USER_PASSWORD_ERROR(10003, HttpStatus.UNAUTHORIZED, "用户密码错误"),
    USER_NOT_LOGIN(10004, HttpStatus.UNAUTHORIZED, "用户未登录"),
    USER_NOT_ACTIVE(10005, HttpStatus.UNAUTHORIZED, "用户未激活"),
    USER_NOT_EXISTS(10006, HttpStatus.NOT_FOUND, "用户不存在"),
    USER_NOT_LOGOUT(10007, HttpStatus.UNAUTHORIZED, "用户未登出"),
    // ========== (11XXX) ==========
    EMAIL_EXISTS(11001, HttpStatus.CONFLICT, "邮箱已存在" ),
    PHONE_EXISTS(11002, HttpStatus.CONFLICT, "手机号已存在" ),
    USER_PASSWORD_ERROR_MESSAGE(11003, HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    USER_PASSWORD_NULL_ERROR(11004, HttpStatus.UNAUTHORIZED, "用户密码不能为空"),
    ROLE_ADD_FAILED(11005, HttpStatus.CONFLICT, "角色已存在"),
    PERMISSION_CODE_EXIST(11006, HttpStatus.CONFLICT, "权限码已存在"),
    PERMISSION_ID_NOT_NULL(11007, HttpStatus.NOT_FOUND, "权限ID不能为空"),
    PERMISSION_ID_NOT_EXIST(11008, HttpStatus.NOT_FOUND, "权限ID不存在"),
    PERMISSION_STATUS_CHANGE_FAILED(11009,HttpStatus.CONFLICT , "权限状态修改失败" ),
    // ========== (12XXX) ==========
    ROLE_PERMISSION_EXIST(12001, HttpStatus.CONFLICT, "存在角色关联，请先解除关联关系"),
    // ========== (13XXX) ==========
    USER_STATUS_ERROR(13001, HttpStatus.UNAUTHORIZED, "用户状态异常"),
    USER_TOKEN_ERROR(13002, HttpStatus.UNAUTHORIZED, "用户Token异常"),
    INVALID_PARAMETER_ERROR(13003, HttpStatus.BAD_REQUEST, "参数错误"),
    FORBIDDEN_ERROR(13004, HttpStatus.FORBIDDEN, "权限不足"),
    PERMISSION_DELETE_FAILED(14001, HttpStatus.FORBIDDEN, "权限删除失败"),
    ROLE_DELETE_FAILED(14002, HttpStatus.FORBIDDEN, "角色删除失败"),


    // ========== (14XXX) ==========



    // ========== (20XXX) ==========
    // ========== (21XXX) ==========
    WORK_NOT_FOUND(21001, HttpStatus.NOT_FOUND, "作品不存在"),
    ATTACHMENT_NOT_FOUND(21002, HttpStatus.NOT_FOUND, "附件不存在"),
    // ========== (22XXX) ==========

    INVALID_STATUS(22301, HttpStatus.FORBIDDEN, "只能修改草稿"),
    WORK_ALREADY_DELETED(22302, HttpStatus.FORBIDDEN, "作品已删除"),
    // ========== (23XXX) ==========
    PERMISSION_DENIED(23001, HttpStatus.FORBIDDEN, "无权修改非本人作品"),


    // ========== (24XXX) ==========

    // ========== (30XXX) ==========
    // ========== (31XXX) ==========
    // ========== (32XXX) ==========
    // ========== (33XXX) ==========
    // ========== (34XXX) ==========
    INTERNAL_ERROR( 34001, HttpStatus.INTERNAL_SERVER_ERROR, "文件上传失败" ),
    // ========== (40XXX) ==========
    // ========== (41XXX) ==========
    DTO_ARGUMENT_ERROR(41001, HttpStatus.BAD_REQUEST, "DTO参数错误"),
    // ========== (42XXX) ==========
    // ========== (43XXX) ==========
    // ========== (44XXX) ==========

    // ========== (50XXX) ==========
    SERVICE_UNAVAILABLE(50011, HttpStatus.SERVICE_UNAVAILABLE, "服务不可用，请稍后重试"),
    // ========== (51XXX) ==========
    // ========== (52XXX) ==========
    INVALID_PARAMETER(52001, HttpStatus.BAD_REQUEST, "参数错误"),
    PAYMENT_FAILED(52002, HttpStatus.PAYMENT_REQUIRED, "支付失败"),
    PAYMENT_NOT_FOUND(52003, HttpStatus.PAYMENT_REQUIRED, "支付订单不存在" ),
    INVALID_FILE_TYPE( 53001, HttpStatus.BAD_REQUEST, "文件类型错误"),
    FILE_TOO_LARGE( 53002, HttpStatus.BAD_REQUEST, "文件大小超出限制"),
    FILE_UPLOAD_FAILED( 53003, HttpStatus.BAD_REQUEST, "文件上传失败"),
    USER_NOT_FOUND_ERROR(53004, HttpStatus.NOT_FOUND, "用户不存在"),
    USER_STATS_NOT_FOUND_ERROR(53005, HttpStatus.NOT_FOUND,"用户统计信息不存在"),
    RESOURCE_NOT_FOUND(53006, HttpStatus.NOT_FOUND, "资源不存在" );



    // ========== (53XXX) ==========
    // ========== (54XXX) ==========

    // ========== (60XXX) ==========
    // ========== (61XXX) ==========
    // ========== (62XXX) ==========
    // ========== (63XXX) ==========
    // ========== (64XXX) ==========






    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 获取HTTP状态码
     */
    public int getHttpStatusCode() {
        return this.httpStatus.value();
    }
}