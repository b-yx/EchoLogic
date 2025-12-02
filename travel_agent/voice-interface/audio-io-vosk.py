# 旅行助手 - Vosk离线语音识别版（修复空格问题）
import sounddevice as sd
import numpy as np
import win32com.client
import time
import json
from vosk import Model, KaldiRecognizer

class OfflineTravelAssistant:
    def __init__(self):
        print("🚀 启动离线旅行助手...")
        
        # 初始化Vosk模型
        try:
            # 使用原始字符串避免转义问题
            self.vosk_model = Model(r"C:\Users\HP\Desktop\voice-interface\vosk-model-small-cn-0.22") # 修改为用户vosk所在位置！！！！！！！！！！！！！！！！！！！！！
            print("✅ Vosk离线语音识别模型加载成功")
        except Exception as e:
            print(f"❌ 模型加载失败: {e}")
            print("💡 请检查模型路径是否正确，并确保已下载中文模型")
            raise
        
        # 语音合成
        self.speaker = win32com.client.Dispatch("SAPI.SpVoice")
        
        # 数据库
        self.attractions_db = {
            '北京': ['故宫', '天安门', '长城', '颐和园', '天坛', '鸟巢', '水立方'],
            '上海': ['外滩', '东方明珠', '豫园', '南京路', '迪士尼乐园', '陆家嘴'],
            '广州': ['广州塔', '白云山', '越秀公园', '沙面', '陈家祠', '珠江夜游'],
            '深圳': ['世界之窗', '欢乐谷', '大梅沙', '东部华侨城', '深圳湾公园'],
            '杭州': ['西湖', '灵隐寺', '雷峰塔', '千岛湖', '宋城', '西溪湿地'],
            '成都': ['宽窄巷子', '锦里', '大熊猫基地', '都江堰', '青城山', '武侯祠']
        }
        
        self.weather_db = {
            '北京': '晴天，15到25度，适合出游',
            '上海': '多云，18到28度，微风舒适',
            '广州': '阵雨，20到30度，记得带伞', 
            '深圳': '晴朗，22到32度，天气炎热',
            '杭州': '晴转多云，16到26度，非常舒适',
            '成都': '雾转晴，14到24度，轻度污染'
        }
        
        # 输入模式
        self.input_mode = None
        
        print("✅ 离线助手初始化完成！")
    
    def select_initial_mode(self):
        """让用户选择初始输入模式"""
        print("\n" + "="*50)
        print("🎒 请选择初始输入模式")
        print("="*50)
        print("1. ⌨️  文本输入模式（键盘输入）")
        print("2. 🎤  离线语音输入模式（完全本地识别）")
        print("="*50)
        
        while True:
            try:
                choice = input("请选择 (1 或 2): ").strip()
                if choice == '1':
                    self.input_mode = "text"
                    return "文本输入模式"
                elif choice == '2':
                    self.input_mode = "voice"
                    return "离线语音输入模式"
                else:
                    print("❌ 请输入 1 或 2")
            except KeyboardInterrupt:
                print("\n👋 用户取消选择，退出程序")
                exit()
            except Exception as e:
                print(f"选择错误: {e}")
    
    def speak(self, text):
        """语音输出"""
        print(f"助手: {text}")
        try:
            self.speaker.Speak(text)
            time.sleep(0.3)
        except Exception as e:
            print(f"语音播放失败: {e}")
    
    def show_menu(self):
        """显示主菜单"""
        print("\n" + "="*50)
        print("🎒 离线旅行助手")
        print("="*50)
        print(f"当前输入模式: {'🎤 离线语音输入' if self.input_mode == 'voice' else '⌨️ 文本输入'}")
        print("\n📋 可用命令:")
        print("• '切换模式' - 切换输入方式")
        print("• '北京天气' - 查询天气")
        print("• '上海景点' - 推荐景点") 
        print("• '帮助' - 显示帮助信息")
        print("• '退出' - 结束程序")
        print("="*50)
    
    def switch_mode(self):
        """切换输入模式"""
        if self.input_mode == "voice":
            self.input_mode = "text"
            return "已切换到文本输入模式"
        else:
            self.input_mode = "voice"
            return "已切换到离线语音输入模式"
    
    def record_audio(self, duration=5, sample_rate=16000):
        """使用sounddevice录音"""
        try:
            print(f"🎤 正在录音 {duration} 秒...（说完话请等待识别）")
            audio_data = sd.rec(int(duration * sample_rate),
                              samplerate=sample_rate,
                              channels=1,
                              dtype='int16')
            sd.wait()
            print("录音完成，正在本地识别...")
            return audio_data.flatten()
        except Exception as e:
            print(f"录音错误: {e}")
            return None
    
    def vosk_voice_input(self):
        """使用Vosk进行离线语音识别"""
        try:
            audio_data = self.record_audio(duration=5)
            if audio_data is None:
                return "录音失败"
            
            # 创建识别器
            recognizer = KaldiRecognizer(self.vosk_model, 16000)
            
            # 处理音频数据
            audio_data_bytes = audio_data.tobytes()
            
            # 识别语音
            if recognizer.AcceptWaveform(audio_data_bytes):
                result = json.loads(recognizer.Result())
                text = result.get('text', '').strip()
                if text:
                    print(f"本地识别结果: {text}")
                    return text.lower()
                else:
                    return "无法识别"
            else:
                return "无法识别"
                
        except Exception as e:
            print(f"离线语音识别错误: {e}")
            return "错误"
    
    def text_input(self):
        """文本输入"""
        try:
            user_input = input("💬 请输入命令: ").strip()
            return user_input.lower() if user_input else ""
        except Exception as e:
            print(f"文本输入错误: {e}")
            return "错误"
    
    def process_command(self, command):
        """处理命令"""
        # 移除所有空格，解决Vosk识别添加空格的问题
        command = command.replace(" ", "")
        
        if command in ["无法识别", "错误", "录音失败", ""]:
            if command == "无法识别":
                return "抱歉，我没有听清楚，请重试", False
            elif command == "":
                return "请输入命令", False
            else:
                return "出现了一些问题，请重试", False
        
        print(f"收到命令: {command}")
        
        # 切换模式
        if any(keyword in command for keyword in ['切换模式', '切换输入', '改变模式', '模式切换']):
            response = self.switch_mode()
            return response, False
        
        # 帮助
        if any(keyword in command for keyword in ['帮助', '菜单', '功能', '你能做什么']):
            self.show_menu()
            return "已显示功能菜单", False
        
        # 天气查询
        if any(keyword in command for keyword in ['天气', '气温', '温度']):
            for city in self.attractions_db.keys():
                if city in command:
                    weather = self.weather_db.get(city)
                    return f"{city}的天气：{weather}", False
            return "请问您想查询哪个城市的天气？", False
        
        # 景点推荐
        if any(keyword in command for keyword in ['景点', '旅游', '好玩', '玩什么']):
            for city in self.attractions_db.keys():
                if city in command:
                    attractions = "、".join(self.attractions_db[city][:4])
                    return f"{city}的推荐景点：{attractions}", False
            return "请问您想了解哪个城市的景点？", False
        
        # 问候
        if any(keyword in command for keyword in ['你好', '您好', '嗨', 'hello']):
            return "您好！我是离线旅行助手，支持语音和文本输入", False
        
        # 退出
        if any(keyword in command for keyword in ['退出', '结束', '再见', '拜拜']):
            return "感谢使用，再见！", True
        
        return "抱歉，我不明白。可以说'切换模式'、'帮助'、或查询天气景点", False
    
    def run(self):
        """运行助手"""
        # 第一步：让用户选择初始模式
        initial_mode = self.select_initial_mode()
        
        # 第二步：欢迎和初始化
        self.speak(f"欢迎使用离线旅行助手！当前是{initial_mode}")
        self.show_menu()
        
        while True:
            try:
                print(f"\n当前模式: {'🎤 离线语音输入' if self.input_mode == 'voice' else '⌨️ 文本输入'}")
                print("输入 '切换模式' 来改变输入方式")
                print("-" * 40)
                
                # 根据当前模式获取输入
                if self.input_mode == "voice":
                    command = self.vosk_voice_input()
                else:
                    command = self.text_input()
                
                # 特殊处理：在文本模式下直接输入quit可以退出
                if self.input_mode == "text" and command == "quit":
                    break
                
                # 处理命令
                response, should_exit = self.process_command(command)
                
                # 语音输出
                self.speak(response)
                
                if should_exit:
                    break
                    
            except KeyboardInterrupt:
                self.speak("程序被中断，再见！")
                break
            except Exception as e:
                print(f"系统错误: {e}")
                self.speak("请重试")

def main():
    print("=== 离线旅行助手 ===")
    print("特点: 支持🎤离线语音识别和⌨️文本输入")
    print("提示: 语音识别完全在本地进行，无需网络")
    print("=" * 50)
    
    try:
        assistant = OfflineTravelAssistant()
        assistant.run()
    except Exception as e:
        print(f"启动失败: {e}")
        print("请检查Vosk模型是否正确安装")

if __name__ == "__main__":
    main()