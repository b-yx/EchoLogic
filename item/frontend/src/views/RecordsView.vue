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


      </div>
    </el-card>

    <!-- 列表 (保持不变) -->
    <el-card class="records-list">
      <el-table :data="records" style="width: 100%" v-loading="loading" stripe>
        <el-table-column label="ID" width="80" type="index" :index="indexMethod" align="center" />
        <el-table-column prop="title" label="标题" min-width="280" align="left">
          <template #default="{ row }">
            <div style="white-space: normal; padding: 4px 0;">{{ row.title }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="标签" min-width="200" align="left">
          <template #default="{ row }">
            <div class="tags-wrapper">
              <template v-if="(row.tags || []).length > 0">
                <el-tag 
                  v-for="(tag, index) in (row.tags || [])" 
                  :key="index" 
                  size="small"
                  :style="{ backgroundColor: tag?.color || '#409eff', borderColor: tag?.color || '#409eff', color: 'white' }"
                  class="mr-1"
                >
                  {{ tag?.name || `标签${index+1}` }}
                </el-tag>
              </template>
              <span v-else class="text-gray-500">暂无标签</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="contentType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getContentTypeTag(row.contentType)" effect="plain">
              {{ getContentTypeLabel(row.contentType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="内容" min-width="350" align="left">
          <template #default="{ row }">
            <div style="white-space: normal; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; padding: 4px 0;">
              {{ row.content || '' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160" sortable align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="160" fixed="right" align="center">
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
            placeholder="请选择标签或直接输入新标签"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
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
          <div class="text-xs text-gray-500 mt-1">
            提示：直接输入标签内容即可创建新标签，系统会自动查重
          </div>
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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '暂无时间'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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
    // 调试标签数据
    if (data.length > 0) {
      const firstRecord = data[0]
      console.log('第一条记录的标签:', firstRecord.tags)
      if (firstRecord.tags && firstRecord.tags.length > 0) {
        console.log('标签数据格式:', firstRecord.tags[0])
      }
    }
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

// 修改创建逻辑：支持新增标签和集合关联
const createRecord = async () => {
  if (!newRecord.value.title.trim()) return ElMessage.warning('请输入标题')

  creating.value = true
  try {
    // 处理标签：区分已存在的标签ID和新创建的标签名称
    const existingTagIds = []
    const newTagNames = []
    
    newRecord.value.tagIds.forEach(tagValue => {
      if (typeof tagValue === 'number') {
        // 已存在的标签ID
        existingTagIds.push(tagValue)
      } else if (typeof tagValue === 'string' && tagValue.trim()) {
        // 新创建的标签名称
        newTagNames.push(tagValue.trim())
      }
    })

    // 创建记录（只包含已存在的标签ID）
    const payload = {
      title: newRecord.value.title,
      contentType: newRecord.value.contentType,
      content: newRecord.value.content,
      tagIds: existingTagIds
    }

    // 创建记录
    const createdRecord = await recordsApi.createRecord(payload)
    
    // 处理新标签的创建
    if (newTagNames.length > 0) {
      // 去重
      const uniqueNewTagNames = [...new Set(newTagNames)]
      
      // 创建新标签并关联到记录
      for (const tagName of uniqueNewTagNames) {
        try {
          // 创建新标签
          const newTag = await tagsApi.createTag({ name: tagName, color: `#${Math.floor(Math.random()*16777215).toString(16)}` })
          // 关联到记录
          await tagsApi.associateWithRecord(createdRecord.id, newTag.id)
        } catch (error) {
          console.error(`创建标签失败: ${tagName}`, error)
          // 继续处理其他标签，不中断整个流程
        }
      }
    }
    
    // 如果用户选择了集合，建立多对多关系
    if (newRecord.value.collectionId) {
      await collectionsApi.addRecordToCollection(newRecord.value.collectionId, createdRecord.id)
      // 更新集合数据
      if (updateCollections) updateCollections()
    }
    
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadRecords()
    loadTags() // 刷新标签列表，包含新创建的标签
    
    // 更新悬浮记录
    if (updateRecords) updateRecords()
  } catch (error) {
    console.error('创建记录失败:', error)
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
/* 优化信息间隔，使页面更美观 */
.records-view { max-width: 1200px; margin: 0 auto; padding: 20px 0; }
.toolbar { margin-bottom: 24px; }
.toolbar-header { display: flex; justify-content: space-between; align-items: center; }
.toolbar-content { margin-top: 15px; }
.font-bold { font-weight: bold; font-size: 16px; }
.mr-1 { margin-right: 8px; }
.tags-wrapper { display: flex; flex-wrap: wrap; gap: 8px; padding: 4px 0; }
.color-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 8px; }
.text-gray-500 { color: #909399; }

/* 优化表格样式 */
:deep(.el-table__row) { height: 70px; /* 增加行高，提升可读性 */ }
:deep(.el-table__cell) { 
  padding: 12px 20px; /* 增大单元格内边距，增加列之间的间隔 */
  vertical-align: middle; /* 内容垂直居中 */
}
:deep(.el-table__header-wrapper .el-table__cell) { 
  padding: 12px 20px; /* 表头内边距与内容一致 */
  background-color: #f5f7fa; /* 表头背景色 */
  font-weight: bold; /* 表头字体加粗 */
}
:deep(.el-button--small) { margin: 0 6px; /* 按钮间距 */ }

/* 优化标签样式 */
:deep(.el-tag--small) { margin-bottom: 4px; /* 标签垂直间距 */ }

/* 优化内容显示 */
:deep(.el-table__body-wrapper) { font-size: 14px; /* 统一字体大小 */ }

/* 优化表格边框和阴影 */
:deep(.el-table) { border-radius: 8px; overflow: hidden; }
:deep(.el-table__inner-wrapper) { border-radius: 8px; }
</style>