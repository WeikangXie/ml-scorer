"""
特征提取模块
"""

from .text_features import extract_text_features, extract_relevance_features, build_feature_matrix

__all__ = [
    "extract_text_features",
    "extract_relevance_features", 
    "build_feature_matrix"
]
