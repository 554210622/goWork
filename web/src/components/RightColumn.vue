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

        <el-select size="small" class="mode-select" v-model="selectedStyle" placeholder="风格选择(可选)" style="width: 240px">
          <el-option v-for="item in styleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

      </div>

      <!-- 文字模式 - 提示词输入 -->
      <div v-if="!isImageMode" class="prompt-section">
        <el-input v-model="promptText" type="textarea" :rows="4" placeholder="描述你想要生成的3D模型..." maxlength="500"
          show-word-limit class="no-resize" />
      </div>

      <!-- 图片模式 - 图片上传 -->
      <div v-if="isImageMode" class="upload-section">
        <el-upload class="image-uploader" :show-file-list="false" :before-upload="beforeUpload" accept="image/*" drag
          :limit="1" :on-exceed="handleExceed" ref="upload" v-model:file-list="fileList" :disabled="isUploadDisabled">
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
            <el-button :icon="Close" circle size="small" class="remove-btn" @click="removeImage(index)" />
          </div>
        </div>
      </div>

      <!-- 生成按钮 -->
      <div class="generate-section">
        <el-button type="primary" size="large" :loading="generating" :disabled="!canGenerate" @click="generateModel"
          class="generate-btn">
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
      <div class="search-section">
        <el-input v-model="assetSearchQuery" placeholder="搜索资产..." :prefix-icon="Search" clearable />
      </div>

      <!-- 资产列表 -->
      <div class="asset-list">
        <div v-for="asset in filteredAssets" :key="asset.id" class="asset-card" :class="{ highlighted: asset.isNew }"
          @click="selectAsset(asset)">
          <div class="asset-thumbnail">
            <img :src="asset.thumbnail" :alt="asset.name" />
            <div class="asset-overlay">
              <el-button :icon="View" circle size="small" @click.stop="previewAsset(asset)" />
              <el-button :icon="Download" circle size="small" @click.stop="importAsset(asset)" />
            </div>
          </div>
          <div class="asset-info">
            <div class="asset-name">{{ asset.name }}</div>
            <div class="asset-tags">
              <el-tag v-for="tag in asset.tags" :key="tag" size="small" type="info">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Plus,
  Close,
  Star,
  ArrowUp,
  ArrowDown,
  Delete,
  Box,
  Search,
  View,
  Download,
  EditPen,
  Picture
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadImage, generateModelFromText, generateModelFromImage } from '@/api/modelGeneration'

const promptText = ref('')
const generating = ref(false)
const chatExpanded = ref(false)
const uploadedImages = ref([])
const assetSearchQuery = ref('')
const isImageMode = ref(false) // 默认为文字模式

// 聊天记录
const chatHistory = ref([
  {
    id: 1,
    type: 'user',
    content: '生成一个现代风格的沙发',
    timestamp: new Date('2024-01-15 14:30')
  },
  {
    id: 2,
    type: 'assistant',
    content: '已为您生成现代风格沙发模型，请查看3D视图',
    timestamp: new Date('2024-01-15 14:31')
  }
])

// 资产数据
const assets = ref([
  {
    id: 1,
    name: '现代沙发',
    thumbnail: 'https://via.placeholder.com/120x120/409EFF/fff?text=沙发',
    tags: ['家具', '现代'],
    isNew: true,
    modelPath: '/models/sofa.glb'
  },
  {
    id: 2,
    name: '科幻机器人',
    thumbnail: 'https://via.placeholder.com/120x120/67C23A/fff?text=机器人',
    tags: ['科幻', '角色'],
    isNew: false,
    modelPath: '/models/robot.glb'
  },
  {
    id: 3,
    name: '建筑模型',
    thumbnail: 'https://via.placeholder.com/120x120/E6A23C/fff?text=建筑',
    tags: ['建筑', '场景'],
    isNew: false,
    modelPath: '/models/building.glb'
  }
])

const filteredAssets = computed(() => {
  if (!assetSearchQuery.value) return assets.value
  return assets.value.filter(asset =>
    asset.name.toLowerCase().includes(assetSearchQuery.value.toLowerCase()) ||
    asset.tags.some(tag => tag.toLowerCase().includes(assetSearchQuery.value.toLowerCase()))
  )
})

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

const toggleChatHistory = () => {
  chatExpanded.value = !chatExpanded.value
}

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
      duration: 0
    })

  try {
    

    // 上传图片到后端
    const imageUrl = await uploadImage(file)
    uploadedImageUrl.value = imageUrl

    // 创建本地预览URL
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedImages.value.push({
        name: file.name,
        url: e.target.result, // 本地预览URL
        serverUrl: imageUrl   // 服务器URL
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

  // 根据模式添加不同的用户消息到聊天记录
  const userContent = isImageMode.value
    ? `上传了 ${uploadedImages.value.length} 张图片进行模型生成`
    : promptText.value

  chatHistory.value.push({
    id: Date.now(),
    type: 'user',
    content: userContent,
    timestamp: new Date()
  })

  try {
    let modelUrl = ''
    const userId = 1 // 这里应该从用户状态或登录信息中获取

    if (isImageMode.value) {
      // 图片模式生成
      const params = {
        userId: userId,
        img_url: uploadedImageUrl.value,
        ...(selectedStyle.value && { style: selectedStyle.value })
      }
      modelUrl = await generateModelFromImage(params)
    } else {
      // 文字模式生成
      const params = {
        userId: userId,
        prompt: promptText.value.trim(),
        ...(selectedStyle.value && { style: selectedStyle.value })
      }
      modelUrl = await generateModelFromText(params)
    }

    // 存储生成的模型信息
    const modelInfo = {
      id: Date.now(),
      name: `${isImageMode.value ? '图片生成' : 'AI生成'}的模型 ${assets.value.length + 1}`,
      downloadUrl: modelUrl,
      style: selectedStyle.value,
      createdAt: new Date(),
      inputType: isImageMode.value ? 'image' : 'text',
      inputContent: isImageMode.value ? uploadedImageUrl.value : promptText.value.trim()
    }
    generatedModels.value.push(modelInfo)

    // 添加助手回复
    chatHistory.value.push({
      id: Date.now() + 1,
      type: 'assistant',
      content: '模型生成完成！已添加到资产库中。',
      timestamp: new Date()
    })

    // 添加新生成的资产到资产库
    const newAsset = {
      id: modelInfo.id,
      name: modelInfo.name,
      thumbnail: 'https://via.placeholder.com/120x120/F56C6C/fff?text=新模型',
      tags: [isImageMode.value ? '图片生成' : 'AI生成'],
      isNew: true,
      modelPath: modelUrl,
      downloadUrl: modelUrl
    }
    assets.value.unshift(newAsset)

    // 发送模型生成完成事件给父组件，传递下载URL
    emit('model-generated', {
      downloadUrl: modelUrl,
      modelName: modelInfo.name,
      modelInfo: modelInfo
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
    
    // 添加错误消息到聊天记录
    chatHistory.value.push({
      id: Date.now() + 1,
      type: 'assistant',
      content: '模型生成失败，请重试。',
      timestamp: new Date()
    })
  } finally {
    generating.value = false
  }
}

const clearHistory = () => {
  chatHistory.value = []
}

const selectAsset = (asset) => {
  emit('model-selected', asset)
}

const previewAsset = (asset) => {
  console.log('预览资产:', asset)
}

const importAsset = (asset) => {
  emit('model-selected', asset)
  ElMessage.success(`已导入 ${asset.name}`)
}

const formatTime = (date) => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 上传组件
const upload = ref()
const fileList = ref([])
const handleExceed = (files) => {
  console.log("超过")
  upload.value.clearFiles()
  const file = files[0]
  file.uid = genFileId()
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

.mode-select{
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
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.asset-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
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
  width: 100%;
  height: 100px;
  overflow: hidden;
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
  padding: 8px;
}

.asset-name {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
</style>