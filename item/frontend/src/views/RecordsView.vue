<template>
  <div class="records-view">
    <!-- 工具栏 -->
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
        <!-- 🟢 搜索改为按下回车或点击清除时触发，避免频繁请求 -->
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

        <!--类型筛选-->
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

    <!-- 列表 -->
    <el-card class="records-list">
      <el-table :data="records" style="width: 100%" v-loading="loading" stripe>
        <el-table-column label="ID" width="100" type="index" :index="indexMethod" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        
        <!-- 标签列 -->
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <div class="tags-wrapper">
              <!-- 后端返回的 tags 数组 -->
              <el-tag 
                v-for="tag in row.tags" 
                :key="tag.id" 
                size="small"
                :style="{ backgroundColor: tag.color, borderColor: tag.color, color: 'white' }"
                class="mr-1"
              >
                {{ tag.name }}
              </el-tag>
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
        
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable />
        
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

        <el-form-item label="关联标签">
          <el-select 
            v-model="newRecord.tagIds" 
            multiple 
            placeholder="请选择标签"
            style="width: 100%"
          >
            <!-- 注意：这里暂时使用本地 options，如果有 tagsApi，应该在 onMounted 里获取 -->
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

    <!-- 查看详情弹窗 -->
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
// 🟢 1. 引入你的 API 文件
import recordsApi from '@/api/records'

// 状态定义
const records = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const filterType = ref('')
const createDialogVisible = ref(false)
const creating = ref(false)
const viewDialogVisible = ref(false)
const currentRecord = ref({})

// 暂时硬编码的标签选项（真实项目应该调用 tagsApi.getAllTags()）
const tagOptions = ref([
  { id: 1, name: '工作', color: '#409EFF' },
  { id: 2, name: '生活', color: '#67C23A' },
  { id: 3, name: '学习', color: '#E6A23C' }
])

const newRecord = ref({
  title: '',
  contentType: 'TEXT',
  content: '',
  tagIds: []
})

// 辅助函数
const getContentTypeLabel = (type) => {
  const map = { TEXT: '文本', LINK: '链接', IMAGE: '图片' }
  return map[type] || type
}

// 序号计算函数 - 从1开始
const indexMethod = (index) => {
  return index + 1
}
const getContentTypeTag = (type) => {
  const map = { TEXT: '', LINK: 'success', IMAGE: 'warning' }
  return map[type] || ''
}

// === 🟢 核心逻辑：全部替换为 API 调用 ===

// 1. 加载数据
const loadRecords = async () => {
  loading.value = true
  try {
    console.log('开始获取记录数据...')
    // 调用 getAllRecords，http.js 会处理 baseURL
    const data = await recordsApi.getAllRecords()
    console.log('获取记录数据成功:', data)
    // 假设后端直接返回数组，如果后端返回 { code: 200, data: [...] }，这里要改成 data.data
    records.value = data 
    console.log('记录数据已更新:', records.value)
  } catch (error) {
    console.error('获取记录数据失败:', error)
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

// 2. 搜索
const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadRecords()
    return
  }
  loading.value = true
  try {
    // 调用 records.js 里的 searchRecords
    const data = await recordsApi.searchRecords(searchKeyword.value)
    records.value = data
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 3. 创建记录
const createRecord = async () => {
  if (!newRecord.value.title.trim()) return ElMessage.warning('请输入标题')

  creating.value = true
  try {
    // 构造发送给后端的数据
    const payload = {
      title: newRecord.value.title,
      contentType: newRecord.value.contentType,
      content: newRecord.value.content,
      tagIds: newRecord.value.tagIds // 发送 ID 数组给后端
    }

    // 发送请求 POST /records
    await recordsApi.createRecord(payload)
    
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    // 创建成功后，重新加载列表，看到最新数据
    loadRecords()
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

// 4. 删除记录
const deleteRecord = (record) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
    .then(async () => {
      try {
        // 发送请求 DELETE /records/:id
        await recordsApi.deleteRecord(record.id)
        ElMessage.success('删除成功')
        // 重新刷新列表
        loadRecords()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const showCreateDialog = () => { createDialogVisible.value = true }
const resetForm = () => { newRecord.value = { title: '', contentType: 'TEXT', content: '', tagIds: [] } }
const viewRecord = (row) => { currentRecord.value = { ...row }; viewDialogVisible.value = true }

// 初始化
onMounted(() => {
  loadRecords()
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