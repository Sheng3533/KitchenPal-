<template>
  <div class="h-screen flex overflow-hidden font-sans antialiased">
    <aside class="hidden md:flex flex-col text-white overflow-y-auto no-scrollbar flex-shrink-0" 
           :style="{ width: sidebarCollapsed ? '64px' : 'var(--sidebar-width)', minWidth: sidebarCollapsed ? '64px' : 'var(--sidebar-width)', background: '#1a1a1a' }">
      <div class="flex items-center gap-3 px-5 py-5 pb-6 border-b border-white/10">
        <div class="w-9 h-9 rounded-md flex items-center justify-center flex-shrink-0" style="background: var(--brand-primary);">
          <Flame class="w-5 h-5 text-white" />
        </div>
        <span v-if="!sidebarCollapsed" class="text-lg font-semibold whitespace-nowrap tracking-tight">AI厨房助手</span>
      </div>
      
      <nav class="flex-1 flex flex-col gap-1 p-3">
        <router-link v-for="item in navItems" :key="item.key"
                     :to="item.path"
                     class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium cursor-pointer transition-all duration-150 no-underline border-l-3 relative"
                     :class="{ 'text-white': isActive(item.key) }"
                     :style="isActive(item.key) ? activeNavStyle : inactiveNavStyle">
          <component :is="item.icon" class="w-[18px] h-[18px] flex-shrink-0 opacity-80"
                     :style="isActive(item.key) ? { color: 'var(--brand-primary)', opacity: 1 } : {}" />
          <span v-if="!sidebarCollapsed">{{ item.label }}</span>
        </router-link>
      </nav>
      
      <div class="flex flex-col gap-1 p-3 pb-4 border-t border-white/10">
        <router-link to="/settings"
                     class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium cursor-pointer transition-all duration-150 no-underline"
                     :style="isActive('settings') ? activeNavStyle : inactiveNavStyle">
          <Settings class="w-[18px] h-[18px] flex-shrink-0 opacity-80"
                    :style="isActive('settings') ? { color: 'var(--brand-primary)', opacity: 1 } : {}" />
          <span v-if="!sidebarCollapsed">设置</span>
        </router-link>
        <a @click="handleLogout"
           class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium cursor-pointer transition-all duration-150 border-l-3 border-transparent"
           style="color: rgba(255,255,255,0.6);">
          <LogOut class="w-[18px] h-[18px] flex-shrink-0 opacity-80" />
          <span v-if="!sidebarCollapsed">退出登录</span>
        </a>
      </div>
    </aside>

    <div class="flex-1 flex flex-col min-w-0 bg-background">
      <header class="h-14 min-h-14 flex items-center justify-between px-6 border-b border-border bg-white flex-shrink-0 relative">
        <div class="flex items-center min-w-0 gap-3">
          <button @click="mobileMenuOpen = !mobileMenuOpen" class="md:hidden p-2 rounded-md hover:bg-muted transition-colors">
            <Menu class="w-5 h-5 text-muted-foreground" />
          </button>
          <button @click="toggleSidebar" class="hidden md:flex p-2 rounded-md hover:bg-muted transition-colors">
            <PanelLeft class="w-5 h-5 text-muted-foreground" />
          </button>
          <h1 class="text-xl font-semibold text-foreground whitespace-nowrap overflow-hidden text-ellipsis">
            {{ currentTitle }}
          </h1>
        </div>
        <div class="flex items-center gap-3 flex-shrink-0">
          <div class="hidden sm:flex items-center gap-2 bg-muted border border-input rounded-full px-4 py-1 w-64 transition-colors duration-150 focus-within:ring-2 focus-within:ring-primary/20 relative"
               style="outline: none;">
            <Search class="w-4 h-4 text-muted-foreground flex-shrink-0" />
            <input v-model="searchQuery" type="text" placeholder="搜索食谱、用户、通知..." 
                   class="border-none outline-none bg-transparent text-sm text-foreground w-full font-sans"
                   @focus="searchOpen = true"
                   @blur="handleSearchBlur">
            <div v-if="searchOpen && searchResults.length > 0" 
                 class="absolute top-full left-0 right-0 mt-1 bg-white border border-border rounded-lg shadow-lg overflow-hidden z-50 max-h-80 overflow-y-auto">
              <div v-for="group in searchResultGroups" :key="group.type" class="border-b border-border last:border-b-0">
                <div class="px-3 py-2 text-xs font-medium text-muted-foreground bg-muted/50">{{ group.label }}</div>
                <div v-for="item in group.items" :key="item.id + item.type"
                     class="px-3 py-2 flex items-center gap-3 cursor-pointer hover:bg-muted transition-colors"
                     @mousedown.prevent="handleSearchClick(item)">
                  <component :is="item.icon" class="w-4 h-4 text-muted-foreground flex-shrink-0" />
                  <div class="flex-1 min-w-0">
                    <div class="text-sm text-foreground truncate">{{ item.title }}</div>
                    <div class="text-xs text-muted-foreground truncate">{{ item.subtitle }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else-if="searchOpen && searchQuery && searchResults.length === 0"
                 class="absolute top-full left-0 right-0 mt-1 bg-white border border-border rounded-lg shadow-lg z-50 p-4 text-center">
              <div class="text-sm text-muted-foreground">没有找到相关结果</div>
            </div>
          </div>

          <div class="relative">
            <button @click="notificationOpen = !notificationOpen"
                    class="w-9 h-9 flex items-center justify-center rounded-full cursor-pointer text-muted-foreground hover:bg-muted hover:text-foreground transition-all duration-150">
              <Bell class="w-[18px] h-[18px]" />
              <span v-if="unreadCount > 0" class="absolute top-1 right-1 min-w-[16px] h-4 px-1 flex items-center justify-center text-[10px] font-semibold text-white rounded-full" style="background: var(--state-error);">
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </span>
            </button>
            <Teleport to="body">
              <div v-if="notificationOpen" class="fixed inset-0 z-40" @click="notificationOpen = false">
                <div class="absolute top-14 right-4 w-80 bg-white border border-border rounded-lg shadow-xl overflow-hidden z-50" @click.stop>
                  <div class="flex items-center justify-between px-4 py-3 border-b border-border">
                    <div class="font-semibold text-foreground">通知中心</div>
                    <button v-if="unreadCount > 0" @click="handleMarkAllRead" class="text-xs text-orange-500 hover:text-orange-600 cursor-pointer">
                      全部已读
                    </button>
                  </div>
                  <div class="max-h-96 overflow-y-auto">
                    <div v-if="notifications.length === 0" class="py-8 text-center">
                      <Bell class="w-8 h-8 text-muted-foreground mx-auto mb-2 opacity-50" />
                      <div class="text-sm text-muted-foreground">暂无通知</div>
                    </div>
                    <div v-else>
                      <div v-for="item in notifications" :key="item.id"
                           class="px-4 py-3 border-b border-border last:border-b-0 cursor-pointer hover:bg-muted/50 transition-colors"
                           :class="{ 'bg-orange-50/50': !item.isRead }"
                           @click="handleNotificationClick(item)">
                        <div class="flex items-start gap-3">
                          <div class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
                               :style="item.type === 'system' ? 'background: #dbeafe;' : item.type === 'update' ? 'background: #dcfce7;' : 'background: #fef3c7;'">
                            <component :is="item.icon" class="w-4 h-4"
                                       :style="item.type === 'system' ? 'color: #2563eb;' : item.type === 'update' ? 'color: #16a34a;' : 'color: #d97706;'" />
                          </div>
                          <div class="flex-1 min-w-0">
                            <div class="text-sm font-medium text-foreground truncate">{{ item.title }}</div>
                            <div class="text-xs text-muted-foreground mt-0.5 line-clamp-2">{{ item.content }}</div>
                            <div class="text-xs text-muted-foreground mt-1">{{ item.time }}</div>
                          </div>
                          <div v-if="!item.isRead" class="w-2 h-2 rounded-full flex-shrink-0 mt-1.5" style="background: var(--state-error);"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="px-4 py-2 border-t border-border text-center">
                    <router-link to="/notifications" @click="notificationOpen = false" class="text-xs text-orange-500 hover:text-orange-600 cursor-pointer">
                      查看全部通知 →
                    </router-link>
                  </div>
                </div>
              </div>
            </Teleport>
          </div>

          <div class="relative">
            <button @click="profileOpen = !profileOpen"
                    class="w-[34px] h-[34px] rounded-full flex items-center justify-center cursor-pointer text-xs font-semibold text-white flex-shrink-0 transition-shadow duration-150 hover:shadow-[0_0_0_2px_rgba(249,115,22,0.25)]"
                    style="background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-dark));">
              {{ adminInitial }}
            </button>
            <Teleport to="body">
              <div v-if="profileOpen" class="fixed inset-0 z-40" @click="profileOpen = false">
                <div class="absolute top-14 right-4 w-56 bg-white border border-border rounded-lg shadow-xl overflow-hidden z-50" @click.stop>
                  <div class="px-4 py-3 border-b border-border">
                    <div class="font-semibold text-foreground">{{ adminName }}</div>
                    <div class="text-xs text-muted-foreground mt-0.5">{{ adminPhone }}</div>
                  </div>
                  <div class="py-1">
                    <button @click="handleProfileClick" class="w-full px-4 py-2 flex items-center gap-3 text-sm text-foreground hover:bg-muted transition-colors cursor-pointer">
                      <User class="w-4 h-4 text-muted-foreground" />
                      <span>个人信息</span>
                    </button>
                    <button @click="handlePasswordClick" class="w-full px-4 py-2 flex items-center gap-3 text-sm text-foreground hover:bg-muted transition-colors cursor-pointer">
                      <Lock class="w-4 h-4 text-muted-foreground" />
                      <span>修改密码</span>
                    </button>
                    <button @click="handleSettingsClick" class="w-full px-4 py-2 flex items-center gap-3 text-sm text-foreground hover:bg-muted transition-colors cursor-pointer">
                      <Settings class="w-4 h-4 text-muted-foreground" />
                      <span>系统设置</span>
                    </button>
                  </div>
                  <div class="py-1 border-t border-border">
                    <button @click="handleLogout" class="w-full px-4 py-2 flex items-center gap-3 text-sm text-red-500 hover:bg-red-50 transition-colors cursor-pointer">
                      <LogOut class="w-4 h-4" />
                      <span>退出登录</span>
                    </button>
                  </div>
                </div>
              </div>
            </Teleport>
          </div>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-6 content-scroll">
        <router-view />
      </div>
    </div>

    <Teleport to="body">
      <div v-if="profileModalOpen" class="fixed inset-0 z-50 flex items-center justify-center" @click="profileModalOpen = false">
        <div class="absolute inset-0 bg-black/50"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 overflow-hidden" @click.stop>
          <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h3 class="text-lg font-semibold text-foreground">个人信息</h3>
            <button @click="profileModalOpen = false" class="p-1 rounded-md hover:bg-muted transition-colors cursor-pointer">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6">
            <div class="flex flex-col items-center mb-6">
              <div class="w-20 h-20 rounded-full flex items-center justify-center text-2xl font-semibold text-white"
                   style="background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-dark));">
                {{ adminInitial }}
              </div>
              <div class="mt-3 text-base font-semibold text-foreground">{{ profileForm.nickname }}</div>
              <div class="text-sm text-muted-foreground">{{ profileForm.phone }}</div>
            </div>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium mb-2 text-foreground">昵称</label>
                <input v-model="profileForm.nickname" type="text" class="input-field">
              </div>
              <div>
                <label class="block text-sm font-medium mb-2 text-foreground">手机号</label>
                <input v-model="profileForm.phone" type="text" class="input-field" disabled style="opacity: 0.6; cursor: not-allowed;">
              </div>
              <div v-if="profileForm.createdAt">
                <label class="block text-sm font-medium mb-2 text-foreground">注册时间</label>
                <input :value="profileForm.createdAt" type="text" class="input-field" disabled style="opacity: 0.6; cursor: not-allowed;">
              </div>
            </div>
          </div>
          <div class="flex justify-end gap-3 px-6 py-4 border-t border-border bg-muted/30">
            <button @click="profileModalOpen = false" class="btn-secondary">取消</button>
            <button @click="handleSaveProfile" :disabled="profileSaving" class="btn-primary">
              <Loader2 v-if="profileSaving" class="w-4 h-4 mr-2 animate-spin" />
              {{ profileSaving ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <div v-if="mobileMenuOpen" class="fixed inset-0 bg-black/50 z-40 md:hidden" @click="mobileMenuOpen = false">
      <aside class="absolute left-0 top-0 h-full flex flex-col text-white overflow-y-auto no-scrollbar"
             style="width: var(--sidebar-width); min-width: var(--sidebar-width); background: #1a1a1a;">
        <div class="flex items-center gap-3 px-5 py-5 pb-6 border-b border-white/10">
          <div class="w-9 h-9 rounded-md flex items-center justify-center flex-shrink-0" style="background: var(--brand-primary);">
            <Flame class="w-5 h-5 text-white" />
          </div>
          <span class="text-lg font-semibold whitespace-nowrap tracking-tight">AI厨房助手</span>
        </div>
        
        <nav class="flex-1 flex flex-col gap-1 p-3">
          <router-link v-for="item in navItems" :key="item.key"
                       :to="item.path"
                       @click="mobileMenuOpen = false"
                       class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium cursor-pointer transition-all duration-150 no-underline border-l-3 relative"
                       :class="{ 'text-white': isActive(item.key) }"
                       :style="isActive(item.key) ? activeNavStyle : inactiveNavStyle">
            <component :is="item.icon" class="w-[18px] h-[18px] flex-shrink-0 opacity-80"
                       :style="isActive(item.key) ? { color: 'var(--brand-primary)', opacity: 1 } : {}" />
            <span>{{ item.label }}</span>
          </router-link>
        </nav>
        
        <div class="flex flex-col gap-1 p-3 pb-4 border-t border-white/10">
          <router-link to="/settings" @click="mobileMenuOpen = false"
                       class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium cursor-pointer transition-all duration-150 no-underline"
                       :style="isActive('settings') ? activeNavStyle : inactiveNavStyle">
            <Settings class="w-[18px] h-[18px] flex-shrink-0 opacity-80"
                      :style="isActive('settings') ? { color: 'var(--brand-primary)', opacity: 1 } : {}" />
            <span>设置</span>
          </router-link>
          <a @click="handleLogout"
             class="flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium cursor-pointer transition-all duration-150 border-l-3 border-transparent"
             style="color: rgba(255,255,255,0.6);">
            <LogOut class="w-[18px] h-[18px] flex-shrink-0 opacity-80" />
            <span>退出登录</span>
          </a>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Flame, LayoutDashboard, BookOpen, Users, Bell, ChefHat, 
  Brain, Settings, LogOut, Search, PanelLeft, Menu, 
  User, Lock, AlertCircle, FileText, Clock, X, Loader2, MessageCircle, 
  Utensils, Newspaper, Video
} from 'lucide-vue-next'
import { getRecipeList } from '@/api/recipe'
import { getUserList, getUserInfo, updateUser } from '@/api/user'
import { getNotificationList, markAsRead, markAllAsRead } from '@/api/notification'

const route = useRoute()
const router = useRouter()

const sidebarCollapsed = ref(false)
const mobileMenuOpen = ref(false)

const navItems = [
  { key: 'dashboard', label: '数据看板', path: '/dashboard', icon: LayoutDashboard },
  { key: 'recipes', label: '食谱管理', path: '/recipes', icon: BookOpen },
  { key: 'users', label: '用户管理', path: '/users', icon: Users },
  { key: 'chefs', label: '大厨管理', path: '/chefs', icon: Utensils },
  { key: 'contents', label: '内容与活动', path: '/contents', icon: Newspaper },
  { key: 'videos', label: '视频教学', path: '/videos', icon: Video },
  { key: 'notifications', label: '通知管理', path: '/notifications', icon: Bell },
  { key: 'cooking-records', label: '烹饪记录', path: '/cooking-records', icon: ChefHat },
  { key: 'ai-logs', label: 'AI识别日志', path: '/ai-logs', icon: Brain },
  { key: 'assistant', label: 'AI助手', path: '/assistant', icon: MessageCircle },
]

const activeNavStyle = {
  background: 'rgba(249, 115, 22, 0.12)',
  color: '#ffffff',
  borderLeftColor: 'var(--brand-primary)'
}

const inactiveNavStyle = {
  background: 'transparent',
  color: 'rgba(255,255,255,0.6)',
  borderLeftColor: 'transparent'
}

const currentTitle = computed(() => route.meta.title || '')

const adminUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('admin_user') || '{}')
  } catch {
    return {}
  }
})

const adminName = computed(() => adminUser.value.nickname || '管理员')
const adminPhone = computed(() => adminUser.value.phone || 'admin')
const adminInitial = computed(() => adminName.value.charAt(0).toUpperCase())

function isActive(key) {
  return route.path.includes(key) || (key === 'dashboard' && route.path === '/dashboard')
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function handleLogout() {
  localStorage.removeItem('admin_logged_in')
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_user')
  profileOpen.value = false
  router.push('/login')
}

const searchQuery = ref('')
const searchOpen = ref(false)
const allRecipes = ref([])
const allUsers = ref([])
const allNotifications = ref([])

async function loadSearchData() {
  try {
    const [recipes, users, notifications] = await Promise.all([
      getRecipeList().catch(() => []),
      getUserList().catch(() => []),
      getNotificationList().catch(() => [])
    ])
    allRecipes.value = recipes || []
    allUsers.value = users || []
    allNotifications.value = notifications || []
  } catch {
  }
}

const searchResults = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return []
  
  const results = []
  
  allRecipes.value.forEach(r => {
    if (r.title?.toLowerCase().includes(q) || r.category?.toLowerCase().includes(q)) {
      results.push({
        type: 'recipe',
        id: r.id,
        title: r.title,
        subtitle: r.category || '食谱',
        icon: BookOpen,
        path: '/recipes'
      })
    }
  })
  
  allUsers.value.forEach(u => {
    if (u.nickname?.toLowerCase().includes(q) || u.phone?.includes(q)) {
      results.push({
        type: 'user',
        id: u.id,
        title: u.nickname || '用户',
        subtitle: u.phone || '',
        icon: Users,
        path: '/users'
      })
    }
  })
  
  allNotifications.value.forEach(n => {
    if (n.title?.toLowerCase().includes(q) || n.content?.toLowerCase().includes(q)) {
      results.push({
        type: 'notification',
        id: n.id,
        title: n.title,
        subtitle: n.content ? n.content.substring(0, 30) : '通知',
        icon: Bell,
        path: '/notifications'
      })
    }
  })
  
  return results.slice(0, 10)
})

const searchResultGroups = computed(() => {
  const groups = []
  const recipeItems = searchResults.value.filter(r => r.type === 'recipe')
  const userItems = searchResults.value.filter(r => r.type === 'user')
  const notificationItems = searchResults.value.filter(r => r.type === 'notification')
  
  if (recipeItems.length > 0) {
    groups.push({ type: 'recipe', label: '食谱', items: recipeItems })
  }
  if (userItems.length > 0) {
    groups.push({ type: 'user', label: '用户', items: userItems })
  }
  if (notificationItems.length > 0) {
    groups.push({ type: 'notification', label: '通知', items: notificationItems })
  }
  
  return groups
})

function handleSearchBlur() {
  setTimeout(() => {
    searchOpen.value = false
  }, 200)
}

function handleSearchClick(item) {
  searchOpen.value = false
  searchQuery.value = ''
  router.push(item.path)
}

const notificationOpen = ref(false)
const notifications = ref([])
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

function getNotificationIcon(type) {
  if (type === 'system') return AlertCircle
  if (type === 'update') return FileText
  return Bell
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const t = timeStr.replace('T', ' ').substring(0, 16)
  return t
}

async function loadNotifications() {
  try {
    const list = await getNotificationList()
    if (Array.isArray(list)) {
      notifications.value = list.slice(0, 10).map(n => ({
        id: n.id,
        title: n.title || '系统通知',
        content: n.content || '',
        time: formatTime(n.createTime || n.createdAt),
        isRead: n.isRead === 1 || n.isRead === true,
        type: n.type || 'system',
        icon: getNotificationIcon(n.type)
      }))
    }
  } catch {
    notifications.value = [
      { id: 1, title: '欢迎使用管理端', content: '您可以在这里管理所有系统功能', time: '刚刚', isRead: false, type: 'system', icon: AlertCircle },
      { id: 2, title: '系统更新通知', content: 'v1.0.0 版本已上线，包含多项功能优化', time: '1小时前', isRead: false, type: 'update', icon: FileText },
      { id: 3, title: '新用户注册', content: '有 5 位新用户注册了平台', time: '今天 10:30', isRead: true, type: 'info', icon: Bell },
    ]
  }
}

async function handleNotificationClick(item) {
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      const n = notifications.value.find(x => x.id === item.id)
      if (n) n.isRead = true
    } catch {
    }
  }
}

async function handleMarkAllRead() {
  try {
    const userStr = localStorage.getItem('admin_user') || '{}'
    const user = JSON.parse(userStr)
    const userId = user.id || 1
    await markAllAsRead(userId)
    notifications.value.forEach(n => n.isRead = true)
  } catch {
    notifications.value.forEach(n => n.isRead = true)
  }
}

const profileOpen = ref(false)
const profileModalOpen = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({
  id: null,
  nickname: '',
  phone: '',
  createdAt: ''
})

async function handleProfileClick() {
  profileOpen.value = false
  try {
    const userStr = localStorage.getItem('admin_user') || '{}'
    const user = JSON.parse(userStr)
    const userId = user.id
    if (userId) {
      const detail = await getUserInfo(userId)
      profileForm.id = detail.id
      profileForm.nickname = detail.nickname || ''
      profileForm.phone = detail.phone || ''
      profileForm.createdAt = detail.createdAt ? detail.createdAt.replace('T', ' ').substring(0, 16) : ''
    } else {
      profileForm.nickname = user.nickname || '管理员'
      profileForm.phone = user.phone || 'admin'
      profileForm.createdAt = ''
    }
  } catch {
    const userStr = localStorage.getItem('admin_user') || '{}'
    const user = JSON.parse(userStr)
    profileForm.id = user.id || null
    profileForm.nickname = user.nickname || '管理员'
    profileForm.phone = user.phone || 'admin'
    profileForm.createdAt = ''
  }
  profileModalOpen.value = true
}

async function handleSaveProfile() {
  if (!profileForm.nickname.trim()) {
    alert('请输入昵称')
    return
  }
  profileSaving.value = true
  try {
    const userId = profileForm.id
    if (userId) {
      await updateUser(userId, { nickname: profileForm.nickname })
    }
    const userStr = localStorage.getItem('admin_user') || '{}'
    const user = JSON.parse(userStr)
    user.nickname = profileForm.nickname
    localStorage.setItem('admin_user', JSON.stringify(user))
    profileModalOpen.value = false
    alert('个人信息保存成功')
  } catch (err) {
    alert('保存失败：' + (err?.message || '未知错误'))
  } finally {
    profileSaving.value = false
  }
}

function handlePasswordClick() {
  profileOpen.value = false
  router.push('/settings')
}

function handleSettingsClick() {
  profileOpen.value = false
  router.push('/settings')
}

watch(notificationOpen, (val) => {
  if (val) loadNotifications()
})

onMounted(() => {
  loadSearchData()
  loadNotifications()
})
</script>

<style scoped>
.border-l-3 {
  border-left-width: 3px;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
