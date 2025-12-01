<template>
  <div class="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-md transition-shadow cursor-pointer h-32 flex flex-col">
    <div class="font-medium truncate mb-2 text-gray-800">{{ data.title || 'Untitled' }}</div>
    <div class="text-sm text-gray-500 line-clamp-3 overflow-hidden flex-1">
      {{ data.content || 'No content...' }}
    </div>
    <div class="mt-2 flex gap-2">
      <!-- 假装显示一些标签 -->
      <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">Tag</span>
    </div>
  </div>
</template>

<script setup>
defineProps(['data'])
</script>