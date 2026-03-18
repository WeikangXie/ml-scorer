package com.example.aiscorer.service;

import com.example.aiscorer.dto.PredictResponse;
import com.example.aiscorer.dto.PredictResponse.FusionResult;
import com.example.aiscorer.dto.PredictResponse.JudgeResult;
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
     * @param response 响应对象（已包含 ML 结果）
     * @param llmResult LLM 判断结果
     */
    public void fuse(PredictResponse response, LlmJudgeResult llmResult) {
        FusionResult fusion = new FusionResult();
        
        if (llmResult == null) {
            // 没有 LLM 结果，直接用 ML 结果
            JudgeResult ml = response.getMl();
            fusion.setScore(ml.getHumanProbability() != null 
                ? ml.getHumanProbability() * 100 : 50.0);
            fusion.setLabel(ml.getLabel());
            fusion.setDescription("仅 ML 判断");
            response.setFusion(fusion);
            return;
        }
        
        // 构建 LLM 结果
        JudgeResult llm = new JudgeResult();
        llm.setScore(llmResult.getScore());
        llm.setAiProbability(llmResult.getAiProbability());
        llm.setHumanProbability(1 - llmResult.getAiProbability());
        llm.setLabel(llmResult.isAi() ? "AI" : "真人");
        llm.setReason(llmResult.getReason());
        response.setLlm(llm);
        
        // 计算 ML 分数
        JudgeResult ml = response.getMl();
        double mlScore = ml.getHumanProbability() != null 
            ? ml.getHumanProbability() * 100 : 50.0;
        double llmScore = llmResult.getScore();
        
        // 加权融合
        double finalScore = mlScore * mlWeight + llmScore * (1 - mlWeight);
        
        fusion.setScore(finalScore);
        fusion.setLabel(finalScore >= 50 ? "真人" : "AI");
        fusion.setDescription(String.format("ML权重=%.0f%%, LLM权重=%.0f%%", 
            mlWeight * 100, (1 - mlWeight) * 100));
        
        response.setFusion(fusion);
    }
    
    public double getMlWeight() {
        return mlWeight;
    }
    
    public void setMlWeight(double mlWeight) {
        this.mlWeight = mlWeight;
    }
}
