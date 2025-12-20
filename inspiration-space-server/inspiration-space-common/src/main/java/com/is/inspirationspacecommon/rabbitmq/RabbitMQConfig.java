package com.is.inspirationspacecommon.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    // 1. 主交换机
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification_exchange", true, false);
    }


    // 2. 高安全性队列
    @Bean
    public Queue secureQueue() {
        return QueueBuilder.durable("secure_notification_queue")
                .deadLetterExchange("notification_exchange")
                .deadLetterRoutingKey("dlq.secure")
                .quorum()
                .ttl(604800000)//7天
                .build();
    }

    // 3. 低延迟队列
    @Bean
    public Queue fastQueue() {
        return QueueBuilder.durable("fast_notification_queue")
                .deadLetterExchange("notification_exchange")
                .deadLetterRoutingKey("dlq.fast")
                .withArgument("x-queue-mode", "default") // 默认内存优先
                .maxLength(100000)
                .ttl(300000)//5分钟
                .build();
    }

    // 4. 共享死信队列（分区处理）
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable("dlq_notification_queue")
                .quorum()
                .maxLength(100000)
                .ttl(604800000)
                .build();
    }


    @Bean
    public Queue offlineQueue() {
        return QueueBuilder.durable("chat.offline_queue")
                .deadLetterExchange("notification_exchange")
                .deadLetterRoutingKey("dlq.offline")
                .ttl(604800000) // 7天过期
                .maxLength(100000)
                .build();
    }

    // 5.交换机绑定队列
    @Bean
    public Binding secureBinding(TopicExchange exchange, Queue secureQueue) {
        return BindingBuilder.bind(secureQueue)
                .to(exchange)
                .with("secure.*");
    }

    @Bean
    public Binding fastBinding(TopicExchange exchange, Queue fastQueue) {
        return BindingBuilder.bind(fastQueue)
                .to(exchange)
                .with("fast.*");
    }

    @Bean
    public Binding offlineBinding(TopicExchange exchange, Queue offlineQueue) {
        return BindingBuilder.bind(offlineQueue)
                .to(exchange)
                .with("offline.*"); // 路由键格式：offline.{userId}
    }


    @Bean
    public Binding dlqSecureBinding(TopicExchange exchange, Queue dlqQueue) {
        return BindingBuilder.bind(dlqQueue)
                .to(exchange)
                .with("dlq.secure"); // 安全队列的死信
    }

    @Bean
    public Binding dlqFastBinding(TopicExchange exchange, Queue dlqQueue) {
        return BindingBuilder.bind(dlqQueue)
                .to(exchange)
                .with("dlq.fast"); // 高速队列的死信
    }

    @Bean
    public Binding dlqBinding(TopicExchange exchange, Queue dlqQueue) {
        return BindingBuilder.bind(dlqQueue)
                .to(exchange)
                .with("dlq.offline");
    }
    // 6. JSON 消息转换器
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 7. 消息头处理器
    @Bean
    public MessagePostProcessor messageHeaderProcessor() {
        return message -> {
            // 从原始路由键提取模块名并注入headers
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            if (routingKey != null && routingKey.startsWith("secure.") && routingKey.split("\\.").length >= 2) {
                String module = routingKey.split("\\.")[1]; // secure.chat.room.123 → "chat"
                message.getMessageProperties().setHeader("x-original-module", module);
            }
            return message;
        };
    }
}
