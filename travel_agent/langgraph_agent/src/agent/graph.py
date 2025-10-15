from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent
import requests

llm = ChatOpenAI(
    model= 'deepseek-chat',
    temperature= 0.8,
    base_url= 'https://api.deepseek.com/v1/',
    extra_body={'chat_template_kwargs':{'enabled_thingking':False}},
)

def get_weather(city: str) -> str:
    """通过 wttr.in API 查询指定城市的实时天气"""
    url = f"https://wttr.in/{city}?format=j1"  # 请求JSON格式数据
    try:
        response = requests.get(url)
        response.raise_for_status()  # 若状态码非200，抛出异常
        data = response.json()

        # 提取核心天气信息
        current_condition = data["current_condition"][0]
        weather_desc = current_condition["weatherDesc"][0]["value"]
        temp_c = current_condition["temp_C"]

        return f"{city}当前天气：{weather_desc}，气温{temp_c}摄氏度"

    except requests.exceptions.RequestException as e:
        return f"错误：查询天气时遇到网络问题 - {e}"
    except (KeyError, IndexError) as e:
        return f"错误：解析天气数据失败，可能是城市名称无效 - {e}"

graph = create_react_agent(
    llm,
    tools=[get_weather],
    prompt="你是智能助手"
)

