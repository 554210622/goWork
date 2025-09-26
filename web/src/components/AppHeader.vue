<template>
  <el-header class="app-header">
    <!-- 左侧 Logo 和导航 -->
    <div class="header-left">
      <div class="logo">
        <el-icon size="24"><Box /></el-icon>
        <span class="logo-text">ModelStudio</span>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        class="nav-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="workspace">工作区</el-menu-item>
        <el-menu-item index="community">社区</el-menu-item>
        <el-menu-item index="assets">我的资产</el-menu-item>
        <el-menu-item index="join">加入我们</el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧工具栏 -->
    <div class="header-right">
      <!-- 暗黑模式切换 -->
      <el-switch
        v-model="themeStore.isDark"
        :active-icon="Moon"
        :inactive-icon="Sunny"
      />
      
      <!-- 通知 -->
      <el-badge :value="notificationCount" class="notification-badge">
        <el-button :icon="Bell" circle />
      </el-badge>
      
      <!-- 设置 -->
      <el-button :icon="Setting" circle @click="openSettings" />
      
      <!-- 用户头像 -->
      <el-dropdown @command="handleUserCommand">
        <el-avatar :size="32" :src="userAvatar" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="settings">设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { ref } from 'vue'
import { 
  Box, 
  Bell, 
  Setting, 
  Moon, 
  Sunny 
} from '@element-plus/icons-vue'
import { useThemeStore } from '../stores/theme'

const themeStore = useThemeStore()
const activeMenu = ref('workspace')
const notificationCount = ref(3)
const userAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

const handleMenuSelect = (index) => {
  activeMenu.value = index
  // 这里可以添加路由跳转逻辑
  console.log('切换到:', index)
}

// 移除不需要的toggleDarkMode函数，因为el-switch会自动处理v-model绑定

const openSettings = () => {
  console.log('打开设置')
}

const handleUserCommand = (command) => {
  console.log('用户操作:', command)
}
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: var(--app-header-bg);
  border-bottom: 1px solid var(--app-border-color);
  box-shadow: var(--app-shadow);
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.logo-text {
  color: var(--el-text-color-primary);
  transition: color 0.3s ease;
}

.nav-menu {
  border-bottom: none;
  background: transparent;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  margin-right: 8px;
}

/* 暗黑模式下的特殊样式 */
:global(.dark) .app-header {
  background: var(--app-header-bg);
  border-bottom-color: var(--app-border-color);
}
</style>