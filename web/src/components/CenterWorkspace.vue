<template>
  <el-main class="center-workspace">
    <!-- 顶部工具栏 -->
    <div class="top-toolbar">
      <div class="toolbar-left">
        <!-- 缩放控制 -->
        <div class="tool-group">
          <el-button-group>
            <el-button :icon="ZoomIn" @click="zoomIn" />
            <el-button :icon="ZoomOut" @click="zoomOut" />
            <el-button @click="resetZoom">重置</el-button>
          </el-button-group>
        </div>

        <!-- 光照控制 -->
        <div class="tool-group">
          <el-tooltip content="光照强度">
            <el-slider
              v-model="lightIntensity"
              :min="0"
              :max="2"
              :step="0.1"
              :show-tooltip="false"
              style="width: 100px"
            />
          </el-tooltip>
        </div>

        <!-- 网格显示 -->
        <div class="tool-group">
          <el-switch
            v-model="showGrid"
            active-text="网格"
            @change="toggleGrid"
          />
        </div>

        <!-- 统计信息 -->
        <div class="tool-group">
          <el-switch
            v-model="showStats"
            active-text="统计"
            @change="toggleStats"
          />
        </div>
      </div>

      <!-- <div class="toolbar-right">
        <el-tag v-if="currentModel" type="info">
          {{ currentModel.name }}
        </el-tag>
      </div> -->
    </div>

    <!-- 3D 视图区域 -->
    <div class="model-display">
      <ThreeScene
        ref="threeSceneRef"
        :model-path="currentModelPath"
        :light-intensity="lightIntensity"
        :show-grid="showGrid"
        :show-stats="showStats"
        :dark-mode="isDarkMode"
        @model-loaded="onModelLoaded"
      />
      
      <!-- 加载状态遮罩 -->
      <div v-if="loadingStatus === 'loading'" class="loading-overlay">
        <div class="loading-content">
          <el-icon class="loading-icon" :size="48">
            <Loading />
          </el-icon>
          <div class="loading-text">
            <h3>模型生成中...</h3>
            <p>请耐心等待，预计需要几分钟时间</p>
            <div class="loading-progress">
              <el-progress 
                :percentage="progressPercentage" 
                :show-text="false"
                :stroke-width="4"
                color="#409eff"
              />
              <span class="progress-text">{{ progressText }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 失败状态 -->
      <div v-if="loadingStatus === 'failed'" class="error-overlay">
        <div class="error-content">
          <el-icon class="error-icon" :size="48">
            <CircleClose />
          </el-icon>
          <div class="error-text">
            <h3>模型生成失败</h3>
            <p>生成过程中出现错误或超时</p>
            <el-button type="primary" @click="retryGeneration">重试</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部工具栏 -->
    <div class="bottom-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <!-- <el-button :icon="Edit" @click="editTexture">编辑纹理</el-button> -->
          <el-button :icon="View" @click="toggleVisibility">
            {{ modelVisible ? '隐藏' : '显示' }}
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-button-group>
          <el-button :icon="Download" @click="exportModel">导出下载</el-button>
          <!-- <el-button :icon="Printer" @click="printModel">打印</el-button> -->
        </el-button-group>
      </div>
    </div>
  </el-main>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { 
  ZoomIn, 
  ZoomOut, 
  View, 
  Download, 
  Loading,
  CircleClose
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getModelByIdApi, getModelHistoryApi } from '@/api/modelGeneration'
import ThreeScene from './ThreeScene.vue'

const threeSceneRef = ref(null)
const lightIntensity = ref(1.0)
const showGrid = ref(false)
const showStats = ref(false)
const modelVisible = ref(true)
const currentModelPath = ref('')
const currentModel = ref(null)
const currentModelId = ref(null) // 存储当前模型的ID
const currentModelData = ref(null) // 存储当前模型的详细数据

// 轮询相关状态
const isPolling = ref(false)
const pollingTimer = ref(null)
const pollingTimeout = ref(null)
const loadingStatus = ref('') // 'loading', 'success', 'failed', ''
const progressPercentage = ref(0)
const progressText = ref('')
const pollingStartTime = ref(0)

// 轮询配置
const POLLING_INTERVAL = 10 * 1000 // 10秒
const POLLING_TIMEOUT = 10 * 60 * 1000 // 10分钟

// 暗黑模式检测
const isDarkMode = ref(false)

// 检测暗黑模式状态
const checkDarkMode = () => {
  if (typeof document !== 'undefined') {
    isDarkMode.value = document.documentElement.classList.contains('dark')
  }
}

// 监听暗黑模式变化
const observeDarkMode = () => {
  if (typeof document !== 'undefined') {
    const observer = new MutationObserver(() => {
      checkDarkMode()
    })
    
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class']
    })
    
    return observer
  }
  return null
}

const zoomIn = () => {
  if (threeSceneRef.value) {
    threeSceneRef.value.zoomIn()
  }
}

const zoomOut = () => {
  if (threeSceneRef.value) {
    threeSceneRef.value.zoomOut()
  }
}

const resetZoom = () => {
  if (threeSceneRef.value) {
    threeSceneRef.value.resetCamera()
  }
}



const toggleGrid = (value) => {
  if (threeSceneRef.value) {
    threeSceneRef.value.toggleGrid(value)
  }
}

const toggleStats = (value) => {
  if (threeSceneRef.value) {
    threeSceneRef.value.toggleStats(value)
  }
}

// const editTexture = () => {
//   console.log('编辑纹理')
// }

const toggleVisibility = () => {
  modelVisible.value = !modelVisible.value
  if (threeSceneRef.value) {
    threeSceneRef.value.toggleModelVisibility(modelVisible.value)
  }
}

const exportModel = async () => {
  if (!currentModelId.value && !defaultModelId.value) {
    console.log("test",currentModel.value ,' ', defaultModelId.value)
    ElMessage.warning('没有可导出的模型')
    return
  }

  try {
    // 直接调用API获取模型详情，不更新当前模型路径
    const modelData = await getModelByIdApi(currentModelId.value || defaultModelId.value)
    
    if (!modelData.primaryModelUrl) {
      ElMessage.error('模型下载链接不存在')
      return
    }

    // 下载模型文件
    const response = await fetch(modelData.primaryModelUrl)
    const blob = await response.blob()
    
    // 创建下载链接
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `${currentModel.value?.name || 'model'}.glb`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 清理URL对象
    window.URL.revokeObjectURL(downloadUrl)
    
    ElMessage.success('模型导出成功！')
  } catch (error) {
    console.error('模型导出失败:', error)
    ElMessage.error('模型导出失败: ' + error.message)
  }
}

// const printModel = () => {
//   console.log('打印模型')
// }

const onModelLoaded = (modelInfo) => {
  currentModel.value = modelInfo
  console.log('模型加载完成:', modelInfo)
}

// 处理模型生成完成事件
// const handleModelGenerated = async (modelData) => {
//   currentModelId.value = modelData.modelId
//   currentModel.value = {
//     name: modelData.modelName,
//     ...modelData.modelInfo
//   }
  
//   try {
//     // 根据模型ID查询模型详情，获取3D模型路径用于预览
//     const modelDetails = await getModelByIdApi(modelData.modelId)
//     console.log("模型生成数据",modelDetails)
//     currentModelData.value = modelDetails
    
//     // 如果有3D模型路径，加载到场景中
//     // if (modelDetails.modelUrl || modelDetails.previewUrl) {
//     //   currentModelPath.value = modelDetails.modelUrl || modelDetails.previewUrl
//     // }
//   } catch (error) {
//     console.error('获取模型详情失败:', error)
//     ElMessage.error('获取模型详情失败')
//   }
// }

// // 暴露方法给父组件
// const loadModel = (modelPath) => {
//   currentModelPath.value = modelPath
// }

// defineExpose({
//   loadModel,
//   handleModelGenerated
// })

const getModelById = async(id) => {
  const res = await getModelByIdApi(id)
  console.log("模型生成数据",res)
  currentModelPath.value = res.pbrModelUrl
  return res
}

// 轮询执行函数
const pollModelData = async (modelId) => {
  try {
    const data = await getModelByIdApi(modelId)
    
    if (data && data.status === 'SUCCESS') {
      // 模型生成完成
      currentModelData.value = data
      currentModelPath.value = data.pbrModelUrl
      loadingStatus.value = 'success'
      stopPolling()
      ElMessage.success('模型生成完成！')
    } else if (data && data.status === 'FAILED') {
      // 模型生成失败
      loadingStatus.value = 'failed'
      stopPolling()
      ElMessage.error('模型生成失败')
    } else {
      // 如果状态是 'processing' 或其他，继续轮询
      console.log('模型正在生成中...', data?.status)
    }
  } catch (error) {
    console.error('轮询获取模型数据失败:', error)
  }
}

// 开始轮询
const startPolling = (modelId) => {
  if (!modelId || isPolling.value) return
  
  isPolling.value = true
  loadingStatus.value = 'loading'
  progressPercentage.value = 0
  progressText.value = '正在初始化...'
  pollingStartTime.value = Date.now()
  ElMessage.info('开始获取模型数据...')
  
  // 立即执行一次
  pollModelData(modelId)
  
  // 设置轮询定时器
  pollingTimer.value = setInterval(() => {
    updateProgress()
    pollModelData(modelId)
  }, POLLING_INTERVAL)
  
  // 设置超时定时器
  pollingTimeout.value = setTimeout(() => {
    stopPolling()
    loadingStatus.value = 'failed'
    ElMessage.error('模型生成超时，10分钟内未完成')
  }, POLLING_TIMEOUT)
}

// 更新进度显示
const updateProgress = () => {
  const elapsed = Date.now() - pollingStartTime.value
  const percentage = Math.min((elapsed / POLLING_TIMEOUT) * 100, 95) // 最多显示95%，避免到100%但还没完成
  progressPercentage.value = Math.floor(percentage)
  
  const minutes = Math.floor(elapsed / 60000)
  const seconds = Math.floor((elapsed % 60000) / 1000)
  
  if (minutes === 0) {
    progressText.value = `已等待 ${seconds} 秒...`
  } else {
    progressText.value = `已等待 ${minutes} 分 ${seconds} 秒...`
  }
}

// 重试生成
const retryGeneration = () => {
  if (props.modelId) {
    loadingStatus.value = ''
    startPolling(props.modelId)
  }
}

// 停止轮询
const stopPolling = () => {
  isPolling.value = false
  
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
  
  if (pollingTimeout.value) {
    clearTimeout(pollingTimeout.value)
    pollingTimeout.value = null
  }
}

const props = defineProps({
  modelId: Number
})

// 监听 modelId 变化
watch(() => props.modelId, (newModelId) => {
  if (newModelId) {
    // 更新当前模型ID
    currentModelId.value = newModelId
    // 先停止之前的轮询
    stopPolling()
    // 开始新的轮询
    startPolling(newModelId)
  }
}, { immediate: true })

// 默认值
const defaultModelId = ref()
const getModelHistory = async () => {
  const res = await getModelHistoryApi()
  console.log('资产列表res',res)
  defaultModelId.value = res[0].id
  currentModelId.value = defaultModelId.value
}

let darkModeObserver = null

onMounted(async() => {
  // 如果没有传入 modelId，使用默认值
  await getModelHistory()
  if (!props.modelId) {
    await getModelById(defaultModelId.value)
  }
  
  // 初始化暗黑模式检测
  checkDarkMode()
  darkModeObserver = observeDarkMode()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopPolling()
  if (darkModeObserver) {
    darkModeObserver.disconnect()
  }
})
</script>

<style scoped>
.center-workspace {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
  background: var(--app-workspace-bg);
  transition: background-color 0.3s ease;
}

.top-toolbar,
.bottom-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--app-panel-bg);
  border-bottom: 1px solid var(--app-border-color);
  box-shadow: var(--app-shadow);
  transition: all 0.3s ease;
}

.bottom-toolbar {
  border-bottom: none;
  border-top: 1px solid var(--app-border-color);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tool-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-display {
  flex: 1;
  position: relative;
  background: #2c3e50;
  border-radius: 8px;
  margin: 8px;
  overflow: hidden;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: box-shadow 0.3s ease;
}

/* 暗黑模式下的3D视图背景 */
:global(.dark) .model-display {
  background: #1a1a1a;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.5);
}

/* 加载状态遮罩 */
.loading-overlay,
.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.loading-content,
.error-content {
  text-align: center;
  color: white;
  padding: 32px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  max-width: 400px;
}

.loading-icon,
.error-icon {
  margin-bottom: 16px;
  color: #409eff;
}

.error-icon {
  color: #f56c6c;
}

.loading-icon {
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text h3,
.error-text h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.loading-text p,
.error-text p {
  margin: 0 0 20px 0;
  font-size: 14px;
  opacity: 0.8;
}

.loading-progress {
  margin-top: 20px;
}

.progress-text {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.7;
}

/* 暗黑模式下的遮罩样式 */
:global(.dark) .loading-overlay,
:global(.dark) .error-overlay {
  background: rgba(0, 0, 0, 0.9);
}

:global(.dark) .loading-content,
:global(.dark) .error-content {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
</style>