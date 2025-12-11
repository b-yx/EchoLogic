<template>
  <div class="incubator-container">
    <!-- 顶部导航栏 -->
    <div class="app-header">
      <div class="header-left">
        <el-icon class="header-icon" :size="24"><MagicStick /></el-icon>
        <h1>灵感孵化器</h1>
      </div>
      <div class="header-actions">
         <el-button @click="showHistoryDialog = true" plain>
           <el-icon><Clock /></el-icon> 历史记录
         </el-button>
        <el-button type="primary" @click="createNewTask" circle>
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="main-layout">
      <!-- 左侧：素材准备区 -->
      <div class="left-panel">
        <el-card class="panel-card selection-card" shadow="never">
          <template #header>
            <div class="card-header-title">
              <span><el-icon><Files /></el-icon> 选择收藏内容</span>
            </div>
          </template>
          
          <el-tabs v-model="activeSelectionTab" class="custom-tabs">
            <!-- 搜索 -->
            <el-tab-pane label="搜索" name="search">
              <div class="tab-content">
                <el-select
                  v-model="selectedContentIds"
                  multiple
                  filterable
                  remote
                  reserve-keyword
                  placeholder="搜索并选择收藏内容..."
                  :remote-method="remoteSearchContent"
                  :loading="searchLoading"
                  class="full-width-select"
                >
                  <el-option v-for="item in contentOptions" :key="item.id" :label="item.title" :value="item.id">
                    <div class="content-option-item">
                      <span class="option-title">{{ item.title }}</span>
                      <el-tag size="small" type="info">{{ item.contentType || '文本' }}</el-tag>
                    </div>
                  </el-option>
                </el-select>
                <el-button class="mt-2" type="primary" plain block @click="loadSelectedContents" :disabled="selectedContentIds.length === 0">
                  添加选中内容
                </el-button>
              </div>
            </el-tab-pane>
            
            <!-- 集合 -->
            <el-tab-pane label="集合" name="collection">
              <div class="tab-content scrollable-list">
                <div v-if="collections.length === 0" class="empty-text">暂无集合</div>
                <div v-for="collection in collections" :key="collection.id" class="list-item">
                  <el-checkbox v-model="selectedCollectionIds" :label="collection.id">
                    <span class="item-title">{{ collection.name }}</span>
                  </el-checkbox>
                  <el-button link type="primary" size="small" @click="selectAllInCollection(collection.id)">
                    全选
                  </el-button>
                </div>
              </div>
              <div class="tab-footer">
                 <el-button type="primary" plain block @click="selectAllInSelectedCollections" :disabled="selectedCollectionIds.length === 0">
                   加载所选集合
                 </el-button>
              </div>
            </el-tab-pane>
            
            <!-- 标签 -->
            <el-tab-pane label="标签" name="tags">
               <div class="tab-content">
                 <div class="filter-controls">
                    <el-radio-group v-model="tagFilterLogic" size="small">
                      <el-radio-button label="any">包含任一</el-radio-button>
                      <el-radio-button label="all">包含所有</el-radio-button>
                    </el-radio-group>
                 </div>
                 <div class="tag-cloud scrollable-list-small">
                    <el-check-tag 
                      v-for="tag in tags" 
                      :key="tag.id"
                      :checked="selectedTagIds.includes(tag.id)" 
                      @change="toggleTagSelection(tag.id)"
                      class="custom-check-tag"
                    >
                      {{ tag.name }}
                    </el-check-tag>
                 </div>
                 <el-button class="mt-2" type="primary" plain block @click="filterByTags" :disabled="selectedTagIds.length === 0">筛选内容</el-button>
                 
                 <!-- 筛选结果微缩预览 -->
                 <div v-if="filteredContents.length > 0" class="filter-mini-result">
                    <div class="result-header">
                       <span>找到 {{ filteredContents.length }} 条</span>
                       <el-checkbox v-model="selectAllFiltered" @change="handleSelectAllFiltered" size="small">全选</el-checkbox>
                    </div>
                    <el-button type="success" link size="small" @click="loadFilteredContents" :disabled="selectedFilteredContentIds.length === 0">
                      加载选中
                    </el-button>
                 </div>
               </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <!-- 已加载内容清单 -->
        <el-card class="panel-card loaded-card" shadow="never">
          <template #header>
            <div class="card-header-title space-between">
              <span><el-icon><List /></el-icon> 已加载素材 ({{ loadedContents.length }})</span>
              <el-button v-if="loadedContents.length > 0" type="danger" link size="small" @click="clearAll">清空</el-button>
            </div>
          </template>
          
          <div v-if="loadedContents.length === 0" class="empty-placeholder">
            <el-empty description="请从上方添加素材" :image-size="60" />
          </div>
          
          <div v-else class="loaded-list scrollable-list">
            <div v-for="content in loadedContents" :key="content.id" class="loaded-item">
              <div class="loaded-item-main">
                <div class="loaded-title">{{ content.title }}</div>
                <div class="loaded-preview">{{ truncateText(content.content, 40) }}</div>
              </div>
              <el-button type="danger" :icon="Close" circle size="small" link @click="removeContent(content.id)" />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：生成与交互区 -->
      <div class="right-panel">
        <el-card class="panel-card workspace-card" shadow="never">
          <!-- 创作目标输入 -->
          <div class="generation-input-area">
             <h2 class="section-title"><el-icon><Aim /></el-icon> 生成设定目标</h2>
             <div class="input-wrapper">
               <el-input
                type="textarea"
                v-model="generationGoal"
                :rows="2"
                placeholder="告诉AI你想基于左侧素材创作什么？例如：总结核心观点、写一篇博客文章、制定学习计划..."
                resize="none"
              />
              <el-button 
                type="primary" 
                :loading="generating" 
                @click="generateContent"
                :disabled="loadedContents.length === 0 || !generationGoal"
                class="send-btn"
              >
                <el-icon><Promotion /></el-icon> 开始孵化
              </el-button>
             </div>
          </div>

          <el-divider class="workspace-divider" />

          <!-- 对话历史区域 -->
          <div class="chat-container">
            <div v-if="dialogueHistory.length === 0 && !latestGeneratedContent" class="chat-empty">
               <el-icon class="empty-icon" :size="48"><ChatLineSquare /></el-icon>
               <p>(对话历史)AI 正在等待您的指令...</p>
            </div>

            <div class="chat-history custom-scrollbar">
              <!-- 系统提示 -->
              <div class="chat-message system">
                 <span class="message-bubble">基于您选择的素材，AI将为您生成灵感。</span>
              </div>

              <!-- 历史消息 -->
              <div v-for="(message, index) in dialogueHistory" :key="index" :class="['chat-message', message.sender]">
                <div class="avatar">
                  <el-icon v-if="message.sender === 'ai'"><Cpu /></el-icon>
                  <el-icon v-else><User /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-bubble markdown-body" v-html="markdownToHtml(message.content)"></div>
                  <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                </div>
              </div>

              <!-- 最新生成的内容 (高亮显示) -->
              <div v-if="latestGeneratedContent" class="chat-message ai latest">
                 <div class="avatar"><el-icon><Cpu /></el-icon></div>
                 <div class="message-content">
                    <div class="latest-header">
                       <span class="tag">最新生成</span>
                       <div class="actions">
                          <el-tooltip content="保存为笔记" placement="top">
                            <el-button size="small" circle @click="saveAsNote"><el-icon><DocumentAdd /></el-icon></el-button>
                          </el-tooltip>
                          <el-tooltip content="导出" placement="top">
                            <el-button size="small" circle @click="exportContent"><el-icon><Download /></el-icon></el-button>
                          </el-tooltip>
                       </div>
                    </div>
                    <div class="message-bubble markdown-body" v-html="markdownToHtml(latestGeneratedContent.result)"></div>
                    <span class="message-time">{{ formatTime(latestGeneratedContent.timestamp) }}</span>
                 </div>
              </div>
            </div>

            <!-- 继续对话输入框 (仅在有内容后显示) -->
            <div v-if="latestGeneratedContent" class="refine-input-area">
               <el-input
                v-model="continueInstruction"
                placeholder="继续对话：请详细说明、扩写这部分..."
                @keyup.enter.exact="refineContent"
              >
                <template #append>
                   <el-button @click="refineContent" :loading="refining">
                     <el-icon><Position /></el-icon>
                   </el-button>
                </template>
               </el-input>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 弹窗组件 (保持原样逻辑) -->
    <el-dialog v-model="showHistoryDialog" title="任务历史" width="600px">
        <el-timeline>
        <el-timeline-item v-for="record in historyRecords" :key="record.id" :timestamp="formatDate(record.timestamp)" placement="top">
            <el-card shadow="hover" class="history-card-item">
            <h4>{{ record.taskName }}</h4>
            <div class="history-card-footer">
                <el-tag size="small">{{ record.status }}</el-tag>
                <el-button type="danger" link size="small" @click="deleteHistoryRecord(record.id)">删除</el-button>
            </div>
            </el-card>
        </el-timeline-item>
        </el-timeline>
         <div v-if="historyRecords.length === 0" class="text-center py-4 text-gray-400">暂无历史</div>
    </el-dialog>

    <el-dialog v-model="showTagSelectDialog" title="保存为笔记" width="400px">
        <p class="mb-2">选择标签（可选）：</p>
        <div class="tag-select-wrapper">
  <el-check-tag 
    v-for="tag in allTags" 
    :key="tag.id" 
    :checked="selectedTags.includes(tag.id)" 
    @change="handleTagChange(tag.id)"
  >
    {{ tag.name }}
  </el-check-tag>
</div>
        <template #footer>
            <el-button @click="showTagSelectDialog = false">取消</el-button>
            <el-button type="primary" @click="confirmSaveNote">保存</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="showExportDialog" title="导出内容" width="350px">
      <div class="py-4">
        <span class="mr-2">格式：</span>
        <el-select v-model="exportFormat">
          <el-option label="纯文本 (.txt)" value="txt" />
          <el-option label="Markdown (.md)" value="md" />
          <el-option label="HTML (.html)" value="html" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="showExportDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmExport">导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 引入必要的图标
import { 
  MagicStick, Plus, Search, Folder, Collection, PriceTag, 
  Promotion, ChatLineSquare, User, Cpu, Position, 
  DocumentAdd, Download, Clock, Files, List, Close, Check, Aim
} from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import contentApi from '../api/content'
import tagsApi from '../api/tags'
import recordsApi from '../api/records'
import collectionsApi from '../api/collections'
import { ElMessage, ElMessageBox } from 'element-plus'
import showdown from 'showdown'

// ==========================================
// 逻辑部分保持完全一致，仅补充了部分未声明的变量以防报错
// ==========================================

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

// 集合选择相关
const collections = ref([])
const selectedCollectionIds = ref([])

// 标签筛选相关
const tags = ref([])
const selectedTagIds = ref([])
const tagFilterLogic = ref('any')
const filteredContents = ref([])
const selectedFilteredContentIds = ref([])
const selectAllFiltered = ref(false)

// 切换标签页
const activeSelectionTab = ref('search')

// 笔记保存相关
const showTagSelectDialog = ref(false)
const selectedTags = ref([])
const allTags = ref([])
const noteTitle = ref('')
const noteContent = ref('')

// 导出相关
const exportFormat = ref('txt')
const showExportDialog = ref(false)

// --- 以下是原有的所有方法 (Functions) ---
// 处理标签点击切换
const handleTagChange = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    // 如果已存在，则移除
    selectedTags.value.splice(index, 1)
  } else {
    // 如果不存在，则添加
    selectedTags.value.push(tagId)
  }
}
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

const saveHistoryRecords = () => {
  try {
    localStorage.setItem('taskHistory', JSON.stringify(historyRecords.value))
  } catch (error) {
    console.error('保存任务历史记录失败:', error)
  }
}

const addHistoryRecord = (taskName) => {
  const record = {
    id: Date.now(),
    timestamp: new Date().toISOString(),
    taskName: taskName || '新任务',
    status: '已创建'
  }
  historyRecords.value.unshift(record)
  if (historyRecords.value.length > 10) {
    historyRecords.value = historyRecords.value.slice(0, 10)
  }
  saveHistoryRecords()
}

const deleteHistoryRecord = (recordId) => {
  historyRecords.value = historyRecords.value.filter(record => record.id !== recordId)
  saveHistoryRecords()
  ElMessage.success('任务历史记录已删除')
}

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

const loadCollections = async () => {
  try {
    const res = await collectionsApi.getAllCollections()
    collections.value = res
  } catch (error) {
    ElMessage.error('加载集合失败')
    console.error('加载集合失败:', error)
  }
}

const loadTags = async () => {
  try {
    const res = await tagsApi.getAllTags()
    tags.value = res
  } catch (error) {
    ElMessage.error('加载标签失败')
    console.error('加载标签失败:', error)
  }
}

const selectAllInCollection = async (collectionId) => {
  try {
    const contents = await recordsApi.getRecordsByCollectionId(collectionId)
    const contentIds = contents.map(item => item.id)
    for (const id of contentIds) {
      if (!loadedContents.value.some(content => content.id === id)) {
        const content = await recordsApi.getRecordById(id)
        if (content) loadedContents.value.push(content)
      }
    }
    ElMessage.success(`成功加载集合中的${contentIds.length}条内容`)
  } catch (error) {
    ElMessage.error('加载集合内容失败')
    console.error('加载集合内容失败:', error)
  }
}

const selectAllInSelectedCollections = async () => {
  try {
    let totalLoaded = 0
    for (const collectionId of selectedCollectionIds.value) {
      const contents = await recordsApi.getRecordsByCollectionId(collectionId)
      const contentIds = contents.map(item => item.id)
      for (const id of contentIds) {
        if (!loadedContents.value.some(content => content.id === id)) {
          const content = await recordsApi.getRecordById(id)
          if (content) {
            loadedContents.value.push(content)
            totalLoaded++
          }
        }
      }
    }
    ElMessage.success(`成功加载所选集合中的${totalLoaded}条内容`)
  } catch (error) {
    ElMessage.error('加载集合内容失败')
    console.error('加载集合内容失败:', error)
  }
}

const filterByTags = async () => {
  try {
    const allContents = await recordsApi.getAllRecords()
    filteredContents.value = allContents.filter(content => {
      if (!content.tags || content.tags.length === 0) return false
      const contentTagIds = content.tags.map(tag => tag.id)
      if (tagFilterLogic.value === 'any') {
        return selectedTagIds.value.some(tagId => contentTagIds.includes(tagId))
      } else {
        return selectedTagIds.value.every(tagId => contentTagIds.includes(tagId))
      }
    })
    ElMessage.success(`找到${filteredContents.value.length}条匹配内容`)
  } catch (error) {
    ElMessage.error('筛选内容失败')
    console.error('筛选内容失败:', error)
  }
}

const toggleTagSelection = (tagId) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) selectedTagIds.value.splice(index, 1)
  else selectedTagIds.value.push(tagId)
}

const handleSelectAllFiltered = () => {
  if (selectAllFiltered.value) {
    selectedFilteredContentIds.value = filteredContents.value.map(content => content.id)
  } else {
    selectedFilteredContentIds.value = []
  }
}

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

const loadSelectedContents = async () => {
  if (selectedContentIds.value.length === 0) {
    ElMessage.warning('请先选择内容')
    return
  }
  try {
    const newContentIds = selectedContentIds.value.filter(id => 
      !loadedContents.value.some(content => content.id === id)
    )
    if (newContentIds.length === 0) {
      ElMessage.info('所有选中内容已加载')
      return
    }
    for (const id of newContentIds) {
      const content = await recordsApi.getRecordById(id)
      if (content) loadedContents.value.push(content)
    }
    ElMessage.success(`成功加载${newContentIds.length}条内容`)
    selectedContentIds.value = []
  } catch (error) {
    ElMessage.error('加载内容失败')
    console.error('加载内容失败:', error)
  }
}

const removeContent = (id) => {
  loadedContents.value = loadedContents.value.filter(content => content.id !== id)
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有内容吗？', '清除确认', {
      type: 'warning'
    })
    loadedContents.value = []
    generationGoal.value = ''
    latestGeneratedContent.value = null
    continueInstruction.value = ''
    dialogueHistory.value = []
    ElMessage.success('已清除')
  } catch (error) { /* cancel */ }
}

const generateContent = async () => {
  if (loadedContents.value.length === 0) return ElMessage.warning('请先选择内容')
  if (generationGoal.value.trim() === '') return ElMessage.warning('请输入生成目标')
  
  generating.value = true
  try {
    const request = {
      contents: loadedContents.value.map(content => content.content),
      goal: generationGoal.value
    }
    const res = await contentApi.generateContent(request)
    
    // 模拟数据验证 (根据原代码)
    if (!res || !res.result) throw new Error('API响应格式错误')

    latestGeneratedContent.value = res
    
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
    ElMessage.error(`内容生成失败: ${error.message || '未知错误'}`)
    console.error(error)
  } finally {
    generating.value = false
  }
}

const refineContent = async () => {
  if (!latestGeneratedContent.value) return
  if (continueInstruction.value.trim() === '') return
  
  refining.value = true
  try {
    const formattedDialogueHistory = dialogueHistory.value.map(msg => ({
      role: msg.sender,
      content: msg.content
    }))
    const request = {
      currentContent: latestGeneratedContent.value.result,
      instruction: continueInstruction.value,
      dialogueHistory: formattedDialogueHistory
    }
    const res = await contentApi.refineContent(request)
    if (!res || !res.result) throw new Error('API响应错误')
    
    latestGeneratedContent.value = res
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
    continueInstruction.value = ''
  } catch (error) {
    ElMessage.error('内容优化失败')
  } finally {
    refining.value = false
  }
}

const loadAllTags = async () => {
  try {
    const tags = await tagsApi.getAllTags()
    allTags.value = tags
  } catch (error) {
    ElMessage.error('加载标签失败')
  }
}

const saveAsNote = async () => {
  if (!latestGeneratedContent.value) return
  try {
    const title = await ElMessageBox.prompt('请输入笔记标题', '保存为笔记', {
      inputValue: 'AI生成_' + new Date().toISOString().slice(0, 10)
    })
    noteTitle.value = title.value
    noteContent.value = latestGeneratedContent.value.result
    await loadAllTags()
    showTagSelectDialog.value = true
  } catch (error) { /* cancel */ }
}

const confirmSaveNote = async () => {
  try {
    const record = {
      title: noteTitle.value,
      content: noteContent.value,
      contentType: 'note',
      isDraft: false,
      tagIds: selectedTags.value
    }
    await recordsApi.createRecord(record)
    showTagSelectDialog.value = false
    selectedTags.value = []
    ElMessage.success('笔记保存成功')
  } catch (error) {
    ElMessage.error('保存笔记失败')
  }
}

const exportContent = () => {
  if (!latestGeneratedContent.value) return
  showExportDialog.value = true
}

// 辅助函数（Export相关）
const getMimeType = (fmt) => fmt === 'html' ? 'text/html;charset=utf-8' : 'text/plain;charset=utf-8'
const getFileExtension = (fmt) => fmt

const confirmExport = () => {
  try {
    let content = latestGeneratedContent.value.result
    if (exportFormat.value === 'html') {
       const converter = new showdown.Converter()
       content = `<html><body>${converter.makeHtml(content)}</body></html>`
    }
    const blob = new Blob([content], { type: getMimeType(exportFormat.value) })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `Export_${Date.now()}.${getFileExtension(exportFormat.value)}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    showExportDialog.value = false
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const createNewTask = async () => {
  if (loadedContents.value.length > 0 || latestGeneratedContent.value) {
    try {
      await ElMessageBox.confirm('未保存的内容将丢失，确定新建？', '确认', { type: 'warning' })
    } catch { return }
  }
  loadedContents.value = []
  generationGoal.value = ''
  latestGeneratedContent.value = null
  continueInstruction.value = ''
  dialogueHistory.value = []
  addHistoryRecord('新任务 ' + new Date().toLocaleTimeString())
  ElMessage.success('已新建任务')
}

// Utils
const markdownToHtml = (md) => new showdown.Converter().makeHtml(md)
const truncateText = (t, l) => t && t.length > l ? t.slice(0, l) + '...' : t
const formatDate = (d) => d ? new Date(d).toLocaleDateString() : ''
const formatTime = (t) => t ? new Date(t).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : ''

onMounted(() => {
  loadCollections()
  loadTags()
  loadHistoryRecords()
})
</script>

<style scoped>
/* 容器与布局 */
.incubator-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  overflow: hidden;
}

.app-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #303133;
}
.header-left h1 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}
.header-icon {
  color: #409eff;
}

.main-layout {
  flex: 1;
  display: flex;
  padding: 16px;
  gap: 16px;
  overflow: hidden;
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.left-panel {
  flex: 1;
  min-width: 350px;
  max-width: 450px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-panel {
  flex: 2;
  min-width: 500px;
  display: flex;
  flex-direction: column;
}

/* 通用卡片样式 */
.panel-card {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.panel-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.space-between {
  justify-content: space-between;
  width: 100%;
}

/* 左侧：素材选择 */
.selection-card {
  flex: 1; /* 占据上半部分 */
  max-height: 60%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.selection-card :deep(.el-card__body) {
  padding: 0;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.custom-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.custom-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  padding: 0 16px;
  border-bottom: 1px solid #f2f2f2;
}
.custom-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
  padding: 16px;
}

.tab-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.scrollable-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.scrollable-list-small {
  max-height: 200px;
  overflow-y: auto;
}

/* 列表项样式 */
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f5f7fa;
  border-radius: 4px;
}
.list-item:hover {
  background-color: #f9fafc;
}
.item-title {
  font-size: 14px;
  color: #606266;
}

/* Tag Cloud */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.custom-check-tag {
  font-weight: normal;
  border: 1px solid #dcdfe6;
  background: white;
}
.custom-check-tag:hover {
  border-color: #409eff;
  color: #409eff;
}
.custom-check-tag.is-checked {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

/* 筛选结果微缩 */
.filter-mini-result {
  margin-top: 12px;
  padding: 8px 12px;
  background: #f0f9eb;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 左侧：已加载内容 */
.loaded-card {
  flex: 1; /* 占据下半部分 */
  max-height: 40%;
  display: flex;
  flex-direction: column;
}
.loaded-card :deep(.el-card__body) {
  padding: 12px;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.loaded-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 6px;
}
.loaded-item-main {
  flex: 1;
  overflow: hidden;
}
.loaded-title {
  font-weight: 500;
  font-size: 13px;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.loaded-preview {
  font-size: 12px;
  color: #909399;
}

/* 右侧：工作区 */
.workspace-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workspace-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.generation-input-area {
  padding: 20px;
  background: white;
}
.section-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.send-btn {
  align-self: flex-end;
  padding-left: 24px;
  padding-right: 24px;
}

.workspace-divider {
  margin: 0;
  border-color: #ebeef5;
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f9f9f9;
  position: relative;
  overflow: hidden;
}

.chat-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}
.empty-icon {
  margin-bottom: 16px;
  color: #dcdfe6;
}

.chat-history {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-message {
  display: flex;
  gap: 12px;
  max-width: 90%;
}
.chat-message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.chat-message.ai {
  align-self: flex-start;
}
.chat-message.system {
  align-self: center;
  max-width: 100%;
}
.chat-message.system .message-bubble {
  background: none;
  box-shadow: none;
  color: #909399;
  font-size: 12px;
  text-align: center;
  padding: 0;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
  flex-shrink: 0;
}
.chat-message.user .avatar {
  background: #ecf5ff;
  color: #409eff;
  border-color: #b3d8ff;
}
.chat-message.ai .avatar {
  background: #e6a23c;
  color: white;
  border-color: #e6a23c;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  background: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  color: #303133;
}
.chat-message.user .message-bubble {
  background: #409eff;
  color: white;
  border-top-right-radius: 2px;
}
.chat-message.ai .message-bubble {
  border-top-left-radius: 2px;
}

.message-time {
  font-size: 10px;
  color: #c0c4cc;
  align-self: flex-end;
}
.chat-message.user .message-time {
  align-self: flex-start;
}

/* 最新生成内容样式 */
.chat-message.latest {
  max-width: 95%;
  width: 100%;
}
.chat-message.latest .message-bubble {
  border: 1px solid #c6e2ff;
  background: #ecf5ff;
  padding: 20px;
}
.latest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.latest-header .tag {
  font-size: 12px;
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
}

.refine-input-area {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

/* 历史记录卡片 */
.history-card-item h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
}
.history-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-select-wrapper {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px;
    background: #f5f7fa;
    border-radius: 4px;
}

.full-width-select {
  width: 100%;
}

.mt-2 { margin-top: 8px; }
.mr-2 { margin-right: 8px; }
.mb-2 { margin-bottom: 8px; }
.py-4 { padding-top: 16px; padding-bottom: 16px; }

/* Markdown 样式微调 (针对生成的HTML) */
:deep(.markdown-body) {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
}
:deep(.markdown-body h1), :deep(.markdown-body h2) {
    border-bottom: none;
    padding-bottom: 0;
    margin-top: 10px;
    font-size: 1.2em;
}
:deep(.markdown-body p) {
    margin-bottom: 10px;
}
  
</style>
