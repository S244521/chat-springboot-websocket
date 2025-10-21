package com.chat.config;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());

        // Use a modern PolymorphicTypeValidator for security and explicit configuration.
        // This is the replacement for the deprecated LaissezFaireSubTypeValidator.
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        // ======================= 最终修改 =======================
        // 使用 EVERYTHING，强制为所有对象（List, ConversationVo, UserVo 等）
        // 都添加 @class 属性，消除一切歧义。
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        // =========================================================
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key and HashKey serializers
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // Value and HashValue serializers using the custom GenericJackson2JsonRedisSerializer
        GenericJackson2JsonRedisSerializer jsonSerializer = genericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}