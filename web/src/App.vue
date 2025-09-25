<script setup>
import { ref } from 'vue'
import AppHeader from './components/AppHeader.vue'
import LeftNav from './components/LeftNav.vue'
import CenterWorkspace from './components/CenterWorkspace.vue'
import RightColumn from './components/RightColumn.vue'

const centerWorkspaceRef = ref(null)

const handleModelSelected = (asset) => {
  if (centerWorkspaceRef.value) {
    centerWorkspaceRef.value.loadModel(asset.modelPath)
  }
}
</script>

<template>
  <el-container class="app-container">
    <!-- 顶部导航 -->
    <AppHeader />
    
    <!-- 主体内容 -->
    <el-container class="main-container">
      <!-- 左侧导航 -->
      <LeftNav />
      
      <!-- 中间工作区 -->
      <CenterWorkspace ref="centerWorkspaceRef" />
      
      <!-- 右侧控制区 -->
      <RightColumn @model-selected="handleModelSelected" />
    </el-container>
  </el-container>
</template>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-container {
  flex: 1;
  overflow: hidden;
}
</style>
