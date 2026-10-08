<template>
  <div>
    <div class="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <h1 class="text-xl font-bold text-foreground">通知管理</h1>
      <button
        @click="handleSend"
        class="inline-flex items-center gap-2 px-4 py-2 text-white text-sm font-medium rounded-lg transition-colors"
        style="background: var(--brand-primary);"
        @mouseenter="hoverSendBtn = true"
        @mouseleave="hoverSendBtn = false"
        :style="{ background: hoverSendBtn ? 'var(--brand-primary-dark)' : 'var(--brand-primary)' }"
      >
        <Send class="w-4 h-4" />
        发送通知
      </button>
    </div>

    <div class="p-4 card mb-4">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索通知标题..."
            class="w-full pl-10 pr-4 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
            style="border-color: var(--border);"
            @focus="searchFocus = true"
            @blur="searchFocus = false"
            :style="{ borderColor: searchFocus ? 'var(--brand-primary)' : 'var(--border)', boxShadow: searchFocus ? '0 0 0 3px rgba(249, 115, 22, 0.1)' : 'none' }"
          />
        </div>

        <div class="relative min-w-[160px]">
          <select
            v-model="selectedType"
            class="w-full appearance-none pl-4 pr-10 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground cursor-pointer"
            style="border-color: var(--border);"
          >
            <option value="all">全部类型</option>
            <option value="system">系统通知</option>
            <option value="activity">活动通知</option>
            <option value="recipe">食谱更新</option>
            <option value="security">安全提醒</option>
          </select>
          <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
        </div>
      </div>
    </div>

    <div class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px]">
          <thead>
            <tr class="border-b" style="border-color: var(--border);">
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">标题</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">类型</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">内容预览</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">目标用户</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">发送时间</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">状态</th>
              <th class="text-right text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y" style="border-color: var(--border);">
            <tr
              v-for="item in paginatedNotifications"
              :key="item.id"
              class="transition-colors hover:bg-muted/30"
            >
              <td class="px-4 py-3">
                <div class="font-medium text-foreground text-sm">{{ item.title }}</div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <component :is="getTypeIcon(item.type)" class="w-4 h-4" :style="{ color: getTypeColor(item.type) }" />
                  <span class="text-foreground text-sm">{{ getTypeLabel(item.type) }}</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="text-sm text-muted-foreground truncate max-w-[240px]">{{ item.content || '-' }}</div>
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground whitespace-nowrap">
                {{ item.userId == null ? '全体用户' : `用户 #${item.userId}` }}
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground whitespace-nowrap">{{ item.createdAt || '-' }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="item.isRead
                    ? { backgroundColor: 'var(--state-success-light)', color: 'var(--state-success)' }
                    : { backgroundColor: 'var(--state-warning-light)', color: 'var(--state-warning)' }"
                >
                  {{ item.isRead ? '已读' : '未读' }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-end gap-1">
                  <button
                    @click="handleView(item)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    title="查看"
                  >
                    <Eye class="w-4 h-4 text-muted-foreground" />
                  </button>
                  <button
                    @click="handleEdit(item)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    title="编辑"
                  >
                    <Pencil class="w-4 h-4 text-muted-foreground" />
                  </button>
                  <button
                    @click="confirmDelete(item)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-state-error-light"
                    title="删除"
                  >
                    <Trash2 class="w-4 h-4" style="color: var(--state-error);" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredNotifications.length === 0">
              <td colspan="7" class="px-4 py-12 text-center">
                <div class="flex flex-col items-center gap-2">
                  <Search class="w-10 h-10 text-muted-foreground/50" />
                  <p class="text-sm text-muted-foreground">没有找到匹配的通知</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        v-if="filteredNotifications.length > 0"
        class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t"
        style="border-color: var(--border);"
      >
        <div class="text-sm text-muted-foreground">
          共 {{ total }} 条，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="flex items-center gap-1">
          <button
            @click="currentPage = 1"
            :disabled="currentPage === 1"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--border);"
          >
            <ChevronsLeft class="w-4 h-4 text-foreground" />
          </button>
          <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--border);"
          >
            <ChevronLeft class="w-4 h-4 text-foreground" />
          </button>
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="goToPage(page)"
            class="min-w-[36px] h-9 px-2 text-sm font-medium rounded-lg transition-colors"
            :class="currentPage === page ? 'text-white' : 'text-foreground hover:bg-muted'"
            :style="currentPage === page ? { backgroundColor: 'var(--brand-primary)' } : {}"
          >
            {{ page }}
          </button>
          <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--border);"
          >
            <ChevronRight class="w-4 h-4 text-foreground" />
          </button>
          <button
            @click="currentPage = totalPages"
            :disabled="currentPage === totalPages"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--border);"
          >
            <ChevronsRight class="w-4 h-4 text-foreground" />
          </button>
        </div>
      </div>
    </div>

    <!-- 发送/编辑通知弹窗 -->
    <Teleport to="body">
      <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeFormModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <h2 class="text-lg font-semibold text-foreground">{{ isEditing ? '编辑通知' : '发送通知' }}</h2>
            <button @click="closeFormModal" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
            <form @submit.prevent="handleSave" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">标题 *</label>
                <input
                  v-model="formData.title"
                  type="text"
                  required
                  placeholder="请输入通知标题"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField = 'title'"
                  @blur="focusField = ''"
                  :style="{ borderColor: focusField === 'title' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">内容 *</label>
                <textarea
                  v-model="formData.content"
                  rows="5"
                  required
                  placeholder="请输入通知内容"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground resize-none"
                  style="border-color: var(--border);"
                  @focus="focusField = 'content'"
                  @blur="focusField = ''"
                  :style="{ borderColor: focusField === 'content' ? 'var(--brand-primary)' : 'var(--border)' }"
                ></textarea>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">类型 *</label>
                  <select
                    v-model="formData.type"
                    required
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground cursor-pointer"
                    style="border-color: var(--border);"
                  >
                    <option value="system">系统通知</option>
                    <option value="activity">活动通知</option>
                    <option value="recipe">食谱更新</option>
                    <option value="security">安全提醒</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">目标用户ID</label>
                  <input
                    v-model="formData.userIdInput"
                    type="number"
                    placeholder="留空表示全体用户"
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                    style="border-color: var(--border);"
                    @focus="focusField = 'userId'"
                    @blur="focusField = ''"
                    :style="{ borderColor: focusField === 'userId' ? 'var(--brand-primary)' : 'var(--border)' }"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">图标URL（可选）</label>
                <input
                  v-model="formData.iconUrl"
                  type="text"
                  placeholder="请输入图标URL"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                />
              </div>

              <div class="flex items-center justify-end gap-3 pt-4">
                <button type="button" @click="closeFormModal" class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors">
                  取消
                </button>
                <button type="submit" :disabled="saving" class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50" style="background: var(--brand-primary);">
                  <span v-if="saving">保存中...</span>
                  <span v-else>{{ isEditing ? '保存修改' : '发送通知' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 查看详情弹窗 -->
    <Teleport to="body">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="showDetailModal = false"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <h2 class="text-lg font-semibold text-foreground">通知详情</h2>
            <button @click="showDetailModal = false" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]" v-if="detailData">
            <div class="flex items-start gap-3 mb-5">
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center shrink-0"
                :style="{ backgroundColor: getTypeColor(detailData.type) + '22' }"
              >
                <component :is="getTypeIcon(detailData.type)" class="w-5 h-5" :style="{ color: getTypeColor(detailData.type) }" />
              </div>
              <div class="flex-1 min-w-0">
                <h3 class="text-base font-semibold text-foreground break-words">{{ detailData.title }}</h3>
                <div class="flex items-center gap-2 mt-1">
                  <span
                    class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
                    :style="{ backgroundColor: getTypeColor(detailData.type) + '22', color: getTypeColor(detailData.type) }"
                  >
                    {{ getTypeLabel(detailData.type) }}
                  </span>
                  <span
                    class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
                    :style="detailData.isRead
                      ? { backgroundColor: 'var(--state-success-light)', color: 'var(--state-success)' }
                      : { backgroundColor: 'var(--state-warning-light)', color: 'var(--state-warning)' }"
                  >
                    {{ detailData.isRead ? '已读' : '未读' }}
                  </span>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-5 py-4 border-y" style="border-color: var(--border);">
              <div>
                <div class="text-xs text-muted-foreground mb-1">目标用户</div>
                <div class="text-sm font-medium text-foreground">
                  {{ detailData.userId == null ? '全体用户' : `用户 #${detailData.userId}` }}
                </div>
              </div>
              <div>
                <div class="text-xs text-muted-foreground mb-1">发送时间</div>
                <div class="text-sm font-medium text-foreground">{{ detailData.createdAt || '-' }}</div>
              </div>
            </div>

            <div class="mb-5">
              <div class="text-xs text-muted-foreground mb-2">通知内容</div>
              <div class="text-sm text-foreground leading-relaxed whitespace-pre-wrap bg-muted/30 rounded-lg p-4">
                {{ detailData.content || '（无内容）' }}
              </div>
            </div>

            <div v-if="detailData.iconUrl" class="mb-5">
              <div class="text-xs text-muted-foreground mb-2">图标</div>
              <img :src="detailData.iconUrl" alt="icon" class="w-16 h-16 rounded-lg object-cover" />
            </div>

            <div class="flex items-center justify-end gap-3 pt-4 border-t" style="border-color: var(--border);">
              <button @click="showDetailModal = false" class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors">
                关闭
              </button>
              <button
                v-if="!detailData.isRead"
                @click="handleMarkAsRead(detailData)"
                :disabled="markingRead"
                class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50"
                style="background: var(--brand-primary);"
              >
                <span v-if="markingRead">处理中...</span>
                <span v-else>标记为已读</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 删除确认弹窗 -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="showDeleteModal = false"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-sm">
          <div class="px-6 py-4">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-full flex items-center justify-center" style="background: var(--state-error-light);">
                <AlertCircle class="w-5 h-5" style="color: var(--state-error);" />
              </div>
              <h3 class="text-lg font-semibold text-foreground">确认删除</h3>
            </div>
            <p class="text-sm text-muted-foreground">确定要删除通知「{{ deletingItem?.title }}」吗？此操作无法撤销。</p>
          </div>
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t" style="border-color: var(--border);">
            <button @click="showDeleteModal = false" class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors">
              取消
            </button>
            <button @click="handleDelete" :disabled="deleting" class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50" style="background: var(--state-error);">
              <span v-if="deleting">删除中...</span>
              <span v-else>确认删除</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  getNotificationList,
  sendNotification,
  updateNotification,
  deleteNotification,
  markAsRead
} from '@/api/notification'
import {
  Search,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Send,
  Eye,
  Pencil,
  Trash2,
  X,
  AlertCircle,
  Bell,
  PartyPopper,
  BookOpen,
  ShieldAlert
} from 'lucide-vue-next'

const hoverSendBtn = ref(false)
const searchFocus = ref(false)
const focusField = ref('')

const searchQuery = ref('')
const selectedType = ref('all')
const currentPage = ref(1)
const pageSize = 10

const notifications = ref([])

// 弹窗状态
const showFormModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)

const showDetailModal = ref(false)
const detailData = ref(null)
const markingRead = ref(false)

const showDeleteModal = ref(false)
const deletingItem = ref(null)
const deleting = ref(false)

// 类型配置
const typeConfig = {
  system: { label: '系统通知', icon: Bell, color: '#F97316' },
  activity: { label: '活动通知', icon: PartyPopper, color: '#EA580C' },
  recipe: { label: '食谱更新', icon: BookOpen, color: '#16A34A' },
  security: { label: '安全提醒', icon: ShieldAlert, color: '#DC2626' },
}

// 注意：通知数据全部来自后端真实接口，移除前端 mock 兜底，避免展示假数据

// 表单数据
const formData = ref(getEmptyForm())

function getEmptyForm() {
  return {
    id: null,
    title: '',
    content: '',
    type: 'system',
    userIdInput: '',
    iconUrl: '',
  }
}

// 后端 Notification -> 前端展示字段映射
function mapNotification(item) {
  return {
    id: item.id,
    title: item.title || '-',
    content: item.content || '',
    type: item.type || 'system',
    userId: item.userId ?? null,
    isRead: !!item.isRead,
    createdAt: item.createdAt || '-',
    iconUrl: item.iconUrl || '',
  }
}

// 计算属性
const filteredNotifications = computed(() => {
  return notifications.value.filter(item => {
    const matchesSearch = (item.title || '').toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesType = selectedType.value === 'all' || item.type === selectedType.value
    return matchesSearch && matchesType
  })
})

const total = computed(() => filteredNotifications.value.length)

const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

const paginatedNotifications = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredNotifications.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const t = totalPages.value
  const current = currentPage.value

  if (t <= 7) {
    for (let i = 1; i <= t; i++) pages.push(i)
  } else if (current <= 4) {
    for (let i = 1; i <= 5; i++) pages.push(i)
    pages.push('...')
    pages.push(t)
  } else if (current >= t - 3) {
    pages.push(1)
    pages.push('...')
    for (let i = t - 4; i <= t; i++) pages.push(i)
  } else {
    pages.push(1)
    pages.push('...')
    for (let i = current - 1; i <= current + 1; i++) pages.push(i)
    pages.push('...')
    pages.push(t)
  }
  return pages
})

// 工具函数
function getTypeIcon(type) {
  return typeConfig[type]?.icon || Bell
}

function getTypeLabel(type) {
  return typeConfig[type]?.label || type
}

function getTypeColor(type) {
  return typeConfig[type]?.color || '#737373'
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}

function goToPage(page) {
  if (page !== '...' && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 监听筛选条件变化时回到第一页
watch([searchQuery, selectedType], () => {
  currentPage.value = 1
})

// API 调用
async function fetchNotifications() {
  try {
    const data = await getNotificationList()
    if (Array.isArray(data)) {
      notifications.value = data.map(mapNotification)
    } else {
      notifications.value = []
    }
  } catch (e) {
    console.error('[fetchNotifications error]', e)
    notifications.value = []
  }
}

function openFormModal(mode, data = null) {
  isEditing.value = mode === 'edit'
  if (isEditing.value && data) {
    formData.value = {
      id: data.id,
      title: data.title || '',
      content: data.content || '',
      type: data.type || 'system',
      userIdInput: data.userId != null ? String(data.userId) : '',
      iconUrl: data.iconUrl || '',
    }
  } else {
    formData.value = getEmptyForm()
  }
  showFormModal.value = true
}

function closeFormModal() {
  showFormModal.value = false
  formData.value = getEmptyForm()
}

function handleSend() {
  openFormModal('add')
}

function handleEdit(item) {
  openFormModal('edit', item)
}

function handleView(item) {
  detailData.value = { ...item }
  showDetailModal.value = true
}

function confirmDelete(item) {
  deletingItem.value = item
  showDeleteModal.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const submitData = {
      title: formData.value.title,
      content: formData.value.content,
      type: formData.value.type,
      iconUrl: formData.value.iconUrl || null,
      userId: formData.value.userIdInput === '' ? null : Number(formData.value.userIdInput),
    }

    if (isEditing.value) {
      submitData.id = formData.value.id
      submitData.isRead = false
      await updateNotification(formData.value.id, submitData)
    } else {
      submitData.isRead = false
      await sendNotification(submitData)
    }

    closeFormModal()
    await fetchNotifications()
  } catch (e) {
    console.error('[handleSave error]', e)
    alert('保存失败：' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!deletingItem.value) return
  deleting.value = true
  try {
    await deleteNotification(deletingItem.value.id)
    showDeleteModal.value = false
    await fetchNotifications()
    if (currentPage.value > totalPages.value && currentPage.value > 1) {
      currentPage.value = totalPages.value
    }
  } catch (e) {
    console.error('[handleDelete error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  } finally {
    deleting.value = false
    deletingItem.value = null
  }
}

async function handleMarkAsRead(item) {
  markingRead.value = true
  try {
    await markAsRead(item.id)
    item.isRead = true
    // 同步主列表中的状态
    const target = notifications.value.find(n => n.id === item.id)
    if (target) target.isRead = true
  } catch (e) {
    console.error('[markAsRead error]', e)
    alert('标记失败：' + (e?.message || '未知错误'))
  } finally {
    markingRead.value = false
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>
