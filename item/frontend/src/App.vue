<template>
  <div id="app">
    <el-container style="height: 100vh">
      
      <!-- 1. 左侧边栏  -->
      <el-aside width="250px" class="sidebar-container">
        <Sidebar />
      </el-aside>

      <!-- 右侧整体区域 -->
      <el-container>
        <!-- 2. 顶部 Header  -->
        <el-header class="header">
          <div class="header-title">{{ pageTitle }}</div>
        </el-header>

        <!-- 3. 下方内容区分割  -->
        <el-container class="content-wrapper">
          
          <!-- 中间：主路由视图 -->
          <el-main class="main-content">
            <router-view @update-records="handleUpdateRecords" @update-collections="handleUpdateCollections" />
          </el-main>

          <!-- 右侧：AI 对话框 (固定宽度 350px) -->
          <el-aside width="350px" class="ai-panel-container">
            <AIChatPanel @execute-command="handleExecuteCommand" />
          </el-aside>

        </el-container>
      </el-container>
    </el-container>
    
    <!-- 悬浮窗组件 -->
    <FloatingRecords ref="floatingRecordsRef" />
    
    <!-- 底栏组件 -->
    <CollectionsBottomBar ref="collectionsBottomBarRef" />
  </div>
</template>

<script setup>
import { computed, ref, provide } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import AIChatPanel from '@/components/AIChatPanel.vue'
import FloatingRecords from '@/components/FloatingRecords.vue'
import CollectionsBottomBar from '@/components/CollectionsBottomBar.vue'

const route = useRoute()

// 组件引用
const floatingRecordsRef = ref(null)
const collectionsBottomBarRef = ref(null)

const pageTitle = computed(() => {
  const titles = {
    '/': '首页',
    '/incubating': '灵感孵化',
    '/records': '记录',
    '/tags': '标签',
    '/collections': '集合'
  }
  return titles[route.path] || 'EchoLogic'
})

// 数据同步事件处理
const handleUpdateRecords = async () => {
  if (floatingRecordsRef.value) {
    await floatingRecordsRef.value.updateRecords()
  }
}

const handleUpdateCollections = async () => {
  if (collectionsBottomBarRef.value) {
    await collectionsBottomBarRef.value.updateCollections()
  }
}

// 处理AI指令
const handleExecuteCommand = (command) => {
  console.log('收到AI指令:', command)
  
  // 根据指令类型执行不同操作
  switch (command.command) {
    case 'filter':
      handleFilterCommand(command.params)
      break
    case 'search':
      handleSearchCommand(command.params)
      break
    case 'navigate':
      handleNavigateCommand(command.params)
      break
    case 'list':
      handleListCommand(command.params)
      break
    default:
      console.warn('未知指令类型:', command.command)
  }
}

// 处理列表指令
const handleListCommand = (params) => {
  console.log('执行列表指令:', params)
  // 例如，可以导航到记录页面
  if (route.path !== '/records') {
    window.location.href = '#/records'
  }
}

// 处理筛选指令
const handleFilterCommand = (params) => {
  console.log('执行筛选指令:', params)
  // 如果当前不在Records页面，先导航到Records页面
  if (route.path !== '/records') {
    // 使用路由参数传递筛选条件
    window.location.href = `#/records?filter=${encodeURIComponent(JSON.stringify(params))}`
  } else {
    // 如果已经在Records页面，刷新页面并应用筛选条件
    window.location.href = `#/records?filter=${encodeURIComponent(JSON.stringify(params))}`
    // 强制刷新当前路由，以便组件重新加载并应用筛选条件
    window.location.reload()
  }
}

// 处理搜索指令
const handleSearchCommand = (params) => {
  console.log('执行搜索指令:', params)
  // 例如，可以导航到搜索结果页面
}

// 处理导航指令
const handleNavigateCommand = (params) => {
  console.log('执行导航指令:', params)
  // 例如，可以跳转到指定页面
}

// 提供全局数据同步方法
provide('updateRecords', handleUpdateRecords)
provide('updateCollections', handleUpdateCollections)
</script>

<style>
/* 引入全局通用样式 */
@import './assets/main.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  height: 100vh;
  width: 100vw;
  overflow: hidden; 
}

.sidebar-container {
  background-color: #304156;
  overflow: hidden;
  flex-shrink: 0; 
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px; 
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 新增布局样式 */
.content-wrapper {
  height: calc(100vh - 60px); 
  overflow: hidden;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  height: 100%;
  overflow-y: auto; 
}

.ai-panel-container {
  background-color: #fff;
  height: 100%;
  overflow: hidden;
  box-shadow: -2px 0 5px rgba(0,0,0,0.03); 
}
</style>
