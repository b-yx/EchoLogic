<template>
  <div class="collections-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="font-bold">集合管理</span>
          <div class="header-right flex gap-4">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索集合..."
              style="width: 200px"
              clearable
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon class="mr-1"><Plus /></el-icon>
              新建集合
            </el-button>
            <el-button type="success" @click="showAIDialog">
              <el-icon class="mr-1"><ChatRound /></el-icon>
              AI生成集合
            </el-button>
            <el-button 
              type="warning" 
              @click="showMergeDialog" 
              :disabled="selectedCollections.length < 2"
            >
              <el-icon class="mr-1"><RefreshRight /></el-icon>
              合并集合 ({{ selectedCollections.length }})
            </el-button>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-if="!loading && collections.length === 0" description="暂无集合" />

      <!-- 集合列表 -->
      <el-row :gutter="20" v-else>
        <el-col
          v-for="collection in filteredCollections"
          :key="collection.id"
          :xs="24" :sm="12" :md="8"
        >
          <el-card 
            class="collection-card" 
            shadow="hover" 
            @click="viewCollection(collection)"
          >
            <div class="collection-item">
              <div class="collection-select">
                <el-checkbox 
                  :checked="isSelected(collection.id)" 
                  @change="toggleSelect(collection)"
                  @click.stop
                />
              </div>
              <div class="collection-icon">
                <el-icon :size="40" :style="{ color: collection.color }">
                  <!-- 使用 component 动态渲染图标组件 -->
                  <component :is="iconMap[collection.icon] || Folder" />
                </el-icon>
              </div>
              
              <h3>{{ collection.name }}</h3>
              <p class="description">{{ collection.description || '暂无描述' }}</p>
              
              <div class="collection-stats">
                <span>{{ collection.recordCount }}</span>
              </div>
              
              <div class="collection-actions">
                <el-button size="small" @click.stop="editCollection(collection)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click.stop="deleteCollection(collection)">
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑集合' : '新建集合'"
      width="500px"
      @closed="resetForm"
    >
      <el-form :model="collectionForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="collectionForm.name" placeholder="请输入集合名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="collectionForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="简短描述这个集合..."
          />
        </el-form-item>
        <el-form-item label="颜色" v-if="!isEdit">
          <el-color-picker v-model="collectionForm.color" show-alpha predefine />
        </el-form-item>
        <el-form-item label="图标" v-if="!isEdit">
          <el-select v-model="collectionForm.icon" placeholder="选择图标" style="width: 100%">
            <el-option label="文件夹" value="folder">
              <span style="float: left">文件夹</span>
              <el-icon style="float: right; color: #8492a6"><Folder /></el-icon>
            </el-option>
            <el-option label="星形" value="star">
              <span style="float: left">星形</span>
              <el-icon style="float: right; color: #8492a6"><Star /></el-icon>
            </el-option>
            <el-option label="爱心" value="heart">
              <span style="float: left">爱心</span>
              <el-icon style="float: right; color: #8492a6"><Sugar /></el-icon>
            </el-option>
            <el-option label="标签" value="bookmark">
              <span style="float: left">标签</span>
              <el-icon style="float: right; color: #8492a6"><CollectionTag /></el-icon>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCollection" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- AI生成集合弹窗 -->
    <el-dialog
      v-model="aiDialogVisible"
      title="AI生成集合"
      width="600px"
      @closed="resetAIForm"
    >
      <el-form :model="aiForm" label-width="80px">
        <el-form-item label="描述" required>
          <el-input 
            v-model="aiForm.description" 
            type="textarea" 
            :rows="5" 
            placeholder="请描述你想要创建的集合，例如：'所有关于机器学习的笔记'或'我上周创建的会议记录'"
          />
        </el-form-item>
        <el-alert
          title="AI将根据你的描述自动筛选相关记录并创建集合"
          type="info"
          :closable="false"
          class="mt-4"
        />
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="success" @click="generateCollectionWithAI" :loading="aiGenerating">
          <el-icon class="mr-1"><MagicStick /></el-icon>
          开始生成
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 合并集合弹窗 -->
    <el-dialog
      v-model="mergeDialogVisible"
      title="合并集合"
      width="500px"
      @closed="() => selectedCollections.value = []"
    >
      <el-form :model="mergeForm" label-width="80px">
        <el-form-item label="选择目标集合">
          <el-select v-model="targetCollection" placeholder="请选择目标集合" style="width: 100%" @change="updateMergeForm">
            <el-option
              v-for="collection in selectedCollections"
              :key="collection.id"
              :label="collection.name"
              :value="collection"
            >
              {{ collection.name }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="源集合">
          <div class="source-collections">
            <el-tag 
              v-for="collection in selectedCollections.filter(c => c.id !== targetCollection?.id)"
              :key="collection.id"
              closable
              @close="toggleSelect(collection)"
            >
              {{ collection.name }}
            </el-tag>
            <span v-if="selectedCollections.filter(c => c.id !== targetCollection?.id).length === 0" class="text-gray-500">
              无
            </span>
          </div>
        </el-form-item>
        <el-form-item label="合并后名称">
          <el-input v-model="mergeForm.name" placeholder="请输入合并后的集合名称" style="width: 100%" />
        </el-form-item>
        <el-form-item label="合并后描述">
          <el-input v-model="mergeForm.description" type="textarea" rows="3" placeholder="请输入合并后的集合描述" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mergeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="mergeCollections">
              <el-icon class="mr-1"><RefreshRight /></el-icon>
              合并
            </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef, inject, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus, Folder, Star, Sugar, CollectionTag, ChatRound, MagicStick, RefreshRight } from '@element-plus/icons-vue'
import collectionsApi from '@/api/collections'

// 使用 shallowRef 避免不必要的性能开销
const iconMap = {
  folder: Folder,
  star: Star,
  heart: Sugar, 
  bookmark: CollectionTag
}

// 路由
const router = useRouter()

// 获取全局更新方法
const updateCollections = inject('updateCollections')

// 状态定义 ===
const collections = ref([])
const loading = ref(false)
const searchKeyword = ref('')

// 搜索过滤
const filteredCollections = computed(() => {
  if (!searchKeyword.value.trim()) {
    return collections.value
  }
  const keyword = searchKeyword.value.toLowerCase().trim()
  return collections.value.filter(collection => 
    collection.name.toLowerCase().includes(keyword) || 
    (collection.description && collection.description.toLowerCase().includes(keyword))
  )
})
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const aiDialogVisible = ref(false)
const aiGenerating = ref(false)
const mergeDialogVisible = ref(false)
const selectedCollections = ref([])
const targetCollection = ref(null)
const mergeForm = ref({
  name: '',
  description: ''
})

// AI生成集合的表单
const aiForm = ref({
  description: ''
})

const collectionForm = ref({
  id: null,
  name: '',
  description: '',
  color: '#409EFF',
  icon: 'folder'
})

// 1. 加载集合 
const loadCollections = async () => {
  loading.value = true
  try {
    const data = await collectionsApi.getAllCollections()
    collections.value = data
  } catch (error) {
    ElMessage.error('加载集合失败')
  } finally {
    loading.value = false
  }
}

// 2. 打开新建弹窗
const showCreateDialog = () => {
  isEdit.value = false
  collectionForm.value = { 
    id: null, 
    name: '', 
    description: '', 
    color: '#409EFF', 
    icon: 'folder' 
  }
  dialogVisible.value = true
}

// 3. 打开编辑弹窗
const editCollection = (collection) => {
  isEdit.value = true
  collectionForm.value = { ...collection }
  dialogVisible.value = true
}

const resetForm = () => {
  collectionForm.value = { id: null, name: '', description: '', color: '#409EFF', icon: 'folder' }
}

// 重置AI表单
const resetAIForm = () => {
  aiForm.value = { description: '' }
}

// 4. 保存集合 
const saveCollection = async () => {
  if (!collectionForm.value.name.trim()) return ElMessage.warning('请输入名称')

  saving.value = true
  try {
    if (isEdit.value) {
      await collectionsApi.updateCollection(collectionForm.value.id, collectionForm.value)
      ElMessage.success('更新成功')
    } else {
      await collectionsApi.createCollection(collectionForm.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCollections()
    
    // 更新快速分类栏目
    if (updateCollections) updateCollections()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 5. 删除集合
const deleteCollection = (collection) => {
   ElMessageBox.confirm(`确定删除 "${collection.name}" 吗?`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await collectionsApi.deleteCollection(collection.id)
        ElMessage.success('删除成功')
        loadCollections()
        
        // 更新快速分类栏目
        if (updateCollections) updateCollections()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const viewCollection = (collection) => {
  router.push({ name: 'collection-incubator', params: { id: collection.id } })
}

const isSelected = (id) => {
  return selectedCollections.value.some(collection => collection.id === id)
}

const toggleSelect = (collection) => {
  const index = selectedCollections.value.findIndex(item => item.id === collection.id)
  if (index > -1) {
    selectedCollections.value.splice(index, 1)
  } else {
    selectedCollections.value.push(collection)
  }
}

const updateMergeForm = () => {
  if (targetCollection.value) {
    mergeForm.value.name = targetCollection.value.name
    mergeForm.value.description = targetCollection.value.description
  }
}

const showMergeDialog = () => {
  if (selectedCollections.value.length < 2) {
    ElMessage.warning('请至少选择两个集合进行合并')
    return
  }
  targetCollection.value = selectedCollections.value[0]
  updateMergeForm()
  mergeDialogVisible.value = true
}

const mergeCollections = async () => {
  if (!targetCollection.value) {
    ElMessage.warning('请选择目标集合')
    return
  }
  
  const sourceIds = selectedCollections.value
    .filter(collection => collection.id !== targetCollection.value.id)
    .map(collection => collection.id)
    
  if (sourceIds.length === 0) {
    ElMessage.warning('请至少选择一个源集合进行合并')
    return
  }
  
  try {
    await collectionsApi.mergeCollections({
      targetCollectionId: targetCollection.value.id,
      sourceCollectionIds: sourceIds,
      name: mergeForm.value.name,
      description: mergeForm.value.description
    })
    
    ElMessage.success('集合合并成功')
    mergeDialogVisible.value = false
    selectedCollections.value = []
    loadCollections()
    
    // 更新快速分类栏目
    if (updateCollections) updateCollections()
  } catch (error) {
    ElMessage.error('集合合并失败')
  }
}

// AI生成集合相关方法
const showAIDialog = () => {
  aiDialogVisible.value = true
}

// 调用AI生成集合的API
const generateCollectionWithAI = async () => {
  if (!aiForm.value.description.trim()) {
    return ElMessage.warning('请输入描述')
  }

  aiGenerating.value = true
  try {
    const response = await collectionsApi.generateCollectionByAI({ description: aiForm.value.description })
    if (response.success) {
      ElMessage.success(`集合生成成功！匹配到 ${response.matchedRecordsCount} 个记录`)
      aiDialogVisible.value = false
      loadCollections()
      
      // 更新快速分类栏目
      if (updateCollections) updateCollections()
      
      // 自动跳转到新生成的集合页面
      router.push({ name: 'collection-incubator', params: { id: response.collection.id } })
    } else {
      ElMessage.error(response.message || '生成集合失败')
    }
  } catch (error) {
    ElMessage.error('生成集合失败：' + (error.response?.data?.message || error.message))
  } finally {
    aiGenerating.value = false
  }
}

onMounted(() => {
  loadCollections()
})
</script>

<style scoped>
.header-right { display: flex; align-items: center; }
.flex { display: flex; }
.gap-4 { gap: 10px; }

.collections-view {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.font-bold {
  font-weight: bold;
  font-size: 16px;
}

.mr-1 {
  margin-right: 4px;
}

.collection-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.collection-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.collection-item {
  text-align: center;
  padding: 10px;
}

.collection-select {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.source-collections {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.collection-icon {
  margin-bottom: 15px;
  display: inline-flex;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 50%;
}

.collection-item h3 {
  margin: 10px 0;
  color: #303133;
  font-size: 18px;
}

.description {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.collection-stats {
  margin: 10px 0;
  color: #C0C4CC;
  font-size: 12px;
}

.collection-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f2f5;
}
</style>
