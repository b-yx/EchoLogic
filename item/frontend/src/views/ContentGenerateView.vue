<template>
  <div class="content-generate-container">
    <div class="page-header">
      <h1>孵化</h1>
      <el-button
        type="primary"
        @click="createNewTask"
      >
        新建任务
      </el-button>
    </div>
    
    <!-- 选择收藏内容区域 -->
    <div class="content-selection-section">
      <h2>选择收藏内容</h2>
      
      <!-- 筛选和选择方式切换 -->
      <div class="selection-tabs">
        <el-tabs v-model="activeSelectionTab">
          <el-tab-pane label="搜索选择" name="search">
            <div class="selection-controls">
              <el-select
                v-model="selectedContentIds"
                multiple
                filterable
                remote
                reserve-keyword
                placeholder="请选择要使用的收藏内容"
                :remote-method="remoteSearchContent"
                :loading="searchLoading"
                style="width: 100%;"
              >
                <el-option
                  v-for="item in contentOptions"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
                >
                  <div class="content-option-item">
                    <div class="content-option-title">{{ item.title }}</div>
                    <div class="content-option-type">{{ item.contentType || '文本' }}</div>
                  </div>
                </el-option>
              </el-select>
              <el-button type="primary" @click="loadSelectedContents">加载所选内容</el-button>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="按集合选择" name="collection">
            <div class="collection-selection">
              <div class="collection-intro">
                <el-tag type="info">共 {{ collections.length }} 个集合</el-tag>
                <span class="intro-text">选择集合后可加载该集合中的所有内容用于生成</span>
              </div>
              <div class="collection-options">
                <el-checkbox-group v-model="selectedCollectionIds">
                  <div v-for="collection in collections" :key="collection.id" class="collection-item">
                    <div class="collection-info">
                      <el-checkbox :label="collection.id" class="collection-checkbox">
                        <span class="collection-name">{{ collection.name }}</span>
                      </el-checkbox>
                      <div class="collection-actions">
                        <el-button 
                          size="small" 
                          type="primary" 
                          @click="selectAllInCollection(collection.id, false)"
                        >
                          全选
                        </el-button>
                      </div>
                    </div>
                    <div v-if="collection.description" class="collection-description">
                      {{ collection.description }}
                    </div>
                  </div>
                </el-checkbox-group>
              </div>
              <div class="collection-selection-options">
                <el-button 
                    type="primary" 
                    @click="selectAllInSelectedCollections" 
                    :disabled="selectedCollectionIds.length === 0"
                    size="large"
                  >
                    <el-icon><Plus /></el-icon>
                    加载所选集合内容 ({{ selectedCollectionIds.length }})
                  </el-button>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="按标签筛选" name="tags">
            <div class="tag-selection">
              <div class="tag-filter-options">
                <el-select 
                  v-model="tagFilterLogic" 
                  placeholder="选择筛选逻辑"
                  style="width: 200px; margin-bottom: 15px;"
                >
                  <el-option label="包含任一标签" value="any"></el-option>
                  <el-option label="包含所有标签" value="all"></el-option>
                </el-select>
                <el-checkbox-group v-model="selectedTagIds">
                  <el-tag 
                    v-for="tag in tags" 
                    :key="tag.id" 
                    :type="selectedTagIds.includes(tag.id) ? 'primary' : ''"
                    :color="selectedTagIds.includes(tag.id) ? tag.color : ''"
                    :effect="selectedTagIds.includes(tag.id) ? 'light' : 'plain'"
                    @click="toggleTagSelection(tag.id)"
                    class="tag-item"
                  >
                    <template v-if="selectedTagIds.includes(tag.id)">
                      <el-icon><Check /></el-icon>
                      {{ tag.name }}
                    </template>
                    <template v-else>
                      {{ tag.name }}
                    </template>
                  </el-tag>
                </el-checkbox-group>
              </div>
              <div class="tag-selection-actions">
                <el-button 
                  type="primary" 
                  @click="filterByTags" 
                  :disabled="selectedTagIds.length === 0"
                >
                  按标签筛选
                </el-button>
                <el-button 
                  type="success" 
                  @click="selectAllFilteredContents" 
                  :disabled="filteredContents.length === 0"
                >
                  全选筛选结果
                </el-button>
              </div>
              
              <!-- 筛选结果预览 -->
              <div v-if="filteredContents.length > 0" class="filter-results-preview">
                <h4>筛选结果 ({{ filteredContents.length }}条)</h4>
                <el-checkbox 
                  v-model="selectAllFiltered" 
                  @change="handleSelectAllFiltered"
                >全选</el-checkbox>
                <div class="filtered-content-list">
                  <div v-for="content in filteredContents" :key="content.id" class="filtered-content-item">
                    <el-checkbox 
                      :label="content.id" 
                      v-model="selectedFilteredContentIds"
                    >
                      {{ content.title }}
                    </el-checkbox>
                  </div>
                </div>
                <el-button 
                  type="primary" 
                  @click="loadFilteredContents" 
                  :disabled="selectedFilteredContentIds.length === 0"
                >
                  加载所选内容
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 已加载内容列表 -->
      <div v-if="loadedContents.length > 0" class="loaded-contents">
        <h3>已加载内容</h3>
        <div class="content-cards">
          <el-card
            v-for="content in loadedContents"
            :key="content.id"
            class="content-card"
          >
            <template #header>
              <div class="card-header">
                <span>{{ content.title }}</span>
                <el-button
                  type="danger"
                  size="small"
                  @click="removeContent(content.id)"
                >
                  移除
                </el-button>
              </div>
            </template>
            <div class="card-content">
              <div class="content-preview">{{ truncateText(content.content, 100) }}</div>
              <div class="content-meta">
                <span class="content-type">{{ content.contentType || '文本' }}</span>
                <span class="content-date">{{ formatDate(content.createTime) }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
    
    <!-- 生成目标和AI交互区域 -->
    <div class="ai-interaction-section">
      <div class="generation-settings">
        <h2>生成设置</h2>
        <el-input
          type="textarea"
          v-model="generationGoal"
          placeholder="请输入您的创作目标，例如：生成一篇关于AI的文章、总结这些内容、创建一个学习计划等"
          :rows="3"
        ></el-input>
        <el-button
          type="primary"
          :loading="generating"
          @click="generateContent"
          :disabled="loadedContents.length === 0"
          class="generate-btn"
        >
          {{ generating ? '生成中...' : '生成内容' }}
        </el-button>
      </div>
      
      <!-- 对话历史区域 -->
      <div class="dialogue-section">
        <h2>对话历史</h2>
        <div class="dialogue-history">
          <!-- 系统提示 -->
          <div class="system-message">
            基于您选择的收藏内容，AI将生成相关的想法和建议。您可以继续与AI对话以深化想法。
          </div>
          
          <!-- AI生成内容 -->
          <div v-if="dialogueHistory.length > 0" class="ai-messages">
            <div
              v-for="(message, index) in dialogueHistory"
              :key="index"
              class="dialogue-item"
            >
              <div class="dialogue-header">
                <span class="sender">{{ message.sender === 'ai' ? 'AI助手' : '我' }}</span>
                <span class="timestamp">{{ formatTime(message.timestamp) }}</span>
              </div>
              <div class="dialogue-content" v-html="markdownToHtml(message.content)">
              </div>
            </div>
          </div>
          
          <!-- 最新生成内容 -->
          <div v-if="latestGeneratedContent" class="latest-content">
            <div class="dialogue-header">
              <span class="sender">AI助手</span>
              <span class="timestamp">{{ formatTime(latestGeneratedContent.timestamp) }}</span>
            </div>
            <div class="dialogue-content generated-content" v-html="markdownToHtml(latestGeneratedContent.result)">
            </div>
          </div>
        </div>
        
        <!-- 继续对话输入 -->
        <div v-if="latestGeneratedContent" class="continue-dialogue">
          <el-input
            type="textarea"
            v-model="continueInstruction"
            placeholder="请输入您的进一步指令，例如：请详细说明这部分内容、扩展这个想法等"
            :rows="3"
            @keyup.enter.exact="refineContent"
            @keyup.enter.ctrl="$event.target.form?.submit()"
          ></el-input>
          <div class="dialogue-controls">
            <el-button
              type="primary"
              :loading="refining"
              @click="refineContent"
            >
              {{ refining ? '优化中...' : '继续对话' }}
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 内容操作区域 -->
      <div v-if="latestGeneratedContent" class="content-actions">
        <h2>内容操作</h2>
        <el-button-group>
          <el-button
            type="success"
            @click="saveAsNote"
          >
            保存为笔记
          </el-button>
          <el-button
            type="info"
            @click="exportContent"
          >
            导出内容
          </el-button>
          <el-button
            type="warning"
            @click="clearAll"
          >
            清除所有
          </el-button>
          <el-button
            type="primary"
            @click="showHistoryDialog = true"
          >
            历史记录
          </el-button>
        </el-button-group>
      </div>
    </div>
  </div>
  
  <!-- 任务历史记录对话框 -->
  <el-dialog
    v-model="showHistoryDialog"
    title="新建任务历史记录"
    width="800px"
    top="10vh"
  >
    <div v-if="historyRecords.length === 0" class="empty-history">
      <el-empty description="暂无任务历史记录" />
    </div>
    <el-timeline v-else>
      <el-timeline-item
        v-for="record in historyRecords"
        :key="record.id"
        :timestamp="formatDate(record.timestamp)"
      >
        <div class="history-item">
          <div class="history-header">
            <div class="history-title">
              <strong>{{ record.taskName }}</strong>
            </div>
            <div class="history-actions">
              <el-button
                type="danger"
                size="small"
                @click="deleteHistoryRecord(record.id)"
              >
                删除
              </el-button>
            </div>
          </div>
          <div class="history-content">
            <div class="history-status">
              <strong>状态：</strong>
              {{ record.status }}
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-dialog>

  <!-- 标签选择对话框 -->
  <el-dialog
    v-model="showTagSelectDialog"
    title="选择标签"
    width="400px"
  >
    <div class="tag-selection-dialog">
      <div class="tag-selection-title">选择标签（可选）</div>
      <div class="tag-list">
        <el-checkbox-group v-model="selectedTags">
          <el-tag
            v-for="tag in allTags"
            :key="tag.id"
            :value="tag.id"
            :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
            :color="tag.color"
            effect="plain"
            class="tag-item"
          >
            {{ tag.name }}
          </el-tag>
        </el-checkbox-group>
      </div>
      <div v-if="allTags.length === 0" class="no-tags-message">
        暂无标签，可直接保存
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showTagSelectDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveNote">确认保存</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 导出格式选择对话框 -->
  <el-dialog
    v-model="showExportDialog"
    title="导出内容"
    width="400px"
  >
    <div class="export-dialog-content">
      <div class="export-format-selector">
        <span>选择导出格式：</span>
        <el-select v-model="exportFormat" style="width: 200px; margin-left: 10px;">
          <el-option label="纯文本 (.txt)" value="txt"></el-option>
          <el-option label="Markdown (.md)" value="md"></el-option>
          <el-option label="HTML (.html)" value="html"></el-option>
        </el-select>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showExportDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmExport">确认导出</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import contentApi from '../api/content'
import tagsApi from '../api/tags'
import recordsApi from '../api/records'
import collectionsApi from '../api/collections'
import { ElMessage, ElMessageBox } from 'element-plus'
import showdown from 'showdown'
import { Plus, Check } from '@element-plus/icons-vue'

// 状态变量
const selectedContentIds = ref([])
const contentOptions = ref([])
const searchLoading = ref(false)
const loadedContents = ref([])
const generationGoal = ref('')
const generating = ref(false)
const refining = ref(false)
const latestGeneratedContent = ref(null)
const continueInstruction = ref('')
const dialogueHistory = ref([])

// 任务历史记录功能相关状态
const historyRecords = ref([])
const showHistoryDialog = ref(false)

// 从localStorage加载任务历史记录
const loadHistoryRecords = () => {
  try {
    const saved = localStorage.getItem('taskHistory')
    if (saved) {
      historyRecords.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('加载任务历史记录失败:', error)
  }
}

// 保存任务历史记录到localStorage
const saveHistoryRecords = () => {
  try {
    localStorage.setItem('taskHistory', JSON.stringify(historyRecords.value))
  } catch (error) {
    console.error('保存任务历史记录失败:', error)
  }
}

// 添加新的任务历史记录
const addHistoryRecord = (taskName) => {
  const record = {
    id: Date.now(),
    timestamp: new Date().toISOString(),
    taskName: taskName || '新任务',
    status: '已创建'
  }
  
  // 添加到历史记录列表开头
  historyRecords.value.unshift(record)
  
  // 最多保留10条历史记录
  if (historyRecords.value.length > 10) {
    historyRecords.value = historyRecords.value.slice(0, 10)
  }
  
  saveHistoryRecords()
}

// 删除任务历史记录
const deleteHistoryRecord = (recordId) => {
  historyRecords.value = historyRecords.value.filter(record => record.id !== recordId)
  saveHistoryRecords()
  ElMessage.success('任务历史记录已删除')
}

// 新增：集合选择相关
const collections = ref([])
const selectedCollectionIds = ref([])

// 新增：标签筛选相关
const tags = ref([])
const selectedTagIds = ref([])
const tagFilterLogic = ref('any') // 'any' 或 'all'
const filteredContents = ref([])
const selectedFilteredContentIds = ref([])
const selectAllFiltered = ref(false)

// 新增：切换标签页
const activeSelectionTab = ref('search')

// 远程搜索内容
const remoteSearchContent = async (query) => {
  if (query.trim() === '') {
    contentOptions.value = []
    return
  }
  
  searchLoading.value = true
  try {
    const res = await recordsApi.searchRecords(query)
    contentOptions.value = res.map(item => ({
      id: item.id,
      title: item.title,
      contentType: item.contentType
    }))
  } catch (error) {
    ElMessage.error('搜索内容失败')
    console.error('搜索内容失败:', error)
  } finally {
    searchLoading.value = false
  }
}

// 新增：加载所有集合
const loadCollections = async () => {
  try {
    const res = await collectionsApi.getAllCollections()
    collections.value = res
  } catch (error) {
    ElMessage.error('加载集合失败')
    console.error('加载集合失败:', error)
  }
}

// 新增：加载所有标签
const loadTags = async () => {
  try {
    const res = await tagsApi.getAllTags()
    tags.value = res
  } catch (error) {
    ElMessage.error('加载标签失败')
    console.error('加载标签失败:', error)
  }
}

// 新增：选择集合中的所有内容
const selectAllInCollection = async (collectionId, includeSubs) => {
  try {
    const contents = await recordsApi.getRecordsByCollectionId(collectionId)
    const contentIds = contents.map(item => item.id)
    
    // 加载内容
    for (const id of contentIds) {
      if (!loadedContents.value.some(content => content.id === id)) {
        const content = await recordsApi.getRecordById(id)
        if (content) {
          loadedContents.value.push(content)
        }
      }
    }
    
    ElMessage.success(`成功加载集合中的${contentIds.length}条内容`)
  } catch (error) {
    ElMessage.error('加载集合内容失败')
    console.error('加载集合内容失败:', error)
  }
}

// 新增：加载所选集合内容
const selectAllInSelectedCollections = async () => {
  try {
    let totalLoaded = 0
    
    for (const collectionId of selectedCollectionIds.value) {
      const contents = await recordsApi.getRecordsByCollectionId(collectionId)
      const contentIds = contents.map(item => item.id)
      
      // 加载内容
      for (const id of contentIds) {
        if (!loadedContents.value.some(content => content.id === id)) {
          const content = await recordsApi.getRecordById(id)
          if (content) {
            loadedContents.value.push(content)
            totalLoaded++
          }
        }
      }
      
      // 如果包含子集合，这里需要实现子集合的处理逻辑
      // 由于当前API可能不支持子集合查询，暂时省略此功能
    }
    
    ElMessage.success(`成功加载所选集合中的${totalLoaded}条内容`)
  } catch (error) {
    ElMessage.error('加载集合内容失败')
    console.error('加载集合内容失败:', error)
  }
}

// 新增：按标签筛选内容
const filterByTags = async () => {
  try {
    // 获取所有记录
    const allContents = await recordsApi.getAllRecords()
    
    // 筛选包含所选标签的内容
    filteredContents.value = allContents.filter(content => {
      if (!content.tags || content.tags.length === 0) return false
      
      const contentTagIds = content.tags.map(tag => tag.id)
      
      if (tagFilterLogic.value === 'any') {
        // 包含任一标签
        return selectedTagIds.value.some(tagId => contentTagIds.includes(tagId))
      } else {
        // 包含所有标签
        return selectedTagIds.value.every(tagId => contentTagIds.includes(tagId))
      }
    })
    
    ElMessage.success(`找到${filteredContents.value.length}条匹配内容`)
  } catch (error) {
    ElMessage.error('筛选内容失败')
    console.error('筛选内容失败:', error)
  }
}

// 新增：切换标签选择
const toggleTagSelection = (tagId) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value.push(tagId)
  }
}

// 新增：全选筛选结果
const selectAllFilteredContents = async () => {
  try {
    let totalLoaded = 0
    
    for (const content of filteredContents.value) {
      if (!loadedContents.value.some(item => item.id === content.id)) {
        loadedContents.value.push(content)
        totalLoaded++
      }
    }
    
    ElMessage.success(`成功加载${totalLoaded}条内容`)
  } catch (error) {
    ElMessage.error('加载筛选内容失败')
    console.error('加载筛选内容失败:', error)
  }
}

// 新增：选择筛选结果中的所有内容
const handleSelectAllFiltered = () => {
  if (selectAllFiltered.value) {
    selectedFilteredContentIds.value = filteredContents.value.map(content => content.id)
  } else {
    selectedFilteredContentIds.value = []
  }
}

// 新增：加载筛选结果中选中的内容
const loadFilteredContents = async () => {
  try {
    let totalLoaded = 0
    
    for (const contentId of selectedFilteredContentIds.value) {
      const content = filteredContents.value.find(item => item.id === contentId)
      if (content && !loadedContents.value.some(item => item.id === contentId)) {
        loadedContents.value.push(content)
        totalLoaded++
      }
    }
    
    ElMessage.success(`成功加载${totalLoaded}条内容`)
  } catch (error) {
    ElMessage.error('加载筛选内容失败')
    console.error('加载筛选内容失败:', error)
  }
}

// 加载选中的内容
const loadSelectedContents = async () => {
  if (selectedContentIds.value.length === 0) {
    ElMessage.warning('请先选择内容')
    return
  }
  
  try {
    // 从当前已加载内容中移除重复项
    const newContentIds = selectedContentIds.value.filter(id => 
      !loadedContents.value.some(content => content.id === id)
    )
    
    if (newContentIds.length === 0) {
      ElMessage.info('所有选中内容已加载')
      return
    }
    
    // 加载新内容
    for (const id of newContentIds) {
      const content = await recordsApi.getRecordById(id)
      if (content) {
        loadedContents.value.push(content)
      }
    }
    
    ElMessage.success(`成功加载${newContentIds.length}条内容`)
    // 清空已选ID，避免重复加载
    selectedContentIds.value = []
  } catch (error) {
    ElMessage.error('加载内容失败')
    console.error('加载内容失败:', error)
  }
}

// 移除内容
const removeContent = (id) => {
  loadedContents.value = loadedContents.value.filter(content => content.id !== id)
  ElMessage.success('内容已移除')
}

// 生成内容
const generateContent = async () => {
  if (loadedContents.value.length === 0) {
    ElMessage.warning('请先选择内容')
    return
  }
  
  if (generationGoal.value.trim() === '') {
    ElMessage.warning('请输入生成目标')
    return
  }
  
  generating.value = true
  try {
    // 准备请求数据
    const request = {
      contents: loadedContents.value.map(content => content.content),
      goal: generationGoal.value
    }
    console.log('发送请求:', JSON.stringify(request))
    
    // 调用API生成内容
    const res = await contentApi.generateContent(request)
    console.log('接收响应:', JSON.stringify(res))
    
    // 检查响应格式
    if (!res || typeof res !== 'object') {
      throw new Error('响应格式错误：' + JSON.stringify(res))
    }
    
    if (!res.result) {
      throw new Error('响应缺少result字段：' + JSON.stringify(res))
    }
    
    if (!res.timestamp) {
      throw new Error('响应缺少timestamp字段：' + JSON.stringify(res))
    }
    
    latestGeneratedContent.value = res
    
    // 添加到对话历史
    dialogueHistory.value.push({
      sender: 'user',
      content: generationGoal.value,
      timestamp: Date.now()
    })
    
    dialogueHistory.value.push({
      sender: 'ai',
      content: res.result,
      timestamp: res.timestamp
    })
    
    ElMessage.success('内容生成成功')
  } catch (error) {
    console.error('内容生成失败:', error)
    console.error('错误详情:', JSON.stringify(error, null, 2))
    // 显示更详细的错误信息
    ElMessage.error(`内容生成失败: ${error.message}`)
  } finally {
    generating.value = false
  }
}

// 优化/继续对话
const refineContent = async () => {
  if (!latestGeneratedContent.value) {
    ElMessage.warning('请先生成内容')
    return
  }
  
  if (continueInstruction.value.trim() === '') {
    ElMessage.warning('请输入您的指令')
    return
  }
  
  refining.value = true
  try {
    // 准备对话历史，转换为后端期望的格式
    const formattedDialogueHistory = dialogueHistory.value.map(msg => ({
      role: msg.sender,
      content: msg.content
    }))
    
    // 准备请求数据
    const request = {
      currentContent: latestGeneratedContent.value.result,
      instruction: continueInstruction.value,
      dialogueHistory: formattedDialogueHistory
    }
    
    // 调用API优化内容
    const res = await contentApi.refineContent(request)
    console.log('接收响应:', JSON.stringify(res))
    
    // 检查响应格式
    if (!res || typeof res !== 'object') {
      throw new Error('响应格式错误：' + JSON.stringify(res))
    }
    
    if (!res.result) {
      throw new Error('响应缺少result字段：' + JSON.stringify(res))
    }
    
    if (!res.timestamp) {
      throw new Error('响应缺少timestamp字段：' + JSON.stringify(res))
    }
    
    latestGeneratedContent.value = res
    
    // 添加到对话历史
    dialogueHistory.value.push({
      sender: 'user',
      content: continueInstruction.value,
      timestamp: Date.now()
    })
    
    dialogueHistory.value.push({
      sender: 'ai',
      content: res.result,
      timestamp: res.timestamp
    })
    
    // 清空输入
    continueInstruction.value = ''
    ElMessage.success('内容优化成功')
  } catch (error) {
    ElMessage.error('内容优化失败')
    console.error('内容优化失败:', error)
  } finally {
    refining.value = false
  }
}

// 保存为笔记
// 标签选择对话框状态
const showTagSelectDialog = ref(false)
const selectedTags = ref([])
const allTags = ref([])
const noteTitle = ref('')
const noteContent = ref('')

// 加载所有标签
const loadAllTags = async () => {
  try {
    const tags = await tagsApi.getAllTags()
    allTags.value = tags
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  }
}

// 保存为笔记
const saveAsNote = async () => {
  if (!latestGeneratedContent.value) {
    ElMessage.warning('请先生成内容')
    return
  }
  
  try {
    // 弹出输入框获取笔记标题
    const title = await ElMessageBox.prompt('请输入笔记标题', '保存为笔记', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
      inputValue: 'AI生成内容_' + new Date().toISOString().slice(0, 10)
    })
    
    // 保存标题和内容，准备标签选择
    noteTitle.value = title.value
    noteContent.value = latestGeneratedContent.value.result
    
    // 加载所有标签
    await loadAllTags()
    
    // 显示标签选择对话框
    showTagSelectDialog.value = true
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消了操作
      return
    }
    ElMessage.error('保存笔记失败')
    console.error('保存笔记失败:', error)
  }
}

// 确认保存笔记（标签选择后）
const confirmSaveNote = async () => {
  try {
    // 准备记录数据，包括选择的标签ID
    const record = {
      title: noteTitle.value,
      content: noteContent.value,
      contentType: 'note',
      isDraft: false,
      tagIds: selectedTags.value
    }
    
    // 调用API保存记录
    await recordsApi.createRecord(record)
    
    // 关闭对话框并重置状态
    showTagSelectDialog.value = false
    selectedTags.value = []
    noteTitle.value = ''
    noteContent.value = ''
    
    ElMessage.success('笔记保存成功')
  } catch (error) {
    ElMessage.error('保存笔记失败')
    console.error('保存笔记失败:', error)
  }
}

// 导出内容

// 导出功能相关状态
const exportFormat = ref('txt') // 'txt', 'md', 'html'
const showExportDialog = ref(false)

// 格式化导出内容
const formatExportContent = (content, format) => {
  switch (format) {
    case 'txt':
      return content
    case 'md':
      return content // AI生成的内容已经是Markdown格式
    case 'html':
      const converter = new showdown.Converter()
      const htmlContent = `<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>AI生成内容</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      line-height: 1.6;
      color: #333;
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
    }
    h1, h2, h3, h4, h5, h6 {
      color: #2c3e50;
      margin-top: 20px;
      margin-bottom: 10px;
    }
    p {
      margin-bottom: 15px;
    }
    ul, ol {
      margin-bottom: 15px;
      padding-left: 30px;
    }
    blockquote {
      border-left: 4px solid #3498db;
      padding-left: 15px;
      color: #7f8c8d;
      margin-bottom: 15px;
    }
    code {
      background-color: #f5f5f5;
      padding: 2px 4px;
      border-radius: 3px;
      font-family: Consolas, Monaco, 'Andale Mono', monospace;
    }
    pre {
      background-color: #f5f5f5;
      padding: 15px;
      border-radius: 4px;
      overflow-x: auto;
      margin-bottom: 15px;
    }
    pre code {
      background-color: transparent;
      padding: 0;
    }
  </style>
</head>
<body>
${converter.makeHtml(content)}
</body>
</html>`
      return htmlContent
    default:
      return content
  }
}

// 获取MIME类型
const getMimeType = (format) => {
  switch (format) {
    case 'txt':
      return 'text/plain;charset=utf-8'
    case 'md':
      return 'text/markdown;charset=utf-8'
    case 'html':
      return 'text/html;charset=utf-8'
    default:
      return 'text/plain;charset=utf-8'
  }
}

// 获取文件扩展名
const getFileExtension = (format) => {
  switch (format) {
    case 'txt':
      return 'txt'
    case 'md':
      return 'md'
    case 'html':
      return 'html'
    default:
      return 'txt'
  }
}

// 导出内容
const exportContent = () => {
  if (!latestGeneratedContent.value) {
    ElMessage.warning('请先生成内容')
    return
  }
  
  // 显示导出格式选择对话框
  showExportDialog.value = true
}

// 确认导出
const confirmExport = () => {
  try {
    // 获取内容并格式化
    const content = latestGeneratedContent.value.result
    const formattedContent = formatExportContent(content, exportFormat.value)
    const blob = new Blob([formattedContent], { type: getMimeType(exportFormat.value) })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    
    // 设置下载属性
    link.href = url
    link.download = 'AI生成内容_' + new Date().toISOString().slice(0, 10) + '.' + getFileExtension(exportFormat.value)
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    // 关闭对话框
    showExportDialog.value = false
    
    ElMessage.success('内容导出成功')
  } catch (error) {
    ElMessage.error('内容导出失败')
    console.error('内容导出失败:', error)
  }
}

// 清除所有
const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有内容吗？此操作不可恢复。', '清除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 重置所有状态
    loadedContents.value = []
    generationGoal.value = ''
    latestGeneratedContent.value = null
    continueInstruction.value = ''
    dialogueHistory.value = []
    
    ElMessage.success('所有内容已清除')
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消了操作
      return
    }
    ElMessage.error('清除失败')
    console.error('清除失败:', error)
  }
}

// 新建任务
const createNewTask = async () => {
  try {
    // 如果当前有内容，询问用户是否保存
    if (loadedContents.value.length > 0 || latestGeneratedContent.value) {
      await ElMessageBox.confirm('当前有未保存的内容，确定要新建任务吗？', '新建任务确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    }
    
    // 重置所有状态
    loadedContents.value = []
    generationGoal.value = ''
    latestGeneratedContent.value = null
    continueInstruction.value = ''
    dialogueHistory.value = []
    
    // 记录新任务到历史记录
    addHistoryRecord('新任务')
    
    ElMessage.success('已新建任务')
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消了操作
      return
    }
    ElMessage.error('新建任务失败')
    console.error('新建任务失败:', error)
  }
}

// 辅助函数：Markdown转HTML
const markdownToHtml = (markdown) => {
  const converter = new showdown.Converter()
  return converter.makeHtml(markdown)
}

// 辅助函数
const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString()
}

// 组件挂载时的初始化
onMounted(() => {
  loadCollections()
  loadTags()
  loadHistoryRecords()
})
</script>

<style scoped>
.content-generate-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-selection-section, .ai-interaction-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fafafa;
}

.selection-controls {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
    }
    
    /* 历史记录样式 */
    .empty-history {
      text-align: center;
      padding: 50px 0;
    }
    
    .history-item {
      padding: 15px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      background-color: #fafafa;
    }
    
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
    }
    
    .history-title {
      font-weight: bold;
    }
    
    .content-count {
      color: #606266;
      font-size: 12px;
      margin-left: 5px;
    }
    
    .history-actions {
      display: flex;
      gap: 10px;
    }
    
    .history-content {
      font-size: 14px;
      line-height: 1.6;
    }
    
    .history-goal {
      margin-bottom: 8px;
    }
    
    .history-result {
      margin-bottom: 8px;
    }
    
    .result-preview {
      background-color: #fff;
      padding: 8px;
      border-radius: 4px;
      border-left: 3px solid #409eff;
    }
    
    .history-sources {
      margin-top: 8px;
    }
    
    .history-sources ul {
      list-style: disc;
      margin-left: 20px;
      margin-top: 5px;
    }
    
    .history-sources li {
      font-size: 12px;
      color: #606266;
      margin-bottom: 3px;
    }

/* 新增：集合选择样式 */
.collection-selection {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.collection-intro {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.collection-intro .el-tag {
  font-size: 12px;
}

.intro-text {
  font-size: 13px;
  color: #606266;
}

.collection-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 15px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 5px;
}

.collection-options::-webkit-scrollbar {
  width: 6px;
}

.collection-options::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.collection-options::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.collection-options::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.collection-item {
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.collection-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.collection-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.collection-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.collection-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.content-count-tag {
  font-size: 11px;
  padding: 2px 6px;
}

.collection-actions {
  display: flex;
  gap: 8px;
}

.collection-description {
  font-size: 12px;
  color: #909396;
  line-height: 1.5;
  padding-left: 25px;
  margin-top: 5px;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.collection-selection-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

/* 新增：标签选择样式 */
.tag-selection {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tag-filter-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tag-selection .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.tag-selection-actions {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.filter-results-preview {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  background-color: #fff;
}

.filtered-content-list {
  max-height: 300px;
  overflow-y: auto;
  margin-top: 10px;
  margin-bottom: 15px;
}

.filtered-content-item {
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 新增：标签页样式 */
.selection-tabs {
  margin-bottom: 20px;
}

.loaded-contents {
  margin-top: 20px;
}

.content-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.content-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-preview {
  margin-bottom: 10px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.generation-settings {
  margin-bottom: 20px;
}

.generate-btn {
  margin-top: 10px;
}

.dialogue-section {
  margin-bottom: 20px;
}

.dialogue-history {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  max-height: 500px;
  overflow-y: auto;
  background-color: #fff;
}

.dialogue-item, .latest-content {
  margin-bottom: 20px;
  padding: 10px;
  border-radius: 8px;
}

.dialogue-item:nth-child(odd) {
  background-color: #f5f7fa;
}

.dialogue-item:nth-child(even) {
  background-color: #e6f7ff;
}

.dialogue-header {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  margin-bottom: 5px;
}

.sender {
  color: #409eff;
}

.timestamp {
  font-size: 12px;
  color: #909399;
}

.generated-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.continue-dialogue {
  margin-top: 20px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.dialogue-controls {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 优化对话历史展示 */
.ai-messages {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  margin-bottom: 16px;
}

.dialogue-item {
  margin-bottom: 16px;
  padding: 10px 14px;
  border-radius: 12px;
  max-width: 85%;
  word-break: break-word;
}

.dialogue-item:nth-child(odd) {
  background-color: #e6f3ff;
  align-self: flex-start;
  margin-left: 0;
  border-bottom-left-radius: 4px;
}

.dialogue-item:nth-child(even) {
  background-color: #ffffff;
  align-self: flex-end;
  margin-left: auto;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.dialogue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
  color: #606266;
}

.sender {
  font-weight: bold;
  margin-right: 8px;
}

.dialogue-content {
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
}

/* 标签选择对话框样式 */
.tag-selection-dialog {
  padding: 10px 0;
}

.tag-selection-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #303133;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.tag-item {
  cursor: pointer;
  margin-bottom: 0;
}

.no-tags-message {
  text-align: center;
  color: #909396;
  padding: 20px 0;
  font-size: 14px;
}

/* 导出对话框样式 */
.export-dialog-content {
  padding: 10px 0;
}

.export-format-selector {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.export-format-selector span {
  font-size: 14px;
  color: #303133;
}

.content-actions {
  margin-top: 20px;
}

.content-actions .el-button-group {
  margin-top: 10px;
}
</style>