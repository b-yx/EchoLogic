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
            <router-view />
          </el-main>

          <!-- 右侧：AI 对话框 (固定宽度 350px) -->
          <el-aside width="350px" class="ai-panel-container">
            <AIChatPanel />
          </el-aside>

        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'
import AIChatPanel from '@/components/AIChatPanel.vue' 

const route = useRoute()

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
