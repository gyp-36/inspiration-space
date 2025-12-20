package com.is.inspirationspacecommon.config;




import com.is.inspirationspacecommon.util.generator.CommonIdGenerator;
import com.is.inspirationspacecommon.util.generator.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Id算法配置
 */
@Configuration
public class IdConfig {
    @Value("${snowflake.datacenter-id}") // 从配置读取，默认1
    private long datacenterId;

    @Value("${snowflake.machine-id}") // 从配置读取，默认1
    private long machineId;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(datacenterId, machineId);
    }

    @Bean
    public CommonIdGenerator commonIdGenerator() {
    	return new CommonIdGenerator();
    }


}
