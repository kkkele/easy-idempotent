package com.superkele.idempotent.config;


import com.superkele.idempotent.aspect.IdempotentAspect;
import com.superkele.idempotent.config.properties.RepeatProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;

@AutoConfiguration(after = RedisConfiguration.class)
@EnableConfigurationProperties(RepeatProperties.class)
public class IdempotentConfig {

    private static Logger logger = LoggerFactory.getLogger(IdempotentConfig.class);

    @Bean
    public IdempotentAspect idempotentLogAspect(RepeatProperties properties) {
        logger.info("\u001B[32m" + "[EASY-IDEMPOTENT]加载完毕[{}]" + "\u001B[0m", properties);
        return new IdempotentAspect(properties);
    }
}
