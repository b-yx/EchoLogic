"""
FastAPI服务器 - 天气旅行助手后端API
提供聊天接口，支持对话记忆和多个工具调用
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import Optional, Dict, Any
import uvicorn
import os
import sys
from dotenv import load_dotenv

# 配置路径以导入agent模块
current_dir = os.path.dirname(os.path.abspath(__file__))
src_dir = os.path.join(current_dir, "src")
if src_dir not in sys.path:
    sys.path.insert(0, src_dir)

# 导入agent相关模块
from langchain_deepseek import ChatDeepSeek
from langgraph.prebuilt import create_react_agent
from langgraph.checkpoint.memory import MemorySaver
from tools.get_weather import get_weather  # type: ignore
import agent.graph as graph  # type: ignore

# 加载环境变量
load_dotenv()

# 创建FastAPI应用
app = FastAPI(
    title="天气旅行助手API",
    description="基于LangGraph和DeepSeek的智能旅行助手",
    version="1.0.0"
)

# 配置CORS - 允许前端跨域访问
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应该指定具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 请求和响应模型
class ChatRequest(BaseModel):
    """聊天请求模型"""
    message: str
    conversation_id: str = "default-conversation"

class ChatResponse(BaseModel):
    """聊天响应模型"""
    type: str = "success"
    message: str
    weather_data: Optional[Dict[str, Any]] = None

# 初始化Agent
memory = MemorySaver()
llm = ChatDeepSeek(
    model="deepseek-chat", 
    temperature=0.7,
    api_key=os.getenv("DEEPSEEK_API_KEY")
)

# 创建agent（包含天气查询和景点推荐工具）
agent = create_react_agent(
    llm,
    tools=[get_weather, graph.get_introduction],
    prompt="你是智能旅行助手，需要为用户查询旅游目的地的天气并推荐景点。请用友好、专业的语气回答用户问题。",
    checkpointer=memory
)

# 存储活跃的对话配置
conversations: Dict[str, Dict] = {}

def get_or_create_config(conversation_id: str) -> Dict:
    """获取或创建对话配置"""
    if conversation_id not in conversations:
        conversations[conversation_id] = {
            "configurable": {"thread_id": conversation_id}
        }
    return conversations[conversation_id]

@app.get("/")
async def root():
    """返回前端页面"""
    # 获取项目根目录下的 travel_agent.html
    html_path = os.path.join(os.path.dirname(current_dir), "travel_agent.html")
    
    # 如果文件存在，返回HTML页面
    if os.path.exists(html_path):
        return FileResponse(html_path)
    
    # 如果文件不存在，返回API状态（兼容模式）
    return {
        "status": "running",
        "service": "天气旅行助手API",
        "version": "1.0.0",
        "note": "前端文件未找到，请访问 /docs 查看API文档"
    }

@app.get("/health")
async def health_check():
    """健康检查端点（API专用）"""
    return {
        "status": "running",
        "service": "天气旅行助手API",
        "version": "1.0.0"
    }

@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    聊天接口
    
    Args:
        request: 包含用户消息和对话ID的请求
        
    Returns:
        ChatResponse: AI助手的回复
    """
    try:
        # 验证输入
        if not request.message or not request.message.strip():
            raise HTTPException(status_code=400, detail="消息内容不能为空")
        
        # 获取或创建对话配置
        config = get_or_create_config(request.conversation_id)
        
        # 调用agent处理消息
        messages_dict = {"messages": [("user", request.message.strip())]}
        response = agent.invoke(messages_dict, config=config)
        
        # 提取AI回复
        ai_message = response["messages"][-1].content
        
        # 检测是否包含天气信息
        has_weather = "天气" in ai_message or "温度" in ai_message or "°C" in ai_message
        
        return ChatResponse(
            type="success",
            message=ai_message,
            weather_data={"has_weather": has_weather} if has_weather else None
        )
        
    except Exception as e:
        # 记录错误日志
        print(f"处理聊天请求时出错: {str(e)}")
        import traceback
        traceback.print_exc()
        
        # 返回错误响应
        return ChatResponse(
            type="error",
            message="抱歉，处理您的请求时出现了问题",
            weather_data={"error": str(e)}
        )

@app.post("/new-conversation")
async def new_conversation(conversation_id: str):
    """
    创建新对话
    
    Args:
        conversation_id: 新对话的ID
        
    Returns:
        确认消息
    """
    config = get_or_create_config(conversation_id)
    return {
        "status": "success",
        "conversation_id": conversation_id,
        "message": "新对话已创建"
    }

@app.delete("/conversation/{conversation_id}")
async def delete_conversation(conversation_id: str):
    """
    删除对话
    
    Args:
        conversation_id: 要删除的对话ID
        
    Returns:
        确认消息
    """
    if conversation_id in conversations:
        del conversations[conversation_id]
        return {
            "status": "success",
            "message": f"对话 {conversation_id} 已删除"
        }
    else:
        raise HTTPException(status_code=404, detail="对话不存在")

@app.get("/conversations")
async def list_conversations():
    """
    列出所有活跃对话
    
    Returns:
        对话ID列表
    """
    return {
        "conversations": list(conversations.keys()),
        "count": len(conversations)
    }

if __name__ == "__main__":
    # 检查API密钥
    if not os.getenv("DEEPSEEK_API_KEY"):
        print("警告: 未找到DEEPSEEK_API_KEY环境变量")
        print("请在.env文件中设置: DEEPSEEK_API_KEY=your_api_key")
        sys.exit(1)
    
    print("🚀 启动天气旅行助手API服务器...")
    print("📍 访问地址: http://localhost:8000")
    print("📚 API文档: http://localhost:8000/docs")
    
    # 启动服务器
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="info"
    )

