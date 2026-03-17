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
    
    // AI 概率
    private Double aiProbability;
    
    // 真人概率
    private Double humanProbability;
    
    // 各特征值
    private Map<String, Double> features;

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
}
