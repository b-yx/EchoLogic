# 🚀 快速启动指南

## 第一次使用

### 步骤1：安装依赖

```bash
# 进入项目目录
cd langgraph_agent

# 安装Python依赖包
pip install -r requirements.txt
```

### 步骤2：配置API密钥

创建 `.env` 文件（在 `langgraph_agent` 目录下）：

```env
DEEPSEEK_API_KEY=sk-你的API密钥
```

> 💡 获取API密钥：访问 https://platform.deepseek.com/

### 步骤3：启动后端服务

**Windows用户：**
```cmd
start_server.bat
```

**Linux/Mac用户：**
```bash
chmod +x start_server.sh
./start_server.sh
```

看到以下信息表示启动成功：
```
🚀 启动天气旅行助手API服务器...
📍 访问地址: http://localhost:8000
📚 API文档: http://localhost:8000/docs
```

### 步骤4：打开前端

在浏览器中打开项目根目录下的 `travel_agent.html` 文件。

> 💡 推荐使用 Chrome、Edge 或 Safari 浏览器

## 测试对话

试试以下问题：

1. **天气查询**: "北京今天天气怎么样？"
2. **景点推荐**: "推荐一下上海的景点"
3. **综合咨询**: "我想去杭州旅行，帮我看看天气并推荐景点"
4. **上下文对话**: 
   - 第一句: "查一下成都的天气"
   - 第二句: "那边有什么好玩的景点吗？"

## 常见问题

### 问题1：pip install 失败

```bash
# 尝试使用国内镜像
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 问题2：前端连接失败

1. 确认后端服务正在运行
2. 访问 http://localhost:8000 检查服务状态
3. 查看浏览器控制台（F12）的错误信息

### 问题3：API密钥错误

检查 `.env` 文件：
- 文件位置：`langgraph_agent/.env`
- 格式：`DEEPSEEK_API_KEY=sk-xxxxx`（没有引号和空格）
- 密钥有效性：访问 https://platform.deepseek.com/ 确认

### 问题4：Python版本问题

确保Python版本 >= 3.9：
```bash
python --version
```

## 高级功能

### 查看API文档

访问：http://localhost:8000/docs

### 直接测试API

```bash
# 使用curl测试
curl -X POST "http://localhost:8000/chat" \
  -H "Content-Type: application/json" \
  -d '{"message":"北京天气怎么样？","conversation_id":"test-001"}'
```

### 使用Python测试

```python
import requests

response = requests.post(
    "http://localhost:8000/chat",
    json={
        "message": "北京天气怎么样？",
        "conversation_id": "test-001"
    }
)
print(response.json())
```

## 下一步

- 查看完整文档：[README.md](../README.md)
- 了解技术细节：[langgraph_agent/src/](src/)
- 自定义提示词：修改 `api_server.py` 中的 prompt
- 添加新工具：在 `src/tools/` 目录下创建新文件

祝使用愉快！ 🎉

