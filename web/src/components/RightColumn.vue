<template>
  <el-aside width="360px" class="right-column">
    <!-- 生成面板 -->
    <div class="gen-panel">
      <div class="panel-header">
        <h3>AI 生成面板</h3>
        <el-button 
          :icon="chatExpanded ? ArrowUp : ArrowDown" 
          text 
          @click="toggleChatHistory"
        />
      </div>

      <!-- 提示词输入 -->
      <div class="prompt-section">
        <el-input
          v-model="promptText"
          type="textarea"
          :rows="4"
          placeholder="描述你想要生成的3D模型..."
          maxlength="500"
          show-word-limit
        />
      </div>

      <!-- 图片上传 -->
      <div class="upload-section">
        <el-upload
          class="image-uploader"
          :show-file-list="false"
          :before-upload="beforeUpload"
          accept="image/*"
          drag
        >
          <div class="upload-content">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <div class="upload-text">点击或拖拽上传参考图片</div>
          </div>
        </el-upload>
        
        <!-- 已上传的图片预览 -->
        <div v-if="uploadedImages.length" class="uploaded-images">
          <div 
            v-for="(image, index) in uploadedImages" 
            :key="index"
            class="image-preview"
          >
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
          :disabled="!promptText.trim()"
          @click="generateModel"
          class="generate-btn"
        >
          <el-icon><Star /></el-icon>
          {{ generating ? '生成中...' : '生成模型' }}
        </el-button>
      </div>

      <!-- 聊天记录 -->
      <el-collapse-transition>
        <div v-show="chatExpanded" class="chat-history">
          <div class="chat-header">
            <span>聊天记录</span>
            <el-button :icon="Delete" text size="small" @click="clearHistory">
              清空
            </el-button>
          </div>
          <div class="chat-messages">
            <div 
              v-for="message in chatHistory" 
              :key="message.id"
              class="chat-message"
              :class="message.type"
            >
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>
      </el-collapse-transition>
    </div>

    <!-- 资产栏 -->
    <div class="asset-panel">
      <div class="panel-header">
        <h3>资产库</h3>
        <el-badge :value="assets.length" type="info">
          <el-icon><Box /></el-icon>
        </el-badge>
      </div>

      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="assetSearchQuery"
          placeholder="搜索资产..."
          :prefix-icon="Search"
          clearable
        />
      </div>

      <!-- 资产列表 -->
      <div class="asset-list">
        <div 
          v-for="asset in filteredAssets" 
          :key="asset.id"
          class="asset-card"
          :class="{ highlighted: asset.isNew }"
          @click="selectAsset(asset)"
        >
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
              <el-tag 
                v-for="tag in asset.tags" 
                :key="tag" 
                size="small" 
                type="info"
              >
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
  Download 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const promptText = ref('')
const generating = ref(false)
const chatExpanded = ref(false)
const uploadedImages = ref([])
const assetSearchQuery = ref('')

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

const emit = defineEmits(['model-selected'])

const toggleChatHistory = () => {
  chatExpanded.value = !chatExpanded.value
}

const beforeUpload = (file) => {
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

  // 创建预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImages.value.push({
      name: file.name,
      url: e.target.result
    })
  }
  reader.readAsDataURL(file)

  return false // 阻止自动上传
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

const generateModel = async () => {
  if (!promptText.value.trim()) return

  generating.value = true
  
  // 添加用户消息到聊天记录
  chatHistory.value.push({
    id: Date.now(),
    type: 'user',
    content: promptText.value,
    timestamp: new Date()
  })

  try {
    // 模拟生成过程
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    // 添加助手回复
    chatHistory.value.push({
      id: Date.now() + 1,
      type: 'assistant',
      content: '模型生成完成！已添加到资产库中。',
      timestamp: new Date()
    })

    // 添加新生成的资产
    const newAsset = {
      id: Date.now(),
      name: `生成的模型 ${assets.value.length + 1}`,
      thumbnail: 'https://via.placeholder.com/120x120/F56C6C/fff?text=新模型',
      tags: ['AI生成'],
      isNew: true,
      modelPath: '/models/generated.glb'
    }
    assets.value.unshift(newAsset)

    ElMessage.success('模型生成成功！')
    promptText.value = ''
    uploadedImages.value = []
    
  } catch (error) {
    ElMessage.error('生成失败，请重试')
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
</script>

<style scoped>
.right-column {
  background: #fff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.gen-panel {
  flex: 0 0 auto;
  border-bottom: 1px solid #e4e7ed;
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
</style>