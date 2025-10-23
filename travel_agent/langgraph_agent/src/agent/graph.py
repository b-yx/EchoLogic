import os
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent
import requests
# from src.tools.get_weather import get_weather
# 上面的导入改成
# from tools.get_weather import get_weather
# 这份代码应该不用导入其他代码,我先注释掉了

# llm = ChatOpenAI(
#     model='deepseek-chat',
#     temperature=0.8,
#     base_url='https://api.deepseek.com/v1/',
#     api_key=os.environ.get("DEEPSEEK_API_KEY"),  # 从环境变量获取API密钥
#     extra_body={'chat_template_kwargs': {'enabled_thingking': False}},
# )

from langchain_core.tools import tool
from typing import Annotated

@tool('get_introduction')
def get_introduction(city: Annotated[str, '城市名称']) -> str:
    """通过高德地图API查询指定城市的旅游景点"""
    url = f"https://restapi.amap.com/v3/place/text?keywords=景点&region={city}&key=0b3607182ab322a7aad5d6eba763084f"
    try:
        response = requests.get(url)
        response.raise_for_status()
        data = response.json()

        if "pois" not in data or not data["pois"]:
            return f"未找到{city}的景点信息"

        pois = data["pois"][0]
        introduction_address = pois.get("address", "未知地址")
        
        biz_ext = pois.get("biz_ext", [{}])
        if isinstance(biz_ext, list) and biz_ext:
            in_rating = biz_ext[0].get("rating", "无评分")
            in_time = biz_ext[0].get("opentime2", "时间未知")
        else:
            in_rating = "无评分"
            in_time = "时间未知"

        return f"{city}推荐景点：{introduction_address}，该景点位于{introduction_address}，该景点的评分：{in_rating}，推荐前往时间：{in_time}"

    except requests.exceptions.RequestException as e:
        return f"错误：查询攻略时遇到网络问题 - {e}"
    except (KeyError, IndexError) as e:
        return f"错误：解析攻略数据失败，可能是城市名称无效 - {e}"

# graph = create_react_agent(
#     llm,
#     tools=[get_weather,get_introduction],
#     prompt="你是智能旅行手，需要为用户查询旅游目的地的天气并推荐景点"
# )
# res = get_introduction
# print("hello")
