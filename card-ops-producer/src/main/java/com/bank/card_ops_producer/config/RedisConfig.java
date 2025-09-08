// src/main/java/com/bank/card_ops_producer/config/RedisConfig.java
package com.bank.card_ops_producer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory cf) {
        var serializer = new StringRedisSerializer();
        var ctx = RedisSerializationContext.<String, String>newSerializationContext(serializer)
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(cf, ctx);
    }
}
