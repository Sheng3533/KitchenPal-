<template>
  <div>
    <div class="card p-4 mb-4">
      <div class="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div class="flex flex-col sm:flex-row gap-3 w-full sm:w-auto flex-wrap">
          <div class="relative w-full sm:w-64">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input
              v-model="searchText"
              type="text"
              placeholder="搜索记录ID、用户或食谱..."
              class="input-field pl-9 pr-3 py-2 text-sm w-full"
            />
          </div>
          <div class="relative w-full sm:w-44">
            <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <select
              v-model="dateFilter"
              class="input-field pl-9 pr-8 py-2 text-sm w-full appearance-none cursor-pointer"
            >
              <option value="all">全部日期</option>
              <option value="today">今天</option>
              <option value="yesterday">昨天</option>
              <option value="week">近7天</option>
              <option value="month">近30天</option>
            </select>
            <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
          </div>
          <div class="relative w-full sm:w-36">
            <Filter class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <select
              v-model="statusFilter"
              class="input-field pl-9 pr-8 py-2 text-sm w-full appearance-none cursor-pointer"
            >
              <option value="all">全部状态</option>
              <option value="completed">已完成</option>
              <option value="in_progress">进行中</option>
              <option value="failed">失败</option>
              <option value="cancelled">已取消</option>
            </select>
            <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
          </div>
        </div>
      </div>
    </div>

    <div class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px] text-sm">
          <thead>
            <tr class="bg-muted">
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">记录ID</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">用户</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">食谱名称</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">开始时间</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">完成时间</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">时长</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">状态</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="record in paginatedRecords"
              :key="record.id"
              class="border-b border-border last:border-0 transition-colors hover:bg-muted/30"
            >
              <td class="py-3 px-4">
                <span class="font-mono text-foreground">{{ record.recordId }}</span>
              </td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-3">
                  <div
                    class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-semibold text-white flex-shrink-0"
                    :style="{ background: record.avatarGradient }"
                  >
                    {{ record.userInitial }}
                  </div>
                  <span class="font-medium text-foreground">{{ record.userName }}</span>
                </div>
              </td>
              <td class="py-3 px-4 text-foreground">{{ record.recipeName }}</td>
              <td class="py-3 px-4 text-muted-foreground whitespace-nowrap">{{ record.startTime }}</td>
              <td class="py-3 px-4 text-muted-foreground whitespace-nowrap">{{ record.endTime || '-' }}</td>
              <td class="py-3 px-4 text-muted-foreground whitespace-nowrap">{{ record.duration || '-' }}</td>
              <td class="py-3 px-4">
                <span :class="getStatusBadgeClass(record.status)">
                  {{ getStatusText(record.status) }}
                </span>
              </td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-1">
                  <button class="p-1.5 rounded-md text-muted-foreground hover:text-primary hover:bg-primary/10 transition-colors" title="查看详情" @click="viewDetail(record)">
                    <Eye class="w-4 h-4" />
                  </button>
                  <button class="p-1.5 rounded-md text-muted-foreground hover:text-state-error hover:bg-state-error/10 transition-colors" title="删除" @click="deleteRecord(record)">
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRecords.length === 0">
              <td colspan="8" class="py-12 px-4 text-center">
                <div class="flex flex-col items-center gap-2">
                  <Search class="w-10 h-10 text-muted-foreground/50" />
                  <p class="text-sm text-muted-foreground">没有找到匹配的烹饪记录</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-between gap-3 py-3 px-4 border-t border-border">
        <div class="text-sm text-muted-foreground">
          共 <span class="font-medium text-foreground">{{ totalCount.toLocaleString() }}</span> 条记录，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="flex items-center gap-1">
          <button
            class="p-2 rounded-md border border-border text-muted-foreground hover:text-foreground hover:bg-muted transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="currentPage === 1"
            @click="currentPage--"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          <button
            v-for="page in visiblePages"
            :key="page"
            class="min-w-8 h-8 px-2 text-sm rounded-md transition-colors"
            :class="page === currentPage
              ? 'bg-primary text-white font-medium'
              : 'text-muted-foreground hover:text-foreground hover:bg-muted'"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <button
            class="p-2 rounded-md border border-border text-muted-foreground hover:text-foreground hover:bg-muted transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="currentPage === totalPages"
            @click="currentPage++"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <Teleport to="body">
      <div v-if="showDetailModal && detailRecord" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeDetailModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h2 class="text-lg font-semibold text-foreground">烹饪记录详情</h2>
            <button @click="closeDetailModal" class="p-1.5 rounded-md hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
            <div class="flex items-center gap-3 pb-4 mb-4 border-b border-border">
              <div
                class="w-12 h-12 rounded-full flex items-center justify-center text-sm font-semibold text-white flex-shrink-0"
                :style="{ background: detailRecord.avatarGradient }"
              >
                {{ detailRecord.userInitial }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-medium text-foreground">{{ detailRecord.userName }}</span>
                  <span :class="getStatusBadgeClass(detailRecord.status)">
                    {{ getStatusText(detailRecord.status) }}
                  </span>
                </div>
                <div class="text-sm text-muted-foreground font-mono">{{ detailRecord.recordId }}</div>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: var(--brand-primary-light, rgba(249,115,22,0.1));">
                  <User class="w-4 h-4" style="color: var(--brand-primary, #F97316);" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">用户ID</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.userId ?? '-' }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(59,130,246,0.1);">
                  <Utensils class="w-4 h-4" style="color: #2563EB;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">食谱ID</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.raw?.recipeId ?? '-' }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3 md:col-span-2">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(249,115,22,0.1);">
                  <Utensils class="w-4 h-4" style="color: #EA580C;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">食谱名称</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.recipeName }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(16,185,129,0.1);">
                  <Calendar class="w-4 h-4" style="color: #059669;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">烹饪日期</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.raw?.cookDate ?? '-' }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(16,185,129,0.1);">
                  <Clock class="w-4 h-4" style="color: #059669;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">烹饪时间</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.raw?.cookTime ?? '-' }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(245,158,11,0.1);">
                  <Clock class="w-4 h-4" style="color: #D97706;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">烹饪时长</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.duration }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(245,158,11,0.1);">
                  <Star class="w-4 h-4" style="color: #D97706;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">评分</div>
                  <div class="flex items-center gap-1 mt-0.5">
                    <template v-if="detailRecord.raw?.rating != null">
                      <Star
                        v-for="(filled, idx) in getRatingStars(detailRecord.raw.rating)"
                        :key="idx"
                        class="w-3.5 h-3.5"
                        :class="filled ? 'fill-current' : ''"
                        :style="{ color: filled ? '#F59E0B' : 'var(--muted-foreground, #9CA3AF)' }"
                      />
                      <span class="text-xs text-muted-foreground ml-1">{{ detailRecord.raw.rating }} / 5</span>
                    </template>
                    <span v-else class="text-sm text-muted-foreground">-</span>
                  </div>
                </div>
              </div>
              <div class="flex items-start gap-3 md:col-span-2">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(107,114,128,0.1);">
                  <Calendar class="w-4 h-4" style="color: #6B7280;" />
                </div>
                <div class="min-w-0">
                  <div class="text-xs text-muted-foreground">创建时间</div>
                  <div class="text-sm font-medium text-foreground">{{ detailRecord.raw?.createdAt ?? detailRecord.endTime ?? '-' }}</div>
                </div>
              </div>
              <div class="flex items-start gap-3 md:col-span-2">
                <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0" style="background: rgba(139,92,246,0.1);">
                  <MessageSquare class="w-4 h-4" style="color: #7C3AED;" />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="text-xs text-muted-foreground">反馈</div>
                  <div class="text-sm text-foreground mt-0.5 p-3 rounded-lg bg-muted/40 whitespace-pre-wrap break-words">
                    {{ detailRecord.raw?.feedback || '暂无反馈' }}
                  </div>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-end pt-4 mt-2 border-t border-border">
              <button @click="closeDetailModal" class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors" style="background: var(--brand-primary, #F97316);">
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 删除确认弹窗 -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeDeleteModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-sm">
          <div class="px-6 py-4">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-full flex items-center justify-center" style="background: var(--state-error-light);">
                <AlertCircle class="w-5 h-5" style="color: var(--state-error);" />
              </div>
              <h3 class="text-lg font-semibold text-foreground">确认删除</h3>
            </div>
            <p class="text-sm text-muted-foreground">
              确定要删除烹饪记录「<span class="font-medium text-foreground">{{ deletingRecord?.recordId }}</span>」吗？此操作无法撤销。
            </p>
          </div>
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
            <button @click="closeDeleteModal" :disabled="deleting" class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors disabled:opacity-50">
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
import { Search, Calendar, Filter, ChevronDown, Eye, Trash2, ChevronLeft, ChevronRight, X, AlertCircle, Star, Clock, User, Utensils, MessageSquare } from 'lucide-vue-next'
import { getAllCookingHistory, deleteCookingHistory } from '@/api/history'

const searchText = ref('')
const dateFilter = ref('all')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = 10

// 详情弹窗状态
const showDetailModal = ref(false)
const detailRecord = ref(null)

// 删除确认弹窗状态
const showDeleteModal = ref(false)
const deletingRecord = ref(null)
const deleting = ref(false)

const gradients = [
  'linear-gradient(135deg, #F97316, #EA580C)',
  'linear-gradient(135deg, #FB923C, #F97316)',
  'linear-gradient(135deg, #F59E0B, #D97706)',
  'linear-gradient(135deg, #EF4444, #DC2626)',
  'linear-gradient(135deg, #10B981, #059669)',
  'linear-gradient(135deg, #3B82F6, #2563EB)',
]

// Mock 数据：API 失败时兜底（包含后端 CookingHistory 字段）
const mockRecords = [
  { id: 1, recordId: 'CR20240717001', userName: '张三', userInitial: '张', avatarGradient: gradients[0], recipeName: '红烧肉', startTime: '2024-07-17 12:00', endTime: '2024-07-17 12:45', duration: '45分钟', status: 'completed', userId: 1, recipeId: 101, recipeTitle: '红烧肉', cookDate: '2024-07-17', cookTime: '12:00', durationMin: 45, rating: 5, feedback: '味道很好，家人都很喜欢！下次还会再做。', createdAt: '2024-07-17 12:45:00' },
  { id: 2, recordId: 'CR20240717002', userName: '李四', userInitial: '李', avatarGradient: gradients[1], recipeName: '番茄炒蛋', startTime: '2024-07-17 11:30', endTime: '2024-07-17 11:45', duration: '15分钟', status: 'completed', userId: 2, recipeId: 102, recipeTitle: '番茄炒蛋', cookDate: '2024-07-17', cookTime: '11:30', durationMin: 15, rating: 4, feedback: '简单快手，味道不错。', createdAt: '2024-07-17 11:45:00' },
  { id: 3, recordId: 'CR20240717003', userName: '王五', userInitial: '王', avatarGradient: gradients[2], recipeName: '糖醋排骨', startTime: '2024-07-17 13:00', endTime: '', duration: '', status: 'in_progress', userId: 3, recipeId: 103, recipeTitle: '糖醋排骨', cookDate: '2024-07-17', cookTime: '13:00', durationMin: null, rating: null, feedback: null, createdAt: '2024-07-17 13:00:00' },
  { id: 4, recordId: 'CR20240716001', userName: '赵六', userInitial: '赵', avatarGradient: gradients[3], recipeName: '麻婆豆腐', startTime: '2024-07-16 18:00', endTime: '2024-07-16 18:30', duration: '30分钟', status: 'completed', userId: 4, recipeId: 104, recipeTitle: '麻婆豆腐', cookDate: '2024-07-16', cookTime: '18:00', durationMin: 30, rating: 4, feedback: '麻辣鲜香，很正宗。', createdAt: '2024-07-16 18:30:00' },
  { id: 5, recordId: 'CR20240716002', userName: '孙七', userInitial: '孙', avatarGradient: gradients[4], recipeName: '水煮鱼', startTime: '2024-07-16 19:00', endTime: '2024-07-16 19:20', duration: '20分钟', status: 'failed', userId: 5, recipeId: 105, recipeTitle: '水煮鱼', cookDate: '2024-07-16', cookTime: '19:00', durationMin: 20, rating: 2, feedback: '鱼肉煮老了，下次要注意火候。', createdAt: '2024-07-16 19:20:00' },
  { id: 6, recordId: 'CR20240715001', userName: '周八', userInitial: '周', avatarGradient: gradients[5], recipeName: '宫保鸡丁', startTime: '2024-07-15 20:00', endTime: '', duration: '', status: 'cancelled', userId: 6, recipeId: 106, recipeTitle: '宫保鸡丁', cookDate: '2024-07-15', cookTime: '20:00', durationMin: null, rating: null, feedback: null, createdAt: '2024-07-15 20:00:00' },
]

const records = ref([])

// 将后端 CookingHistory 映射为前端表格结构
function mapHistory(item, index) {
  const hasDuration = item.durationMin != null && item.durationMin !== 0
  const startTime = [item.cookDate, item.cookTime].filter(Boolean).join(' ')
  return {
    id: item.id,
    recordId: 'CR' + item.id,
    userId: item.userId,
    userName: '用户' + item.userId,
    userInitial: String(item.userId),
    avatarGradient: gradients[index % gradients.length],
    recipeName: item.recipeTitle || '-',
    startTime: startTime || '-',
    endTime: item.createdAt || '-',
    duration: hasDuration ? item.durationMin + '分钟' : '-',
    status: item.status || (hasDuration ? 'completed' : 'in_progress'),
    raw: item,
  }
}

async function loadHistory() {
  try {
    const data = await getAllCookingHistory()
    const list = Array.isArray(data) ? data : []
    records.value = list.map((item, index) => mapHistory(item, index))
  } catch (e) {
    console.error('[CookingRecords] 获取烹饪历史失败，使用Mock数据兜底', e)
    records.value = mockRecords.map(item => ({ ...item, raw: item }))
  }
}

onMounted(() => {
  loadHistory()
})

// 筛选条件变化时重置到第一页，避免停留在空白页
watch([searchText, dateFilter, statusFilter], () => {
  currentPage.value = 1
})

const statusMap = {
  completed: { text: '已完成', badge: 'badge-success' },
  in_progress: { text: '进行中', badge: 'badge-info' },
  failed: { text: '失败', badge: 'badge-error' },
  cancelled: { text: '已取消', badge: 'badge-warning' },
}

function getStatusText(status) {
  return statusMap[status]?.text || status
}

function getStatusBadgeClass(status) {
  return statusMap[status]?.badge || 'badge-warning'
}

const filteredRecords = computed(() => {
  return records.value.filter(record => {
    const matchesSearch = record.recordId.toLowerCase().includes(searchText.value.toLowerCase()) ||
                          record.userName.toLowerCase().includes(searchText.value.toLowerCase()) ||
                          record.recipeName.toLowerCase().includes(searchText.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || record.status === statusFilter.value
    const matchesDate = dateFilter.value === 'all' || matchDateFilter(record.startTime, dateFilter.value)
    return matchesSearch && matchesStatus && matchesDate
  })
})

function matchDateFilter(dateStr, filter) {
  const recordDate = new Date(dateStr.replace(' ', 'T'))
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  const weekAgo = new Date(today)
  weekAgo.setDate(weekAgo.getDate() - 7)
  const monthAgo = new Date(today)
  monthAgo.setDate(monthAgo.getDate() - 30)

  switch (filter) {
    case 'today':
      return recordDate >= today
    case 'yesterday':
      return recordDate >= yesterday && recordDate < today
    case 'week':
      return recordDate >= weekAgo
    case 'month':
      return recordDate >= monthAgo
    default:
      return true
  }
}

const totalCount = computed(() => filteredRecords.value.length)

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)))

// 前端分页：每页 pageSize 条
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredRecords.value.slice(start, start + pageSize)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  let start = Math.max(1, current - 2)
  let end = Math.min(total, start + 4)
  if (end - start < 4) {
    start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 评分星级展示
function getRatingStars(rating) {
  const r = Number(rating) || 0
  return Array.from({ length: 5 }, (_, i) => i < r)
}

// 详情弹窗：显示完整信息
function viewDetail(record) {
  detailRecord.value = record
  showDetailModal.value = true
}

function closeDetailModal() {
  showDetailModal.value = false
  detailRecord.value = null
}

// 删除确认弹窗
function deleteRecord(record) {
  deletingRecord.value = record
  showDeleteModal.value = true
}

function closeDeleteModal() {
  if (deleting.value) return
  showDeleteModal.value = false
  deletingRecord.value = null
}

async function handleDelete() {
  if (!deletingRecord.value) return
  deleting.value = true
  try {
    try {
      await deleteCookingHistory(deletingRecord.value.id)
    } catch (apiErr) {
      console.error('[CookingRecords] 删除烹饪记录失败', apiErr)
      // API 失败时仅在本地移除，保证界面可用
    }
    const index = records.value.findIndex(r => r.id === deletingRecord.value.id)
    if (index > -1) {
      records.value.splice(index, 1)
    }
    showDeleteModal.value = false
    deletingRecord.value = null
    // 删除后若当前页超出范围，回退到上一页
    if (currentPage.value > totalPages.value && currentPage.value > 1) {
      currentPage.value = totalPages.value
    }
  } finally {
    deleting.value = false
  }
}
</script>
