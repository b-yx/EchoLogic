const API_BASE = window.CHAT_API_BASE || 'http://localhost:8000';

const app = Vue.createApp({
  data() {
    return {
      showHistory: false,
      messages: [],
      input: '',
      loading: false,
      sendDisabled: false,
      // 语音
      isRecording: false,
      recognition: null,
      // 播报
      isSpeaking: false,
      currentSpeechText: '',
      lastClickTime: 0,
      typingId: null
    }
  },
  mounted() {
    // 初始化语音识别（若支持）
    this.initSR();
  },
  methods: {
    // UI 切换
    gotoHistory(){ this.showHistory = true; },
    gotoChat(){ this.showHistory = false; },

    // 消息
    addUser(text){
      this.messages.push({ id: Date.now()+Math.random(), role:'user', text });
      this.$nextTick(this.scrollToBottom);
    },
    addAssistant(res){
      const text = res?.message || '（无内容）';
      this.messages.push({ id: Date.now()+Math.random(), role: res?.type==='error'?'error':'assistant', text, detail: res?.detail, weather: !!res?.weather_data });
      this.$nextTick(this.scrollToBottom);
    },
    addTyping(){
      const id = 'typing-' + Date.now() + Math.random();
      this.messages.push({ id, role:'typing' });
      this.$nextTick(this.scrollToBottom);
      return id;
    },
    replaceTypingWithAssistant(res, id){
      const text = res?.message || '（无内容）';
      const nextMsg = { id: Date.now()+Math.random(), role: res?.type==='error'?'error':'assistant', text, detail: res?.detail, weather: !!res?.weather_data };
      const idx = this.messages.findIndex(m => m.id === id);
      if(idx !== -1){
        this.messages.splice(idx, 1, nextMsg);
      }else{
        this.messages.push(nextMsg);
      }
      this.$nextTick(this.scrollToBottom);
    },
    async send(){
      const msg = this.input.trim();
      if(!msg){ alert('请输入消息内容'); return; }
      this.addUser(msg);
      this.input = '';
      this.sendDisabled = true;
      const typingId = this.addTyping();
      try{
        const r = await fetch(API_BASE + '/chat', {
          method: 'POST', headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ message: msg, conversation_id: 'travel-assistant' })
        });
        if(!r.ok) throw new Error('网络请求失败: ' + r.status);
        const data = await r.json();
        this.replaceTypingWithAssistant(data, typingId);
      }catch(e){
        this.replaceTypingWithAssistant({ type:'error', message:'网络连接异常，无法获取旅行方案', detail:'请检查后端服务是否启动，或稍后重试。错误信息: ' + e.message }, typingId);
      }finally{
        this.sendDisabled = false;
      }
    },
    scrollToBottom(){
      const el = this.$refs.history;
      if(el) el.scrollTop = el.scrollHeight;
    },

    // 语音识别
    initSR(){
      if(!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) return;
      if(this.recognition) return;
      const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
      const rec = new SR();
      rec.continuous = false;
      rec.interimResults = false;
      rec.lang = 'zh-CN';
      rec.onstart = ()=>{ this.isRecording = true; };
      rec.onresult = e => { this.input = e.results[0][0].transcript; };
      rec.onerror = e => { this.stopRec(); alert('语音识别失败: ' + e.error); };
      rec.onend = ()=>{ this.stopRec(); };
      this.recognition = rec;
    },
    startRec(){
      if(!this.recognition){ this.initSR(); if(!this.recognition){ alert('浏览器不支持语音识别'); return; } }
      this.recognition.start();
    },
    stopRec(){ this.isRecording = false; this.recognition && this.recognition.stop(); },

    // 语音播报
    handleSpeech(text, btn){
      const now = Date.now();
      const diff = now - this.lastClickTime;
      if(diff < 300 && this.currentSpeechText === text){
        window.speechSynthesis.cancel();
        this.speak(text, btn);
        this.lastClickTime = 0;
      }else{
        if(this.isSpeaking && this.currentSpeechText === text){
          window.speechSynthesis.pause();
          this.isSpeaking = false;
          this.updateBtn(false, btn);
        }else if(window.speechSynthesis.paused && this.currentSpeechText === text){
          window.speechSynthesis.resume();
          this.isSpeaking = true;
          this.updateBtn(true, btn);
        }else{
          window.speechSynthesis.cancel();
          this.speak(text, btn);
        }
        this.lastClickTime = now;
      }
    },
    updateBtn(play, btn){
      if(!btn) return;
      const i = btn.querySelector('i');
      const s = btn.querySelector('span');
      if(play){ i.className = 'fas fa-pause mr-1'; if(s) s.textContent = '暂停播报'; }
      else { i.className = 'fas fa-volume-up mr-1'; if(s) s.textContent = '语音播报'; }
    },
    speak(text, btn){
      if(!('speechSynthesis' in window)) return alert('浏览器不支持语音播报');
      window.speechSynthesis.cancel();
      const u = new SpeechSynthesisUtterance(text);
      u.lang = 'zh-CN';
      u.rate = 0.95;
      u.onstart = ()=>{ this.isSpeaking = true; this.currentSpeechText = text; this.updateBtn(true, btn); };
      u.onend = ()=>{ this.isSpeaking = false; this.currentSpeechText = ''; this.updateBtn(false, btn); };
      u.onerror = ()=>{ this.isSpeaking = false; this.currentSpeechText = ''; this.updateBtn(false, btn); alert('语音播报出错，请重试'); };
      window.speechSynthesis.speak(u);
    },
    // 添加：将 Markdown 文本解析为安全 HTML，并用于助手消息渲染
    renderMarkdown(text) {
      try {
        const raw = typeof text === 'string' ? text : String(text || '');
        // 使用 marked 解析，换行转为 <br> 以更好显示
        const html = marked.parse(raw, { breaks: true });
        // 使用 DOMPurify 进行 XSS 安全过滤
        return DOMPurify.sanitize(html);
      } catch (e) {
        // 解析失败时，仍做安全过滤并原样返回
        return DOMPurify.sanitize(String(text || ''));
      }
    },
  },
  template:`
  <div class="bg-gray-50 font-inter text-dark min-h-screen flex flex-col">
    <!-- 头部 -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
      <div class="container mx-auto px-4 py-3 flex items-center justify-between">
        <div class="flex items-center space-x-4">
          <button id="history-btn" class="flex items-center space-x-1 text-sm font-medium text-dark hover:text-primary transition-colors" @click="gotoHistory">
            <i class="fas fa-history text-lg"></i>
            <span class="hidden sm:inline"> 历史对话 </span>
          </button>
          <button id="new-chat-btn" class="flex items-center space-x-1 text-sm font-medium text-dark hover:text-primary transition-colors" @click="gotoChat">
            <i class="fas fa-plus-circle text-lg"></i>
            <span class="hidden sm:inline"> 新对话 </span>
          </button>
        </div>
        <div class="flex items-center space-x-4">
          <div class="flex items-center"><span class="font-semibold text-dark hidden sm:inline">EchoLogic</span></div>
          <button class="p-2 rounded-full hover:bg-gray-100 transition-colors"></button>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="flex-1 container mx-auto px-4 py-6 flex flex-col">
      <!-- 历史对话页面 -->
      <div class="flex-1 flex flex-col max-w-4xl mx-auto w-full" v-show="showHistory" id="history-page">
        <div class="text-center mb-8 pt-10">
          <h2 class="text-2xl font-bold text-dark mb-2">历史对话</h2>
          <p class="text-secondary">查看和管理您的历史对话记录</p>
        </div>
        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="divide-y divide-gray-100"></div>
          <div class="py-20 text-center" id="empty-history">
            <div class="w-20 h-20 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <i class="fas fa-inbox text-gray-300 text-2xl"></i>
            </div>
            <h3 class="text-lg font-medium text-dark mb-1">暂无历史对话</h3>
            <p class="text-secondary max-w-xs mx-auto">开始新的对话，您的历史记录将显示在这里</p>
          </div>
        </div>
      </div>

      <!-- 聊天界面区域 -->
      <div class="flex-1 flex flex-col max-w-6xl mx-auto w-full" v-show="!showHistory" id="chat-interface">
        <!-- 对话历史 -->
        <div ref="history" class="flex-1 overflow-y-auto scrollbar-hide p-4 space-y-6 mb-6" id="chat-history">
          <div class="flex-1 overflow-y-auto p-4" ref="chatHistory">
            <!-- 欢迎提示：始终显示在顶部，保持与原前端一致 -->
            <div class="flex items-start space-x-3 mb-4">
              <div class="w-10 h-10 rounded-full bg-primary flex-shrink-0 flex items-center justify-center">
                <i class="fas fa-cloud-sun text-white"></i>
              </div>
              <div class="bg-white rounded-xl shadow-sm p-4 max-w-[85%]">
                <p class="text-secondary">今天有什么可以帮到您？</p>
                <p class="text-sm text-gray-400 mt-2">我可以根据天气情况为您推荐最佳旅行方案</p>
              </div>
            </div>
          
            <div v-for="(m, idx) in messages" :key="idx" class="mb-4">
              <!-- 用户消息（右对齐） -->
              <div v-if="m.role==='user'" class="flex items-start justify-end space-x-3">
                <div class="bg-primary/10 rounded-xl p-4 max-w-[85%]"><p class="text-dark">{{ m.text }}</p></div>
                <div class="w-10 h-10 rounded-full bg-gray-200 flex-shrink-0 flex items-center justify-center"><i class="fas fa-user text-secondary"></i></div>
              </div>
              
              <!-- 错误消息（左对齐，显眼提示） -->
              <div v-else-if="m.role==='error'" class="flex items-start space-x-3">
                <div class="w-10 h-10 rounded-full bg-primary flex-shrink-0 flex items-center justify-center"><i class="fas fa-exclamation-triangle text-white"></i></div>
                <div class="bg-white rounded-xl shadow-sm p-4 max-w-[85%]">
                  <div class="flex items-center text-yellow-600 mb-2">
                    <i class="fas fa-exclamation-triangle mr-2"></i>
                    <h3 class="text-lg font-semibold">{{ m.text }}</h3>
                  </div>
                  <p class="text-gray-600" v-if="m.detail">{{ m.detail }}</p>
                </div>
              </div>

              <!-- 助手打字中（左对齐，三点动画） -->
              <div v-else-if="m.role==='typing'" class="flex items-start space-x-3">
                <div class="w-10 h-10 rounded-full bg-primary flex-shrink-0 flex items-center justify-center">
                  <i class="fas fa-cloud-sun text-white"></i>
                </div>
                <div class="bg-white rounded-xl shadow-sm p-4 max-w-[85%]">
                  <div class="flex items-center space-x-2">
                    <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-duration:1s;"></span>
                    <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-duration:1s;animation-delay:.2s;"></span>
                    <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-duration:1s;animation-delay:.4s;"></span>
                  </div>
                </div>
              </div>

              <!-- 助手消息（左对齐） -->
              <div v-else class="flex items-start space-x-3">
                <div class="w-10 h-10 rounded-full bg-primary flex-shrink-0 flex items-center justify-center"><i class="fas fa-cloud-sun text-white"></i></div>
                <div class="bg-white rounded-xl shadow-sm p-4 max-w-[85%]">
                  <!-- Markdown 内容（安全过滤后） -->
                  <div class="text-secondary space-y-2" v-html="renderMarkdown(m.text)"></div>
                  <!-- 天气信息标记（如有） -->
                  <div class="mt-2 text-sm text-blue-600" v-if="m.weather"><i class="fas fa-cloud-sun mr-1"></i>包含实时天气信息</div>
                  <!-- 语音播报按钮 -->
                  <div class="mt-3 text-right">
                    <button class="text-primary hover:text-primary/80 transition-colors inline-flex items-center" @click="handleSpeech(m.text,$event.currentTarget)">
                      <i class="fas fa-volume-up mr-1"></i><span>语音播报</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
          <div class="relative">
            <textarea class="w-full border border-gray-200 rounded-lg p-4 pr-24 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all resize-none min-h-[100px]" id="message-input" v-model="input" @keydown.enter.exact.prevent="send" placeholder='请输入您的旅行需求，例如："我想去北京旅行，什么时候去最合适？"'></textarea>
            <div class="absolute right-3 bottom-3 flex items-center space-x-2">
              <button class="p-3 rounded-full bg-gray-100 hover:bg-gray-200 transition-colors text-secondary" id="voice-record-btn" :class="{ 'bg-red-500 text-white': isRecording }" @click="isRecording?stopRec():startRec()" title="点击开始语音输入">
                <i class="fas fa-microphone"></i>
              </button>
              <button class="p-3 rounded-full bg-primary hover:bg-primary/90 transition-colors text-white" id="send-btn" :disabled="sendDisabled" @click="send">
                <i class="fas fa-paper-plane"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="bg-white py-4 border-t border-gray-200">
      <div class="container mx-auto px-4 text-center text-sm text-gray-500">
        <p>天气旅行助手 | 您的智能旅行规划伙伴</p>
      </div>
    </footer>

    <!-- 已使用对话内打字动画替代原加载弹窗 -->

    <!-- 录音提示 -->
    <div class="fixed bottom-24 left-1/2 transform -translate-x-1/2 bg-primary text-white px-6 py-3 rounded-full shadow-lg flex items-center space-x-2" v-show="isRecording" id="recording-indicator">
      <div class="w-3 h-3 bg-white rounded-full animate-pulse"></div>
      <span> 正在录音... </span>
      <span class="text-sm opacity-80"> (点击停止) </span>
    </div>
  </div>
  `
});

app.mount('#app');