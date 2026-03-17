#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
训练 BERT 模型

使用方法：
    python scripts/train_bert.py [data_file]
    python scripts/train_bert.py data/raw/data.csv
"""

import sys
import os

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from ai_scorer.models import BertScorer
from ai_scorer.utils import load_data, print_data_stats


def main():
    print("=" * 60)
    print("🦞 AI回复质量评分器 - BERT 训练")
    print("=" * 60)
    
    # 数据文件路径
    if len(sys.argv) > 1:
        data_file = sys.argv[1]
    else:
        data_file = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data", "raw", "data.csv")
    
    # 加载数据
    print(f"\n📂 加载数据: {data_file}")
    try:
        df = load_data(data_file)
        print_data_stats(df)
    except FileNotFoundError:
        print(f"❌ 找不到文件: {data_file}")
        print("请确保数据文件存在")
        return
    except Exception as e:
        print(f"❌ 加载失败: {e}")
        return
    
    # 训练模型
    print("\n" + "-" * 40)
    scorer = BertScorer()
    result = scorer.train(df)
    
    # 保存模型
    print("\n💾 保存模型...")
    scorer.save()
    
    print("\n" + "=" * 60)
    print("🎉 训练完成！")
    print("=" * 60)


if __name__ == "__main__":
    main()
