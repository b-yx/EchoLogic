<template>
  <div class="floating-records" :class="{ collapsed }" ref="floatingContainer">
    <div class="floating-header" @click="collapsed = !collapsed">
      <el-icon><List /></el-icon>
      <span v-if="!collapsed">悬浮记录</span>
      <span class="record-count">{{ records.length }}</span>
      <div class="drag-handle" @mousedown="startDrag">
        <el-icon><Rank /></el-icon>
      </div>
    </div>
    
    <div class="floating-content" v-if="!collapsed">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索记录..."
        style="margin-bottom: 10px"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <div class="records-list">
        <div
          v-for="record in filteredRecords"
          :key="record.id"
          class="record-item"
          draggable="true"
          @dragstart="onDragStart($event, record)"
        >
          <div class="record-title">{{ record.title }}</div>
          <div class="record-type">
            <el-tag :type="getContentTypeTag(record.contentType)" size="small">
              {{ getContentTypeLabel(record.contentType) }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { List, Search, Rank } from '@element-plus/icons-vue'
import recordsApi from '@/api/records'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: true
  }
})

// 状态
const records = ref([])
const collapsed = ref(true) // 初始状态为收起
const searchKeyword = ref('')
const floatingContainer = ref(null)
const position = ref({ x: 100, y: window.innerHeight / 2 - 150 }) // 初始位置：页面中间偏左

// 计算属性
const filteredRecords = computed(() => {
  if (!searchKeyword.value) return records.value
  return records.value.filter(record => 
    record.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 拖拽事件和更新记录事件
const emit = defineEmits(['drag-start', 'update:records'])

const onDragStart = (event, record) => {
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('application/json', JSON.stringify(record))
  emit('drag-start', record)
}

// 悬浮窗移动功能
let isDragging = false
let startPos = { x: 0, y: 0 }
let animationFrameId = null

const startDrag = (event) => {
  isDragging = true
  startPos = {
    x: event.clientX - position.value.x,
    y: event.clientY - position.value.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  
  // 阻止事件冒泡，避免触发其他事件
  event.stopPropagation()
}

const onDrag = (event) => {
  if (!isDragging) return
  
  // 使用requestAnimationFrame优化拖拽性能
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  
  animationFrameId = requestAnimationFrame(() => {
    position.value = {
      x: event.clientX - startPos.x,
      y: event.clientY - startPos.y
    }
    
    // 更新DOM元素的位置
    if (floatingContainer.value) {
      floatingContainer.value.style.left = `${position.value.x}px`
      floatingContainer.value.style.top = `${position.value.y}px`
    }
  })
  
  // 阻止事件冒泡，避免触发其他事件
  event.stopPropagation()
}

const stopDrag = () => {
  isDragging = false
  
  // 取消未执行的动画帧
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
  
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 组件挂载时设置初始位置
onMounted(() => {
  if (floatingContainer.value) {
    floatingContainer.value.style.left = `${position.value.x}px`
    floatingContainer.value.style.top = `${position.value.y}px`
  }
})

// 辅助函数
const getContentTypeLabel = (type) => {
  const map = { TEXT: '文本', LINK: '链接', IMAGE: '图片', VIDEO: '视频', AUDIO: '音频', DOCUMENT: '文档' }
  return map[type] || type
}

const getContentTypeTag = (type) => {
  const map = { TEXT: '', LINK: 'success', IMAGE: 'warning', VIDEO: 'danger', AUDIO: 'info', DOCUMENT: 'primary' }
  return map[type] || ''
}

// 加载记录
const loadRecords = async () => {
  try {
    const data = await recordsApi.getAllRecords()
    records.value = data
  } catch (error) {
    console.error('加载记录失败:', error)
  }
}

// 更新记录的方法，供父组件调用
const updateRecords = async () => {
  await loadRecords()
  emit('update:records', records.value)
}

// 组件挂载时加载记录
loadRecords()

// 暴露方法给父组件
defineExpose({
  updateRecords
})
</script>

<style scoped>
.floating-records {
  position: fixed;
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: all 0.3s ease;
  transform: none;
}

.floating-records.collapsed {
  width: 60px;
}

.floating-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #f0f2f5;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
  user-select: none;
}

.drag-handle {
  cursor: move;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.drag-handle:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.floating-header .record-count {
  background-color: #409eff;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
}

.floating-content {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-item {
  padding: 12px;
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s ease;
}

.record-item:hover {
  background-color: #ecf5ff;
  border-color: #d9ecff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.record-item:active {
  cursor: grabbing;
}

.record-title {
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-type {
  display: flex;
  justify-content: flex-end;
}
</style>