<template>
  <div class="ai-chat-container">
    <div class="ai-header">
      <el-icon><Cpu /></el-icon>
      <span class="ml-2">AI 助手</span>
    </div>

    <!-- 消息列表区域 -->
    <div class="messages-area" ref="messagesRef">
      <div v-for="(msg, index) in messages" :key="index" :class="['message-row', msg.role]">
        <div class="message-bubble">
          {{ msg.content }}
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <el-input
        v-model="input"
        placeholder="有什么可以帮助您..."
        type="textarea"
        :rows="2"
        resize="none"
        @keydown.enter.prevent="sendMessage"
      />
      <el-button type="primary" size="small" class="send-btn" @click="sendMessage" :loading="loading">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, defineEmits } from 'vue'
import { Cpu } from '@element-plus/icons-vue'
import { chatWithAI } from '../api/ai'

// 定义事件
const emit = defineEmits(['execute-command'])

const input = ref('')
const messagesRef = ref(null)
const loading = ref(false)

// 初始化对话历史，包含AI的欢迎消息
const messages = ref([
  { role: 'assistant', content: '您好！我是您的知识库 AI 助手。有什么我可以帮您的吗？' }
])

const sendMessage = async () => {
  if (!input.value.trim() || loading.value) return

  // 添加用户消息
  const userInput = input.value
  messages.value.push({ role: 'user', content: userInput })
  input.value = ''
  loading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 准备对话历史，格式化为后端所需的格式
    const conversationHistory = messages.value.map(msg => {
      return {
        role: msg.role === 'user' ? 'user' : 'assistant',
        content: msg.content
      }
    })

    // 调用AI对话接口
    const response = await chatWithAI(conversationHistory)
    console.log('AI API响应:', response)
    const aiResponse = response.message
    console.log('AI回复内容:', aiResponse)

    // 添加AI回复
    messages.value.push({ role: 'assistant', content: aiResponse })
    console.log('添加AI回复后消息列表长度:', messages.value.length)
    console.log('最后一条消息:', messages.value[messages.value.length - 1])
    await nextTick()
    console.log('DOM更新后')
    scrollToBottom()
    console.log('滚动到底部完成')

    // 检查是否是功能性指令并执行
    handleAICommand(aiResponse)
  } catch (error) {
    console.error('AI对话失败:', error)
    messages.value.push({ 
      role: 'assistant', 
      content: '抱歉，AI服务暂时不可用，请稍后重试。' 
    })
    await nextTick()
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 处理AI的功能性指令
const handleAICommand = (response) => {
  try {
    // 尝试解析JSON格式的指令
    const command = JSON.parse(response)
    if (command.type === 'command') {
      // 向父组件发送指令执行事件
      emit('execute-command', command)
    }
  } catch (e) {
    // 如果不是JSON格式，忽略
    return
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
  border-left: 1px solid #e6e6e6;
}

.ai-header {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
  color: #303133;
  background-color: #fafafa;
}

.ml-2 { margin-left: 8px; }

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background-color: #f9f9f9;
}

.message-row {
  display: flex;
  margin-bottom: 15px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.ai {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
}

.user .message-bubble {
  background-color: #304156; 
  color: #fff;
  border-top-right-radius: 2px;
}

.ai .message-bubble {
  background-color: #fff;
  border: 1px solid #e4e7ed;
  color: #606266;
  border-top-left-radius: 2px;
}

.input-area {
  padding: 10px;
  border-top: 1px solid #eee;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.send-btn {
  align-self: flex-end;
}
</style>