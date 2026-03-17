#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
导出 PMML 模型

使用方法：
    pip install sklearn2pmml
    python scripts/export_pmml.py
"""

import sys
import os

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pickle
from sklearn2pmml import sklearn2pmml, PMMLPipeline

from ai_scorer.config import MODELS_DIR


def main():
    print("=" * 60)
    print("🦞 PMML 导出工具")
    print("=" * 60)
    
    model_path = os.path.join(MODELS_DIR, "ai_scorer.pkl")
    output_path = os.path.join(MODELS_DIR, "model.pmml")
    
    # 加载模型
    print(f"\n📦 加载模型: {model_path}")
    try:
        with open(model_path, 'rb') as f:
            data = pickle.load(f)
        model = data['model']
        feature_names = data['feature_names']
        print(f"   加载成功！特征数量：{len(feature_names)}")
    except FileNotFoundError:
        print(f"   ❌ 找不到模型文件: {model_path}")
        print("   请先运行 python scripts/train_rf.py 训练模型")
        return
    
    # 创建 PMML Pipeline
    print("\n⚙️ 创建 PMML Pipeline...")
    pipeline = PMMLPipeline([('classifier', model)])
    
    # 导出
    print(f"\n💾 导出 PMML: {output_path}")
    try:
        sklearn2pmml(pipeline, output_path, with_repr=True)
        print("   导出成功！")
    except Exception as e:
        print(f"   ❌ 导出失败: {e}")
        print("   提示：请确保已安装 sklearn2pmml:")
        print("   pip install sklearn2pmml")
        return
    
    # 输出特征列表
    print(f"\n📋 特征列表（共 {len(feature_names)} 个，Java 端需按此顺序提取）：")
    for i, name in enumerate(feature_names):
        print(f"   {i+1:2d}. {name}")
    
    print("\n" + "=" * 60)
    print("🎉 导出完成！")
    print("=" * 60)
    print("\n下一步：")
    print(f"   1. 将 {output_path} 复制到 Java 项目")
    print("   2. 在 Java 中使用 jpmml 库加载模型")


if __name__ == "__main__":
    main()
