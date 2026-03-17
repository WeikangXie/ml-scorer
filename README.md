# AI 回复质量评分器

判断回复是 AI 生成还是真人写的

作者：阿爪 🦞

---

## 项目结构

```
ml-scorer/
├── README.md                   # 项目说明
├── setup.py                    # 安装配置
├── requirements.txt            # 依赖清单
├── .gitignore                  # Git 忽略文件
│
├── src/                        # 源代码
│   └── ai_scorer/
│       ├── __init__.py
│       ├── config.py           # 配置文件
│       ├── features/           # 特征提取
│       │   ├── __init__.py
│       │   └── text_features.py
│       ├── models/             # 模型定义
│       │   ├── __init__.py
│       │   ├── random_forest.py
│       │   └── bert.py
│       ├── training/           # 训练相关
│       │   └── __init__.py
│       ├── inference/          # 推理相关
│       │   ├── __init__.py
│       │   └── predict.py
│       └── utils/              # 工具函数
│           ├── __init__.py
│           └── data.py
│
├── scripts/                    # 脚本
│   ├── train_rf.py             # 训练随机森林
│   ├── train_bert.py           # 训练 BERT
│   ├── predict.py              # 预测脚本
│   └── export_pmml.py          # 导出 PMML
│
├── data/                       # 数据目录
│   ├── raw/                    # 原始数据
│   └── processed/              # 处理后数据
│
├── saved_models/               # 模型文件
│   ├── ai_scorer.pkl           # 随机森林模型
│   ├── model.pmml              # PMML 格式模型
│   └── bert_scorer/            # BERT 模型
│
├── java-service/               # Java HTTP 服务
│   ├── pom.xml
│   └── src/
│
├── tests/                      # 测试
└── notebooks/                  # Jupyter notebooks
```

---

## 快速开始

### 1. 安装依赖

```bash
# 基础依赖
pip install -r requirements.txt

# 或安装为包
pip install -e .
```

### 2. 训练模型

```bash
# 随机森林（推荐先尝试）
python scripts/train_rf.py data/raw/data.csv

# BERT（需要 GPU 更快）
python scripts/train_bert.py data/raw/data.csv
```

### 3. 预测

```bash
# 交互模式
python scripts/predict.py

# 命令行模式
python scripts/predict.py "感谢您的分享，让我们一起加油！"

# 使用 BERT 模型
python scripts/predict.py --model bert "这是一段文本"
```

### 4. 导出 PMML（供 Java 使用）

```bash
pip install sklearn2pmml
python scripts/export_pmml.py
```

---

## 代码使用示例

### Python

```python
from ai_scorer.models import RandomForestScorer, BertScorer
from ai_scorer.inference import Predictor

# 方式一：直接使用模型类
scorer = RandomForestScorer("saved_models/ai_scorer.pkl")
result = scorer.predict("感谢您的分享！")
print(result)

# 方式二：使用统一接口
predictor = Predictor(model_type='random_forest')
result = predictor.predict("这是一段文本")

# 批量预测
results = predictor.predict_batch(["文本1", "文本2", "文本3"])
```

### Java

```bash
# 1. 导出 PMML
python scripts/export_pmml.py

# 2. 复制到 Java 项目
cp saved_models/model.pmml java-service/src/main/resources/

# 3. 启动 Java 服务
cd java-service && mvn spring-boot:run

# 4. 调用 API
curl -X POST http://localhost:8080/api/v1/predict \
  -H "Content-Type: application/json" \
  -d '{"content": "感谢您的分享！"}'
```

---

## 特征说明

| 特征名 | 说明 |
|--------|------|
| length | 文本长度 |
| char_count | 字符数（不含空格） |
| sentence_count | 句子数 |
| avg_sentence_length | 平均句子长度 |
| exclamation_count | 感叹号数量 |
| question_count | 问号数量 |
| tilde_count | 波浪号数量 |
| period_count | 句号数量 |
| punct_density | 标点密度 |
| emoji_count | Emoji 数量 |
| emoji_density | Emoji 密度 |
| ai_template_count | AI 模板词命中数 |
| colloquial_count | 口语词命中数 |
| ends_with_period | 是否以句号结尾 |
| has_complete_structure | 是否有完整结构 |
| first_person_count | 第一人称数量 |
| nin_count | "您"数量 |
| ni_count | "你"数量 |

---

## 数据格式

训练数据 CSV 格式：

```csv
commentContent,extendType
"感谢您的分享！",AI_CHECK_IN
"哈哈牛逼！",null
```

- `commentContent`: 回复内容（必需）
- `extendType`: 标签（AI 类型或 null）

可选字段：
- `originalContent`: 原帖内容
- `auditStatus`: 审核状态

---

## 模型对比

| 模型 | 优点 | 缺点 |
|------|------|------|
| 随机森林 | 训练快、模型小、可解释 | 依赖特征工程 |
| BERT | 效果好、自动学习语义 | 训练慢、模型大、需 GPU |

---

## 发展路线

- [x] 随机森林模型
- [x] BERT 模型
- [x] PMML 导出
- [x] Java HTTP 服务
- [ ] 扩充数据集
- [ ] 模型效果监控
- [ ] FastAPI 服务

---

## 许可证

MIT
