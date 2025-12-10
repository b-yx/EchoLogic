<!-- src/views/IncubatingView.vue -->
<template>
  <div class="incubating-container">
    <!-- 标题 -->
    <div class="page-header">
      <h2>孵化</h2>
    </div>

    <!-- === 核心编辑器区域 (复刻 a.html) === -->
    <div class="editor-card">
      <!-- 工具栏 -->
      <div class="toolbar">
        <button class="format-btn font-bold">B</button>
        <button class="format-btn italic">/</button>
        <button class="format-btn underline">U</button>
        <div class="divider"></div>
        <button class="format-btn"><i class="el-icon"><Operation /></i></button>
      </div>

      <!-- 输入框 -->
      <div 
        class="editable-area" 
        contenteditable="true" 
        placeholder="捕捉瞬时灵感..."
      ></div>

      <!-- 底部栏 -->
      <div class="editor-footer">
        <div class="left-section">
          <!-- 标签展示 -->
          <el-tag 
            v-for="(tag, index) in tags" 
            :key="tag" 
            type="info" 
            effect="plain"
            class="tag-item"
            closable
            @close="removeTag(index)"
        >
            {{ tag }}
        </el-tag>
          <!-- 添加标签按钮 -->
          <button class="add-tag-btn" @click="addTag">
            <el-icon><PriceTag /></el-icon> Add tag
          </button>
        </div>

        <div class="right-section">
          <el-dropdown trigger="click" @command="handleRecordCommand">
            <el-button type="primary" color="#304156" size="small">
              Record <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="draft">Save as Draft</el-dropdown-item>
                <el-dropdown-item command="publish">Publish</el-dropdown-item>
                <el-dropdown-item command="export" divided>Export</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- === 下方的筛选栏 (复刻 a.html) === -->
    <div class="filter-bar">
      <div class="sort-section">
        <el-dropdown trigger="click" @command="handleSortCommand">
          <span class="el-dropdown-link">
            <el-icon class="mr-1"><Sort /></el-icon> 
            Sort By <span class="font-semibold">{{ currentSort }}</span> 
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="Date">Date</el-dropdown-item>
              <el-dropdown-item command="Name">Name</el-dropdown-item>
              <el-dropdown-item command="Relevance">Relevance</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="filter-section">
        <el-dropdown trigger="click" @command="handleFilterCommand">
          <span class="el-dropdown-link">
            <el-icon class="mr-1"><Filter /></el-icon> 
            Filter: <span class="font-semibold">{{ currentFilter }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="All Tags">All Tags</el-dropdown-item>
              <el-dropdown-item command="Economy">Economy</el-dropdown-item>
              <el-dropdown-item command="Trading">Trading</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- === 卡片列表 (复刻 a.html) === -->
    <div class="card-grid">
      <!-- 模拟 6 个空卡片 -->
      <div class="content-card" v-for="n in 6" :key="n">
        <div class="card-placeholder"></div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Operation, PriceTag, ArrowDown, Sort } from '@element-plus/icons-vue'

const tags = ref(['Idea', 'Draft'])

const addTag = () => {
  const val = prompt('输入标签名')
  if (val) tags.value.push(val)
}

const removeTag = (index) => {
  tags.value.splice(index, 1)
}
</script>

<style scoped>
/* 容器布局 */
.incubating-container {
  max-width: 900px; 
  margin: 0 auto;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

/* === 编辑器卡片样式 === */
.editor-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  margin-bottom: 24px;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  border-bottom: 1px solid #f2f6fc;
  padding-bottom: 8px;
}

.format-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  margin-right: 4px;
  color: #606266;
  font-family: serif;
  display: flex;
  align-items: center;
  justify-content: center;
}
.format-btn:hover { background-color: #f0f2f5; }
.format-btn.font-bold { font-weight: bold; }
.format-btn.italic { font-style: italic; }
.format-btn.underline { text-decoration: underline; }

.divider {
  width: 1px;
  height: 14px;
  background: #dcdfe6;
  margin: 0 8px;
}

/* 核心输入框 */
.editable-area {
  min-height: 80px;
  outline: none;
  font-size: 15px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 16px;
}

/* Placeholder 实现 */
.editable-area:empty:before {
  content: attr(placeholder);
  color: #a8abb2;
  pointer-events: none;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-tag-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}
.add-tag-btn:hover { background-color: #f5f7fa; }

.tag-item {
  border-radius: 12px;
}

/* === 筛选栏样式 === */
.filter-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  color: #606266;
  font-size: 14px;
}

.sort-section, .filter-section {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

/* === 卡片列表样式 === */
.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); 
  gap: 16px;
}

.content-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  height: 120px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.content-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-placeholder {
  width: 100%;
  height: 60px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-top: 10px;
}
</style>