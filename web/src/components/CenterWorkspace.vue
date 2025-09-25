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
import ThreeScene from './ThreeScene.vue'

const threeSceneRef = ref(null)
const lightIntensity = ref(1.0)
const showGrid = ref(false)
const showStats = ref(false)
const modelVisible = ref(true)
const currentModelPath = ref('/models/gltf/house.glb')
const currentModel = ref(null)

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

const exportModel = () => {
  console.log('导出模型')
}

const printModel = () => {
  console.log('打印模型')
}

const onModelLoaded = (modelInfo) => {
  currentModel.value = modelInfo
  console.log('模型加载完成:', modelInfo)
}

// 暴露方法给父组件
const loadModel = (modelPath) => {
  currentModelPath.value = modelPath
}

defineExpose({
  loadModel
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