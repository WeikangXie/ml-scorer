package com.example.aiscorer.dto;

import java.util.Map;

/**
 * 预测响应 DTO
 * 
 * @author 阿爪 🦞
 */
public class PredictResponse {
    
    // 输入文本
    private String content;
    
    // ML 判断结果
    private JudgeResult ml;
    
    // LLM 判断结果
    private JudgeResult llm;
    
    // 融合结果
    private FusionResult fusion;
    
    // ===== 内部类 =====
    
    /**
     * 判断结果（ML 或 LLM）
     */
    public static class JudgeResult {
        // 预测结果：AI 或 真人
        private String label;
        
        // 预测值：0=真人, 1=AI
        private Integer prediction;
        
        // AI 概率（0-1）
        private Double aiProbability;
        
        // 真人概率（0-1）
        private Double humanProbability;
        
        // 分数（0-100，越高越像真人）
        private Double score;
        
        // 判断理由
        private String reason;
        
        // 特征值（仅 ML）
        private Map<String, Double> features;
        
        // getter/setter
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public Integer getPrediction() { return prediction; }
        public void setPrediction(Integer prediction) { this.prediction = prediction; }
        
        public Double getAiProbability() { return aiProbability; }
        public void setAiProbability(Double aiProbability) { this.aiProbability = aiProbability; }
        
        public Double getHumanProbability() { return humanProbability; }
        public void setHumanProbability(Double humanProbability) { this.humanProbability = humanProbability; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public Map<String, Double> getFeatures() { return features; }
        public void setFeatures(Map<String, Double> features) { this.features = features; }
    }
    
    /**
     * 融合结果
     */
    public static class FusionResult {
        // 融合后分数（0-100，越高越像真人）
        private Double score;
        
        // 融合后标签
        private String label;
        
        // 融合说明
        private String description;
        
        // getter/setter
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    // ===== getter/setter =====
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public JudgeResult getMl() { return ml; }
    public void setMl(JudgeResult ml) { this.ml = ml; }
    
    public JudgeResult getLlm() { return llm; }
    public void setLlm(JudgeResult llm) { this.llm = llm; }
    
    public FusionResult getFusion() { return fusion; }
    public void setFusion(FusionResult fusion) { this.fusion = fusion; }
}
