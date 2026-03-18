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
    
    // 预测结果：AI 或 真人
    private String label;
    
    // 预测值：0=真人, 1=AI
    private int prediction;
    
    // ML 模型输出的 AI 概率
    private Double aiProbability;
    
    // ML 模型输出的真人概率
    private Double humanProbability;
    
    // 各特征值
    private Map<String, Double> features;
    
    // ===== LLM 相关字段 =====
    
    // LLM 判断分数（0-100）
    private Double llmScore;
    
    // LLM 判断是否为 AI
    private Boolean llmIsAi;
    
    // LLM 判断理由
    private String llmReason;
    
    // ===== 融合结果 =====
    
    // 融合后的最终分数（0-100）
    private Double finalScore;
    
    // 融合后的最终判断
    private String finalLabel;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public Double getAiProbability() {
        return aiProbability;
    }

    public void setAiProbability(Double aiProbability) {
        this.aiProbability = aiProbability;
    }

    public Double getHumanProbability() {
        return humanProbability;
    }

    public void setHumanProbability(Double humanProbability) {
        this.humanProbability = humanProbability;
    }

    public Map<String, Double> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Double> features) {
        this.features = features;
    }

    public Double getLlmScore() {
        return llmScore;
    }

    public void setLlmScore(Double llmScore) {
        this.llmScore = llmScore;
    }

    public Boolean getLlmIsAi() {
        return llmIsAi;
    }

    public void setLlmIsAi(Boolean llmIsAi) {
        this.llmIsAi = llmIsAi;
    }

    public String getLlmReason() {
        return llmReason;
    }

    public void setLlmReason(String llmReason) {
        this.llmReason = llmReason;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public String getFinalLabel() {
        return finalLabel;
    }

    public void setFinalLabel(String finalLabel) {
        this.finalLabel = finalLabel;
    }
}
