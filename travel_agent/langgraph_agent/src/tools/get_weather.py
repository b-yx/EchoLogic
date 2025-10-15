import requests
from langchain_core.tools import tool
from typing import Annotated
import time
from functools import lru_cache

# 添加缓存减少重复API调用
@lru_cache(maxsize=50)
def get_weather_cached(city: str) -> str:
    """
    带缓存的天气查询函数
    """
    # API端点，我们请求JSON格式的数据
    url = f"https://wttr.in/{city}?format=j1"
    
    # 设置合理的超时时间
    timeout = 10
    
    try:
        # 使用Session提高连接效率
        with requests.Session() as session:
            session.headers.update({
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
            })
            
            # 发起网络请求
            response = session.get(url, timeout=timeout)
            # 检查响应状态码是否为200 (成功)
            response.raise_for_status()
            # 解析返回的JSON数据
            data = response.json()

            # 提取当前天气状况
            current_condition = data['current_condition'][0]
            weather_desc = current_condition['weatherDesc'][0]['value']
            temp_c = current_condition['temp_C']
            feels_like = current_condition['FeelsLikeC']
            humidity = current_condition['humidity']
            wind_speed = current_condition['windspeedKmph']

            # 获取今日预报
            today_forecast = data['weather'][0]
            max_temp = today_forecast['maxtempC']
            min_temp = today_forecast['mintempC']

            # 格式化成更详细的自然语言返回
            return (f"{city}当前天气：{weather_desc}，"
                   f"气温{temp_c}°C（体感{feels_like}°C），"
                   f"湿度{humidity}%，风速{wind_speed}km/h。"
                   f"今日温度范围：{min_temp}°C ~ {max_temp}°C")

    except requests.exceptions.Timeout:
        return f"错误：查询{city}天气时请求超时，请稍后重试"
    except requests.exceptions.ConnectionError:
        return f"错误：网络连接异常，无法查询{city}天气"
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 404:
            return f"错误：未找到城市'{city}'，请检查城市名称是否正确"
        else:
            return f"错误：HTTP请求失败 - {e.response.status_code}"
    except (KeyError, IndexError) as e:
        return f"错误：解析{city}天气数据失败，可能是API响应格式变化"
    except Exception as e:
        return f"错误：查询{city}天气时遇到未知问题 - {str(e)}"

@tool('get_weather')
def get_weather(
        city: Annotated[str, '城市名称，支持中文和英文名称']
                ) -> str:
    """
    工具函数：通过调用 wttr.in API 查询真实的天气信息。
    提供当前天气状况、温度、体感温度、湿度、风速和当日温度范围。
    
    Args:
        city: 城市名称，如"北京"、"Shanghai"等
        
    Returns:
        str: 格式化的天气信息字符串
    """
    # 去除前后空格并验证输入
    city = city.strip()
    if not city:
        return "错误：城市名称不能为空"
    
    if len(city) > 50:
        return "错误：城市名称过长，请检查输入"
    
    return get_weather_cached(city)

# 可选：添加批量查询功能
def get_weather_multiple(cities: list) -> dict:
    """
    批量查询多个城市的天气
    """
    results = {}
    for city in cities:
        results[city] = get_weather(city)
        # 添加短暂延迟避免频繁请求
        time.sleep(0.1)
    return results