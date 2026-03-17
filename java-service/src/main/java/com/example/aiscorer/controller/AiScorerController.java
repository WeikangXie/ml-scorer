package com.example.aiscorer.controller;

import com.example.aiscorer.dto.PredictRequest;
import com.example.aiscorer.dto.PredictResponse;
import com.example.aiscorer.service.PmmlModelService;
import com.example.aiscorer.service.PmmlModelService.PredictionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 预测接口
     * 
     * POST /api/v1/predict
     * 
     * 请求体：
     * {
     *   "content": "这是要判断的文本内容"
     * }
     * 
     * 响应：
     * {
     *   "content": "这是要判断的文本内容",
     *   "label": "AI",
     *   "prediction": 1,
     *   "aiProbability": 0.85,
     *   "humanProbability": 0.15,
     *   "features": {
     *     "length": 12,
     *     "char_count": 12,
     *     "sentence_count": 1,
     *     ...
     *   }
     * }
     */
    @PostMapping("/predict")
    public PredictResponse predict(@RequestBody PredictRequest request) {
        PredictionResult result = modelService.predict(request.getContent());
        
        PredictResponse response = new PredictResponse();
        response.setContent(request.getContent());
        response.setLabel(result.getLabel());
        response.setPrediction(result.getPrediction());
        response.setAiProbability(result.getAiProbability());
        response.setHumanProbability(result.getHumanProbability());
        response.setFeatures(result.getFeatures());
        
        return response;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
