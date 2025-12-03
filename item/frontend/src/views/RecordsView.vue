<template>
  <div class="records-view">
    <!-- 工具栏 (保持不变) -->
    <el-card class="toolbar">
      <template #header>
        <div class="toolbar-header">
          <span class="font-bold">记录管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon class="mr-1"><Plus /></el-icon>
            新建记录
          </el-button>
        </div>
      </template>
      <div class="toolbar-content">
        <el-input
          v-model="searchKeyword"
          placeholder="输入关键词并回车..."
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
          @clear="loadRecords"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select 
          v-model="filterType" 
          placeholder="所有类型" 
          style="width: 150px" 
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="文本" value="TEXT" />
          <el-option label="链接" value="LINK" />
          <el-option label="图片" value="IMAGE" />
          <el-option label="视频" value="VIDEO" />
          <el-option label="音频" value="AUDIO" />
          <el-option label="文档" value="DOCUMENT" />
        </el-select>
      </div>
    </el-card>

    <!-- 列表 (保持不变) -->
    <el-card class="records-list">
      <el-table :data="records" style="width: 100%" v-loading="loading" stripe>
        <el-table-column label="ID" width="100" type="index" :index="indexMethod" />
        <el-table-column prop="title" label="标题" min-width="250">
          <template #default="{ row }">
            <div style="white-space: normal;">{{ row.title }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <div class="tags-wrapper">
              <template v-if="(row.tags || []).length > 0">
                <el-tag 
                  v-for="(tag, index) in (row.tags || [])" 
                  :key="tag?.id || index" 
                  size="small"
                  :style="{ backgroundColor: tag?.color || '#409eff', borderColor: tag?.color || '#409eff', color: 'white' }"
                  class="mr-1"
                  v-if="index < 3"
                >
                  {{ tag?.name || '未知标签' }}
                </el-tag>
                <el-tag 
                  v-if="(row.tags || []).length > 3" 
                  size="small" 
                  effect="plain"
                >
                  +{{ (row.tags || []).length - 3 }}
                </el-tag>
              </template>
              <span v-else class="text-gray-500">暂无标签</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="contentType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getContentTypeTag(row.contentType)" effect="plain">
              {{ getContentTypeLabel(row.contentType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="内容" min-width="300">
          <template #default="{ row }">
            <div style="white-space: normal; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">
              {{ row.content || '' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" sortable>
          <template #default="{ row }">
            {{ row.createTime || '暂无时间' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewRecord(row)">查看</el-button>
            <el-button size="small" type="danger" @click="deleteRecord(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建记录"
      width="600px"
      @closed="resetForm" 
    >
      <el-form :model="newRecord" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="newRecord.title" placeholder="请输入标题" />
        </el-form-item>

        <!-- 所属集合（可选） -->
        <el-form-item label="所属集合">
          <el-select 
            v-model="newRecord.collectionId" 
            placeholder="选择所属集合（可选）" 
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="item in collectionOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <!-- ✨✨✨ 结束 ✨✨✨ -->

        <el-form-item label="关联标签">
          <el-select 
            v-model="newRecord.tagIds" 
            multiple 
            placeholder="请选择标签"
            style="width: 100%"
          >
            <el-option
              v-for="item in tagOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span class="flex items-center">
                <span class="color-dot" :style="{ background: item.color }"></span>
                {{ item.name }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="类型">
          <el-select v-model="newRecord.contentType" style="width: 100%">
            <el-option label="文本" value="TEXT" />
            <el-option label="链接" value="LINK" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="音频" value="AUDIO" />
            <el-option label="文档" value="DOCUMENT" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="内容">
          <el-input v-model="newRecord.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createRecord" :loading="creating">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 (保持不变) -->
    <el-dialog v-model="viewDialogVisible" title="记录详情" width="600px">
      <el-descriptions border :column="1">
        <el-descriptions-item label="标题">
          <span class="text-lg font-bold">{{ currentRecord.title }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          {{ getContentTypeLabel(currentRecord.contentType) }}
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap;">{{ currentRecord.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import recordsApi from '@/api/records'
import tagsApi from '@/api/tags'
// ✨✨✨ 1. 引入 Collections API ✨✨✨
import collectionsApi from '@/api/collections'

// 状态定义
const records = ref([])

// 获取路由信息
const route = useRoute()

// 获取全局更新方法
const updateRecords = inject('updateRecords')
const updateCollections = inject('updateCollections')
const loading = ref(false)
const searchKeyword = ref('')
const filterType = ref('')
const createDialogVisible = ref(false)
const creating = ref(false)
const viewDialogVisible = ref(false)
const currentRecord = ref({})

const tagOptions = ref([])
// ✨✨✨ 2. 定义集合列表状态 ✨✨✨
const collectionOptions = ref([])

const newRecord = ref({
  title: '',
  contentType: 'TEXT',
  content: '',
  tagIds: [],
  collectionId: null // ✨✨✨ 3. 初始化 collectionId
})

const getContentTypeLabel = (type) => {
  const map = { TEXT: '文本', LINK: '链接', IMAGE: '图片', VIDEO: '视频', AUDIO: '音频', DOCUMENT: '文档' }
  // 处理空字符串情况
  if (!type) return '未知类型'
  return map[type] || type
}

const indexMethod = (index) => {
  return index + 1
}
const getContentTypeTag = (type) => {
  const map = { TEXT: '', LINK: 'success', IMAGE: 'warning' }
  return map[type] || ''
}

// 加载标签
const loadTags = async () => {
  try {
    const data = await tagsApi.getAllTags()
    tagOptions.value = data 
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// ✨✨✨ 4. 新增：加载所有集合 ✨✨✨
const loadCollections = async () => {
  try {
    // 假设你的 collectionsApi 有 getAllCollections 方法
    const data = await collectionsApi.getAllCollections()
    collectionOptions.value = data
  } catch (error) {
    console.error('加载集合列表失败:', error)
    ElMessage.warning('无法加载集合列表')
  }
}

// 加载记录
const loadRecords = async () => {
  loading.value = true
  try {
    const data = await recordsApi.getAllRecords()
    console.log('获取到的记录数据:', data)
    records.value = data 
    console.log('records数组长度:', records.value.length)
  } catch (error) {
    console.error('获取记录数据失败:', error)
    console.error('错误详情:', JSON.stringify(error))
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadRecords()
    return
  }
  loading.value = true
  try {
    const data = await recordsApi.searchRecords(searchKeyword.value)
    records.value = data
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 修改创建逻辑：集合不再是必填项
const createRecord = async () => {
  if (!newRecord.value.title.trim()) return ElMessage.warning('请输入标题')

  creating.value = true
  try {
    const payload = {
      title: newRecord.value.title,
      contentType: newRecord.value.contentType,
      content: newRecord.value.content,
      tagIds: newRecord.value.tagIds
    }

    // 创建记录
    const createdRecord = await recordsApi.createRecord(payload)
    
    // 如果用户选择了集合，建立多对多关系
    if (newRecord.value.collectionId) {
      await collectionsApi.addRecordToCollection(newRecord.value.collectionId, createdRecord.id)
      // 更新集合数据
      if (updateCollections) updateCollections()
    }
    
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadRecords()
    
    // 更新悬浮记录
    if (updateRecords) updateRecords()
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const deleteRecord = (record) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await recordsApi.deleteRecord(record.id)
        ElMessage.success('删除成功')
        loadRecords()
        
        // 更新悬浮记录
        if (updateRecords) updateRecords()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const showCreateDialog = () => { createDialogVisible.value = true }

// ✨✨✨ 6. 重置表单时也要清空 collectionId ✨✨✨
const resetForm = () => { 
  newRecord.value = { 
    title: '', 
    contentType: 'TEXT', 
    content: '', 
    tagIds: [],
    collectionId: null 
  } 
}

const viewRecord = (row) => { currentRecord.value = { ...row }; viewDialogVisible.value = true }

// 检查并应用URL参数中的筛选条件
const checkFilterParams = () => {
  if (route.query.filter) {
    try {
      const filterParams = JSON.parse(decodeURIComponent(route.query.filter))
      console.log('从URL参数获取到的筛选条件:', filterParams)
      if (filterParams.keyword) {
        searchKeyword.value = filterParams.keyword
        handleSearch()
      }
    } catch (error) {
      console.error('解析筛选参数失败:', error)
    }
  }
}

// 初始化
onMounted(() => {
  loadRecords()
  loadTags()
  loadCollections() // ✨✨✨ 7. 页面加载时获取集合列表
  
  // 检查URL参数中的筛选条件
  checkFilterParams()
})
</script>

<style scoped>
/* 保持样式不变 */
.records-view { max-width: 1200px; margin: 0 auto; }
.toolbar { margin-bottom: 20px; }
.toolbar-header { display: flex; justify-content: space-between; align-items: center; }
.toolbar-content { margin-top: 15px; }
.font-bold { font-weight: bold; font-size: 16px; }
.mr-1 { margin-right: 4px; }
.tags-wrapper { display: flex; flex-wrap: wrap; gap: 4px; }
.color-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 8px; }
</style>