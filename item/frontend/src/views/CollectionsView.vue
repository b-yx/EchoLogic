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
        </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-if="!loading && collections.length === 0" description="暂无集合" />

      <!-- 集合列表 -->
      <el-row :gutter="20" v-else>
        <el-col
          v-for="collection in collections"
          :key="collection.id"
          :xs="24" :sm="12" :md="8"
        >
          <el-card class="collection-card" shadow="hover" @click="viewCollection(collection)">
            <div class="collection-item">
              <div class="collection-icon">
                <el-icon :size="40" :style="{ color: collection.color }">
                  <!-- 使用 component 动态渲染图标组件 -->
                  <component :is="iconMap[collection.icon] || Folder" />
                </el-icon>
              </div>
              
              <h3>{{ collection.name }}</h3>
              <p class="description">{{ collection.description || '暂无描述' }}</p>
              
              <div class="collection-stats">
                <span>{{ collection.recordCount }} 个记录</span>
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
        <el-form-item label="颜色">
          <el-color-picker v-model="collectionForm.color" show-alpha predefine />
        </el-form-item>
        <el-form-item label="图标">
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
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus, Folder, Star, Sugar, CollectionTag } from '@element-plus/icons-vue'
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

// === 状态定义 ===
const collections = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)

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
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const viewCollection = (collection) => {
  router.push({ name: 'collection-incubator', params: { id: collection.id } })
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
