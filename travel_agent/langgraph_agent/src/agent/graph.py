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

def get_introduction(city: str) -> str:
    """通过高德地图API查询指定城市的旅游景点"""
    # 修正了字符串闭合问题
    url = f"https://restapi.amap.com/v3/place/text?keywords=景点&region={city}&key=0b3607182ab322a7aad5d6eba763084f"
    try:
        response = requests.get(url)
        response.raise_for_status()
        data = response.json()

        # 添加数据检查
        if "pois" not in data or not data["pois"]:
            return f"未找到{city}的景点信息"

        pois = data["pois"][0]
        introduction_address = pois.get("address", "未知地址")
        
        # 安全地获取嵌套数据
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

graph = create_react_agent(
    llm,
    tools=[get_weather,get_introduction],
    prompt="你是智能助手"
)

