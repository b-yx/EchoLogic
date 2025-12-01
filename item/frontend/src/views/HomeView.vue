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
        <el-button type="primary" @click="$router.push('/records')">
          开始使用
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, PriceTag, Folder } from '@element-plus/icons-vue'
import recordsApi from '@/api/records'
import tagsApi from '@/api/tags'
import collectionsApi from '@/api/collections'

const stats = ref({
  totalRecords: 0,
  totalTags: 0,
  totalCollections: 0
})

onMounted(async () => {
  try {
    // 获取记录数量
    const records = await recordsApi.getAllRecords()
    stats.value.totalRecords = records.length
    
    // 获取标签数量
    const tags = await tagsApi.getAllTags()
    stats.value.totalTags = tags.length
    
    // 获取集合数量
    const collections = await collectionsApi.getAllCollections()
    stats.value.totalCollections = collections.length
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
})
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
