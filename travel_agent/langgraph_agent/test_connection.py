"""
测试脚本 - 验证后端API连接
运行此脚本前请确保后端服务器已启动
"""
import requests
import json
import sys

BASE_URL = "http://localhost:8000"

def test_health_check():
    """测试健康检查端点"""
    print("🔍 [1/4] 测试健康检查...")
    try:
        response = requests.get(f"{BASE_URL}/", timeout=5)
        if response.status_code == 200:
            print("✅ 健康检查通过")
            print(f"   响应: {response.json()}")
            return True
        else:
            print(f"❌ 健康检查失败: HTTP {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print("❌ 无法连接到服务器，请确保后端服务已启动")
        print("   启动命令: python api_server.py")
        return False
    except Exception as e:
        print(f"❌ 错误: {e}")
        return False

def test_chat_simple():
    """测试简单聊天"""
    print("\n🔍 [2/4] 测试简单对话...")
    try:
        payload = {
            "message": "你好",
            "conversation_id": "test-connection-001"
        }
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            timeout=30
        )
        
        if response.status_code == 200:
            data = response.json()
            print("✅ 简单对话测试通过")
            print(f"   用户: {payload['message']}")
            print(f"   助手: {data.get('message', '')[:100]}...")
            return True
        else:
            print(f"❌ 对话测试失败: HTTP {response.status_code}")
            print(f"   响应: {response.text}")
            return False
    except Exception as e:
        print(f"❌ 错误: {e}")
        return False

def test_chat_weather():
    """测试天气查询功能"""
    print("\n🔍 [3/4] 测试天气查询...")
    try:
        payload = {
            "message": "北京今天天气怎么样？",
            "conversation_id": "test-connection-002"
        }
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            timeout=30
        )
        
        if response.status_code == 200:
            data = response.json()
            print("✅ 天气查询测试通过")
            print(f"   用户: {payload['message']}")
            print(f"   助手: {data.get('message', '')[:150]}...")
            
            # 检查是否包含天气信息
            if data.get('weather_data'):
                print("   ℹ️  检测到天气数据")
            return True
        else:
            print(f"❌ 天气查询失败: HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 错误: {e}")
        return False

def test_list_conversations():
    """测试对话列表功能"""
    print("\n🔍 [4/4] 测试对话列表...")
    try:
        response = requests.get(f"{BASE_URL}/conversations", timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ 对话列表获取成功")
            print(f"   当前对话数: {data.get('count', 0)}")
            if data.get('conversations'):
                print(f"   对话ID: {data['conversations'][:3]}")
            return True
        else:
            print(f"❌ 获取对话列表失败: HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 错误: {e}")
        return False

def main():
    """运行所有测试"""
    print("=" * 60)
    print("天气旅行助手 - API连接测试")
    print("=" * 60)
    
    results = []
    
    # 运行测试
    results.append(("健康检查", test_health_check()))
    
    if not results[0][1]:
        print("\n" + "=" * 60)
        print("⚠️  无法连接到后端服务，请先启动服务器")
        print("   启动命令: python api_server.py")
        print("   或使用: start_server.bat (Windows)")
        print("=" * 60)
        sys.exit(1)
    
    results.append(("简单对话", test_chat_simple()))
    results.append(("天气查询", test_chat_weather()))
    results.append(("对话列表", test_list_conversations()))
    
    # 汇总结果
    print("\n" + "=" * 60)
    print("测试结果汇总")
    print("=" * 60)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{test_name:15} {status}")
    
    print("=" * 60)
    print(f"总计: {passed}/{total} 测试通过")
    
    if passed == total:
        print("🎉 所有测试通过！系统运行正常")
        print("\n下一步:")
        print("1. 在浏览器中打开 travel_agent.html")
        print("2. 开始与AI助手对话")
        print("3. 尝试语音输入功能")
        sys.exit(0)
    else:
        print("⚠️  部分测试失败，请检查配置")
        print("\n常见问题:")
        print("1. 确保.env文件包含有效的DEEPSEEK_API_KEY")
        print("2. 检查网络连接")
        print("3. 查看服务器日志获取详细错误信息")
        sys.exit(1)

if __name__ == "__main__":
    main()

