package com.is.inspirationspacecommon.rabbitmq;

/**
 * 消息生产者接口
 * 定义所有消息生产者需要实现的方法
 */
public interface MessageProducer {

    /**
     * 发送高安全性消息
     * @param module 模块名称
     * @param message 消息内容
     */
    void sendSecureMessage(String module, Object message);

    /**
     * 发送低延迟消息
     * @param module 模块名称
     * @param message 消息内容
     */
    void sendFastMessage(String module, Object message);
}