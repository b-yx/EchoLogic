from dotenv import load_dotenv
from langgraph.prebuilt import create_react_agent
from langchain_deepseek import ChatDeepSeek # DeepSeek 聊天模型
from langgraph.checkpoint.memory import MemorySaver
# 只import了get_weather一个函数,对吗?
# from tools.get_weather import get_weather
from tools import get_weather
# from graph import get_introduction
import graph

load_dotenv()

memory = MemorySaver()  # 简易内存存储；进程退出会丢。可换成持久化实现。
llm = ChatDeepSeek(model="deepseek-chat", temperature=0)

# 下面只是一个示例函数
# def get_weather(city: str) -> str:
#     """Get weather for a given city."""
#     return f"It's always sunny in {city}!"

# 下面只是一个示例函数
# def get_introduction(city: str) -> str:
#     """Get weather for a given city."""
#     return f"It's always sunny in {city}!"
agent = create_react_agent(
    llm,
    # tools=[get_weather,get_introduction], # 这边可以添加tools
    tools=[get_weather.get_weather,graph.get_introduction], # 这边可以添加tools
    prompt="你是智能旅行手，需要为用户查询旅游目的地的天气并推荐景点",
    checkpointer=memory # 记忆化要用
)

cnt_id = 1
thread_id = "demo-thread-1"
CONFIG = {
    "configurable":{"thread_id":"demo-thread-1"}
}

# 开启新的对话
def new_conversation():
    global cnt_id
    cnt_id += 1
    CONFIG["configurable"]["thread_id"] = f"demo-thread-{cnt_id}"

# 输入对话内容
def main_work():
    str_get = input("") #从这里输入,本地在这里测试
    # 下面三行用于本地测试
    # if str_get == "0":
    #     new_conversation()
    #     return
    messages_dict = {"messages": [("user", str_get)]}
    resp = agent.invoke(messages_dict,config=CONFIG) #得到返回值
    print(resp["messages"][-1].content) # 本地测试用 输出文本

# 测试,确实能记忆上下文
# while True:
#     main_work()

"""
这个版本是进程退出,历史记录就消失的版本
"""