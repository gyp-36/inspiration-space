package com.is.inspirationspacecommon.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

public abstract class ModuleConsumer implements MessageConsumer {

    protected abstract String getModule();

    // 监听高安全队列，但只处理本模块消息
    @RabbitListener(queues = "secure_notification_queue")
    public final void handleSecure(@Payload Object payload,
                                   @Headers Map<String, Object> headers,
                                   Message message) {
        if (isMyModule(headers, "secure")) {
            handleSecureMessage(payload, headers);
        }
    }

    // 监听低延迟队列，但只处理本模块消息
    @RabbitListener(queues = "fast_notification_queue")
    public final void handleFast(@Payload Object payload,
                                 @Headers Map<String, Object> headers,
                                 Message message) {
        if (isMyModule(headers, "fast")) {
            handleFastMessage(payload, headers);
        }
    }

    // 监听死信队列，但只处理本模块死信
    @RabbitListener(queues = "dlq_notification_queue")
    public final void handleDlq(@Payload Object payload,
                                @Headers Map<String, Object> headers,
                                Message message) {
        if (isDlqForMyModule(headers)) {
            handleDeadLetterMessage(payload, headers);
        }
    }

    // 检查是否是本模块消息
    private boolean isMyModule(Map<String, Object> headers, String prefix) {
        String routingKey = (String) headers.get("amqp_receivedRoutingKey");
        if (routingKey == null) return false;
        return routingKey.startsWith(prefix + "." + getModule() + ".");
    }

    // 检查是否是本模块死信
    private boolean isDlqForMyModule(Map<String, Object> headers) {
        // 优先使用原始模块名（死信消息的routingKey已被覆盖为dlq.secure）
        String originalModule = (String) headers.get("x-original-module");
        if (originalModule != null) {
            return getModule().equals(originalModule);
        }

        // 兜底：尝试从routingKey解析（兼容非死信场景）
        String routingKey = (String) headers.get("amqp_receivedRoutingKey");
        return routingKey != null &&
                routingKey.matches("dlq\\.(secure|fast)\\." + getModule());
    }

    @Override
    public abstract void handleSecureMessage(Object payload, Map<String, Object> headers);

    @Override
    public abstract void handleFastMessage(Object payload, Map<String, Object> headers);

    @Override
    public abstract void handleDeadLetterMessage(Object payload, Map<String, Object> headers);

}
