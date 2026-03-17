"""
随机森林模型
"""

import pickle
import os
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix

from ..config import RANDOM_FOREST_PARAMS, MODELS_DIR
from ..features import build_feature_matrix


class RandomForestScorer:
    """随机森林评分器"""
    
    def __init__(self, model_path=None):
        """
        初始化评分器
        
        参数：
            model_path: 模型文件路径（可选，用于加载已有模型）
        """
        self.model = None
        self.feature_names = None
        self.available_features = None
        
        if model_path:
            self.load(model_path)
    
    def train(self, df, test_size=0.2):
        """
        训练模型
        
        参数：
            df: 训练数据 DataFrame，必须包含 commentContent 和 is_ai 列
            test_size: 测试集比例
        
        返回：
            dict: 训练结果，包含评估指标
        """
        # 提取特征
        print("提取特征...")
        X = build_feature_matrix(df)
        y = df['is_ai'].values
        self.feature_names = list(X.columns)
        
        # 划分数据集
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=test_size, random_state=42, stratify=y
        )
        
        # 训练模型
        print("训练模型...")
        self.model = RandomForestClassifier(**RANDOM_FOREST_PARAMS)
        self.model.fit(X_train, y_train)
        
        # 评估
        y_pred = self.model.predict(X_test)
        report = classification_report(y_test, y_pred, target_names=['真人', 'AI'])
        cm = confusion_matrix(y_test, y_pred)
        
        # 特征重要性
        importance = pd.DataFrame({
            'feature': self.feature_names,
            'importance': self.model.feature_importances_
        }).sort_values('importance', ascending=False)
        
        # 记录可用特征
        has_original = 'originalContent' in df.columns
        has_audit = 'auditStatus' in df.columns
        self.available_features = {
            'text_features': True,
            'relevance_features': has_original,
            'audit_features': has_audit
        }
        
        return {
            'report': report,
            'confusion_matrix': cm,
            'feature_importance': importance,
            'train_size': len(X_train),
            'test_size': len(X_test)
        }
    
    def predict(self, text, return_proba=True):
        """
        预测单条文本
        
        参数：
            text: 输入文本
            return_proba: 是否返回概率
        
        返回：
            dict: 预测结果
        """
        if self.model is None:
            raise ValueError("模型未加载，请先调用 load() 或 train()")
        
        from ..features import extract_text_features
        
        features = extract_text_features(text)
        X = pd.DataFrame([features])
        
        # 填充缺失特征
        for fname in self.feature_names:
            if fname not in X.columns:
                X[fname] = 0
        X = X[self.feature_names]
        
        prediction = self.model.predict(X)[0]
        
        result = {
            'text': text[:50] + '...' if len(text) > 50 else text,
            'is_ai': bool(prediction),
            'label': 'AI' if prediction == 1 else '真人'
        }
        
        if return_proba:
            proba = self.model.predict_proba(X)[0]
            result['probability'] = {
                '真人': float(proba[0]),
                'AI': float(proba[1])
            }
            result['confidence'] = float(max(proba))
        
        return result
    
    def save(self, path=None):
        """
        保存模型
        
        参数：
            path: 保存路径（默认 saved_models/ai_scorer.pkl）
        """
        if path is None:
            path = os.path.join(MODELS_DIR, "ai_scorer.pkl")
        
        os.makedirs(os.path.dirname(path), exist_ok=True)
        
        with open(path, 'wb') as f:
            pickle.dump({
                'model': self.model,
                'feature_names': self.feature_names,
                'available_features': self.available_features
            }, f)
        
        print(f"模型已保存到: {path}")
    
    def load(self, path=None):
        """
        加载模型
        
        参数：
            path: 模型文件路径（默认 saved_models/ai_scorer.pkl）
        """
        if path is None:
            path = os.path.join(MODELS_DIR, "ai_scorer.pkl")
        
        with open(path, 'rb') as f:
            data = pickle.load(f)
        
        self.model = data['model']
        self.feature_names = data['feature_names']
        self.available_features = data.get('available_features', {})
        
        print(f"模型已加载: {path}")
