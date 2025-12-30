package com.is.inspirationspaceclient;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan({
        "com.is.inspirationspaceclient",
        "com.is.inspirationspacecommon",
        "com.is.inspirationspaceadmin"
})
@MapperScan({
        "com.is.inspirationspaceclient.**.mapper",
        "com.is.inspirationspacecommon.**.mapper",
        "com.is.inspirationspaceadmin.**.mapper"
})

public class InspirationSpaceClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(InspirationSpaceClientApplication.class, args);
    }

}
