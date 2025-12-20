package com.is.inspirationspaceclient;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "com.is.inspirationspaceclient",
        "com.is.inspirationspacecommon"
})
@MapperScan({"com.is.inspirationspaceclient.forum.mapper",
        "com.is.inspirationspaceclient.user.mapper",
        "com.is.inspirationspaceclient.chat.mapper",
        "com.is.inspirationspaceclient.work.mapper",
        "com.is.inspirationspaceclient.payment.mapper",
        "com.is.inspirationspaceclient.notification.mapper"
})

public class InspirationSpaceClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(InspirationSpaceClientApplication.class, args);
    }

}
