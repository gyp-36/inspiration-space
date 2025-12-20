package com.is.inspirationspacecommon.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 参数错误
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArgumentError implements Serializable {
    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 错误字段名
     */
    private String field;

    /**
     * 错误字段值
     */
    private String value;


    /**
     * 错误信息
     */
    private String message;



}
