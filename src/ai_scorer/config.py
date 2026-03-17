"""
配置文件
"""

import os

# 项目根目录
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# 路径配置
DATA_DIR = os.path.join(PROJECT_ROOT, "data")
RAW_DATA_DIR = os.path.join(DATA_DIR, "raw")
PROCESSED_DATA_DIR = os.path.join(DATA_DIR, "processed")
MODELS_DIR = os.path.join(PROJECT_ROOT, "saved_models")

# AI 类型的 extendType 值
AI_EXTEND_TYPES = {
    'AI_CHECK_IN',
    'AI_OPINION_SHARE', 
    'AI_PRODUCT_RULE'
}

# AI模板词库 - 可根据数据扩展
AI_TEMPLATE_WORDS = [
    "一起加油", "感谢您的分享", "感谢分享", "值得", "让我们", 
    "更好地", "更加", "确实", "希望", "期待", "继续",
    "投资路上", "投资的道路", "成为更", "理性", "乐观",
    "风险管理", "长期投资", "保持", "见证", "奇迹"
]

# 口语词库 - 真人常用的口语表达
COLLOQUIAL_WORDS = [
    "哈哈", "呵呵", "嗯", "啊", "吧", "嘛", "诶",
    "牛逼", "牛", "服了", "醉了", "无语", "草", "靠",
    "卧槽", "吊", "666"
]

# 模型参数
RANDOM_FOREST_PARAMS = {
    "n_estimators": 100,
    "max_depth": 10,
    "random_state": 42,
    "n_jobs": -1
}

BERT_PARAMS = {
    "model_name": "bert-base-chinese",
    "max_length": 128,
    "batch_size": 16,
    "epochs": 3,
    "learning_rate": 2e-5
}
