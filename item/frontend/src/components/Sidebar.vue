<template>
  <div class="sidebar-wrapper">
    <!-- Logo -->
    <div class="logo">
      <h2>EchoLogic</h2>
    </div>

    <!-- 菜单区域 -->
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      :router="true"
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/records">
          <el-icon><Document /></el-icon>
          <span>记录</span>
        </el-menu-item>

        <el-menu-item index="/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签</span>
        </el-menu-item>

        <el-menu-item index="/collections">
          <el-icon><Folder /></el-icon>
          <span>集合</span>
        </el-menu-item>

        <el-menu-item index="/content-generate">
          <el-icon><EditPen /></el-icon>
          <span>孵化</span>
        </el-menu-item>
      </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
// 引入所有需要的图标
import { 
  House, Document, PriceTag, Folder, // 原有的
  EditPen                            // 新增的
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
/* 保持原有风格 */
.sidebar-wrapper {
  height: 100%;
  background-color: #304156;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1e2936;
}

.logo h2 {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.sidebar-menu {
  border-right: none;
}

/* 调整分组标题的样式，使其在深色背景下更好看 */
:deep(.el-menu-item-group__title) {
  padding: 10px 0 5px 20px;
  font-size: 12px;
  color: #909399;
}
</style>


