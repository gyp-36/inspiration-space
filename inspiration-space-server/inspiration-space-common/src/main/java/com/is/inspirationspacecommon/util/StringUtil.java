package com.is.inspirationspacecommon.util;

import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 */
public class StringUtil {

    /**
     * 检查字符串是否非空（不为null且不为空字符串）
     *
     * @param str 输入字符串
     * @return 非空返回true，否则false
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 检查字符串是否为空（null或空字符串）
     *
     * @param str 输入字符串
     * @return 空返回true，否则false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }



    /**
     * 验证手机号格式（简单中国手机号验证）
     * @param mobile 手机号
     * @return 格式正确返回true，否则false
     */
    public static boolean isValidMobile(String mobile) {
        if (isEmpty(mobile)) return false;
        // 匹配1开头的11位数字
        return Pattern.compile("^1[3-9]\\d{9}$").matcher(mobile).matches();
    }

    /**
     * 安全处理null字符串（避免NPE）
     * @param str 输入字符串
     * @return 非null字符串，null会转为空字符串
     */
    public static String safeString(String str) {
        return str == null ? "" : str;
    }

    /**
     * 截断字符串并添加省略号
     * @param str 输入字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (isEmpty(str)) return str;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}