package dev.heowc.khpayment.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Configuration
public class RedisServerConfig {

    @Bean
    RedisServer redisServer(RedisProperties properties) {
        RedisServer redisServer = new RedisServer(properties.getPort());
        redisServer.start();
        return redisServer;
    }
}
