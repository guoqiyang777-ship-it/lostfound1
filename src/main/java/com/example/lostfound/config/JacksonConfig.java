package com.example.lostfound.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson配置类，用于处理Java 17日期时间类型的序列化和反序列化
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper以支持Java 17日期时间API (JSR-310)
     * 主要解决LocalDateTime, LocalDate, LocalTime等类型的序列化问题
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳的功能，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}