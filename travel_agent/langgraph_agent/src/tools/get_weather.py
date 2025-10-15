import requests
from langchain_core.tools import tool
from typing import Annotated
import time
from functools import lru_cache
from datetime import datetime, timedelta

# 添加缓存减少重复API调用
@lru_cache(maxsize=100)  # 增加缓存大小以适应更多查询组合
def get_weather_cached(city: str, days: int = 0) -> str:
    """
    带缓存的天气查询函数，支持未来几天预报
    
    Args:
        city: 城市名称
        days: 查询未来几天天气，0表示只查询当前天气，1-3表示未来1-3天
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

            # 如果只查询当前天气
            if days == 0:
                return _format_current_weather(data, city)
            # 查询未来几天天气
            else:
                return _format_forecast(data, city, days)

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


def _format_current_weather(data: dict, city: str) -> str:
    """格式化当前天气信息"""
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


def _format_forecast(data: dict, city: str, days: int) -> str:
    """格式化未来几天天气预报"""
    # 确保请求的天数不超过API提供的天数（通常为3天）
    available_days = min(days, len(data['weather']) - 1)  # 减1是因为第0天是今天
    
    if available_days < 1:
        return "错误：无法获取未来天气信息"
    
    result = [f"{city}未来{available_days}天天气预报："]
    
    # 获取今天日期
    today = datetime.now().date()
    
    for i in range(1, available_days + 1):
        day_forecast = data['weather'][i]
        date = today + timedelta(days=i)
        date_str = date.strftime("%m月%d日")
        
        max_temp = day_forecast['maxtempC']
        min_temp = day_forecast['mintempC']
        weather_desc = day_forecast['hourly'][4]['weatherDesc'][0]['value']  # 取中午时段的天气描述
        
        result.append(f"{date_str}：{weather_desc}，温度{min_temp}°C ~ {max_temp}°C")
    
    return "\n".join(result)


@tool('get_weather')
def get_weather(
        city: Annotated[str, '城市名称，支持中文和英文名称'],
        days: Annotated[int, '查询未来几天天气，0表示只查询当前天气，1-3表示未来1-3天预报'] = 0
                ) -> str:
    """
    工具函数：通过调用 wttr.in API 查询真实的天气信息。
    提供当前天气状况或未来几天天气预报。
    
    Args:
        city: 城市名称，如"北京"、"Shanghai"等
        days: 查询未来几天天气，0表示只查询当前天气，1-3表示未来1-3天
        
    Returns:
        str: 格式化的天气信息字符串
    """
    # 去除前后空格并验证输入
    city = city.strip()
    if not city:
        return "错误：城市名称不能为空"
    
    if len(city) > 50:
        return "错误：城市名称过长，请检查输入"
    
    # 验证days参数
    if days < 0 or days > 3:
        return "错误：days参数只能是0-3，0表示当前天气，1-3表示未来1-3天预报"
    
    return get_weather_cached(city, days)


# 可选：添加批量查询功能
def get_weather_multiple(cities: list, days: int = 0) -> dict:
    """
    批量查询多个城市的天气
    
    Args:
        cities: 城市名称列表
        days: 查询未来几天天气，0表示只查询当前天气，1-3表示未来1-3天
    """
    results = {}
    for city in cities:
        results[city] = get_weather(city, days)
        # 添加短暂延迟避免频繁请求
        time.sleep(0.1)
    return results