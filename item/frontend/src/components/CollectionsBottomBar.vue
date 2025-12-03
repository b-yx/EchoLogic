<template>
  <div class="collections-bottom-bar">
    <div class="bar-header" @click="toggleExpand">
      <el-icon><CollectionTag /></el-icon>
      <span>快速分类</span>
      <el-button 
        size="small" 
        type="text" 
        @click.stop="toggleExpand"
      >
        <el-icon><component :is="expanded ? ArrowUp : ArrowDown" /></el-icon>
        {{ expanded ? '收起' : '展开' }}
      </el-button>
    </div>
    
    <div v-if="expanded" class="collections-scroll" :class="{ expanded }" @dragover.prevent @drop="onDrop">
      <div 
        v-for="collection in collections"
        :key="collection.id"
        class="collection-drop-area"
        :style="{ borderColor: collection.color }"
      >
        <div class="collection-icon" :style="{ color: collection.color }">
          <el-icon :size="24">
            <component :is="iconMap[collection.icon] || Folder" />
          </el-icon>
        </div>
        <div class="collection-info">
          <div class="collection-name">{{ collection.name }}</div>
          <div class="collection-count">{{ collection.recordCount }} 个记录</div>
        </div>
      </div>
      
      <div v-if="collections.length === 0" class="no-collections">
        <el-icon><WarningFilled /></el-icon>
        <span>暂无集合，请先创建集合</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { CollectionTag, ArrowDown, ArrowUp, Folder, Star, Sugar, WarningFilled } from '@element-plus/icons-vue'
import collectionsApi from '@/api/collections'

// 图标映射
const iconMap = shallowRef({
  folder: Folder,
  star: Star,
  heart: Sugar
})

// 状态
const collections = ref([])
const expanded = ref(false)

// 加载集合数据
const loadCollections = async () => {
  try {
    const data = await collectionsApi.getAllCollections()
    collections.value = data
  } catch (error) {
    console.error('加载集合失败:', error)
  }
}

// 切换展开/收起状态
const toggleExpand = () => {
  expanded.value = !expanded.value
}

// 拖拽放置事件
const onDrop = async (event) => {
  event.preventDefault()
  
  try {
    // 获取拖拽的记录数据
    const record = JSON.parse(event.dataTransfer.getData('application/json'))
    
    // 确定放置目标
    const dropTarget = event.target.closest('.collection-drop-area')
    if (!dropTarget) {
      ElMessage.warning('请将记录拖拽到具体的集合上')
      return
    }
    
    // 获取集合ID
    const collectionIndex = Array.from(dropTarget.parentNode.children).indexOf(dropTarget)
    const collection = collections.value[collectionIndex]
    
    if (!collection) {
      ElMessage.warning('集合不存在')
      return
    }
    
    // 添加记录到集合
    await collectionsApi.addRecordToCollection(collection.id, record.id)
    
    // 更新集合记录计数
    collection.recordCount++
    
    ElMessage.success(`已将记录添加到 ${collection.name}`)
  } catch (error) {
    console.error('添加记录到集合失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 更新集合数据的方法，供父组件调用
const updateCollections = async () => {
  await loadCollections()
}

// 组件挂载时加载数据
onMounted(() => {
  loadCollections()
})

// 暴露方法给父组件
defineExpose({
  updateCollections
})
</script>

<style scoped>
.collections-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.bar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: #f0f2f5;
  cursor: pointer;
  user-select: none;
}

.collections-scroll {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  overflow-x: auto;
  max-height: 120px;
  transition: all 0.3s ease;
}

.collections-scroll.expanded {
  max-height: 250px;
  flex-wrap: wrap;
  overflow-y: auto;
}

.collection-drop-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background-color: #fafafa;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  min-width: 200px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.collection-drop-area:hover {
  background-color: #f0f9ff;
  border-style: solid;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.collection-drop-area.drag-over {
  background-color: #ecf5ff;
  border-color: #409eff;
  border-style: solid;
}

.collection-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.collection-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.collection-name {
  font-weight: 500;
  font-size: 14px;
}

.collection-count {
  font-size: 12px;
  color: #909399;
}

.no-collections {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}
</style>