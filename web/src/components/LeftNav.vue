<template>
  <el-aside :width="collapsed ? '64px' : '280px'" class="left-nav">
    <!-- 收起/展开按钮 -->
    <div class="left-nav-collapse">
      <div class="collapse-btn" :class="[collapsed ? 'collapsed-btn_right' : '']" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
      </div>
    </div>

    <!-- 新建模型聊天 -->
    <div class="nav-section">
      <el-button v-if="!collapsed" type="primary" :icon="Plus" class="new-chat-btn" @click="createNewChat"
        :class="{ collapsed: collapsed }">
        <span  >新建模型生成</span>
      </el-button>
      <el-button v-else  type="primary" :icon="Plus" @click="createNewChat"
        :class="{ collapsed: collapsed }">
      </el-button>
      <!-- <el-icon class="btn_hover" v-else >
        <Plus/>
      </el-icon> -->
    </div>

    <!-- 搜索 -->
    <div class="nav-section" v-if="!collapsed">
      <el-input v-model="searchQuery" placeholder="搜索生成模型记录..." :prefix-icon="Search" clearable @input="handleSearch" />
    </div>

    <!-- 模型聊天列表 -->
    <div class="chat-list" >
      <div class="section-title" v-if="!collapsed">模型记录</div>
      <div class="chat-items">
        <div v-for="chat in filteredChats" :key="chat.id" class="chat-item"
          :class="{ active: chat.id === activeChatId, collapsed: collapsed }" @click="selectChat(chat.id)">
          <el-icon class="chat-icon">
            <ChatDotRound />
          </el-icon>
          <div class="chat-info" v-if="!collapsed">
            <div class="chat-title">{{ chat.title }}</div>
            <div class="chat-time">{{ formatTime(chat.updatedAt) }}</div>
          </div>
          <el-dropdown v-if="!collapsed" trigger="click" @command="(command) => handleChatAction(command, chat.id)"
            @click.stop>
            <el-icon size="20" class="more-icon">
              <MoreFilled />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Plus,
  Search,
  ChatDotRound,
  MoreFilled,
  Fold,
  Expand
} from '@element-plus/icons-vue'

const collapsed = ref(false)
const searchQuery = ref('')
const activeChatId = ref(1)

// 模拟聊天数据
const chats = ref([
  { id: 1, title: '3D动物', updatedAt: new Date('2024-01-15 14:30') },
])

const filteredChats = computed(() => {
  if (!searchQuery.value) return chats.value
  return chats.value.filter(chat =>
    chat.title.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const createNewChat = () => {
  console.log('创建新聊天')
}

const handleSearch = (value) => {
  console.log('搜索:', value)
}

const selectChat = (chatId) => {
  activeChatId.value = chatId
  console.log('选择聊天:', chatId)
}

const handleChatAction = (command, chatId) => {
  console.log('聊天操作:', command, chatId)
}

const formatTime = (date) => {
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}
</script>

<style scoped>
.left-nav {
  background: var(--app-sidebar-bg);
  border-right: 1px solid var(--app-border-color);
  transition: width 0.3s ease, background-color 0.3s ease, border-color 0.3s ease;
  position: relative;
  overflow: hidden;
}

.left-nav-collapse{
   min-height: 57px;
   border-bottom: 1px solid #e0e0e0;
}

.collapse-btn {
  position: absolute;
  top: 18px;
  right: 10px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  background: var(--el-bg-color);
  box-shadow: var(--app-shadow);
  z-index: 10;
  transition: all 0.3s ease;
  color: var(--el-text-color-primary);
}

.collapsed-btn_right{
  right: 18px;
}

.collapse-btn:hover {
  background: var(--el-fill-color-light);
  color: #409eff;
}

.nav-section {
  box-sizing: border-box;
  padding: 16px 8px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.new-chat-btn {
  width: 100%;
  height: 40px;
  transition: all 0.3s ease;
}

.new-chat-btn.collapsed {
  width: 40px;
  padding: 0;
}


.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.section-title {
  padding: 8px 16px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.chat-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.chat-item:hover {
  background: #e6f7ff;
}

.chat-item.active {
  background: #409eff;
  color: white;
}

.chat-item.collapsed {
  justify-content: center;
  padding: 12px 8px;
}

.chat-icon {
  font-size: 16px;
  margin-right: 12px;
  flex-shrink: 0;
}

.chat-item.collapsed .chat-icon {
  margin-right: 0;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 2px;
}

.more-icon {
  /* opacity: 0; */
  transition: opacity 0.2s ease;
  padding: 4px;
  border-radius: 4px;
}

/* .chat-item:hover .more-icon {
  opacity: 1;
} */

.more-icon:hover {
  background: rgba(0, 0, 0, 0.1);
}

.chat-item.active .more-icon:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>