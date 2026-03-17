#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
预测脚本

使用方法：
    python scripts/predict.py                    # 交互模式
    python scripts/predict.py "这是一段文本"      # 命令行模式
    python scripts/predict.py --model bert       # 使用 BERT 模型
"""

import sys
import os

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from ai_scorer.inference import Predictor, interactive_predict


def main():
    # 解析参数
    model_type = 'random_forest'
    text = None
    
    args = sys.argv[1:]
    
    if '--model' in args:
        idx = args.index('--model')
        if idx + 1 < len(args):
            model_type = args[idx + 1]
            args = args[:idx] + args[idx+2:]
    
    if args:
        text = ' '.join(args)
    
    if text:
        # 命令行模式
        predictor = Predictor(model_type)
        result = predictor.predict(text)
        print(f"预测结果: {result['label']}")
        if 'probability' in result:
            print(f"AI 概率: {result['probability']['AI']:.2%}")
    else:
        # 交互模式
        interactive_predict(model_type)


if __name__ == "__main__":
    main()
