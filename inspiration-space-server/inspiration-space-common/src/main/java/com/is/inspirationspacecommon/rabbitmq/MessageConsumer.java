package com.is.inspirationspacecommon.rabbitmq;

import java.util.Map;

/**
 * 消息消费者接口
 * 定义所有消息消费者需要实现的方法
 */
public interface MessageConsumer {

    /**
     * 处理高安全性队列消息
     * @param payload 消息体
     * @param headers 消息头
     */
    void handleSecureMessage(Object payload, Map<String, Object> headers);

    /**
     * 处理低延迟队列消息
     * @param payload 消息体
     * @param headers 消息头
     */
    void handleFastMessage(Object payload, Map<String, Object> headers);

    /**
     * 处理死信队列消息
     * @param payload 消息体
     * @param headers 消息头
     */
    void handleDeadLetterMessage(Object payload, Map<String, Object> headers);


}