package com.cj.genieq.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 * 
 * 결제 임시 데이터 저장을 위한 Redis 연결 및 직렬화 설정
 * Jackson2JsonRedisSerializer를 사용하여 객체를 JSON으로 저장
 */
@Slf4j
@Configuration
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    /**
     * Redis 연결 팩토리 설정
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        // 비밀번호가 설정된 경우에만 적용
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.setPassword(redisPassword);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        
        log.info("Redis 연결 설정 완료 - Host: {}, Port: {}", redisHost, redisPort);
        
        return factory;
    }
    
    /**
     * RedisTemplate 설정
     * JSON 직렬화를 통해 객체를 Redis에 저장
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Jackson2JsonRedisSerializer 설정
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
                new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // Java 8 시간 API 지원
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        
        // String 직렬화 설정
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // Key는 String으로, Value는 JSON으로 직렬화
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        // 트랜잭션 지원 활성화
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        
        log.info("RedisTemplate 설정 완료 - JSON 직렬화 활성화");
        
        return template;
    }
    
    /**
     * ObjectMapper Bean (PaymentTemporaryStorage에서 사용)
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}