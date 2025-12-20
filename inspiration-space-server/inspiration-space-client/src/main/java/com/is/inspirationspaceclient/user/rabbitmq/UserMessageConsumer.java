package com.is.inspirationspaceclient.user.rabbitmq;




import com.is.inspirationspacecommon.rabbitmq.ModuleConsumer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserMessageConsumer extends ModuleConsumer {

    @Override
    protected String getModule() {
        return "user"; // 声明所属模块
    }

    @Override
    public void handleSecureMessage(Object payload, Map<String, Object> headers) {
        // 处理通知模块的高安全消息

    }

    @Override
    public void handleFastMessage(Object payload, Map<String, Object> headers) {
        // 处理通知模块的低延迟消息
    }

    @Override
    public void handleDeadLetterMessage(Object payload, Map<String, Object> headers) {
        // 处理通知模块的死信
    }


}
