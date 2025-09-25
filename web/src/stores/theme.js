import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(true)

  // 初始化主题
  const initTheme = () => {
    // 从localStorage读取主题设置
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      isDark.value = savedTheme === 'dark'
    } else {
      // 检测系统主题偏好
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
  }

  // 应用主题
  const applyTheme = (dark) => {
    const html = document.documentElement
    
    if (dark) {
      html.classList.add('dark')
      html.style.setProperty('--el-color-primary', '#409eff')
      html.style.setProperty('--el-color-primary-light-3', '#79bbff')
      html.style.setProperty('--el-color-primary-light-5', '#a0cfff')
      html.style.setProperty('--el-color-primary-light-7', '#c6e2ff')
      html.style.setProperty('--el-color-primary-light-8', '#d9ecff')
      html.style.setProperty('--el-color-primary-light-9', '#ecf5ff')
      html.style.setProperty('--el-color-primary-dark-2', '#337ecc')
      
      // 暗黑模式背景色
      html.style.setProperty('--el-bg-color', '#141414')
      html.style.setProperty('--el-bg-color-page', '#0a0a0a')
      html.style.setProperty('--el-bg-color-overlay', '#1d1e1f')
      html.style.setProperty('--el-text-color-primary', '#e5eaf3')
      html.style.setProperty('--el-text-color-regular', '#cfd3dc')
      html.style.setProperty('--el-text-color-secondary', '#a3a6ad')
      html.style.setProperty('--el-text-color-placeholder', '#8d9095')
      html.style.setProperty('--el-text-color-disabled', '#6c6e72')
      html.style.setProperty('--el-border-color', '#4c4d4f')
      html.style.setProperty('--el-border-color-light', '#414243')
      html.style.setProperty('--el-border-color-lighter', '#363637')
      html.style.setProperty('--el-border-color-extra-light', '#2b2b2c')
      html.style.setProperty('--el-fill-color', '#303133')
      html.style.setProperty('--el-fill-color-light', '#262727')
      html.style.setProperty('--el-fill-color-lighter', '#1d1d1d')
      html.style.setProperty('--el-fill-color-extra-light', '#191919')
      html.style.setProperty('--el-fill-color-blank', 'transparent')
      html.style.setProperty('--el-fill-color-dark', '#39393a')
      html.style.setProperty('--el-fill-color-darker', '#424243')
      
      // 自定义暗黑模式变量
      html.style.setProperty('--app-bg-color', '#0a0a0a')
      html.style.setProperty('--app-header-bg', '#1a1a1a')
      html.style.setProperty('--app-sidebar-bg', '#141414')
      html.style.setProperty('--app-workspace-bg', '#0f0f0f')
      html.style.setProperty('--app-panel-bg', '#1a1a1a')
      html.style.setProperty('--app-border-color', '#333333')
      html.style.setProperty('--app-shadow', '0 2px 12px rgba(0, 0, 0, 0.5)')
      
    } else {
      html.classList.remove('dark')
      // 重置为默认的Element Plus浅色主题
      html.style.removeProperty('--el-color-primary')
      html.style.removeProperty('--el-color-primary-light-3')
      html.style.removeProperty('--el-color-primary-light-5')
      html.style.removeProperty('--el-color-primary-light-7')
      html.style.removeProperty('--el-color-primary-light-8')
      html.style.removeProperty('--el-color-primary-light-9')
      html.style.removeProperty('--el-color-primary-dark-2')
      html.style.removeProperty('--el-bg-color')
      html.style.removeProperty('--el-bg-color-page')
      html.style.removeProperty('--el-bg-color-overlay')
      html.style.removeProperty('--el-text-color-primary')
      html.style.removeProperty('--el-text-color-regular')
      html.style.removeProperty('--el-text-color-secondary')
      html.style.removeProperty('--el-text-color-placeholder')
      html.style.removeProperty('--el-text-color-disabled')
      html.style.removeProperty('--el-border-color')
      html.style.removeProperty('--el-border-color-light')
      html.style.removeProperty('--el-border-color-lighter')
      html.style.removeProperty('--el-border-color-extra-light')
      html.style.removeProperty('--el-fill-color')
      html.style.removeProperty('--el-fill-color-light')
      html.style.removeProperty('--el-fill-color-lighter')
      html.style.removeProperty('--el-fill-color-extra-light')
      html.style.removeProperty('--el-fill-color-blank')
      html.style.removeProperty('--el-fill-color-dark')
      html.style.removeProperty('--el-fill-color-darker')
      
      // 浅色模式自定义变量
      html.style.setProperty('--app-bg-color', '#f5f7fa')
      html.style.setProperty('--app-header-bg', '#ffffff')
      html.style.setProperty('--app-sidebar-bg', '#f5f7fa')
      html.style.setProperty('--app-workspace-bg', '#f0f2f5')
      html.style.setProperty('--app-panel-bg', '#ffffff')
      html.style.setProperty('--app-border-color', '#e4e7ed')
      html.style.setProperty('--app-shadow', '0 2px 12px rgba(0, 0, 0, 0.1)')
    }
  }

  // 切换主题
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }

  // 监听主题变化
  watch(isDark, (newValue) => {
    applyTheme(newValue)
    localStorage.setItem('theme', newValue ? 'dark' : 'light')
  }, { immediate: true })

  // 初始化
  initTheme()

  return {
    isDark,
    toggleTheme,
    applyTheme,
    initTheme
  }
})