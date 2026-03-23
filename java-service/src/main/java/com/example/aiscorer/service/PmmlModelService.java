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

    /** 模型输入字段（缓存，避免每次预测重复查询） */
    private List<InputField> cachedInputFields;
    /** 模型目标字段（缓存） */
    private List<TargetField> cachedTargetFields;
    /** 模型输出字段（缓存） */
    private List<OutputField> cachedOutputFields;
    /** 特征名列表（缓存） */
    private List<String> cachedFeatureNames;

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

            // 缓存模型字段信息，避免每次预测重复查询
            cachedInputFields = evaluator.getInputFields();
            cachedTargetFields = evaluator.getTargetFields();
            cachedOutputFields = evaluator.getOutputFields();
            cachedFeatureNames = TextFeatureExtractor.getFeatureNames();

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
        return doPredict(text, cachedFeatureNames, cachedInputFields,
                cachedTargetFields, cachedOutputFields);
    }

    /**
     * 预测多条文本（批量），内部只查询一次模型字段，避免重复计算
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
            results.add(doPredict(text, cachedFeatureNames, cachedInputFields,
                    cachedTargetFields, cachedOutputFields));
        }
        return results;
    }

    /**
     * 实际执行预测的内部方法，模型字段由调用方传入，避免重复查询
     */
    private PredictionResult doPredict(String text, List<String> featureNames,
            List<InputField> inputFields, List<TargetField> targetFields,
            List<OutputField> outputFields) {

        // 1. 提取特征
        Map<String, Double> features = TextFeatureExtractor.extractFeatures(text);
        double[] featureValues = TextFeatureExtractor.getFeatureValues(text);

        // 2. 构建模型输入（按特征名匹配）
        Map<FieldName, Object> inputMap = new LinkedHashMap<>();
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
        TargetField targetField = targetFields.get(0);
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
        for (OutputField outputField : outputFields) {
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
