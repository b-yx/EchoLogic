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
            <el-button type="primary" @click="showCreateDialog=true">
              <el-icon><Plus /></el-icon>
              创建记录
            </el-button>
          </div>
          
          <div v-if="records.length === 0" class="empty-records">
            <el-empty description="该集合暂无记录" />
          </div>
          
          <el-table v-else :data="records" style="width: 100%" stripe>
            <el-table-column type="index" label="#" width="60" align="center" />

            <el-table-column prop="title" label="标题" min-width="300" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="font-bold">{{ row.title }}</span>
              </template>
            </el-table-column>

            <el-table-column label="标签" width="250">
              <template #default="{ row }">
                <div class="tags-wrapper" v-if="row.tags && row.tags.length">
                  <el-tag 
                    v-for="tag in row.tags" 
                    :key="tag.id" 
                    size="small"
                    :style="getTagStyle(tag)"
                  >
                    {{ tag.name }}
                  </el-tag>
                </div>
                <span v-else class="text-gray-400 text-xs">无标签</span>
              </template>
            </el-table-column>

            <el-table-column prop="contentType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getContentTypeTag(row.contentType)" effect="plain">
                  {{ getContentTypeLabel(row.contentType) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="createdAt" label="创建时间" width="180" >
            <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleView(row)">
                  查看
                </el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空状态 -->
          <el-empty v-if="records.length === 0" description="还没孵化出任何记录呢~" />

        </div>
      </div>
    </el-card>
    
    <!-- 创建记录对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新记录"
      width="600px"
      @closed="resetForm"
    >
      <el-form label-width="80px">
        <el-form-item label="记录标题">
          <el-input v-model="newRecord.title" placeholder="请输入记录标题" />
        </el-form-item>
        <el-form-item label="记录类型">
          <el-select v-model="newRecord.contentType" style="width: 100%">
            <el-option label="文本" value="TEXT" />
            <el-option label="链接" value="LINK" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="音频" value="AUDIO" />
            <el-option label="文档" value="DOCUMENT" />
          </el-select>
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
        <el-form-item label="标签">
          <div class="tag-input-wrapper">
            <!-- 使用 el-select 替代原来的 input -->
            <el-select
              v-model="recordTagNames"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入新标签"
              style="width: 100%"
            >
              <el-option
                v-for="item in availableTags"
                :key="item.id"
                :label="item.name"
                :value="item.name"
              >
                <!-- 下拉框中显示颜色圆点 -->
                <span class="flex items-center">
                  <span 
                    class="color-dot" 
                    :style="{ 
                      display: 'inline-block', 
                      width: '8px', 
                      height: '8px', 
                      borderRadius: '50%', 
                      marginRight: '8px',
                      backgroundColor: item.color || '#409EFF' 
                    }"
                  ></span>
                  {{ item.name }}
                </span>
              </el-option>
            </el-select>

            <!-- 保留推荐标签功能（可选，点击快速添加到上面的选择框中） -->
            <div class="suggestions" v-if="availableTags.length > 0">
              <span class="text-xs text-gray-400 mr-2" style="margin-top: 8px; display: inline-block;">推荐:</span>
              <el-tag
                v-for="tag in availableTags.slice(0, 5)"
                :key="tag.id"
                size="small"
                class="cursor-pointer mr-1 hover-effect"
                :style="{ color: tag.color, borderColor: tag.color, marginTop: '8px' }"
                effect="plain"
                @click="selectSuggestion(tag)"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createRecord">创建</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog v-model="viewDialogVisible" title="记录详情" width="600px">
      <el-descriptions border :column="1">
        <el-descriptions-item label="标题">
          <span class="font-bold">{{ currentRecord.title }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getContentTypeTag(currentRecord.contentType)" size="small">
            {{ getContentTypeLabel(currentRecord.contentType) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="标签">
          <div v-if="currentRecord.tags && currentRecord.tags.length">
            <el-tag 
              v-for="tag in currentRecord.tags" 
              :key="tag.id" 
              size="small" 
              class="mr-1"
              :style="getTagStyle(tag)"
            >
              {{ tag.name }}
            </el-tag>
          </div>
          <span v-else>无</span>
        </el-descriptions-item>

        <el-descriptions-item label="创建时间">
          {{ formatDate(currentRecord.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap; line-height: 1.6;">{{ currentRecord.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElDialog, ElMessageBox } from 'element-plus'
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
const creating = ref(false)
const progress = ref(0)
const collection = ref(null)
const records = ref([])
const availableTags = ref([])

// 创建记录相关状态
const showCreateDialog = ref(false)
const newRecord = ref({
  title: '',
  content: '',
  contentType: 'TEXT'
})
// 标签相关状态
const recordTagNames = ref([])

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

// 加载集合数据
const loadCollectionData = async () => {
  try {
    const id = route.params.id
    
    // 1. 并行请求：集合详情、当前集合的记录、所有标签
    const [colRes, recRes, tagRes] = await Promise.all([
      collectionsApi.getCollectionById(id),
      recordsApi.getRecordsByCollectionId(id), // ✨ 直接获取该集合下的记录，精准且快
      tagsApi.getAllTags()
    ])

    collection.value = colRes
    
    // 2. 直接赋值
    records.value = recRes 
    
    availableTags.value = tagRes

  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败')
    router.push('/collections')
  } finally {
  }
}

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
const selectSuggestion = (tagObj) => {
  if (!recordTagNames.value.includes(tagObj.name)) {
    recordTagNames.value.push(tagObj.name)
  }
}

// 创建记录
const createRecord = async () => {
  if (!newRecord.value.title.trim()) return ElMessage.warning('请输入标题')

  creating.value = true
  try {
    //创建记录主体（不包含collectionId，因为现在是多对多关系）
    const recordPayload = {
      ...newRecord.value
    }
    const createdRecord = await recordsApi.createRecord(recordPayload)
    
    // 通过多对多关系API将记录添加到集合
    await collectionsApi.addRecordToCollection(collection.value.id, createdRecord.id)
    
    // 处理标签 (关联旧标签 或 创建新标签)
    const finalTags = []

    if (recordTagNames.value.length > 0) {
      // 获取最新标签库，防止 ID 冲突（也可以直接用 availableTags）
      const currentAllTags = await tagsApi.getAllTags()
      
      for (const name of recordTagNames.value) {
        // 查找是否已存在
        const existTag = currentAllTags.find(t => t.name === name)
        
        if (existTag) {
          // 存在 -> 关联
          await tagsApi.associateWithRecord(createdRecord.id, existTag.id)
          finalTags.push(existTag)
        } else {
          // 不存在 -> 创建新标签 (后端通常会自动处理关联，或者我们传入 recordId)
          const newTagRes = await tagsApi.createTag({ 
            name, 
            recordId: createdRecord.id // 假设后端支持创建时直接关联
          })
          finalTags.push(newTagRes)
        }
      }
    }

    // C. 更新前端列表 (手动组合数据，避免刷新页面)
    const recordForDisplay = {
      ...createdRecord,
      tags: finalTags // 将处理好的标签数组挂载上去
    }
    
    records.value.unshift(recordForDisplay) // 添加到列表顶部
    ElMessage.success('创建成功！')
    showCreateDialog.value = false
    
    // 刷新一下推荐标签列表，因为可能有新创建的
    loadAvailableTags() 

  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
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

// 重置表单
const resetForm = () => {
  newRecord.value = {
    title: '',
    content: '',
    contentType: 'TEXT'
  }
  recordTagNames.value = []
}

const viewDialogVisible = ref(false)
const currentRecord = ref({})

//查看函数
const handleView = (row) => {
  currentRecord.value = { ...row } 
  viewDialogVisible.value = true
}

// 删除处理函数
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要将这条记录从集合中移除吗？',
    '警告',
    {
      confirmButtonText: '确定移除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await collectionsApi.removeRecordFromCollection(collection.value.id, row.id)
        ElMessage.success('移除成功')
        // 重新加载数据
        loadCollectionData()
      } catch (error) {
        console.error(error)
        ElMessage.error('移除失败')
      }
    })
    .catch(() => {
      // 取消移除，不做任何操作
    })
}

//标签颜色
const defaultColors = [
  '#409EFF', // 蓝
  '#67C23A', // 绿
  '#E6A23C', // 黄
  '#F56C6C', // 红
  '#909399', // 灰
  '#a29bfe', // 紫
  '#00cec9', // 青
  '#fd79a8'  // 粉
]

// 2. 获取标签样式的辅助函数
const getTagStyle = (tag) => {
  let color = tag.color
  
  // 如果后端没返回颜色，根据标签名的长度取一个预设颜色
  if (!color && tag.name) {
    const index = tag.name.length % defaultColors.length
    color = defaultColors[index]
  }

  // 如果还是没有，默认用蓝色
  if (!color) color = '#409EFF'

  return {
    backgroundColor: color,
    borderColor: color,
    color: '#fff', // 白字
    marginRight: '4px',
    marginBottom: '4px'
  }
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