# 旅行助手
import sounddevice as sd
import speech_recognition as sr
import numpy as np
import win32com.client
import time
import wave
import io

class NoPyaudioVoiceAssistant:
    def __init__(self):
        print("🚀 启动语音助手...")
        
        # 语音识别
        self.recognizer = sr.Recognizer()
        
        # 语音合成
        self.speaker = win32com.client.Dispatch("SAPI.SpVoice")
        
        # 数据库
        self.attractions_db = {
            '北京': ['故宫', '天安门', '长城', '颐和园', '天坛'],
            '上海': ['外滩', '东方明珠', '豫园', '南京路', '迪士尼乐园'],
            '广州': ['广州塔', '白云山', '越秀公园', '沙面', '陈家祠'],
            '深圳': ['世界之窗', '欢乐谷', '大梅沙', '东部华侨城', '深圳湾公园'],
            '杭州': ['西湖', '灵隐寺', '雷峰塔', '千岛湖', '宋城'],
            '成都': ['宽窄巷子', '锦里', '大熊猫基地', '都江堰', '青城山']
        }
        
        self.weather_db = {
            '北京': '晴天，15到25度，适合出游',
            '上海': '多云，18到28度，微风舒适',
            '广州': '阵雨，20到30度，记得带伞', 
            '深圳': '晴朗，22到32度，天气炎热',
            '杭州': '晴转多云，16到26度，非常舒适',
            '成都': '雾转晴，14到24度，轻度污染'
        }
        
        print("✅ 助手初始化完成！")
    
    def speak(self, text):
        """语音输出"""
        print(f"助手: {text}")
        try:
            self.speaker.Speak(text)
            time.sleep(0.3)
        except Exception as e:
            print(f"语音播放失败: {e}")
    
    def record_audio(self, duration=5, sample_rate=16000):
        """使用sounddevice录音"""
        try:
            print(f"🎤 正在录音 {duration} 秒...")
            # 录音
            audio_data = sd.rec(int(duration * sample_rate),
                              samplerate=sample_rate,
                              channels=1,
                              dtype='int16')
            sd.wait()  # 等待录音完成
            print("录音完成，正在识别...")
            return audio_data.flatten()
        except Exception as e:
            print(f"录音错误: {e}")
            return None
    
    def audio_to_text(self, audio_data, sample_rate=16000):
        """将音频数据转换为文字"""
        try:
            # 将numpy数组转换为AudioData格式
            audio_bytes = audio_data.tobytes()
            audio_segment = sr.AudioData(audio_bytes, sample_rate, 2)
            
            # 语音识别
            text = self.recognizer.recognize_google(audio_segment, language='zh-CN')
            print(f"识别结果: {text}")
            return text.lower()
        except sr.UnknownValueError:
            return "无法识别"
        except sr.RequestError as e:
            print(f"识别服务错误: {e}")
            return "服务错误"
        except Exception as e:
            print(f"识别错误: {e}")
            return "错误"
    
    def listen(self):
        """语音输入主函数"""
        try:
            # 录音
            audio_data = self.record_audio(duration=5)
            if audio_data is None:
                return "录音失败"
            
            # 识别
            text = self.audio_to_text(audio_data)
            return text
            
        except Exception as e:
            print(f"语音输入错误: {e}")
            return "错误"
    
    def process_command(self, command):
        """处理命令"""
        if command in ["无法识别", "服务错误", "错误", "录音失败"]:
            return command, False
        
        # 天气查询
        if any(keyword in command for keyword in ['天气', '气温', '温度']):
            for city in self.attractions_db.keys():
                if city in command:
                    weather = self.weather_db.get(city)
                    return f"{city}的天气：{weather}", False
            return "请问您想查询哪个城市的天气？", False
        
        # 景点推荐
        if any(keyword in command for keyword in ['景点', '旅游', '好玩']):
            for city in self.attractions_db.keys():
                if city in command:
                    attractions = "、".join(self.attractions_db[city])
                    return f"{city}的推荐景点：{attractions}", False
            return "请问您想了解哪个城市的景点？", False
        
        # 问候
        if any(keyword in command for keyword in ['你好', '您好', '嗨']):
            return "您好！我是旅行助手，可以帮您查询天气和推荐景点", False
        
        # 退出
        if any(keyword in command for keyword in ['退出', '结束', '再见']):
            return "感谢使用，再见！", True
        
        return "抱歉，我不明白。请说'天气'或'景点'", False
    
    def run(self):
        """运行助手"""
        self.speak("欢迎使用语音旅行助手！")
        self.speak("请说话，我会识别您的需求")
        
        while True:
            try:
                print("\n" + "="*40)
                # 语音输入
                command = self.listen()
                
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
    print("=== 语音助手 ===")
    print("使用说明：")
    print("• 请确保麦克风权限")
    print("• 说话时请清晰")
    print("• 录音时长5秒")
    print("=" * 30)
    
    # 安装检查
    try:
        import sounddevice
        print("✅ sounddevice 可用")
    except ImportError:
        print("❌ 需要安装 sounddevice")
        print("请在命令行运行: pip install sounddevice")
        return
    
    try:
        assistant = NoPyaudioVoiceAssistant()
        assistant.run()
    except Exception as e:
        print(f"启动失败: {e}")

if __name__ == "__main__":
    main()