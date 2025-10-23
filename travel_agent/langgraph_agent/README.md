# 天气旅行助手 - 后端API

基于LangGraph和FastAPI的智能旅行助手后端服务。

## 🚀 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 配置环境变量

创建 `.env` 文件：
```env
DEEPSEEK_API_KEY=sk-your-api-key-here
```

### 3. 启动服务器

```bash
# Windows
start_server.bat

# Linux/Mac
./start_server.sh

# 或直接运行
python api_server.py
```

服务器将在 `http://localhost:8000` 启动

## 📚 API文档

访问 http://localhost:8000/docs 查看完整的API文档（Swagger UI）

### 主要端点

- `POST /chat` - 发送聊天消息
- `GET /conversations` - 获取对话列表
- `POST /new-conversation` - 创建新对话
- `DELETE /conversation/{id}` - 删除对话

### 使用示例

```python
import requests

response = requests.post(
    "http://localhost:8000/chat",
    json={
        "message": "北京今天天气怎么样？",
        "conversation_id": "user-001"
    }
)

print(response.json())
```

## 🛠️ 项目结构

```
langgraph_agent/
├── api_server.py          # FastAPI服务器
├── requirements.txt       # Python依赖
├── start_server.bat       # Windows启动脚本
├── start_server.sh        # Linux/Mac启动脚本
├── test_connection.py     # 连接测试脚本
├── src/
│   ├── agent/
│   │   ├── graph.py       # 景点推荐工具
│   │   └── memory.py      # 对话记忆管理
│   └── tools/
│       └── get_weather.py # 天气查询工具
└── .env                   # 环境配置（需创建）
```

## 🧪 测试

运行测试脚本验证系统是否正常：

```bash
python test_connection.py
```

## 📖 更多文档

- [快速启动指南](QUICKSTART.md)
- [部署指南](DEPLOYMENT.md)
- [完整文档](../README.md)

## 🔧 开发

### 添加新工具

1. 在 `src/tools/` 创建新文件
2. 使用 `@tool` 装饰器定义函数
3. 在 `api_server.py` 导入并添加到tools列表

示例：
```python
from langchain_core.tools import tool
from typing import Annotated

@tool('my_tool')
def my_tool(param: Annotated[str, '参数描述']) -> str:
    """工具功能描述"""
    return f"结果: {param}"
```

### 自定义提示词

修改 `api_server.py` 第65行的prompt参数。

## 📝 技术栈

- **LangGraph** - AI Agent框架
- **FastAPI** - Web框架
- **DeepSeek** - 大语言模型
- **LangChain** - LLM工具链

## ⚙️ 环境要求

- Python >= 3.9
- DeepSeek API密钥

## 📄 许可证

MIT License

