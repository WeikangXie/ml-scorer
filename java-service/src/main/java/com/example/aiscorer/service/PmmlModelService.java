package com.example.aiscorer.service;

import com.example.aiscorer.feature.TextFeatureExtractor;
import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.util.*;

/**
 * PMML 模型推理服务
 * 
 * @author 阿爪 🦞
 */
@Service
public class PmmlModelService {

    private Evaluator evaluator;

    /**
     * 服务启动时加载 PMML 模型
     */
    @PostConstruct
    public void init() {
        try {
            // 从 classpath 加载模型文件
            ClassPathResource resource = new ClassPathResource("model.pmml");
            try (InputStream is = resource.getInputStream()) {
                LoadingModelEvaluatorBuilder builder = new LoadingModelEvaluatorBuilder();
                evaluator = builder.load(is).build();
                evaluator.verify();
                System.out.println("✅ PMML 模型加载成功！");
            }
        } catch (Exception e) {
            System.err.println("❌ 加载 PMML 模型失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 服务停止时关闭 evaluator
     */
    @PreDestroy
    public void destroy() {
        if (evaluator != null) {
            evaluator.close();
        }
    }

    /**
     * 预测单条文本
     * 
     * @param text 输入文本
     * @return 预测结果
     */
    public PredictionResult predict(String text) {
        // 1. 提取特征
        Map<String, Double> features = TextFeatureExtractor.extractFeatures(text);
        double[] featureValues = TextFeatureExtractor.getFeatureValues(text);
        List<String> featureNames = TextFeatureExtractor.getFeatureNames();

        // 2. 构建模型输入（按特征名匹配，更安全）
        Map<FieldName, Object> inputMap = new LinkedHashMap<>();
        List<InputField> inputFields = evaluator.getInputFields();

        for (InputField inputField : inputFields) {
            FieldName fieldName = inputField.getName();
            String featureName = fieldName.getValue();
            
            // 查找对应的特征值
            int featureIndex = featureNames.indexOf(featureName);
            if (featureIndex >= 0 && featureIndex < featureValues.length) {
                inputMap.put(fieldName, featureValues[featureIndex]);
            }
        }

        // 3. 执行预测
        Map<FieldName, ?> outputMap = evaluator.evaluate(inputMap);

        // 4. 解析预测结果
        PredictionResult result = new PredictionResult();
        result.setFeatures(features);
        
        // 获取预测标签
        List<TargetField> targetFields = evaluator.getTargetFields();
        TargetField targetField = targetFields.get(0);  // 通常第一个就是预测目标
        FieldName targetFieldName = targetField.getName();
        Object targetValue = outputMap.get(targetFieldName);
        
        if (targetValue instanceof Computable) {
            targetValue = ((Computable) targetValue).getResult();
        }
        
        int prediction = (targetValue instanceof Number) 
            ? ((Number) targetValue).intValue() 
            : Integer.parseInt(targetValue.toString());
        
        result.setPrediction(prediction);
        result.setLabel(prediction == 1 ? "AI" : "真人");

        // 获取概率（如果有）
        for (OutputField outputField : evaluator.getOutputFields()) {
            String fieldName = outputField.getName().getValue();
            Object value = outputMap.get(outputField.getName());
            
            if (value instanceof Computable) {
                value = ((Computable) value).getResult();
            }
            
            if (fieldName.contains("probability") || fieldName.equals("probability(1)")) {
                if (value instanceof Number) {
                    result.setAiProbability(((Number) value).doubleValue());
                }
            }
            if (fieldName.equals("probability(0)")) {
                if (value instanceof Number) {
                    result.setHumanProbability(((Number) value).doubleValue());
                }
            }
        }

        return result;
    }

    /**
     * 预测多条文本（批量）
     * 
     * @param texts 输入文本列表
     * @return 每条文本对应的预测结果列表，顺序与输入一致
     */
    public List<PredictionResult> predictBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<PredictionResult> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            results.add(predict(text));
        }
        return results;
    }

    /**
     * 预测结果类
     */
    public static class PredictionResult {
        private Map<String, Double> features;
        private int prediction;
        private String label;
        private Double aiProbability;
        private Double humanProbability;

        public Map<String, Double> getFeatures() {
            return features;
        }

        public void setFeatures(Map<String, Double> features) {
            this.features = features;
        }

        public int getPrediction() {
            return prediction;
        }

        public void setPrediction(int prediction) {
            this.prediction = prediction;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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
    }
}
