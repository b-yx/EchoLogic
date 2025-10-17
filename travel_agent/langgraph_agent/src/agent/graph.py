import os
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent

from src.tools.get_weather import get_weather

llm = ChatOpenAI(
    model='deepseek-chat',
    temperature=0.8,
    base_url='https://api.deepseek.com/v1/',
    api_key=os.environ.get("DEEPSEEK_API_KEY"),  # 从环境变量获取API密钥
    extra_body={'chat_template_kwargs': {'enabled_thingking': False}},
)

graph = create_react_agent(
    llm,
    tools=[get_weather],
    prompt="你是智能助手"
)