<template>
  <div class="home-view">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <el-statistic title="总记录数" :value="stats.totalRecords">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <el-statistic title="标签数量" :value="stats.totalTags">
            <template #prefix>
              <el-icon><PriceTag /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <el-statistic title="集合数量" :value="stats.totalCollections">
            <template #prefix>
              <el-icon><Folder /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card">
      <template #header>
        <span>欢迎使用 EchoLogic</span>
      </template>
      <div class="welcome-content">
        <h3>个人知识管理系统</h3>
        <p>快速保存、整理和检索您的知识内容</p>
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="$router.push('/records')" style="margin-right: 10px;">
            开始使用
          </el-button>
          <el-button type="success" @click="showUrlImportDialog">
            <el-icon><Link /></el-icon>
            从URL导入
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 从URL导入对话框 -->
    <el-dialog
      v-model="showImportDialog"
      title="从URL导入内容"
      width="600px"
      @closed="resetUrlImport"
    >
      <el-form label-width="80px">
        <el-form-item label="URL地址">
          <el-input
            v-model="urlImport.url"
            placeholder="请输入网页URL地址"
            :disabled="urlImport.loading"
            suffix-icon="Link"
          >
            <template #append>
              <el-button 
                type="primary" 
                @click="importFromUrl" 
                :loading="urlImport.loading"
              >
                导入
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="选择集合（可选）">
          <el-select v-model="urlImport.collectionId" placeholder="选择要保存的集合（可选）" style="width: 100%">
            <el-option
              v-for="collection in collections"
              :key="collection.id"
              :label="collection.name"
              :value="collection.id"
            >
              <span style="float: left;">{{ collection.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px;">{{ collection.recordCount }} 个记录</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="导入结果" v-if="urlImport.result">
          <el-card shadow="hover">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="标题">
                {{ urlImport.result.title }}
              </el-descriptions-item>
              <el-descriptions-item label="内容摘要">
                <div style="white-space: pre-wrap; line-height: 1.6; max-height: 200px; overflow-y: auto;">
                  {{ urlImport.result.parsedText || '无摘要' }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="自动标签">
                <div>
                  <el-tag 
                    v-for="tag in urlImport.result.tags" 
                    :key="tag" 
                    size="small" 
                    class="mr-1"
                  >
                    {{ tag }}
                  </el-tag>
                  <span v-if="!urlImport.result.tags || urlImport.result.tags.length === 0">无标签</span>
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showImportDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="createRecordFromUrl" 
            :loading="creating"
            :disabled="!urlImport.result"
          >
            保存记录
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, PriceTag, Folder, Link } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import recordsApi from '@/api/records'
import tagsApi from '@/api/tags'
import collectionsApi from '@/api/collections'
import contentApi from '@/api/content'

const stats = ref({
  totalRecords: 0,
  totalTags: 0,
  totalCollections: 0
})

// 集合数据
const collections = ref([])

// URL导入相关状态
const showImportDialog = ref(false)
const creating = ref(false)
const urlImport = ref({
  url: '',
  collectionId: '',
  loading: false,
  result: null
})

onMounted(async () => {
  try {
    // 获取记录数量
    const records = await recordsApi.getAllRecords()
    stats.value.totalRecords = records.length
    
    // 获取标签数量
    const tags = await tagsApi.getAllTags()
    stats.value.totalTags = tags.length
    
    // 获取集合数据
    const collectionsData = await collectionsApi.getAllCollections()
    collections.value = collectionsData
    stats.value.totalCollections = collectionsData.length
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
})



// 显示URL导入对话框
const showUrlImportDialog = () => {
  showImportDialog.value = true
}

// 重置URL导入状态
const resetUrlImport = () => {
  urlImport.value = {
    url: '',
    collectionId: '',
    loading: false,
    result: null
  }
  creating.value = false
}

// 从URL导入
const importFromUrl = async () => {
  if (!urlImport.value.url.trim()) {
    return ElMessage.warning('请输入URL地址')
  }
  
  urlImport.value.loading = true
  try {
    // 传递完整的请求对象，包括URL和可选的集合ID
    // 确保当用户没有选择任何集合时，传递的是null，而不是空字符串
    const result = await contentApi.parseUrl({
      url: urlImport.value.url,
      collectionId: urlImport.value.collectionId || null
    })
    // 将后端返回的逗号分隔标签字符串转换为数组
    if (result.tags && typeof result.tags === 'string') {
      result.tags = result.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
    }
    urlImport.value.result = result
    ElMessage.success('导入成功！')
  } catch (error) {
    console.error('从URL导入失败:', error)
    ElMessage.error('导入失败，请检查URL是否正确或稍后重试')
  } finally {
    urlImport.value.loading = false
  }
}

// 从URL结果创建记录
const createRecordFromUrl = async () => {
  if (!urlImport.value.result) {
    return ElMessage.warning('请先导入URL')
  }
  
  creating.value = true
  try {
    const { title, parsedText, tags } = urlImport.value.result
    let createdRecord = null
    
    // 单独处理记录创建，确保即使后续步骤失败也能正确反馈
    try {
      // 创建记录主体
      const recordPayload = {
        title: title,
        content: parsedText,
        contentType: 'TEXT'
      }
      createdRecord = await recordsApi.createRecord(recordPayload)
    } catch (error) {
      console.error('创建记录失败:', error)
      ElMessage.error('保存失败')
      return
    }
    
    // 处理可选的集合关联和标签
    try {
      // 如果选择了集合，则将记录添加到集合
      if (urlImport.value.collectionId) {
        await collectionsApi.addRecordToCollection(
          urlImport.value.collectionId,
          createdRecord.id
        )
      }
      
      // 处理标签
      if (tags && tags.length > 0) {
        const currentAllTags = await tagsApi.getAllTags()
        
        for (const name of tags) {
          // 查找是否已存在
          const existTag = currentAllTags.find(t => t.name === name)
          
          if (existTag) {
            // 存在 -> 关联
            await tagsApi.associateWithRecord(createdRecord.id, existTag.id)
          } else {
            // 不存在 -> 创建新标签
            await tagsApi.createTag({ 
              name, 
              recordId: createdRecord.id 
            })
          }
        }
      }
      
      // 更新统计数据
      stats.value.totalRecords += 1
      // 如果选择了集合，更新集合记录数
      if (urlImport.value.collectionId) {
        const collectionIndex = collections.value.findIndex(c => c.id === urlImport.value.collectionId)
        if (collectionIndex !== -1) {
          collections.value[collectionIndex].recordCount += 1
        }
      }
      
      ElMessage.success('保存成功！')
    } catch (error) {
      console.error('记录关联操作失败:', error)
      // 记录已保存，只提示关联操作可能失败
      ElMessage.warning('记录保存成功，但部分关联操作失败')
    }
    
    showImportDialog.value = false
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-card {
  text-align: center;
}

.welcome-card {
  margin-top: 20px;
}

.welcome-content {
  text-align: center;
  padding: 40px 0;
}

.welcome-content h3 {
  font-size: 24px;
  margin-bottom: 10px;
  color: #303133;
}

.welcome-content p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 20px;
}
</style>
