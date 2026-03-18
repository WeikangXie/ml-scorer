package com.example.aiscorer.dto;

import java.util.List;

/**
 * LLM 响应 DTO（OpenAI 兼容格式）
 * 
 * @author 阿爪 🦞
 */
public class LlmResponse {
    
    private String id;
    private String object;
    private List<Choice> choices;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    
    /**
     * 获取完整响应文本
     */
    public String getFullContent() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Choice choice : choices) {
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                sb.append(choice.getMessage().getContent());
            }
        }
        return sb.toString();
    }
    
    /**
     * 选择项
     */
    public static class Choice {
        private int index;
        private Message message;
        private Delta delta;
        private String finishReason;
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public Message getMessage() {
            return message;
        }
        
        public void setMessage(Message message) {
            this.message = message;
        }
        
        public Delta getDelta() {
            return delta;
        }
        
        public void setDelta(Delta delta) {
            this.delta = delta;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
        
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }
    
    /**
     * 消息（非流式）
     */
    public static class Message {
        private String role;
        private String content;
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * 增量消息（流式）
     */
    public static class Delta {
        private String role;
        private String content;
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}
