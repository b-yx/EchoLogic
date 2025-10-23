# 📖 使用示例

## 对话示例

### 1. 天气查询

**用户输入：**
```
北京今天天气怎么样？
```

**AI回复：**
```
北京当前天气：晴，气温15°C（体感13°C），湿度45%，风速12km/h。
今日温度范围：8°C ~ 18°C

今天北京天气不错，适合外出旅行。温度适中，建议穿长袖衣物。
```

---

### 2. 景点推荐

**用户输入：**
```
上海有什么好玩的景点推荐？
```

**AI回复：**
```
上海推荐景点：外滩风景区，该景点位于黄浦区中山东一路，
该景点的评分：4.7，推荐前往时间：全天开放

外滩是上海的标志性景点之一，可以欣赏黄浦江两岸的美景，
尤其是夜晚灯光璀璨，非常适合拍照留念。
```

---

### 3. 综合咨询

**用户输入：**
```
我想下周去杭州旅行，帮我看看天气并推荐景点
```

**AI回复：**
```
让我为您查询杭州的天气和景点信息。

【天气情况】
杭州当前天气：多云，气温22°C（体感21°C），湿度65%，风速8km/h。
今日温度范围：18°C ~ 25°C

【景点推荐】
杭州推荐景点：西湖风景名胜区，该景点位于西湖区龙井路1号，
该景点的评分：4.8，推荐前往时间：全天开放

【旅行建议】
杭州现在天气宜人，非常适合旅游。西湖是必游景点，建议您：
1. 早晨或傍晚游览西湖，光线柔和适合拍照
2. 可以租用共享单车环湖骑行
3. 带上雨具，杭州天气多变
4. 品尝当地特色美食如龙井虾仁、叫化鸡
```

---

### 4. 多轮对话（上下文记忆）

**第一轮：**
```
用户: 查一下成都的天气
AI: 成都当前天气：阴，气温18°C，湿度78%...
```

**第二轮：**
```
用户: 那边有什么好吃的？
AI: 成都是著名的美食之都，推荐您品尝：
    1. 火锅 - 川味火锅享誉全国
    2. 串串香 - 成都特色小吃
    3. 担担面、龙抄手等传统小吃
    ...
```

**第三轮：**
```
用户: 景点呢？
AI: 成都推荐景点：宽窄巷子，位于青羊区...
```

---

## API调用示例

### Python示例

```python
import requests

# 1. 简单对话
def chat(message, conversation_id="default"):
    response = requests.post(
        "http://localhost:8000/chat",
        json={
            "message": message,
            "conversation_id": conversation_id
        }
    )
    return response.json()

# 使用示例
result = chat("北京天气怎么样？", "user-123")
print(result['message'])

# 2. 创建新对话
def new_conversation(conv_id):
    response = requests.post(
        f"http://localhost:8000/new-conversation?conversation_id={conv_id}"
    )
    return response.json()

new_conversation("user-456")

# 3. 查看所有对话
def list_conversations():
    response = requests.get("http://localhost:8000/conversations")
    return response.json()

conversations = list_conversations()
print(f"活跃对话数: {conversations['count']}")
```

### JavaScript示例

```javascript
// 1. 发送聊天消息
async function chat(message, conversationId = "default") {
    const response = await fetch('http://localhost:8000/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            message: message,
            conversation_id: conversationId
        })
    });
    
    const data = await response.json();
    return data;
}

// 使用示例
chat("上海有什么景点？", "user-001")
    .then(result => {
        console.log(result.message);
        if (result.weather_data) {
            console.log("包含天气数据");
        }
    });

// 2. 获取对话列表
async function getConversations() {
    const response = await fetch('http://localhost:8000/conversations');
    const data = await response.json();
    return data;
}

// 3. 删除对话
async function deleteConversation(conversationId) {
    const response = await fetch(
        `http://localhost:8000/conversation/${conversationId}`,
        { method: 'DELETE' }
    );
    return await response.json();
}
```

### cURL示例

```bash
# 1. 健康检查
curl http://localhost:8000/

# 2. 发送聊天消息
curl -X POST "http://localhost:8000/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "北京今天天气怎么样？",
    "conversation_id": "test-001"
  }'

# 3. 获取对话列表
curl http://localhost:8000/conversations

# 4. 创建新对话
curl -X POST "http://localhost:8000/new-conversation?conversation_id=new-chat-001"

# 5. 删除对话
curl -X DELETE "http://localhost:8000/conversation/test-001"
```

---

## 前端集成示例

### 基础聊天功能

```html
<!DOCTYPE html>
<html>
<head>
    <title>天气旅行助手</title>
</head>
<body>
    <div id="chat-container"></div>
    <input id="message-input" type="text" placeholder="输入消息...">
    <button onclick="sendMessage()">发送</button>

    <script>
        const conversationId = "user-" + Date.now();
        
        async function sendMessage() {
            const input = document.getElementById('message-input');
            const message = input.value.trim();
            
            if (!message) return;
            
            // 显示用户消息
            addMessage('user', message);
            input.value = '';
            
            try {
                // 调用API
                const response = await fetch('http://localhost:8000/chat', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        message: message,
                        conversation_id: conversationId
                    })
                });
                
                const data = await response.json();
                
                // 显示AI回复
                addMessage('ai', data.message);
                
            } catch (error) {
                console.error('错误:', error);
                addMessage('system', '连接失败，请检查后端服务');
            }
        }
        
        function addMessage(sender, text) {
            const container = document.getElementById('chat-container');
            const messageDiv = document.createElement('div');
            messageDiv.className = sender;
            messageDiv.textContent = text;
            container.appendChild(messageDiv);
            container.scrollTop = container.scrollHeight;
        }
    </script>
</body>
</html>
```

---

## 高级功能示例

### 1. 流式响应（未来功能）

```python
@app.post("/chat-stream")
async def chat_stream(request: ChatRequest):
    async def generate():
        # 实现流式输出
        for chunk in agent.stream(messages):
            yield f"data: {json.dumps(chunk)}\n\n"
    
    return StreamingResponse(generate(), media_type="text/event-stream")
```

### 2. 自定义工具

```python
from langchain_core.tools import tool

@tool('get_hotel')
def get_hotel(city: str, budget: str) -> str:
    """查询酒店信息"""
    # 实现酒店查询逻辑
    return f"{city}的{budget}酒店推荐..."

# 添加到agent
agent = create_react_agent(
    llm,
    tools=[get_weather, get_introduction, get_hotel],
    ...
)
```

### 3. 错误处理

```python
@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    return JSONResponse(
        status_code=500,
        content={
            "type": "error",
            "message": "服务器内部错误",
            "detail": str(exc)
        }
    )
```

---

## 调试技巧

### 1. 查看详细日志

```python
import logging
logging.basicConfig(level=logging.DEBUG)
```

### 2. 测试单个工具

```python
from tools.get_weather import get_weather

result = get_weather("北京", 0)
print(result)
```

### 3. 查看对话历史

```python
# 在api_server.py添加
@app.get("/conversation/{conversation_id}/history")
async def get_history(conversation_id: str):
    config = conversations.get(conversation_id)
    if not config:
        raise HTTPException(404, "对话不存在")
    
    # 获取历史记录
    history = memory.get_messages(config)
    return {"messages": history}
```

---

需要更多帮助？查看 [完整文档](README.md)

