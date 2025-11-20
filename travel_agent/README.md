# 🌤️ 天气旅行助手 (Travel Agent)

一个基于LangGraph和DeepSeek的智能旅行助手，可以为用户提供天气查询和旅游景点推荐服务。

## ✨ 功能特性

- 🤖 **智能对话**: 基于DeepSeek AI模型的自然语言对话
- 🌦️ **天气查询**: 实时查询全球城市天气信息
- 🏛️ **景点推荐**: 基于高德地图API推荐旅游景点
- 💾 **对话记忆**: 支持上下文记忆，多轮对话连贯
- 🎤 **语音交互**: 支持语音输入和语音播报（前端）
- 📱 **现代UI**: 美观的响应式聊天界面

## 🏗️ 项目结构

```
travel_agent/
├── langgraph_agent/         # 后端AI Agent
│   ├── api_server.py         # FastAPI服务器
│   ├── requirements.txt      # Python依赖
│   ├── start_server.bat      # Windows启动脚本
│   ├── start_server.sh       # Linux/Mac启动脚本
│   ├── src/
│   │   ├── agent/
│   │   │   ├── graph.py      # 景点推荐工具
│   │   │   └── memory.py     # 对话记忆管理
│   │   └── tools/
│   │       └── get_weather.py # 天气查询工具
│   └── .env                  # 环境配置（需创建）
├── travel_agent.html         # 前端聊天界面
└── README.md                 # 本文件
```

## 🚀 快速开始

### 1️⃣ 环境准备

**必需条件:**
- Python 3.11 或更高版本
- DeepSeek API密钥 ([获取地址](https://platform.deepseek.com/))

**推荐:**
- 使用虚拟环境

### 2️⃣ 安装依赖

```bash
# 进入后端目录
cd langgraph_agent

# （推荐）创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt
```

### 3️⃣ 配置环境变量

在 `langgraph_agent/` 目录下创建 `.env` 文件：

```env
# DeepSeek API密钥
DEEPSEEK_API_KEY=your_deepseek_api_key_here
```

### 4️⃣ 启动后端服务

直接运行

```bash
cd langgraph_agent
python api_server.py
```

服务器启动后，你将看到：
```
🚀 启动天气旅行助手API服务器...
📍 访问地址: http://localhost:8000
📚 API文档: http://localhost:8000/docs
```

### 5️⃣ 打开前端界面

在浏览器中打开 `travel_agent.html` 文件即可使用！

推荐使用：
- Google Chrome
- Microsoft Edge
- Safari

## 📖 使用说明

### 对话示例

```
用户: 我想去北京旅行，帮我查查天气怎么样？
助手: [查询北京天气并推荐景点]

用户: 上海呢？
助手: [根据上下文查询上海信息]
```

### API接口

后端提供以下API接口：

**POST /chat** - 发送聊天消息
```json
{
  "message": "我想去北京旅行",
  "conversation_id": "user-123"
}
```

**GET /conversations** - 获取所有对话列表

**POST /new-conversation** - 创建新对话

**DELETE /conversation/{id}** - 删除指定对话

详细API文档：访问 http://localhost:8000/docs

## 🛠️ 技术栈

**后端:**
- [LangGraph](https://github.com/langchain-ai/langgraph) - AI Agent框架
- [FastAPI](https://fastapi.tiangolo.com/) - Web框架
- [DeepSeek](https://platform.deepseek.com/) - AI模型
- [LangChain](https://langchain.com/) - LLM工具链

**前端:**
- HTML5 + JavaScript
- [Tailwind CSS](https://tailwindcss.com/) - UI框架
- Web Speech API - 语音识别和合成

**外部API:**
- [wttr.in](https://wttr.in/) - 天气查询
- [高德地图](https://lbs.amap.com/) - 景点推荐

## 🔧 开发指南

### 添加新工具

1. 在 `src/tools/` 创建新工具文件
2. 使用 `@tool` 装饰器定义工具函数
3. 在 `api_server.py` 中导入并添加到agent的tools列表

示例：
```python
from langchain_core.tools import tool

@tool('my_new_tool')
def my_new_tool(param: str) -> str:
    """工具描述"""
    return f"处理结果: {param}"
```

### 自定义提示词

修改 `api_server.py` 中的 `prompt` 参数：
```python
agent = create_react_agent(
    llm,
    tools=[...],
    prompt="你的自定义提示词"
)
```

## ❓ 常见问题

**Q: 提示"未找到DEEPSEEK_API_KEY"**
A: 确保在 `langgraph_agent/.env` 文件中正确配置了API密钥

**Q: 前端无法连接后端**
A: 检查后端服务是否正常运行在 http://localhost:8000

**Q: 语音功能不可用**
A: 语音功能需要使用Chrome/Edge/Safari浏览器，且需要HTTPS或localhost

**Q: 天气查询失败**
A: 检查网络连接，确保可以访问 wttr.in API

## 📄 许可证

MIT License

## 🙏 致谢

- [LangChain](https://langchain.com/)
- [DeepSeek](https://www.deepseek.com/)
- [wttr.in](https://wttr.in/)
- [高德地图](https://lbs.amap.com/)

---

**开发者**: EchoLogic Team
**联系方式**: [GitHub Issues](https://github.com/yourusername/travel_agent/issues)

欢迎提交Issue和PR！ 🎉

