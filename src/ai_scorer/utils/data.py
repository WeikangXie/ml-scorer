"""
数据处理工具
"""

import pandas as pd
from ..config import AI_EXTEND_TYPES


def convert_extend_type_to_label(extend_type):
    """
    将 extendType 转换为 is_ai 标签
    
    AI_CHECK_IN, AI_OPINION_SHARE, AI_PRODUCT_RULE → 1 (AI)
    其他值（包括 null）→ 0 (真人)
    """
    if pd.isna(extend_type):
        return 0  # null 为真人
    
    extend_type_str = str(extend_type).strip()
    
    if extend_type_str in AI_EXTEND_TYPES:
        return 1  # AI
    else:
        return 0  # 其他值为真人


def load_data(file_path, label_column='extendType'):
    """
    加载训练数据
    
    参数：
        file_path: 数据文件路径
        label_column: 标签列名
    
    返回：
        DataFrame: 处理后的数据
    """
    df = pd.read_csv(file_path)
    
    # 检查必需列
    if 'commentContent' not in df.columns:
        raise ValueError("数据文件缺少 commentContent 列")
    
    # 转换标签
    if label_column in df.columns:
        df['is_ai'] = df[label_column].apply(convert_extend_type_to_label)
    
    return df


def print_data_stats(df):
    """
    打印数据统计信息
    """
    print(f"数据总量: {len(df)} 条")
    
    if 'is_ai' in df.columns:
        label_counts = df['is_ai'].value_counts()
        print("标签分布:")
        for label, count in label_counts.items():
            label_name = "AI" if label == 1 else "真人"
            print(f"  {label_name}（{label}）：{count} 条")
    
    # 检查可选字段
    has_original = 'originalContent' in df.columns
    has_audit = 'auditStatus' in df.columns
    print(f"\n可选字段:")
    print(f"  originalContent: {'✅' if has_original else '❌'}")
    print(f"  auditStatus: {'✅' if has_audit else '❌'}")
