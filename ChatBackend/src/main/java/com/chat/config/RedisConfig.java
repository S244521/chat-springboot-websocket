package com.chat.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;


/**
 * Redis配置
 *
 */
@Configuration
public class RedisConfig {

    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // Key HashKey使用String序列化
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // Value HashValue使用Json序列化
        template.setValueSerializer(genericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer());

        template.setConnectionFactory(factory);

        template.afterPropertiesSet();
        return template;
    }
// 数组序列化
//    @Bean
//    public RedisTemplate<String, Object> redisTemplateArray(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//
//        // 1. 配置 ObjectMapper
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        mapper.registerModule(new JavaTimeModule());
//        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
//                ObjectMapper.DefaultTyping.NON_FINAL);
//
//        // 2. 创建 Jackson2JsonRedisSerializer 时传入 ObjectMapper
//        Jackson2JsonRedisSerializer<Object> serializer =
//                new Jackson2JsonRedisSerializer<>(mapper, Object.class);
//
//        // 3. 设置序列化器
//        template.setKeySerializer(RedisSerializer.string());
//        template.setHashKeySerializer(RedisSerializer.string());
//        template.setValueSerializer(serializer);
//        template.setHashValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
}

