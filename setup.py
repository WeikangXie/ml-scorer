#!/usr/bin/env python3
"""
AI 回复质量评分器

判断回复是 AI 生成还是真人写的
"""

from setuptools import setup, find_packages

setup(
    name="ai-scorer",
    version="1.0.0",
    author="阿爪",
    author_email="ai-scorer@example.com",
    description="AI 回复质量评分器 - 判断回复是 AI 生成还是真人写的",
    long_description=open("README.md", encoding="utf-8").read(),
    long_description_content_type="text/markdown",
    url="https://github.com/example/ai-scorer",
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    classifiers=[
        "Development Status :: 3 - Alpha",
        "Intended Audience :: Developers",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
    ],
    python_requires=">=3.8",
    install_requires=[
        "pandas>=1.3.0",
        "numpy>=1.21.0",
        "scikit-learn>=1.0.0",
    ],
    extras_require={
        "bert": [
            "torch>=1.10.0",
            "transformers>=4.15.0",
            "tqdm>=4.62.0",
        ],
        "pmml": [
            "sklearn2pmml>=1.0.0",
        ],
        "dev": [
            "pytest>=6.0.0",
            "black>=21.0",
            "flake8>=3.9.0",
        ],
    },
    entry_points={
        "console_scripts": [
            "ai-scorer=ai_scorer.inference.predict:main",
        ],
    },
)
