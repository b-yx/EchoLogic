**语音识别模块说明**

版本对比

Vosk 离线版本：离线版本，需要下载 vosk-model，识别精度较差但是稳定，推荐使用 vosk 版本

Google 在线版本：在线版本，有的时候会因为网络问题无法访问

使用 vosk 版本需要安装 vosk 模型， 官网下载网址：https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip ，并将 vosk 模型的路径填入代码的指定区域（已标注）


自定义模块调用例子：
导入自定义模块
```python
  try:
      import a1  # 你的自定义模块
      print("✅ 自定义模块 a1 加载成功")
  except ImportError:
      print("⚠️  未找到自定义模块 a1，相关功能将不可用")
      a1 = None
```

在 process_command 方法里加入

```python
    # 自定义模块调用：北京 + 景点
    if '北京' in command and any(keyword in command for keyword in ['景点', '旅游', '好玩', '玩什么']):
        response = self.call_custom_module_a1('北京')
        return response, False
```
