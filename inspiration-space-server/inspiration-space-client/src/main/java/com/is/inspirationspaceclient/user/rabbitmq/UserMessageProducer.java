package com.is.inspirationspaceclient.user.rabbitmq;



import com.is.inspirationspacecommon.rabbitmq.MessageProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserMessageProducer implements MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public UserMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendSecureMessage(String module, Object message) {
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "secure.user", // 固定为user模块
                message
        );
    }

    @Override
    public void sendFastMessage(String module, Object message) {
        rabbitTemplate.convertAndSend(
                "notification_exchange",
                "fast.user",
                message
        );
    }
}
