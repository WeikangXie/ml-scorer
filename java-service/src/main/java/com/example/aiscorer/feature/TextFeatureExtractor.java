package com.example.aiscorer.feature;

import java.util.*;
import java.util.regex.*;

/**
 * 文本特征提取器
 * 
 * 与 Python 训练脚本的特征提取逻辑保持一致
 * 
 * @author 阿爪 🦞
 */
public class TextFeatureExtractor {

    // AI 模板词库
    private static final Set<String> AI_TEMPLATE_WORDS = new HashSet<>(Arrays.asList(
        "一起加油", "感谢您的分享", "感谢分享", "值得", "让我们",
        "更好地", "更加", "确实", "希望", "期待", "继续",
        "投资路上", "投资的道路", "成为更", "理性", "乐观",
        "风险管理", "长期投资", "保持", "见证", "奇迹"
    ));

    // 口语词库
    private static final Set<String> COLLOQUIAL_WORDS = new HashSet<>(Arrays.asList(
        "哈哈", "呵呵", "嗯", "啊", "吧", "嘛", "诶",
        "牛逼", "牛", "服了", "醉了", "无语", "草", "靠",
        "卧槽", "吊", "666"
    ));

    // Emoji 正则表达式
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
        "[\\x{10000}-\\x{10FFFF}]|[\\x{FE00}-\\x{FE0F}]"
    );

    // 句子分割正则
    private static final Pattern SENTENCE_SPLITTER = Pattern.compile("[。！？\\n]");
    
    // 第一人称正则
    private static final Pattern FIRST_PERSON_PATTERN = Pattern.compile("[我我们]");

    /**
     * 从文本中提取所有特征
     * 
     * @param text 输入文本
     * @return 特征名称到特征值的映射
     */
    public static Map<String, Double> extractFeatures(String text) {
        Map<String, Double> features = new LinkedHashMap<>();
        
        if (text == null) {
            text = "";
        }

        // ========== 基础统计特征 ==========
        int length = text.length();
        int charCount = text.replace(" ", "").length();
        
        features.put("length", (double) length);
        features.put("char_count", (double) charCount);

        // ========== 句子相关特征 ==========
        String[] sentences = SENTENCE_SPLITTER.split(text);
        List<String> validSentences = new ArrayList<>();
        for (String s : sentences) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                validSentences.add(trimmed);
            }
        }
        
        int sentenceCount = validSentences.size();
        features.put("sentence_count", (double) sentenceCount);
        
        double avgSentenceLength = 0;
        if (!validSentences.isEmpty()) {
            int totalLen = 0;
            for (String s : validSentences) {
                totalLen += s.length();
            }
            avgSentenceLength = (double) totalLen / validSentences.size();
        }
        features.put("avg_sentence_length", avgSentenceLength);

        // ========== 标点符号特征 ==========
        int exclamationCount = countChar(text, '！') + countChar(text, '!');
        int questionCount = countChar(text, '？') + countChar(text, '?');
        int tildeCount = countChar(text, '～') + countChar(text, '~');
        int periodCount = countChar(text, '。');
        
        features.put("exclamation_count", (double) exclamationCount);
        features.put("question_count", (double) questionCount);
        features.put("tilde_count", (double) tildeCount);
        features.put("period_count", (double) periodCount);
        
        int totalPunct = exclamationCount + questionCount + periodCount;
        double punctDensity = charCount > 0 ? (double) totalPunct / charCount : 0;
        features.put("punct_density", punctDensity);

        // ========== Emoji 特征 ==========
        Matcher emojiMatcher = EMOJI_PATTERN.matcher(text);
        int emojiCount = 0;
        while (emojiMatcher.find()) {
            emojiCount++;
        }
        
        features.put("emoji_count", (double) emojiCount);
        double emojiDensity = charCount > 0 ? (double) emojiCount / charCount : 0;
        features.put("emoji_density", emojiDensity);

        // ========== AI 模板词特征 ==========
        int aiTemplateCount = 0;
        for (String word : AI_TEMPLATE_WORDS) {
            if (text.contains(word)) {
                aiTemplateCount++;
            }
        }
        features.put("ai_template_count", (double) aiTemplateCount);

        // ========== 口语词特征 ==========
        int colloquialCount = 0;
        for (String word : COLLOQUIAL_WORDS) {
            if (text.contains(word)) {
                colloquialCount++;
            }
        }
        features.put("colloquial_count", (double) colloquialCount);

        // ========== 结构特征 ==========
        int endsWithPeriod = (text.endsWith("。") || text.endsWith(".")) ? 1 : 0;
        int hasCompleteStructure = sentenceCount >= 2 ? 1 : 0;
        
        features.put("ends_with_period", (double) endsWithPeriod);
        features.put("has_complete_structure", (double) hasCompleteStructure);

        // ========== 人称特征 ==========
        Matcher firstPersonMatcher = FIRST_PERSON_PATTERN.matcher(text);
        int firstPersonCount = 0;
        while (firstPersonMatcher.find()) {
            firstPersonCount++;
        }
        features.put("first_person_count", (double) firstPersonCount);
        
        int ninCount = countChar(text, '您');
        int niCount = countChar(text, '你');
        features.put("nin_count", (double) ninCount);
        features.put("ni_count", (double) niCount);

        return features;
    }

    /**
     * 获取特征名称列表（按顺序）
     * 用于构建模型输入向量
     */
    public static List<String> getFeatureNames() {
        // 按照训练时的特征顺序
        return Arrays.asList(
            "length",
            "char_count",
            "sentence_count",
            "avg_sentence_length",
            "exclamation_count",
            "question_count",
            "tilde_count",
            "period_count",
            "punct_density",
            "emoji_count",
            "emoji_density",
            "ai_template_count",
            "colloquial_count",
            "ends_with_period",
            "has_complete_structure",
            "first_person_count",
            "nin_count",
            "ni_count"
        );
    }

    /**
     * 获取特征值数组（按顺序）
     * 用于 PMML 模型输入
     */
    public static double[] getFeatureValues(String text) {
        Map<String, Double> features = extractFeatures(text);
        List<String> featureNames = getFeatureNames();
        
        double[] values = new double[featureNames.size()];
        for (int i = 0; i < featureNames.size(); i++) {
            String name = featureNames.get(i);
            values[i] = features.getOrDefault(name, 0.0);
        }
        return values;
    }

    // 辅助方法：统计字符出现次数
    private static int countChar(String text, char c) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}
