<template>
  <div class="bg-white rounded-lg border border-gray-200 p-4">
    <!-- 简化的工具栏 -->
    <div class="flex space-x-2 mb-2 border-b border-gray-100 pb-2">
      <button class="w-6 h-6 font-bold hover:bg-gray-100 rounded">B</button>
      <button class="w-6 h-6 italic hover:bg-gray-100 rounded">/</button>
    </div>
    
    <!-- 输入区域 -->
    <textarea 
      class="w-full h-20 resize-none outline-none text-gray-700 placeholder-gray-400"
      placeholder="Type something..."
      v-model="inputValue"
    ></textarea>
    
    <div class="flex justify-between items-center mt-2">
      <el-button size="small" circle icon="Plus" />
      <el-button type="primary" size="small" @click="$emit('submit', inputValue)">
        {{ mode === 'incubating' ? 'Incubate' : 'Record' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
defineProps(['mode'])
const inputValue = ref('')
</script>