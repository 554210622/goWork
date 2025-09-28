<template>
  <el-aside width="360px" class="right-column">
    <!-- 生成面板 -->
    <div class="gen-panel">
      <div class="panel-header">
        <h3>AI 生成面板</h3>
        <!-- <el-button :icon="chatExpanded ? ArrowUp : ArrowDown" text @click="toggleChatHistory" /> -->
      </div>

      <!-- 模式切换 -->
      <div class="mode">
        <div class="mode-label">
          <el-icon :color="isImageMode == false ? '#409EFF' : ''">
            <EditPen />
          </el-icon>
          <span v-if="!isImageMode">文字模式</span>
          <span v-else>图片模式</span>
        </div>
        <el-switch v-model="isImageMode" />

        <div class="mode-label">
          <el-icon :color="isImageMode == true ? '#409EFF' : ''">
            <Picture />
          </el-icon>
        </div>

        <el-select
          size="small"
          class="mode-select"
          v-model="selectedStyle"
          placeholder="风格选择(可选)"
          style="width: 240px"
        >
          <el-option
            v-for="item in styleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>

      <!-- 文字模式 - 提示词输入 -->
      <div v-if="!isImageMode" class="prompt-section">
        <el-input
          v-model="promptText"
          type="textarea"
          :rows="4"
          placeholder="描述你想要生成的3D模型..."
          maxlength="500"
          show-word-limit
          class="no-resize"
        />
      </div>

      <!-- 图片模式 - 图片上传 -->
      <div v-if="isImageMode" class="upload-section">
        <el-upload
          class="image-uploader"
          :show-file-list="false"
          :before-upload="beforeUpload"
          accept="image/*"
          drag
          :limit="1"
          :on-exceed="handleExceed"
          ref="upload"
          v-model:file-list="fileList"
          :disabled="isUploadDisabled"
        >
          <div class="upload-content" :class="{ disabled: isUploadDisabled }">
            <el-icon class="upload-icon">
              <Plus />
            </el-icon>
            <div class="upload-text">
              {{ isUploadDisabled ? '已上传图片（最多1张）' : '点击或拖拽上传参考图片' }}
            </div>
          </div>
        </el-upload>

        <!-- 已上传的图片预览 -->
        <div v-if="uploadedImages.length" class="uploaded-images">
          <div v-for="(image, index) in uploadedImages" :key="index" class="image-preview">
            <img :src="image.url" :alt="image.name" />
            <el-button
              :icon="Close"
              circle
              size="small"
              class="remove-btn"
              @click="removeImage(index)"
            />
          </div>
        </div>
      </div>

      <!-- 生成按钮 -->
      <div class="generate-section">
        <el-button
          type="primary"
          size="large"
          :loading="generating"
          :disabled="!canGenerate"
          @click="generateModel"
          class="generate-btn"
        >
          <el-icon>
            <Star />
          </el-icon>
          {{ generating ? '生成中...' : '生成模型' }}
        </el-button>
      </div>

      <!-- 聊天记录 -->
      <!-- <el-collapse-transition>
        <div v-show="chatExpanded" class="chat-history">
          <div class="chat-header">
            <span>聊天记录</span>
            <el-button :icon="Delete" text size="small" @click="clearHistory">
              清空
            </el-button>
          </div>
          <div class="chat-messages">
            <div v-for="message in chatHistory" :key="message.id" class="chat-message" :class="message.type">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>
      </el-collapse-transition> -->
    </div>

    <!-- 资产栏 -->
    <div class="asset-panel">
      <div class="panel-header">
        <h3>资产库</h3>
        <el-badge :value="assets.length" type="info">
          <el-icon>
            <Box />
          </el-icon>
        </el-badge>
      </div>

      <!-- 搜索框 -->
      <!-- <div class="search-section">
        <el-input
          v-model="assetSearchQuery"
          placeholder="搜索资产..."
          :prefix-icon="Search"
          clearable
        />
      </div> -->

      <!-- 资产列表 -->
      <div class="asset-list">
        <!-- 资产加载状态遮罩 -->
        <div v-if="assetLoadingStatus === 'loading'" class="asset-loading-overlay">
          <div class="asset-loading-content">
            <el-icon class="asset-loading-icon" :size="32">
              <Loading />
            </el-icon>
            <div class="asset-loading-text">
              <h4>资产图片生成中...</h4>
              <p>模型已生成，正在生成预览图片</p>
              <div class="asset-loading-progress">
                <el-progress
                  :percentage="assetProgressPercentage"
                  :show-text="false"
                  :stroke-width="3"
                  color="#409eff"
                />
                <span class="asset-progress-text">{{ assetProgressText }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 资产失败状态 -->
        <div v-if="assetLoadingStatus === 'failed'" class="asset-error-overlay">
          <div class="asset-error-content">
            <el-icon class="asset-error-icon" :size="32">
              <Close />
            </el-icon>
            <div class="asset-error-text">
              <h4>图片生成超时</h4>
              <p>模型已生成成功，图片稍后会更新</p>
              <el-button type="primary" size="small" @click="retryAssetGeneration">
                <el-icon><Refresh /></el-icon>
                重试
              </el-button>
            </div>
          </div>
        </div>

        <!-- 资产卡片列表 -->
        <div
          v-for="asset in assets"
          :key="asset.id"
          class="asset-card"
          :class="{ highlighted: asset.isNew }"
          @click="selectAsset(asset)"
        >
          <div class="asset-thumbnail">
            <el-image
              :ref="(el) => setImageRef(el, asset.id)"
              :src="asset.thumbnail"
              :alt="asset.name"
              fit="cover"
              :preview-src-list="[asset.thumbnail]"
              :initial-index="0"
              preview-teleported
            />
            <div class="asset-overlay">
              <el-button :icon="View" circle size="small" @click.stop="previewAsset(asset)" />
              <el-button :icon="Refresh" circle size="small" @click.stop="importAsset(asset)" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="资产预览"
      width="600px"
      :before-close="handlePreviewClose"
    >
      <div v-if="currentPreviewAsset" class="preview-content">
        <div class="preview-image">
          <el-image
            :src="currentPreviewAsset.thumbnail"
            :alt="currentPreviewAsset.name"
            fit="contain"
            style="width: 100%; max-height: 400px"
            :preview-src-list="[currentPreviewAsset.thumbnail]"
            :initial-index="0"
            preview-teleported
          />
        </div>
      </div>
    </el-dialog>
  </el-aside>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  Plus,
  Close,
  Star,
  Box,
  View,
  EditPen,
  Picture,
  Loading,
  Refresh,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  uploadImage,
  generateModelFromText,
  generateModelFromImage,
  getModelHistoryApi,
} from '@/api/modelGeneration'

const promptText = ref('')
const generating = ref(false)
// const chatExpanded = ref(false)
const uploadedImages = ref([])
const isImageMode = ref(false) // 默认为文字模式

// 聊天记录
// const chatHistory = ref([
//   {
//     id: 1,
//     type: 'user',
//     content: '生成一个现代风格的沙发',
//     timestamp: new Date('2024-01-15 14:30')
//   },
//   {
//     id: 2,
//     type: 'assistant',
//     content: '已为您生成现代风格沙发模型，请查看3D视图',
//     timestamp: new Date('2024-01-15 14:31')
//   }
// ])

// 资产数据
const assets = ref([
  {
    id: 1,

    thumbnail:
      'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/27/legacy_mesh.webp',
    isNew: true,
  },
  {
    id: 2,
    name: '科幻机器人',
    thumbnail:
      'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/27/legacy_mesh.webp',
    tags: ['科幻', '角色'],
    isNew: false,
    modelPath: '/models/robot.glb',
  },
  {
    id: 3,
    name: '建筑模型',
    thumbnail:
      'https://seven-mulik.oss-cn-guangzhou.aliyuncs.com/model3d/preview/2025/09/27/legacy_mesh.webp',
    tags: ['建筑', '场景'],
    isNew: false,
    modelPath: '/models/building.glb',
  },
])

// const filteredAssets = computed(() => {
//   if (!assetSearchQuery.value) return assets.value
//   return assets.value.filter(asset =>
//     asset.name.toLowerCase().includes(assetSearchQuery.value.toLowerCase()) ||
//     asset.tags.some(tag => tag.toLowerCase().includes(assetSearchQuery.value.toLowerCase()))
//   )
// })

// 计算是否应该禁用上传
const isUploadDisabled = computed(() => {
  return uploadedImages.value.length >= 1
})

// 计算是否可以生成模型
const canGenerate = computed(() => {
  if (isImageMode.value) {
    // 图片模式：需要有上传的图片
    return uploadedImages.value.length > 0
  } else {
    // 文字模式：需要有文字描述
    return promptText.value.trim().length > 0
  }
})

const emit = defineEmits(['model-selected', 'model-generated'])

// const toggleChatHistory = () => {
//   chatExpanded.value = !chatExpanded.value
// }

const beforeUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  // 显示上传中提示
  const loadingMessage = ElMessage({
    message: '图片上传中...',
    type: 'info',
    duration: 0,
  })

  try {
    // 上传图片到后端
    const response = await uploadImage(file)
    const imageUrl = response.url // 从响应中获取图片URL
    uploadedImageUrl.value = imageUrl

    // 创建本地预览URL
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedImages.value.push({
        name: file.name,
        url: e.target.result, // 本地预览URL
        serverUrl: imageUrl, // 服务器URL
      })
    }
    reader.readAsDataURL(file)

    ElMessage.success('图片上传成功!')
  } catch (error) {
    ElMessage.error('图片上传失败,请联系管理员')
  } finally {
    loadingMessage.close()
  }

  return false // 阻止自动上传
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
  // 如果删除了所有图片，清空服务器URL
  if (uploadedImages.value.length === 0) {
    uploadedImageUrl.value = ''
  }
}

const generateModel = async () => {
  if (!canGenerate.value) return

  generating.value = true

  try {
    let response = null
    const userId = 1 // 这里应该从用户状态或登录信息中获取

    if (isImageMode.value) {
      // 图片模式生成
      const params = {
        userId: 1,
        inputType: 'image',
        inputData: uploadedImageUrl.value,
        ...(selectedStyle.value && { style: selectedStyle.value }),
      }
      response = await generateModelFromImage(params)
    } else {
      // 文字模式生成
      const params = {
        userId: userId,
        inputType: 'text',
        prompt: promptText.value.trim(),
        ...(selectedStyle.value && { style: selectedStyle.value }),
      }
      response = await generateModelFromText(params)
      console.log('id', response)
    }

    // 从响应中获取模型ID
    const modelId = response.id

    // 存储生成的模型信息
    const modelInfo = {
      id: modelId,
    }
    generatedModels.value.push(modelInfo)

    // 开始轮询资产更新
    startAssetPolling(modelId)

    // 发送模型生成完成事件给父组件，传递模型ID
    emit('model-generated', {
      modelId: modelId,
    })

    ElMessage.success('模型生成成功！')

    // 清空当前模式的输入
    if (isImageMode.value) {
      uploadedImages.value = []
      uploadedImageUrl.value = ''
    } else {
      promptText.value = ''
    }
    selectedStyle.value = '' // 清空样式选择
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    generating.value = false
  }
}

// 图片预览相关
const previewDialogVisible = ref(false)
const currentPreviewAsset = ref(null)
const imageRefs = ref(new Map())

// 设置图片引用
const setImageRef = (el, assetId) => {
  if (el) {
    imageRefs.value.set(assetId, el)
  } else {
    imageRefs.value.delete(assetId)
  }
}

const selectAsset = (asset) => {
  emit('model-selected', asset)
}

const previewAsset = (asset) => {
  // 直接触发 el-image 的预览功能
  const imageEl = imageRefs.value.get(asset.id)
  if (imageEl && imageEl.$el) {
    // 触发 el-image 的点击事件来打开预览
    const imgElement = imageEl.$el.querySelector('img')
    if (imgElement) {
      imgElement.click()
    }
  }
}

const handlePreviewClose = () => {
  previewDialogVisible.value = false
  currentPreviewAsset.value = null
}

const importAsset = (asset) => {
  console.log('asset', asset)

  emit('model-generated', {
    modelId: asset.id,
  })
  assets.value.forEach((a) => {
    a.isNew = false
  })
  asset.isNew = true
}


// 上传组件
const upload = ref()
const fileList = ref([])
const handleExceed = (files) => {
  console.log('超过')
  upload.value.clearFiles()
  const file = files[0]
  
  upload.value.handleStart(file)
}

// 样式选择器
const selectedStyle = ref('')

const styleOptions = [
  {
    value: 'person:person2cartoon',
    label: '人物卡通',
  },
  {
    value: 'object:clay',
    label: '粘土',
  },
  {
    value: 'object:steampunk',
    label: '蒸汽朋克',
  },
  {
    value: 'animal:venom',
    label: '毒液',
  },
  {
    value: 'object:barbie',
    label: '芭比',
  },
  {
    value: 'object:christmas',
    label: '圣诞',
  },
  {
    value: 'gold',
    label: '金',
  },
  {
    value: 'ancient_bronze',
    label: '古铜器',
  },
]

// 存储上传的图片URL和生成的模型URL
const uploadedImageUrl = ref('')
const generatedModels = ref([]) // 存储生成的模型信息

// 资产轮询相关状态
const isAssetPolling = ref(false)
const assetPollingTimer = ref(null)
const assetLoadingStatus = ref('') // 'loading', 'success', 'failed', ''
const lastGeneratedModelId = ref(null) // 记录最后生成的模型ID
const assetPollingStartTime = ref(0)
const assetProgressPercentage = ref(0)
const assetProgressText = ref('')

// 资产轮询配置
const ASSET_POLLING_INTERVAL = 10 * 1000 // 10秒
const ASSET_POLLING_TIMEOUT = 10 * 60 * 1000 // 10分钟

// 获取模型记录
const getModelHistory = async () => {
  try {
    const res = await getModelHistoryApi()
    console.log('模型记录:', res)
    assets.value = res.map((item) => ({
      id: item.id,
      thumbnail: item.previewUrl,
      isNew: false,
      status: item.status
    })).filter((item) => item.status !== "FAILED" )
    if (assets.value.length > 0) {
      assets.value[0].isNew = true
    }
    return res
  } catch (error) {
    console.error('获取模型记录失败:', error)
    throw error
  }
}

// 轮询检查资产更新
const pollAssetUpdate = async () => {
  try {
    const res = await getModelHistoryApi()
    const newAssets = res.map((item) => ({
      id: item.id,
      thumbnail: item.previewUrl,
      isNew: false,
    }))

    // 检查是否有新的资产（特别是刚生成的模型）
    if (lastGeneratedModelId.value) {
      const newAsset = newAssets.find((asset) => asset.id === lastGeneratedModelId.value)
      if (newAsset && newAsset.thumbnail) {
        // 找到了新生成的模型且有缩略图，停止轮询
        newAsset.isNew = true // 标记为新资产
        assets.value = newAssets
        assetLoadingStatus.value = 'success'
        stopAssetPolling()
        ElMessage.success('资产图片生成完成！')
        return
      }
    }

    // 更新资产列表
    assets.value = newAssets
    console.log('资产轮询中...', res.length, '个资产')
  } catch (error) {
    console.error('轮询获取资产失败:', error)
  }
}

// 开始资产轮询
const startAssetPolling = (modelId) => {
  if (isAssetPolling.value) return

  isAssetPolling.value = true
  assetLoadingStatus.value = 'loading'
  lastGeneratedModelId.value = modelId
  assetPollingStartTime.value = Date.now()
  assetProgressPercentage.value = 0
  assetProgressText.value = '正在生成资产图片...'

  console.log('开始轮询资产更新，模型ID:', modelId)

  // 立即执行一次
  pollAssetUpdate()

  // 设置轮询定时器
  assetPollingTimer.value = setInterval(() => {
    updateAssetProgress()
    pollAssetUpdate()
  }, ASSET_POLLING_INTERVAL)

  // 设置超时定时器
  setTimeout(() => {
    if (isAssetPolling.value) {
      stopAssetPolling()
      assetLoadingStatus.value = 'failed'
      ElMessage.warning('资产图片生成超时，但模型已生成成功')
    }
  }, ASSET_POLLING_TIMEOUT)
}

// 停止资产轮询
const stopAssetPolling = () => {
  isAssetPolling.value = false

  if (assetPollingTimer.value) {
    clearInterval(assetPollingTimer.value)
    assetPollingTimer.value = null
  }

  lastGeneratedModelId.value = null
}

// 更新资产进度显示
const updateAssetProgress = () => {
  const elapsed = Date.now() - assetPollingStartTime.value
  const percentage = Math.min((elapsed / ASSET_POLLING_TIMEOUT) * 100, 95)
  assetProgressPercentage.value = Math.floor(percentage)

  const minutes = Math.floor(elapsed / 60000)
  const seconds = Math.floor((elapsed % 60000) / 1000)

  if (minutes === 0) {
    assetProgressText.value = `生成中... ${seconds}s`
  } else {
    assetProgressText.value = `生成中... ${minutes}m ${seconds}s`
  }
}

// 重试资产生成
const retryAssetGeneration = () => {
  if (lastGeneratedModelId.value) {
    assetLoadingStatus.value = ''
    startAssetPolling(lastGeneratedModelId.value)
  }
}

onMounted(() => {
  getModelHistory()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAssetPolling()
})
</script>

<style>
.right-column {
  background: var(--app-panel-bg);
  border-left: 1px solid var(--app-border-color);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.gen-panel {
  flex: 0 0 auto;
  border-bottom: 1px solid var(--app-border-color);
  padding: 16px;
}

.asset-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.mode {
  display: flex;
  align-items: center;
  flex-direction: row;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 8px;
}

.mode-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6c757d;
  transition: color 0.3s ease;
}

.mode .el-switch {
  --el-switch-on-color: #409eff;
}

.mode-select {
  flex: 1;
  justify-self: end;
  margin-left: auto;
}

.prompt-section {
  margin-bottom: 16px;
}

.upload-section {
  margin-bottom: 16px;
}

.image-uploader {
  width: 100%;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #909399;
  transition: all 0.3s ease;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.uploaded-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.image-preview {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
}

.generate-section {
  margin-bottom: 16px;
}

.generate-btn {
  width: 100%;
  height: 44px;
}

.chat-history {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
}

.chat-messages {
  max-height: 200px;
  overflow-y: auto;
}

.chat-message {
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
}

.chat-message.user {
  background: #e6f7ff;
  margin-left: 20px;
}

.chat-message.assistant {
  background: #f0f2f5;
  margin-right: 20px;
}

.message-content {
  margin-bottom: 4px;
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
}

.search-section {
  margin-bottom: 16px;
}

.asset-list {
  overflow-y: auto;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12px;
  padding: 8px 4px;
  max-height: calc(100vh - 400px);
  scroll-behavior: smooth;
}

.asset-list::-webkit-scrollbar {
  width: 8px;
}

.asset-list::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 4px;
}

.asset-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
  transition: background 0.3s ease;
}

.asset-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.asset-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  /* background: #fff; */
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 12px;
  min-height: 80px;
}

.asset-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.asset-card.highlighted {
  border-color: #67c23a;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}

.asset-thumbnail {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  flex-shrink: 0;
}

.asset-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.asset-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.asset-card:hover .asset-overlay {
  opacity: 1;
}

.asset-info {
  flex: 1;
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.asset-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #303133;
}

.asset-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.no-resize textarea {
  resize: none !important;
  /* 禁止拉伸 */
}

/* 预览对话框样式 */
.preview-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-image {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  min-height: 200px;
}

.preview-info {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.preview-info h4 {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.preview-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.preview-actions {
  display: flex;
  justify-content: center;
}

.preview-actions .el-button {
  padding: 12px 24px;
  font-size: 14px;
}

/* 资产加载状态样式 */
.asset-loading-overlay,
.asset-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(2px);
  border-radius: 8px;
}

.asset-loading-content,
.asset-error-content {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 280px;
}

.asset-loading-icon,
.asset-error-icon {
  margin-bottom: 12px;
  color: #409eff;
}

.asset-error-icon {
  color: #f56c6c;
}

.asset-loading-icon {
  animation: spin 2s linear infinite;
}

.asset-loading-text h4,
.asset-error-text h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.asset-loading-text p,
.asset-error-text p {
  margin: 0 0 16px 0;
  font-size: 13px;
  color: #606266;
}

.asset-loading-progress {
  margin-top: 16px;
}

.asset-progress-text {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: #909399;
}

/* 暗黑模式下的资产加载样式 */
:global(.dark) .asset-loading-overlay,
:global(.dark) .asset-error-overlay {
  background: rgba(0, 0, 0, 0.9);
}

:global(.dark) .asset-loading-content,
:global(.dark) .asset-error-content {
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid #4c4d4f;
  color: #e5eaf3;
}

:global(.dark) .asset-loading-text h4,
:global(.dark) .asset-error-text h4 {
  color: #e5eaf3;
}

:global(.dark) .asset-loading-text p,
:global(.dark) .asset-error-text p {
  color: #a3a6ad;
}

/* 资产列表需要相对定位以支持遮罩 */
.asset-list {
  position: relative;
}
</style>
