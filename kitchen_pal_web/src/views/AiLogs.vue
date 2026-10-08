<template>
  <div>
    <div class="card p-4 mb-4">
      <div class="flex flex-col sm:flex-row gap-3 items-start sm:items-center justify-between">
        <div class="flex flex-col sm:flex-row gap-3 w-full sm:w-auto">
          <div class="relative w-full sm:w-64">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input
              v-model="searchText"
              type="text"
              placeholder="搜索日志ID或用户..."
              class="input-field pl-9 pr-3 py-2 text-sm"
            />
          </div>
          <div class="relative w-full sm:w-auto">
            <Calendar class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input
              v-model="dateFilter"
              type="date"
              class="input-field pl-9 pr-3 py-2 text-sm w-full sm:w-auto"
            />
          </div>
          <select
            v-model="statusFilter"
            class="input-field py-2 text-sm w-full sm:w-auto"
          >
            <option value="all">全部状态</option>
            <option value="success">成功</option>
            <option value="failed">失败</option>
            <option value="processing">处理中</option>
          </select>
        </div>
      </div>
    </div>

    <div class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm min-w-[900px]">
          <thead>
            <tr class="border-b border-border bg-muted">
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">日志ID</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">用户</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">识别食材</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">识别时间</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">置信度</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">状态</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider py-3 px-4 whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="log in paginatedLogs"
              :key="log.id"
              class="border-b border-border last:border-0 transition-colors hover:bg-muted/30"
            >
              <td class="py-3 px-4 font-mono text-xs text-foreground">{{ log.logId }}</td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-3">
                  <div
                    class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold text-white flex-shrink-0"
                    :style="{ background: log.avatarGradient }"
                  >
                    {{ log.userInitial }}
                  </div>
                  <span class="font-medium text-foreground">{{ log.userName }}</span>
                </div>
              </td>
              <td class="py-3 px-4 text-muted-foreground">{{ log.ingredients || '-' }}</td>
              <td class="py-3 px-4 text-muted-foreground whitespace-nowrap">{{ log.time }}</td>
              <td class="py-3 px-4">
                <span v-if="log.confidence" class="text-foreground font-medium">{{ log.confidence }}</span>
                <span v-else class="text-muted-foreground">-</span>
              </td>
              <td class="py-3 px-4">
                <span :class="getStatusBadgeClass(log.status)">
                  {{ getStatusText(log.status) }}
                </span>
              </td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-1">
                  <button
                    class="inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium rounded-md transition-colors text-primary hover:bg-primary/10"
                    @click="viewDetail(log)"
                  >
                    <Eye class="w-3.5 h-3.5" />
                    查看详情
                  </button>
                  <button
                    class="inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium rounded-md transition-colors hover:bg-state-error-light"
                    style="color: var(--state-error);"
                    @click="confirmDelete(log)"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                    删除
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredLogs.length === 0">
              <td colspan="8" class="px-4 py-12 text-center">
                <div class="flex flex-col items-center gap-2">
                  <Search class="w-10 h-10 text-muted-foreground/50" />
                  <p class="text-sm text-muted-foreground">没有找到匹配的日志</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-between gap-3 py-3 px-4 border-t border-border">
        <div class="text-sm text-muted-foreground">
          共 <span class="font-medium text-foreground">{{ totalCount.toLocaleString() }}</span> 条记录
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
            @click="currentPage = page"
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

    <Teleport to="body">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeDetailModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <h2 class="text-lg font-semibold text-foreground">日志详情</h2>
            <button @click="closeDetailModal" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
            <div v-if="detailLoading" class="flex items-center justify-center py-12">
              <div class="text-sm text-muted-foreground">加载中...</div>
            </div>
            <div v-else-if="detailLog" class="space-y-5">
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <div class="text-xs text-muted-foreground mb-1">日志ID</div>
                  <div class="text-sm font-mono text-foreground">{{ detailLog.logId }}</div>
                </div>
                <div>
                  <div class="text-xs text-muted-foreground mb-1">状态</div>
                  <span :class="getStatusBadgeClass(detailLog.status)">
                    {{ getStatusText(detailLog.status) }}
                  </span>
                </div>
                <div>
                  <div class="text-xs text-muted-foreground mb-1">用户ID</div>
                  <div class="text-sm text-foreground">{{ detailLog.userId ?? '-' }}</div>
                </div>
                <div>
                  <div class="text-xs text-muted-foreground mb-1">用户</div>
                  <div class="text-sm text-foreground">{{ detailLog.userName || '-' }}</div>
                </div>
                <div>
                  <div class="text-xs text-muted-foreground mb-1">识别时间</div>
                  <div class="text-sm text-foreground whitespace-nowrap">{{ detailLog.time || '-' }}</div>
                </div>
                <div>
                  <div class="text-xs text-muted-foreground mb-1">置信度</div>
                  <div class="text-sm text-foreground">{{ detailLog.confidence || '-' }}</div>
                </div>
              </div>

              <div>
                <div class="text-xs text-muted-foreground mb-2">识别食材</div>
                <div v-if="detailLog.ingredientsList && detailLog.ingredientsList.length" class="flex flex-wrap gap-2">
                  <span
                    v-for="(item, idx) in detailLog.ingredientsList"
                    :key="idx"
                    class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium"
                    style="background: #FFEDD5; color: #EA580C;"
                  >
                    {{ item }}
                  </span>
                </div>
                <div v-else class="text-sm text-muted-foreground">无</div>
              </div>

              <div>
                <div class="text-xs text-muted-foreground mb-2">推荐食谱</div>
                <div v-if="detailLog.suggestedRecipeIdList && detailLog.suggestedRecipeIdList.length" class="flex flex-wrap gap-2">
                  <span
                    v-for="rid in detailLog.suggestedRecipeIdList"
                    :key="rid"
                    class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium"
                    style="background: #DBEAFE; color: #2563EB;"
                  >
                    食谱 #{{ rid }}
                  </span>
                </div>
                <div v-else class="text-sm text-muted-foreground">无</div>
              </div>

              <div v-if="detailLog.detectedIngredientsRaw">
                <div class="text-xs text-muted-foreground mb-2">原始识别结果（JSON）</div>
                <pre class="text-xs font-mono text-foreground bg-muted p-3 rounded-lg overflow-x-auto whitespace-pre-wrap break-all">{{ detailLog.detectedIngredientsRaw }}</pre>
              </div>
            </div>
            <div v-else class="flex items-center justify-center py-12">
              <div class="text-sm text-muted-foreground">暂无数据</div>
            </div>
            <div class="flex items-center justify-end pt-4 border-t" style="border-color: var(--border);">
              <button @click="closeDetailModal" class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors" style="background: var(--brand-primary);">
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

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
            <p class="text-sm text-muted-foreground">确定要删除日志「{{ deletingLog?.logId }}」吗？此操作无法撤销。</p>
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
import { Search, Calendar, Eye, ChevronLeft, ChevronRight, Trash2, X, AlertCircle } from 'lucide-vue-next'
import { getScanLogs, getScanLogDetail, deleteScanLog } from '@/api/scan'

const searchText = ref('')
const dateFilter = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = 10

const gradients = [
  'linear-gradient(135deg, #F97316, #EA580C)',
  'linear-gradient(135deg, #FB923C, #F97316)',
  'linear-gradient(135deg, #F59E0B, #D97706)',
  'linear-gradient(135deg, #EF4444, #DC2626)',
  'linear-gradient(135deg, #10B981, #059669)',
  'linear-gradient(135deg, #3B82F6, #2563EB)',
]

// Mock 数据兜底（结构与后端 ScanLog 对齐）
const mockLogs = [
  { id: 1, logId: 'AI20240717001', userId: 1001, userName: '张三', detectedIngredients: JSON.stringify(['番茄', '鸡蛋', '葱']), suggestedRecipeIds: '1,2,3', scanTime: '2024-07-17 12:00:15', status: 'success', duration: '1.2s', confidence: '95.8%' },
  { id: 2, logId: 'AI20240717002', userId: 1002, userName: '李四', detectedIngredients: JSON.stringify(['牛肉', '土豆', '胡萝卜']), suggestedRecipeIds: '4,5', scanTime: '2024-07-17 11:30:42', status: 'success', duration: '1.5s', confidence: '92.3%' },
  { id: 3, logId: 'AI20240717003', userId: 1003, userName: '王五', detectedIngredients: '', suggestedRecipeIds: '', scanTime: '2024-07-17 10:15:30', status: 'failed', duration: '2.0s', confidence: '' },
  { id: 4, logId: 'AI20240716001', userId: 1004, userName: '赵六', detectedIngredients: JSON.stringify(['鸡胸肉', '生菜', '番茄']), suggestedRecipeIds: '6', scanTime: '2024-07-16 18:20:08', status: 'success', duration: '1.1s', confidence: '97.2%' },
  { id: 5, logId: 'AI20240716002', userId: 1005, userName: '孙七', detectedIngredients: JSON.stringify(['三文鱼', '柠檬']), suggestedRecipeIds: '7,8', scanTime: '2024-07-16 19:45:55', status: 'success', duration: '1.3s', confidence: '94.6%' },
  { id: 6, logId: 'AI20240717004', userId: 1006, userName: '周八', detectedIngredients: '', suggestedRecipeIds: '', scanTime: '2024-07-17 14:00:00', status: 'processing', duration: '', confidence: '' },
]

const logs = ref([])
const loading = ref(false)
const error = ref('')

// Mock 用户名映射（后端仅返回 userId 时使用）
const userMap = mockLogs.reduce((acc, log) => {
  acc[log.userId] = { userName: log.userName, userInitial: log.userName.charAt(0) }
  return acc
}, {})

function getAvatarGradient(seed) {
  const idx = Math.abs(seed) % gradients.length
  return gradients[idx]
}

// 解析 detectedIngredients JSON 字符串为食材名称列表
function parseIngredients(raw) {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return parsed.map(item => {
        if (typeof item === 'string') return item
        if (item && typeof item === 'object') {
          return item.name || item.ingredient || item.label || ''
        }
        return String(item)
      }).filter(Boolean)
    }
    if (parsed && typeof parsed === 'object' && Array.isArray(parsed.ingredients)) {
      return parseIngredients(JSON.stringify(parsed.ingredients))
    }
    return []
  } catch (e) {
    // 非 JSON，尝试按分隔符拆分
    return String(raw).split(/[,，、]/).map(s => s.trim()).filter(Boolean)
  }
}

// 解析逗号分隔的 suggestedRecipeIds 为数字数组
function parseRecipeIds(raw) {
  if (!raw) return []
  return String(raw)
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
    .map(s => Number(s))
    .filter(n => !Number.isNaN(n))
}

// 格式化时间：将 LocalDateTime ISO 格式转为 "YYYY-MM-DD HH:mm:ss"
function formatTime(t) {
  if (!t) return ''
  if (typeof t === 'string') {
    if (t.includes('T')) return t.replace('T', ' ').replace(/\.\d+$/, '')
    return t
  }
  return String(t)
}

// 将后端 ScanLog 实体映射为展示对象
function mapLog(item, idx = 0) {
  const ingredientsList = parseIngredients(item.detectedIngredients)
  const suggestedRecipeIdList = parseRecipeIds(item.suggestedRecipeIds)
  const userName = item.userName || userMap[item.userId]?.userName || `用户${item.userId ?? ''}`
  const userInitial = item.userInitial || userMap[item.userId]?.userInitial || (userName ? userName.charAt(0) : '?')
  const seed = item.id != null ? item.id : idx
  return {
    id: item.id,
    logId: item.logId || `AI${String(item.id ?? '').padStart(9, '0')}`,
    userId: item.userId,
    userName,
    userInitial,
    ingredients: ingredientsList.join('、'),
    ingredientsList,
    detectedIngredientsRaw: item.detectedIngredients || '',
    suggestedRecipeIds: item.suggestedRecipeIds || '',
    suggestedRecipeIdList,
    time: formatTime(item.scanTime || item.time),
    duration: item.duration || '',
    // 后端 ScanLog 无顶层 confidence 字段，置信度存在 detectedIngredients 的 JSON 里，
    // 取各食材 confidence 的平均值作为整条日志的置信度（整数百分比）。
    confidence: item.confidence || avgConfidence(item.detectedIngredients),
    status: item.status || 'success',
    avatarGradient: item.avatarGradient || getAvatarGradient(seed),
  }
}

/**
 * 从 detectedIngredients JSON 字符串中计算平均置信度。
 * 形如 [{"name":"番茄","confidence":98,"weight":"约200g"}, ...] → "95%"
 * 无法解析或无有效置信度时返回空串，前端渲染为 "-"。
 */
function avgConfidence(raw) {
  if (!raw) return ''
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return ''
    const list = parsed
      .map(it => Number(it?.confidence))
      .filter(n => Number.isFinite(n) && n > 0)
    if (list.length === 0) return ''
    const avg = list.reduce((a, b) => a + b, 0) / list.length
    return Math.round(avg) + '%'
  } catch (e) {
    return ''
  }
}

async function fetchLogs() {
  loading.value = true
  error.value = ''
  try {
    const data = await getScanLogs()
    if (Array.isArray(data)) {
      logs.value = data.map((item, idx) => mapLog(item, idx))
    } else {
      logs.value = mockLogs.map((l, idx) => mapLog(l, idx))
    }
  } catch (e) {
    console.error('[fetchLogs error]', e)
    error.value = e?.message || '获取日志列表失败，已使用本地数据兜底'
    logs.value = mockLogs.map((l, idx) => mapLog(l, idx))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const keyword = searchText.value.toLowerCase().trim()
    const matchesSearch = !keyword ||
                          log.logId.toLowerCase().includes(keyword) ||
                          log.userName.toLowerCase().includes(keyword) ||
                          String(log.userId ?? '').includes(keyword)
    const matchesStatus = statusFilter.value === 'all' || log.status === statusFilter.value
    const matchesDate = !dateFilter.value || (log.time && log.time.startsWith(dateFilter.value))
    return matchesSearch && matchesStatus && matchesDate
  })
})

const totalCount = computed(() => filteredLogs.value.length)

const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)))

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredLogs.value.slice(start, start + pageSize)
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

// 筛选条件变化时回到第一页
watch([searchText, dateFilter, statusFilter], () => {
  currentPage.value = 1
})

function getStatusBadgeClass(status) {
  const map = {
    success: 'badge-success',
    failed: 'badge-error',
    processing: 'badge-info',
  }
  return map[status] || 'badge-success'
}

function getStatusText(status) {
  const map = {
    success: '成功',
    failed: '失败',
    processing: '处理中',
  }
  return map[status] || '成功'
}

// 详情弹窗
const showDetailModal = ref(false)
const detailLog = ref(null)
const detailLoading = ref(false)

async function viewDetail(log) {
  // 先用列表行数据立即展示
  detailLog.value = { ...log }
  showDetailModal.value = true
  detailLoading.value = true
  try {
    const data = await getScanLogDetail(log.id)
    if (data) {
      detailLog.value = mapLog(data)
    }
  } catch (e) {
    console.error('[viewDetail error]', e)
    // 保留列表行数据，不弹 alert
  } finally {
    detailLoading.value = false
  }
}

function closeDetailModal() {
  showDetailModal.value = false
  detailLog.value = null
}

// 删除确认弹窗
const showDeleteModal = ref(false)
const deletingLog = ref(null)
const deleting = ref(false)

function confirmDelete(log) {
  deletingLog.value = log
  showDeleteModal.value = true
}

async function handleDelete() {
  if (!deletingLog.value) return
  deleting.value = true
  try {
    await deleteScanLog(deletingLog.value.id)
    // 从本地列表中移除
    logs.value = logs.value.filter(l => l.id !== deletingLog.value.id)
    showDeleteModal.value = false
    // 若当前页已空，回退页码
    if (currentPage.value > totalPages.value && currentPage.value > 1) {
      currentPage.value = totalPages.value
    }
  } catch (e) {
    console.error('[handleDelete error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  } finally {
    deleting.value = false
    deletingLog.value = null
  }
}
</script>
