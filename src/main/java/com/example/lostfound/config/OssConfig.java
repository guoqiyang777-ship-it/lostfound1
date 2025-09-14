package com.example.lostfound.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 */
@Configuration
public class OssConfig {

    @Value("${app.oss.endpoint}")
    private String endpoint;

    @Value("${app.oss.access-key-id}")
    private String accessKeyId;

    @Value("${app.oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * 创建OSS客户端
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}