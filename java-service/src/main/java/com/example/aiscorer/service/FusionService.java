package com.example.aiscorer.service;

import com.example.aiscorer.dto.PredictResponse;
import com.example.aiscorer.service.LlmService.LlmJudgeResult;
import org.springframework.stereotype.Service;

/**
 * 结果融合服务
 * 
 * 将 ML 和 LLM 的判断结果进行融合
 * 
 * @author 阿爪 🦞
 */
@Service
public class FusionService {
    
    /**
     * ML 结果权重（0-1）
     * LLM 权重 = 1 - mlWeight
     */
    private double mlWeight = 0.6;
    
    /**
     * 融合 ML 和 LLM 的结果
     * 
     * @param mlResult ML 预测结果
     * @param llmResult LLM 判断结果
     * @return 融合后的响应
     */
    public PredictResponse fuse(PredictResponse mlResult, LlmJudgeResult llmResult) {
        if (llmResult == null) {
            // 没有 LLM 结果，直接用 ML 结果
            mlResult.setFinalScore(mlResult.getAiProbability() != null 
                ? mlResult.getAiProbability() * 100 : 50.0);
            mlResult.setFinalLabel(mlResult.getLabel());
            return mlResult;
        }
        
        // 计算 AI 概率（0-100 分制，越高越像真人）
        double mlScore = mlResult.getHumanProbability() != null 
            ? mlResult.getHumanProbability() * 100 : 50.0;
        double llmScore = llmResult.getScore();
        
        // 加权融合
        double finalScore = mlScore * mlWeight + llmScore * (1 - mlWeight);
        
        // 设置融合结果
        mlResult.setLlmScore(llmScore);
        mlResult.setLlmIsAi(llmResult.isAi());
        mlResult.setLlmReason(llmResult.getReason());
        mlResult.setFinalScore(finalScore);
        mlResult.setFinalLabel(finalScore >= 50 ? "真人" : "AI");
        
        return mlResult;
    }
    
    /**
     * 简单权重融合
     * 
     * @param mlProb ML 模型输出的 AI 概率（0-1）
     * @param llmScore LLM 输出的分数（0-100，越高越像真人）
     * @return 融合后的 AI 概率（0-1）
     */
    public double fuseProbability(double mlProb, double llmScore) {
        // 将 LLM 分数转换为 AI 概率（分数越高越像真人 → AI 概率越低）
        double llmProb = (100 - llmScore) / 100.0;
        
        // 加权融合
        return mlProb * mlWeight + llmProb * (1 - mlWeight);
    }
    
    public double getMlWeight() {
        return mlWeight;
    }
    
    public void setMlWeight(double mlWeight) {
        this.mlWeight = mlWeight;
    }
}
