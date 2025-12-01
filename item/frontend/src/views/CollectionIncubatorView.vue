<template>
  <div class="incubator-view">
    <el-card class="incubator-container">
      <!-- 头部信息 -->
      <div class="incubator-header" v-if="collection">
        <div class="collection-info">
          <el-icon :size="48" :style="{ color: collection.color }">
            <component :is="iconMap[collection.icon] || Folder" />
          </el-icon>
          <div class="info-text">
            <h2>{{ collection.name }}</h2>
            <p>{{ collection.description || '暂无描述' }}</p>
          </div>
        </div>
        <el-button type="primary" @click="backToCollections">
          <el-icon><ArrowLeft /></el-icon>
          返回集合列表
        </el-button>
      </div>

      <!-- 孵化动画区域 -->
      <div class="incubator-content" v-if="loading">
        <div class="incubator-animation">
          <div class="incubator-egg" :class="{ 'hatching': progress > 70 }">
            <div class="crack" v-if="progress > 40"></div>
            <div class="crack crack-2" v-if="progress > 60"></div>
          </div>
          <div class="incubator-progress">
            <el-progress 
              type="circle" 
              :percentage="progress" 
              :color="customColorMethod" 
              :width="120"
            />
            <p class="progress-text">{{ progressText }}</p>
          </div>
        </div>
      </div>

      <!-- 孵化完成后的内容区域 -->
      <div class="incubator-results" v-else-if="!loading && collection">
        <h3 class="section-title">集合内容</h3>
        
        <!-- 集合统计 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :xs="24" :sm="12" :md="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon><Document /></el-icon>
                <div>
                  <p class="stat-label">总记录数</p>
                  <p class="stat-value">{{ collection.recordCount || 0 }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon><Clock /></el-icon>
                <div>
                  <p class="stat-label">创建时间</p>
                  <p class="stat-value">{{ formatDate(collection.createdAt) }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-card shadow="hover">
              <div class="stat-item">
                <el-icon><RefreshRight /></el-icon>
                <div>
                  <p class="stat-label">最后更新</p>
                  <p class="stat-value">{{ formatDate(collection.updatedAt || collection.createdAt) }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 集合记录列表 -->
        <div class="records-section">
          <div class="section-header">
            <h4>记录列表</h4>
            <el-button type="primary" @click="showCreateRecordDialog">
              <el-icon><Plus /></el-icon>
              创建记录
            </el-button>
          </div>
          
          <div v-if="records.length === 0" class="empty-records">
            <el-empty description="该集合暂无记录" />
          </div>
          
          <el-table v-else :data="records" style="width: 100%" stripe>
            <el-table-column type="index" label="序号" width="80" />
            <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip />
            <el-table-column prop="contentType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getContentTypeTag(row.contentType)" effect="plain">
                  {{ getContentTypeLabel(row.contentType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </div>
      </div>
    </el-card>
    
    <!-- 创建记录对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新记录"
      width="600px"
    >
      <el-form label-width="80px">
        <el-form-item label="记录标题">
          <el-input v-model="newRecord.title" placeholder="请输入记录标题" />
        </el-form-item>
        <el-form-item label="记录类型">
          <el-radio-group v-model="newRecord.contentType">
            <el-radio label="TEXT">文本</el-radio>
            <el-radio label="LINK">链接</el-radio>
            <el-radio label="IMAGE">图片</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="记录内容">
          <el-input
            v-model="newRecord.content"
            :placeholder="getContentTypePlaceholder"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <!-- 标签输入区域 -->
        <el-form-item label="记录标签">
          <div class="tag-input-container">
            <!-- 已添加的标签 -->
            <el-tag
              v-for="(tag, index) in recordTags"
              :key="index"
              closable
              @close="removeTag(index)"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
            <!-- 标签输入框 -->
            <el-input
              v-model="tagInput"
              placeholder="输入标签并按回车"
              @keyup.enter="addTag"
              class="tag-input"
              size="small"
              clearable
            />
          </div>
          <div class="tag-suggestions" v-if="availableTags.length > 0">
            <span class="suggestion-label">推荐标签：</span>
            <el-tag
              v-for="tag in availableTags"
              :key="tag.id"
              @click="!recordTags.includes(tag.name) && recordTags.push(tag.name)"
              clickable
              size="small"
              class="suggestion-tag"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetForm; showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createRecord">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElDialog } from 'element-plus'
import { 
  Folder, Star, Sugar, CollectionTag, ArrowLeft, 
  Document, Clock, RefreshRight, Plus 
} from '@element-plus/icons-vue'
import collectionsApi from '@/api/collections'
import recordsApi from '@/api/records'
import tagsApi from '@/api/tags'

// 路由相关
const route = useRoute()
const router = useRouter()

// 图标映射
const iconMap = {
  folder: Folder,
  star: Star,
  heart: Sugar,
  bookmark: CollectionTag
}

// 状态定义
const loading = ref(true)
const progress = ref(0)
const collection = ref(null)
const records = ref([])

// 创建记录相关状态
const showCreateDialog = ref(false)
const newRecord = ref({
  title: '',
  content: '',
  contentType: 'TEXT'
})
// 标签相关状态
const recordTags = ref([])
const tagInput = ref('')
const availableTags = ref([]) // 可选标签列表

// 进度条颜色
const customColorMethod = (percentage) => {
  if (percentage < 30) return '#409EFF'
  if (percentage < 70) return '#67C23A'
  return '#F56C6C'
}

// 进度文本
const progressText = computed(() => {
  if (progress.value < 30) return '正在准备孵化...'
  if (progress.value < 70) return '孵化进行中...'
  if (progress.value < 100) return '即将完成孵化...'
  return '孵化完成！'
})

// 模拟孵化进度
const simulateIncubation = () => {
  const timer = setInterval(() => {
    progress.value += Math.random() * 10
    if (progress.value >= 100) {
      progress.value = 100
      clearInterval(timer)
      setTimeout(() => {
        loading.value = false
      }, 1000)
    }
  }, 300)
}

// 加载集合数据
const loadCollectionData = async () => {
  try {
    // 获取集合详情
    const collectionData = await collectionsApi.getCollectionById(route.params.id)
    collection.value = collectionData
    
    // 获取集合下的记录
    // 假设API支持按集合ID查询记录，如果不支持可以先获取所有记录再过滤
    const allRecords = await recordsApi.getAllRecords()
    // 过滤出属于该集合的记录 (这里需要根据实际数据结构调整)
    // 假设记录中有collectionId字段
    records.value = allRecords.filter(record => 
      record.collectionId && record.collectionId === collectionData.id
    )
    
    // 加载所有标签供选择
    loadAvailableTags()
  } catch (error) {
    console.error('加载集合数据失败:', error)
    ElMessage.error('加载集合数据失败')
    router.push('/collections')
  }
}

// 加载可用标签列表
const loadAvailableTags = async () => {
  try {
    const tags = await tagsApi.getAllTags()
    availableTags.value = tags
  } catch (error) {
    console.error('加载标签失败:', error)
    // 不影响主要功能，仅记录错误
  }
}

// 返回集合列表
const backToCollections = () => {
  router.push('/collections')
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取内容类型标签
const getContentTypeTag = (type) => {
  const map = { TEXT: '', LINK: 'success', IMAGE: 'warning' }
  return map[type] || ''
}

// 获取内容类型标签
const getContentTypeLabel = (type) => {
  const map = { TEXT: '文本', LINK: '链接', IMAGE: '图片' }
  return map[type] || type
}

// 获取内容类型占位符
const getContentTypePlaceholder = computed(() => {
  const map = {
    TEXT: '请输入文本内容',
    LINK: '请输入链接地址',
    IMAGE: '请输入图片URL或路径'
  }
  return map[newRecord.value.contentType] || '请输入内容'
})

// 添加标签到记录
const addTag = () => {
  const tagText = tagInput.value.trim()
  if (!tagText) return
  
  // 检查标签是否已存在
  if (recordTags.value.some(tag => tag === tagText)) {
    ElMessage.warning('该标签已添加')
    return
  }
  
  recordTags.value.push(tagText)
  tagInput.value = ''
}

// 移除记录标签
const removeTag = (index) => {
  recordTags.value.splice(index, 1)
}

// 创建记录和关联标签
const createRecord = async () => {
  if (!newRecord.value.title.trim()) {
    ElMessage.warning('请输入记录标题')
    return
  }
  
  try {
    // 确保记录关联到当前集合
    const recordData = {
      ...newRecord.value,
      collectionId: collection.value.id // 确保记录隶属于此集合
    }
    
    // 1. 创建记录
    const createdRecord = await recordsApi.createRecord(recordData)
    const newRecordId = createdRecord.id
    
    // 2. 创建并关联标签到新创建的记录
    if (recordTags.value.length > 0) {
      for (const tagName of recordTags.value) {
        if (!tagName.trim()) continue;
        
        try {
          // 检查标签是否已存在
          const existingTags = await tagsApi.getAllTags();
          let tagId = existingTags.find(t => t.name === tagName)?.id;
          
          // 如果标签不存在，创建新标签并直接关联到记录
          if (!tagId) {
            const newTag = await tagsApi.createTag({
              name: tagName,
              recordId: newRecordId // 确保标签隶属于此记录
            });
            tagId = newTag.id;
          } else {
            // 如果标签已存在，确保它关联到记录
            await tagsApi.associateWithRecord(newRecordId, tagId);
          }
        } catch (tagError) {
          console.error(`处理标签 '${tagName}' 时出错:`, tagError)
          // 继续处理其他标签，不中断整个流程
        }
      }
    }
    
    // 更新本地记录列表
    records.value.push(createdRecord)
    
    // 更新集合记录数量
    if (collection.value) {
      collection.value.recordCount = (collection.value.recordCount || 0) + 1
    }
    
    ElMessage.success('记录和标签创建成功')
    showCreateDialog.value = false
    
    // 重置表单
    resetForm()
  } catch (error) {
    console.error('创建记录失败:', error)
    ElMessage.error('创建记录失败')
  }
}

// 重置表单
const resetForm = () => {
  newRecord.value = {
    title: '',
    content: '',
    contentType: 'TEXT'
  }
  recordTags.value = []
  tagInput.value = ''
}

// 初始化
onMounted(async () => {
  // 开始模拟孵化动画
  simulateIncubation()
  
  // 加载集合数据（模拟网络延迟）
  setTimeout(() => {
    loadCollectionData()
  }, 1000)
})
</script>

<style scoped>
.incubator-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.incubator-container {
  border-radius: 12px;
  overflow: hidden;
}

.incubator-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 30px;
  border-bottom: 2px solid #f0f2f5;
}

.collection-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.info-text h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.info-text p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 孵化动画样式 */
.incubator-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.incubator-animation {
  text-align: center;
}

.incubator-egg {
  position: relative;
  width: 150px;
  height: 180px;
  background: linear-gradient(135deg, #f0e68c 0%, #ffd700 100%);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  margin: 0 auto 30px;
  box-shadow: inset -10px -10px 20px rgba(0, 0, 0, 0.1),
              inset 10px 10px 20px rgba(255, 255, 255, 0.5);
  animation: pulse 2s infinite ease-in-out;
  transition: all 0.5s ease;
}

.incubator-egg.hatching {
  transform: scale(1.1);
  box-shadow: inset -10px -10px 20px rgba(0, 0, 0, 0.1),
              inset 10px 10px 20px rgba(255, 255, 255, 0.5),
              0 0 20px rgba(255, 165, 0, 0.5);
}

.crack {
  position: absolute;
  width: 40px;
  height: 2px;
  background: rgba(0, 0, 0, 0.3);
  top: 40%;
  left: 50%;
  transform: translateX(-50%) rotate(20deg);
  animation: crack 1s infinite alternate;
}

.crack-2 {
  width: 30px;
  top: 50%;
  transform: translateX(-50%) rotate(-15deg);
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes crack {
  0% { width: 40px; }
  100% { width: 50px; }
}

.progress-text {
  margin-top: 20px;
  font-size: 18px;
  color: #606266;
  font-weight: 500;
}

/* 孵化结果样式 */
.incubator-results {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 标签相关样式 */
.tag-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 32px;
}

.tag-item {
  margin-bottom: 4px;
}

.tag-input {
  flex: 1;
  min-width: 120px;
  margin-bottom: 4px;
}

.tag-suggestions {
  margin-top: 12px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-label {
  font-size: 12px;
  color: #606266;
  margin-right: 4px;
}

.suggestion-tag {
  cursor: pointer;
  margin-bottom: 4px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e6f7ff;
}

.stats-row {
  margin-bottom: 30px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-item el-icon {
  font-size: 32px;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 0 0 5px 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.records-section {
  margin-top: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h4 {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.empty-records {
  padding: 60px 0;
}
</style>