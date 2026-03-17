"""
文本特征提取

与 Java 端 TextFeatureExtractor.java 保持一致
"""

import re
import numpy as np
import pandas as pd
from ..config import AI_TEMPLATE_WORDS, COLLOQUIAL_WORDS


def extract_text_features(text):
    """
    从文本中提取特征（针对单条回复内容）
    
    参数：
        text: 回复文本
    
    返回：
        dict: 特征字典
    """
    features = {}
    
    # 确保文本是字符串
    text = str(text) if not pd.isna(text) else ''
    
    # --- 基础统计特征 ---
    features['length'] = len(text)
    features['char_count'] = len(text.replace(' ', ''))
    
    # 句子相关
    sentences = re.split(r'[。！？\n]', text)
    sentences = [s.strip() for s in sentences if s.strip()]
    features['sentence_count'] = len(sentences)
    if sentences:
        features['avg_sentence_length'] = np.mean([len(s) for s in sentences])
    else:
        features['avg_sentence_length'] = 0
    
    # --- 标点符号特征 ---
    features['exclamation_count'] = text.count('！') + text.count('!')
    features['question_count'] = text.count('？') + text.count('?')
    features['tilde_count'] = text.count('～') + text.count('~')
    features['period_count'] = text.count('。')
    
    total_punct = features['exclamation_count'] + features['question_count'] + features['period_count']
    if features['char_count'] > 0:
        features['punct_density'] = total_punct / features['char_count']
    else:
        features['punct_density'] = 0
    
    # --- Emoji 特征 ---
    emoji_pattern = re.compile(r'[\U00010000-\U0010ffff]|[\ufe00-\ufe0f]')
    emojis = emoji_pattern.findall(text)
    features['emoji_count'] = len(emojis)
    if features['char_count'] > 0:
        features['emoji_density'] = len(emojis) / features['char_count']
    else:
        features['emoji_density'] = 0
    
    # --- AI模板词特征 ---
    ai_word_count = 0
    for word in AI_TEMPLATE_WORDS:
        if word in text:
            ai_word_count += 1
    features['ai_template_count'] = ai_word_count
    
    # --- 口语词特征 ---
    colloquial_count = 0
    for word in COLLOQUIAL_WORDS:
        if word in text:
            colloquial_count += 1
    features['colloquial_count'] = colloquial_count
    
    # --- 结构特征 ---
    features['ends_with_period'] = 1 if text.endswith('。') or text.endswith('.') else 0
    features['has_complete_structure'] = 1 if features['sentence_count'] >= 2 else 0
    
    # 第一人称
    features['first_person_count'] = len(re.findall(r'[我我们]', text))
    
    # 您 vs 你
    features['nin_count'] = text.count('您')
    features['ni_count'] = text.count('你')
    
    return features


def extract_relevance_features(reply_text, original_text):
    """
    提取回复与原帖的关联性特征
    
    参数：
        reply_text: 回复内容
        original_text: 被回复的原帖内容
    
    返回：
        dict: 关联性特征字典
    """
    features = {}
    
    reply_text = str(reply_text) if not pd.isna(reply_text) else ''
    original_text = str(original_text) if not pd.isna(original_text) else ''
    
    # 1. 关键词重合度
    reply_words = set(reply_text)
    original_words = set(original_text)
    
    if len(original_words) > 0:
        overlap_ratio = len(reply_words & original_words) / len(original_words)
    else:
        overlap_ratio = 0
    features['keyword_overlap_ratio'] = overlap_ratio
    
    # 2. 回复长度 / 原帖长度 比值
    if len(original_text) > 0:
        length_ratio = len(reply_text) / len(original_text)
    else:
        length_ratio = 0
    features['reply_to_original_length_ratio'] = length_ratio
    
    # 3. 是否引用原帖内容
    has_quote = 0
    for i in range(len(original_text) - 4):
        if original_text[i:i+5] in reply_text:
            has_quote = 1
            break
    features['has_quote'] = has_quote
    
    return features


def extract_audit_features(audit_status):
    """
    提取审核结果相关特征
    
    参数：
        audit_status: 审核状态
    
    返回：
        dict: 审核特征字典
    """
    features = {}
    
    if pd.isna(audit_status):
        features['audit_passed'] = 0
        features['audit_rejected'] = 0
        features['audit_pending'] = 0
        features['audit_unknown'] = 1
    else:
        status = str(audit_status).upper()
        features['audit_passed'] = 1 if status in ['PASSED', 'APPROVED', '通过'] else 0
        features['audit_rejected'] = 1 if status in ['REJECTED', 'FAILED', '拒绝', '不通过'] else 0
        features['audit_pending'] = 1 if status in ['PENDING', '审核中'] else 0
        features['audit_unknown'] = 0
    
    return features


def build_feature_matrix(df):
    """
    批量提取特征，返回特征矩阵（DataFrame）
    
    支持的字段：
    - commentContent: 回复内容（必需）
    - originalContent: 原帖内容（可选）
    - auditStatus: 审核状态（可选）
    
    参数：
        df: 包含文本数据的 DataFrame
    
    返回：
        DataFrame: 特征矩阵
    """
    feature_list = []
    
    # 检测可用的列
    has_original = 'originalContent' in df.columns
    has_audit = 'auditStatus' in df.columns
    
    for idx, row in df.iterrows():
        features = {}
        
        # 1. 回复文本特征（必需）
        text_features = extract_text_features(row.get('commentContent', ''))
        features.update(text_features)
        
        # 2. 关联性特征（如果有原帖内容）
        if has_original:
            relevance_features = extract_relevance_features(
                row.get('commentContent', ''),
                row.get('originalContent', '')
            )
            features.update(relevance_features)
        
        # 3. 审核特征（如果有审核状态）
        if has_audit:
            audit_features = extract_audit_features(row.get('auditStatus'))
            features.update(audit_features)
        
        feature_list.append(features)
    
    return pd.DataFrame(feature_list)


def get_feature_names():
    """
    获取特征名称列表（按固定顺序）
    
    返回：
        list: 特征名称列表
    """
    return [
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
    ]
