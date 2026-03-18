package com.example.aiscorer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 服务配置
 * 
 * @author 阿爪 🦞
 */
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {
    
    /**
     * LLM API 地址
     */
    private String apiUrl = "http://localhost:8081/v1/chat/completions";
    
    /**
     * 模型名称
     */
    private String model = "qwen";
    
    /**
     * API Key（如需要）
     */
    private String apiKey = "";
    
    /**
     * 请求超时时间（秒）
     */
    private int timeout = 60;
    
    /**
     * 批量请求时，每批最大条数
     */
    private int batchSize = 10;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
