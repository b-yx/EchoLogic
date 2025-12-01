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
      <el-button type="primary" size="small" class="send-btn" @click="sendMessage">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Cpu } from '@element-plus/icons-vue'

const input = ref('')
const messagesRef = ref(null)
const messages = ref([
  { role: 'ai', content: '您好！我是您的知识库 AI 助手。有什么我可以帮您的吗？' }
])

const sendMessage = async () => {
  if (!input.value.trim()) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: input.value })
  const userInput = input.value
  input.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 模拟 AI 回复 (1秒后)
  setTimeout(() => {
    messages.value.push({ 
      role: 'ai', 
      content: `关于 "${userInput}"，我正在检索您的知识库... (模拟回复)` 
    })
    nextTick(() => scrollToBottom())
  }, 1000)
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
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