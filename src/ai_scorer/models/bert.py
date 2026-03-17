"""
BERT 模型
"""

import os
import torch
from torch.utils.data import Dataset, DataLoader
from transformers import BertTokenizer, BertForSequenceClassification, AdamW
from tqdm import tqdm
import pandas as pd
import numpy as np

from ..config import BERT_PARAMS, MODELS_DIR


class TextDataset(Dataset):
    """文本数据集"""
    
    def __init__(self, texts, labels, tokenizer, max_length):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length
    
    def __len__(self):
        return len(self.texts)
    
    def __getitem__(self, idx):
        text = str(self.texts[idx])
        label = self.labels[idx]
        
        encoding = self.tokenizer(
            text,
            max_length=self.max_length,
            padding='max_length',
            truncation=True,
            return_tensors='pt'
        )
        
        return {
            'input_ids': encoding['input_ids'].flatten(),
            'attention_mask': encoding['attention_mask'].flatten(),
            'labels': torch.tensor(label, dtype=torch.long)
        }


class BertScorer:
    """BERT 评分器"""
    
    def __init__(self, model_path=None):
        """
        初始化评分器
        
        参数：
            model_path: 模型目录路径（可选）
        """
        self.model = None
        self.tokenizer = None
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        
        if model_path:
            self.load(model_path)
    
    def train(self, df, epochs=None, batch_size=None, learning_rate=None):
        """
        训练模型
        
        参数：
            df: 训练数据 DataFrame，必须包含 commentContent 和 is_ai 列
            epochs: 训练轮数
            batch_size: 批次大小
            learning_rate: 学习率
        
        返回：
            dict: 训练结果
        """
        epochs = epochs or BERT_PARAMS['epochs']
        batch_size = batch_size or BERT_PARAMS['batch_size']
        learning_rate = learning_rate or BERT_PARAMS['learning_rate']
        
        # 加载预训练模型
        print("加载预训练模型...")
        self.tokenizer = BertTokenizer.from_pretrained(BERT_PARAMS['model_name'])
        self.model = BertForSequenceClassification.from_pretrained(
            BERT_PARAMS['model_name'],
            num_labels=2
        )
        self.model.to(self.device)
        
        # 准备数据
        texts = df['commentContent'].tolist()
        labels = df['is_ai'].tolist()
        
        dataset = TextDataset(
            texts, labels, 
            self.tokenizer, 
            BERT_PARAMS['max_length']
        )
        
        dataloader = DataLoader(dataset, batch_size=batch_size, shuffle=True)
        
        # 优化器
        optimizer = AdamW(self.model.parameters(), lr=learning_rate)
        
        # 训练
        print(f"开始训练 (epochs={epochs}, device={self.device})...")
        self.model.train()
        
        for epoch in range(epochs):
            total_loss = 0
            for batch in tqdm(dataloader, desc=f"Epoch {epoch+1}/{epochs}"):
                optimizer.zero_grad()
                
                input_ids = batch['input_ids'].to(self.device)
                attention_mask = batch['attention_mask'].to(self.device)
                labels = batch['labels'].to(self.device)
                
                outputs = self.model(
                    input_ids=input_ids,
                    attention_mask=attention_mask,
                    labels=labels
                )
                
                loss = outputs.loss
                total_loss += loss.item()
                
                loss.backward()
                optimizer.step()
            
            avg_loss = total_loss / len(dataloader)
            print(f"Epoch {epoch+1} - Average Loss: {avg_loss:.4f}")
        
        return {'final_loss': avg_loss}
    
    def predict(self, text):
        """
        预测单条文本
        
        参数：
            text: 输入文本
        
        返回：
            dict: 预测结果
        """
        if self.model is None:
            raise ValueError("模型未加载，请先调用 load() 或 train()")
        
        self.model.eval()
        
        encoding = self.tokenizer(
            text,
            max_length=BERT_PARAMS['max_length'],
            padding='max_length',
            truncation=True,
            return_tensors='pt'
        )
        
        input_ids = encoding['input_ids'].to(self.device)
        attention_mask = encoding['attention_mask'].to(self.device)
        
        with torch.no_grad():
            outputs = self.model(
                input_ids=input_ids,
                attention_mask=attention_mask
            )
            logits = outputs.logits
            proba = torch.softmax(logits, dim=1)[0]
            prediction = torch.argmax(proba).item()
        
        return {
            'text': text[:50] + '...' if len(text) > 50 else text,
            'is_ai': bool(prediction),
            'label': 'AI' if prediction == 1 else '真人',
            'probability': {
                '真人': float(proba[0]),
                'AI': float(proba[1])
            },
            'confidence': float(max(proba))
        }
    
    def save(self, path=None):
        """
        保存模型
        
        参数：
            path: 保存目录（默认 saved_models/bert_scorer）
        """
        if path is None:
            path = os.path.join(MODELS_DIR, "bert_scorer")
        
        os.makedirs(path, exist_ok=True)
        
        self.model.save_pretrained(path)
        self.tokenizer.save_pretrained(path)
        
        print(f"模型已保存到: {path}")
    
    def load(self, path=None):
        """
        加载模型
        
        参数：
            path: 模型目录路径
        """
        if path is None:
            path = os.path.join(MODELS_DIR, "bert_scorer")
        
        self.tokenizer = BertTokenizer.from_pretrained(path)
        self.model = BertForSequenceClassification.from_pretrained(path)
        self.model.to(self.device)
        
        print(f"模型已加载: {path}")
