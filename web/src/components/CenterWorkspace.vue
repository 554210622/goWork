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
              @change="updateLighting"
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

      <div class="toolbar-right">
        <el-tag v-if="currentModel" type="info">
          {{ currentModel.name }}
        </el-tag>
      </div>
    </div>

    <!-- 3D 视图区域 -->
    <div class="model-display">
      <ThreeScene
        ref="threeSceneRef"
        :model-path="currentModelPath"
        :light-intensity="lightIntensity"
        :show-grid="showGrid"
        :show-stats="showStats"
        @model-loaded="onModelLoaded"
      />
    </div>

    <!-- 底部工具栏 -->
    <div class="bottom-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button :icon="Edit" @click="editTexture">编辑纹理</el-button>
          <el-button :icon="View" @click="toggleVisibility">
            {{ modelVisible ? '隐藏' : '显示' }}
          </el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-button-group>
          <el-button :icon="Download" @click="exportModel">导出下载</el-button>
          <el-button :icon="Printer" @click="printModel">打印</el-button>
        </el-button-group>
      </div>
    </div>
  </el-main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  ZoomIn, 
  ZoomOut, 
  Edit, 
  View, 
  Download, 
  Printer 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getModelByIdApi } from '@/api/modelGeneration'
import ThreeScene from './ThreeScene.vue'

const threeSceneRef = ref(null)
const lightIntensity = ref(1.0)
const showGrid = ref(false)
const showStats = ref(false)
const modelVisible = ref(true)
const currentModelPath = ref('')
const currentModel = ref(null)
const currentModelId = ref(21) // 存储当前模型的ID
const currentModelData = ref(null) // 存储当前模型的详细数据

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

const updateLighting = (value) => {
  if (threeSceneRef.value) {
    threeSceneRef.value.setLightIntensity(value)
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

const editTexture = () => {
  console.log('编辑纹理')
}

const toggleVisibility = () => {
  modelVisible.value = !modelVisible.value
  if (threeSceneRef.value) {
    threeSceneRef.value.toggleModelVisibility(modelVisible.value)
  }
}

const exportModel = async () => {
  if (!currentModelId.value) {
    ElMessage.warning('没有可导出的模型')
    return
  }

  try {
    // 根据模型ID查询模型详情
    const modelData = await getModelById(currentModelId.value)
    
    if (!modelData.downloadUrl) {
      ElMessage.error('模型下载链接不存在')
      return
    }

    // 下载模型文件
    const response = await fetch(modelData.downloadUrl)
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

const printModel = () => {
  console.log('打印模型')
}

const onModelLoaded = (modelInfo) => {
  currentModel.value = modelInfo
  console.log('模型加载完成:', modelInfo)
}

// 处理模型生成完成事件
const handleModelGenerated = async (modelData) => {
  currentModelId.value = modelData.modelId
  currentModel.value = {
    name: modelData.modelName,
    ...modelData.modelInfo
  }
  
  try {
    // 根据模型ID查询模型详情，获取3D模型路径用于预览
    const modelDetails = await getModelByIdApi(modelData.modelId)
    console.log("模型生成数据",modelDetails)
    currentModelData.value = modelDetails
    
    // 如果有3D模型路径，加载到场景中
    // if (modelDetails.modelUrl || modelDetails.previewUrl) {
    //   currentModelPath.value = modelDetails.modelUrl || modelDetails.previewUrl
    // }
  } catch (error) {
    console.error('获取模型详情失败:', error)
    ElMessage.error('获取模型详情失败')
  }
}

// 暴露方法给父组件
const loadModel = (modelPath) => {
  currentModelPath.value = modelPath
}

defineExpose({
  loadModel,
  handleModelGenerated
})

const getModelById = async(id) => {
  const res = await getModelByIdApi(id)
  currentModelPath.value = res.pbrModelUrl
  return res
}

onMounted(() => {
  getModelById(currentModelId.value)
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
</style>