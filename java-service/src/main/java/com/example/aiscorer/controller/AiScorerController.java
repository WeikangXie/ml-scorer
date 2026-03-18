package com.example.aiscorer.controller;

import com.example.aiscorer.dto.PredictRequest;
import com.example.aiscorer.dto.PredictResponse;
import com.example.aiscorer.dto.PredictResponse.FusionResult;
import com.example.aiscorer.dto.PredictResponse.JudgeResult;
import com.example.aiscorer.service.FusionService;
import com.example.aiscorer.service.LlmService;
import com.example.aiscorer.service.LlmService.LlmJudgeResult;
import com.example.aiscorer.service.PmmlModelService;
import com.example.aiscorer.service.PmmlModelService.PredictionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 评分器 HTTP 接口
 * 
 * @author 阿爪 🦞
 */
@RestController
@RequestMapping("/api/v1")
public class AiScorerController {

    @Autowired
    private PmmlModelService modelService;
    
    @Autowired
    private LlmService llmService;
    
    @Autowired
    private FusionService fusionService;

    /**
     * 单条预测（仅 ML）
     * 
     * POST /api/v1/predict
     */
    @PostMapping("/predict")
    public PredictResponse predict(@RequestBody PredictRequest request) {
        PredictionResult result = modelService.predict(request.getContent());
        
        PredictResponse response = new PredictResponse();
        response.setContent(request.getContent());
        
        // ML 结果
        JudgeResult ml = new JudgeResult();
        ml.setLabel(result.getLabel());
        ml.setPrediction(result.getPrediction());
        ml.setAiProbability(result.getAiProbability());
        ml.setHumanProbability(result.getHumanProbability());
        ml.setScore(result.getHumanProbability() != null 
            ? result.getHumanProbability() * 100 : 50.0);
        ml.setFeatures(result.getFeatures());
        response.setMl(ml);
        
        // 融合结果（仅 ML）
        FusionResult fusion = new FusionResult();
        fusion.setScore(ml.getScore());
        fusion.setLabel(ml.getLabel());
        fusion.setDescription("仅 ML 判断");
        response.setFusion(fusion);
        
        return response;
    }
    
    /**
     * 批量预测（ML + LLM 融合）
     * 
     * POST /api/v1/predict/batch
     * 
     * 请求体：
     * {
     *   "contents": ["文本1", "文本2", "文本3"]
     * }
     */
    @PostMapping("/predict/batch")
    public List<PredictResponse> predictBatch(@RequestBody BatchPredictRequest request) {
        List<String> contents = request.getContents();
        List<PredictResponse> responses = new ArrayList<>();
        
        if (contents == null || contents.isEmpty()) {
            return responses;
        }
        
        // 1. ML 批量预测
        for (String content : contents) {
            PredictionResult mlResult = modelService.predict(content);
            
            PredictResponse response = new PredictResponse();
            response.setContent(content);
            
            JudgeResult ml = new JudgeResult();
            ml.setLabel(mlResult.getLabel());
            ml.setPrediction(mlResult.getPrediction());
            ml.setAiProbability(mlResult.getAiProbability());
            ml.setHumanProbability(mlResult.getHumanProbability());
            ml.setScore(mlResult.getHumanProbability() != null 
                ? mlResult.getHumanProbability() * 100 : 50.0);
            ml.setFeatures(mlResult.getFeatures());
            response.setMl(ml);
            
            responses.add(response);
        }
        
        // 2. LLM 批量判断
        List<LlmJudgeResult> llmResults = llmService.batchJudge(contents);
        
        // 3. 融合结果
        for (int i = 0; i < responses.size(); i++) {
            PredictResponse response = responses.get(i);
            LlmJudgeResult llmResult = i < llmResults.size() ? llmResults.get(i) : null;
            fusionService.fuse(response, llmResult);
        }
        
        return responses;
    }
    
    /**
     * 单条预测（ML + LLM 融合）
     * 
     * POST /api/v1/predict/fusion
     */
    @PostMapping("/predict/fusion")
    public PredictResponse predictWithFusion(@RequestBody PredictRequest request) {
        // 1. ML 预测
        PredictionResult mlResult = modelService.predict(request.getContent());
        
        PredictResponse response = new PredictResponse();
        response.setContent(request.getContent());
        
        JudgeResult ml = new JudgeResult();
        ml.setLabel(mlResult.getLabel());
        ml.setPrediction(mlResult.getPrediction());
        ml.setAiProbability(mlResult.getAiProbability());
        ml.setHumanProbability(mlResult.getHumanProbability());
        ml.setScore(mlResult.getHumanProbability() != null 
            ? mlResult.getHumanProbability() * 100 : 50.0);
        ml.setFeatures(mlResult.getFeatures());
        response.setMl(ml);
        
        // 2. LLM 判断
        List<LlmJudgeResult> llmResults = llmService.batchJudge(
            java.util.Collections.singletonList(request.getContent())
        );
        LlmJudgeResult llmResult = llmResults.isEmpty() ? null : llmResults.get(0);
        
        // 3. 融合
        fusionService.fuse(response, llmResult);
        
        return response;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    /**
     * 批量预测请求 DTO
     */
    public static class BatchPredictRequest {
        private List<String> contents;

        public List<String> getContents() {
            return contents;
        }

        public void setContents(List<String> contents) {
            this.contents = contents;
        }
    }
}
