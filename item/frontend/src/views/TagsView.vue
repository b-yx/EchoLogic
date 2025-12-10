<template>
  <div class="tags-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="font-bold">标签管理</span>
          <div class="header-right flex gap-4">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索标签..."
              style="width: 200px"
              clearable
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon class="mr-1"><Plus /></el-icon>
            新建标签
          </el-button>
        </div>
        </div>
      </template>

      <!-- 这里的 !loading 是为了防止加载时显示空状态 -->
      <el-empty v-if="!loading && tags.length === 0" description="暂无标签" />

      <el-row :gutter="20" v-else>
        <!-- 循环渲染标签卡片 -->
        <el-col
          v-for="tag in filteredTags"
          :key="tag.id"
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <el-card class="tag-card" shadow="hover">
            <div class="tag-item">
              <!-- 标签展示 -->
              <el-tag 
                effect="dark"
                round
                size="large"
                :style="{ backgroundColor: tag.color, borderColor: tag.color, color: '#fff' }"
              >
                {{ tag.name }}
              </el-tag>
              
              <!-- 增加标签和按钮之间的间隔 -->
              <div style="margin: 20px 0;"></div>
              
              <div class="tag-actions">
                <el-button size="small" @click="editTag(tag)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteTag(tag)">
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 创建/编辑标签对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '新建标签'"
      width="400px"
      @closed="resetForm"
    >
      <el-form :model="tagForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="tagForm.name" placeholder="例如: 学习资料" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="tagForm.color" show-alpha predefine />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import tagsApi from '@/api/tags'

// 状态定义 
const tags = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)

// 搜索过滤计算属性
const filteredTags = computed(() => {
  if (!searchKeyword.value.trim()) {
    return tags.value
  }
  const keyword = searchKeyword.value.toLowerCase().trim()
  return tags.value.filter(tag => 
    tag.name.toLowerCase().includes(keyword)
  )
})

const tagForm = ref({
  id: null,
  name: '',
  color: '#409EFF'
})


// 1. 加载标签 (模拟)
const loadTags = async () => {
  loading.value = true
  try {
    const data = await tagsApi.getAllTags()
    tags.value = data
  } catch (error) {
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

// 2. 显示创建弹窗
const showCreateDialog = () => {
  isEdit.value = false
  tagForm.value = { id: null, name: '', color: '#409EFF' }
  dialogVisible.value = true
}

// 3. 显示编辑弹窗
const editTag = (tag) => {
  isEdit.value = true
  tagForm.value = { ...tag }
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  tagForm.value = { id: null, name: '', color: '#409EFF' }
}

// 4. 保存标签 
const saveTag = async () => {
  // 严格验证标签名称不为空
  const name = tagForm.value.name?.trim()
  if (!name) {
    return ElMessage.warning('请输入名称')
  }

  saving.value = true
  try {
    // 只传递后端需要的字段
    const tagData = {
      name: name,
      color: tagForm.value.color || '#409EFF'
    }

    if (isEdit.value) {
      // 编辑：PUT /tags/:id
      await tagsApi.updateTag(tagForm.value.id, tagData)
      ElMessage.success('更新成功')
    } else {
      // 新建：POST /tags
      await tagsApi.createTag(tagData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTags() // 重新拉取最新列表
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}


// 5. 删除标签
const deleteTag = (tag) => {
   ElMessageBox.confirm(`确定删除 "${tag.name}" 吗?`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await tagsApi.deleteTag(tag.id)
        ElMessage.success('删除成功')
        loadTags()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.header-right { display: flex; align-items: center; }
.flex { display: flex; }
.gap-4 { gap: 10px; }

.tags-view {
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

.tag-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.tag-card:hover {
  transform: translateY(-2px);
}

.tag-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px 0;
  gap: 10px;
}

.tag-info {
  margin: 15px 0;
  color: #909399;
  font-size: 12px;
}

.tag-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  width: 100%;
}

.mr-1 {
  margin-right: 4px;
}
</style>
