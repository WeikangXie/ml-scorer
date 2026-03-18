package com.example.aiscorer.service;

import com.example.aiscorer.config.LlmConfig;
import com.example.aiscorer.dto.LlmRequest;
import com.example.aiscorer.dto.LlmResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM 推理服务
 * 
 * 支持批量调用，将多条内容拼成一个请求
 * 
 * @author 阿爪 🦞
 */
@Service
public class LlmService {
    
    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    
    @Autowired
    private LlmConfig config;
    
    private WebClient webClient;
    private ObjectMapper objectMapper;
    
    /**
     * 批量判断时的 Prompt 模板
     */
    private static final String BATCH_PROMPT_TEMPLATE = 
        "你是一个AI文本检测专家，负责判断以下文本是否由AI生成。\n\n" +
        "## AI生成文本的典型特征：\n" +
        "1. 句子结构完整，有明显的起承转合\n" +
        "2. 语气温和、正能量、礼貌\n" +
        "3. 爱用模板化表达：\"让我们一起...\"、\"每一步都值得...\"、\"加油哦~\"\n" +
        "4. 频繁使用波浪号\"~\"和emoji\n" +
        "5. 正确使用\"您\"，避免口语化表达\n\n" +
        "## 真人回复的典型特征：\n" +
        "1. 句子可能简短、不完整\n" +
        "2. 有错别字或不规范表达\n" +
        "3. 口语化、情绪化\n" +
        "4. 使用网络用语\n\n" +
        "## 评分标准：\n" +
        "- 90-100分：明显真人\n" +
        "- 70-89分：较像真人\n" +
        "- 50-69分：难以判断\n" +
        "- 30-49分：有AI痕迹\n" +
        "- 0-29分：明显AI\n\n" +
        "请对以下 %d 条文本进行评分，每条输出一行JSON：\n" +
        "{\"index\": 0, \"score\": 85, \"is_ai\": false, \"reason\": \"...\"}\n\n" +
        "待评分文本：\n%s\n\n" +
        "请输出评分结果（每条一行JSON）：";
    
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
            .baseUrl(config.getApiUrl())
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Authorization", "Bearer " + config.getApiKey())
            .build();
        this.objectMapper = new ObjectMapper();
        
        log.info("LLM 服务初始化完成，API: {}", config.getApiUrl());
    }
    
    /**
     * 批量判断文本是否为 AI 生成
     * 
     * @param contents 文本列表
     * @return 判断结果列表（按原始顺序）
     */
    public List<LlmJudgeResult> batchJudge(List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 分批处理
        List<LlmJudgeResult> allResults = new ArrayList<>();
        int batchSize = config.getBatchSize();
        
        for (int i = 0; i < contents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, contents.size());
            List<String> batch = contents.subList(i, end);
            List<LlmJudgeResult> batchResults = processBatch(batch, i);
            allResults.addAll(batchResults);
        }
        
        return allResults;
    }
    
    /**
     * 处理一批文本
     */
    private List<LlmJudgeResult> processBatch(List<String> batch, int startIndex) {
        // 构建批量请求 prompt
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < batch.size(); i++) {
            sb.append(String.format("[%d] %s\n", i, batch.get(i)));
        }
        
        String prompt = String.format(BATCH_PROMPT_TEMPLATE, batch.size(), sb.toString());
        
        // 调用 LLM（流式）
        String response = callLlm(prompt).block(Duration.ofSeconds(config.getTimeout()));
        
        // 解析结果
        return parseBatchResults(response, batch.size(), startIndex);
    }
    
    /**
     * 调用 LLM（流式调用，收集完整响应）
     */
    private Mono<String> callLlm(String prompt) {
        LlmRequest request = new LlmRequest();
        request.setModel(config.getModel());
        request.setStream(true);
        request.setMessages(Collections.singletonList(
            new LlmRequest.Message("user", prompt)
        ));
        
        StringBuilder fullResponse = new StringBuilder();
        
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String.class)
            .doOnNext(line -> {
                // 解析 SSE 格式：data: {...}
                if (line.startsWith("data: ")) {
                    String json = line.substring(6).trim();
                    if (!json.equals("[DONE]")) {
                        try {
                            LlmResponse response = objectMapper.readValue(json, LlmResponse.class);
                            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                                LlmResponse.Choice choice = response.getChoices().get(0);
                                if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                    fullResponse.append(choice.getDelta().getContent());
                                }
                            }
                        } catch (Exception e) {
                            log.debug("解析流式响应失败: {}", e.getMessage());
                        }
                    }
                }
            })
            .then(Mono.just(fullResponse.toString()));
    }
    
    /**
     * 解析批量判断结果
     */
    private List<LlmJudgeResult> parseBatchResults(String response, int expectedCount, int startIndex) {
        List<LlmJudgeResult> results = new ArrayList<>();
        
        if (response == null || response.isEmpty()) {
            log.warn("LLM 响应为空，返回默认结果");
            for (int i = 0; i < expectedCount; i++) {
                results.add(new LlmJudgeResult(startIndex + i, 50.0, false, "LLM响应为空"));
            }
            return results;
        }
        
        // 尝试解析每行的 JSON
        // 格式：{"index": 0, "score": 85, "is_ai": false, "reason": "..."}
        Pattern pattern = Pattern.compile("\\{[^}]+\"index\"[^}]+\\}");
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            try {
                String jsonStr = matcher.group();
                JsonNode node = objectMapper.readTree(jsonStr);
                
                int index = node.has("index") ? node.get("index").asInt() : results.size();
                double score = node.has("score") ? node.get("score").asDouble() : 50.0;
                boolean isAi = node.has("is_ai") ? node.get("is_ai").asBoolean() : score < 50;
                String reason = node.has("reason") ? node.get("reason").asText() : "";
                
                results.add(new LlmJudgeResult(startIndex + index, score, isAi, reason));
            } catch (Exception e) {
                log.warn("解析 JSON 失败: {}", e.getMessage());
            }
        }
        
        // 如果解析数量不足，补默认值
        while (results.size() < expectedCount) {
            results.add(new LlmJudgeResult(startIndex + results.size(), 50.0, false, "解析失败"));
        }
        
        return results;
    }
    
    /**
     * LLM 判断结果
     */
    public static class LlmJudgeResult {
        private int index;          // 原始索引
        private double score;       // 0-100 分
        private boolean isAi;       // 是否 AI 生成
        private String reason;      // 判断理由
        
        public LlmJudgeResult(int index, double score, boolean isAi, String reason) {
            this.index = index;
            this.score = score;
            this.isAi = isAi;
            this.reason = reason;
        }
        
        public int getIndex() { return index; }
        public double getScore() { return score; }
        public boolean isAi() { return isAi; }
        public String getReason() { return reason; }
        
        /**
         * 获取 AI 概率（0-1）
         */
        public double getAiProbability() {
            return (100 - score) / 100.0;
        }
    }
}
