"""
预测器
"""

import sys
from ..models import RandomForestScorer, BertScorer


class Predictor:
    """统一预测接口"""
    
    def __init__(self, model_type='random_forest', model_path=None):
        """
        初始化预测器
        
        参数：
            model_type: 模型类型 ('random_forest' 或 'bert')
            model_path: 模型路径
        """
        self.model_type = model_type
        
        if model_type == 'random_forest':
            self.scorer = RandomForestScorer(model_path)
        elif model_type == 'bert':
            self.scorer = BertScorer(model_path)
        else:
            raise ValueError(f"不支持的模型类型: {model_type}")
    
    def predict(self, text):
        """
        预测单条文本
        
        参数：
            text: 输入文本
        
        返回：
            dict: 预测结果
        """
        return self.scorer.predict(text)
    
    def predict_batch(self, texts):
        """
        批量预测
        
        参数：
            texts: 文本列表
        
        返回：
            list: 预测结果列表
        """
        return [self.predict(text) for text in texts]


def interactive_predict(model_type='random_forest', model_path=None):
    """
    交互式预测模式
    
    参数：
        model_type: 模型类型
        model_path: 模型路径
    """
    predictor = Predictor(model_type, model_path)
    
    print("=" * 60)
    print(f"🦞 AI回复质量评分器 - 预测模式 ({model_type})")
    print("=" * 60)
    print("\n💬 输入文本进行预测（输入 'quit' 退出）：")
    
    while True:
        print("\n" + "-" * 40)
        text = input("📝 请输入回复内容：").strip()
        
        if text.lower() == 'quit':
            print("👋 再见！")
            break
        
        if not text:
            print("请输入内容")
            continue
        
        result = predictor.predict(text)
        
        print(f"\n🎯 预测结果：{result['label']}")
        if 'probability' in result:
            print(f"   AI 概率：{result['probability']['AI']:.2%}")
            print(f"   真人概率：{result['probability']['真人']:.2%}")


if __name__ == "__main__":
    # 命令行入口
    model_type = sys.argv[1] if len(sys.argv) > 1 else 'random_forest'
    interactive_predict(model_type)
