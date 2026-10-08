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
              placeholder="搜索用户名或手机号..."
              class="input-field pl-9 pr-3 py-2 text-sm"
            />
          </div>
          <select
            v-model="statusFilter"
            class="input-field py-2 text-sm w-full sm:w-auto"
          >
            <option value="all">全部状态</option>
            <option value="active">正常</option>
            <option value="disabled">禁用</option>
          </select>
        </div>
        <button
          class="btn-primary w-full sm:w-auto"
          style="padding: 8px 16px; font-size: 14px;"
          @click="handleAdd"
        >
          <UserPlus class="w-4 h-4 mr-1.5" />
          新增用户
        </button>
      </div>
    </div>

    <div class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-border bg-muted/30">
              <th class="text-left font-medium text-muted-foreground py-3 px-4 whitespace-nowrap">用户</th>
              <th class="text-left font-medium text-muted-foreground py-3 px-4 whitespace-nowrap">手机号</th>
              <th class="text-left font-medium text-muted-foreground py-3 px-4 whitespace-nowrap">口味偏好</th>
              <th class="text-left font-medium text-muted-foreground py-3 px-4 whitespace-nowrap">注册时间</th>
              <th class="text-left font-medium text-muted-foreground py-3 px-4 whitespace-nowrap">状态</th>
              <th class="text-left font-medium text-muted-foreground py-3 px-4 whitespace-nowrap">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="user in paginatedUsers"
              :key="user.id"
              class="border-b border-border last:border-0 transition-colors hover:bg-muted/30"
            >
              <td class="py-3 px-4">
                <div class="flex items-center gap-3">
                  <div
                    v-if="user.avatar"
                    class="w-9 h-9 rounded-full flex-shrink-0 overflow-hidden"
                  >
                    <img :src="user.avatar" :alt="user.nickname" class="w-full h-full object-cover" />
                  </div>
                  <div
                    v-else
                    class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-semibold text-white flex-shrink-0"
                    :style="{ background: user.avatarGradient }"
                  >
                    {{ user.initial }}
                  </div>
                  <span class="font-medium text-foreground">{{ user.nickname || '--' }}</span>
                </div>
              </td>
              <td class="py-3 px-4 text-muted-foreground">{{ user.phone || '--' }}</td>
              <td class="py-3 px-4 text-muted-foreground">{{ user.tastePreferences || '--' }}</td>
              <td class="py-3 px-4 text-muted-foreground whitespace-nowrap">{{ user.createdAt || '--' }}</td>
              <td class="py-3 px-4">
                <span :class="user.status === 1 ? 'badge-success' : 'badge-error'">
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-1">
                  <button
                    class="p-1.5 rounded-md text-muted-foreground hover:text-primary hover:bg-primary/10 transition-colors"
                    title="编辑"
                    @click="handleEdit(user)"
                  >
                    <Pencil class="w-4 h-4" />
                  </button>
                  <button
                    class="p-1.5 rounded-md transition-colors"
                    :class="user.status === 1
                      ? 'text-muted-foreground hover:text-state-error hover:bg-state-error/10'
                      : 'text-muted-foreground hover:text-state-success hover:bg-state-success/10'"
                    :title="user.status === 1 ? '禁用' : '启用'"
                    @click="handleToggleStatus(user)"
                  >
                    <Ban v-if="user.status === 1" class="w-4 h-4" />
                    <CheckCircle v-else class="w-4 h-4" />
                  </button>
                  <button
                    class="p-1.5 rounded-md text-muted-foreground hover:text-state-error hover:bg-state-error/10 transition-colors"
                    title="删除"
                    @click="confirmDelete(user)"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredUsers.length === 0">
              <td colspan="6" class="py-12 px-4 text-center">
                <div class="flex flex-col items-center gap-2">
                  <Search class="w-10 h-10 text-muted-foreground/50" />
                  <p class="text-sm text-muted-foreground">{{ loading ? '加载中...' : '没有找到匹配的用户' }}</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-between gap-3 py-3 px-4 border-t border-border">
        <div class="text-sm text-muted-foreground">
          共 <span class="font-medium text-foreground">{{ filteredUsers.length }}</span> 条记录，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="flex items-center gap-1">
          <button
            class="p-2 rounded-md border border-border text-muted-foreground hover:text-foreground hover:bg-muted transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="currentPage === 1"
            @click="prevPage"
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
            :disabled="page === '...'"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <button
            class="p-2 rounded-md border border-border text-muted-foreground hover:text-foreground hover:bg-muted transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="currentPage === totalPages"
            @click="nextPage"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑用户弹窗 -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-md max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b border-border">
            <h2 class="text-lg font-semibold text-foreground">{{ isEditing ? '编辑用户' : '新增用户' }}</h2>
            <button @click="closeModal" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
            <form @submit.prevent="handleSave" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">手机号 *</label>
                <input
                  v-model="formData.phone"
                  type="text"
                  required
                  maxlength="11"
                  placeholder="请输入手机号"
                  class="input-field py-2 text-sm"
                  :disabled="isEditing"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">昵称 *</label>
                <input
                  v-model="formData.nickname"
                  type="text"
                  required
                  placeholder="请输入昵称"
                  class="input-field py-2 text-sm"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">
                  密码 {{ isEditing ? '（留空表示不修改）' : '*' }}
                </label>
                <input
                  v-model="formData.password"
                  type="password"
                  :required="!isEditing"
                  minlength="6"
                  placeholder="请输入密码"
                  class="input-field py-2 text-sm"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">口味偏好</label>
                <input
                  v-model="formData.tastePreferences"
                  type="text"
                  placeholder="例如：川菜、清淡、甜食"
                  class="input-field py-2 text-sm"
                />
              </div>
              <div class="flex items-center justify-end gap-3 pt-4">
                <button
                  type="button"
                  @click="closeModal"
                  class="btn-secondary"
                  style="padding: 8px 16px; font-size: 14px;"
                >
                  取消
                </button>
                <button
                  type="submit"
                  :disabled="saving"
                  class="btn-primary"
                  style="padding: 8px 16px; font-size: 14px;"
                >
                  <span v-if="saving">保存中...</span>
                  <span v-else>{{ isEditing ? '保存修改' : '新增用户' }}</span>
                </button>
              </div>
            </form>
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
              <div class="w-10 h-10 rounded-full flex items-center justify-center bg-state-error/10">
                <AlertCircle class="w-5 h-5 text-state-error" />
              </div>
              <h3 class="text-lg font-semibold text-foreground">确认删除</h3>
            </div>
            <p class="text-sm text-muted-foreground">
              确定要删除用户「{{ deletingUser?.nickname || deletingUser?.phone }}」吗？此操作无法撤销。
            </p>
          </div>
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
            <button
              @click="showDeleteModal = false"
              class="btn-secondary"
              style="padding: 8px 16px; font-size: 14px;"
            >
              取消
            </button>
            <button
              @click="handleDelete"
              :disabled="deleting"
              class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50"
              style="background: var(--state-error);"
            >
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
import { ref, computed, onMounted } from 'vue'
import { Search, UserPlus, Pencil, Ban, CheckCircle, Trash2, ChevronLeft, ChevronRight, X, AlertCircle } from 'lucide-vue-next'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  toggleUserStatus
} from '@/api/user'

const searchText = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

const gradients = [
  'linear-gradient(135deg, #F97316, #EA580C)',
  'linear-gradient(135deg, #FB923C, #F97316)',
  'linear-gradient(135deg, #F59E0B, #D97706)',
  'linear-gradient(135deg, #EF4444, #DC2626)',
  'linear-gradient(135deg, #10B981, #059669)',
  'linear-gradient(135deg, #3B82F6, #2563EB)',
]

// mock 数据兜底
const mockUsers = [
  { id: 1, phone: '13800138001', nickname: '张三', avatar: '', tastePreferences: '川菜', status: 1, createdAt: '2024-01-15 10:30', password: '' },
  { id: 2, phone: '13800138002', nickname: '李四', avatar: '', tastePreferences: '清淡', status: 1, createdAt: '2024-02-20 14:12', password: '' },
  { id: 3, phone: '13800138003', nickname: '王五', avatar: '', tastePreferences: '甜食', status: 0, createdAt: '2024-03-10 09:45', password: '' },
  { id: 4, phone: '13800138004', nickname: '赵六', avatar: '', tastePreferences: '咸鲜', status: 1, createdAt: '2024-04-05 16:20', password: '' },
  { id: 5, phone: '13800138005', nickname: '孙七', avatar: '', tastePreferences: '素食', status: 1, createdAt: '2024-05-18 11:30', password: '' },
  { id: 6, phone: '13800138006', nickname: '周八', avatar: '', tastePreferences: '烧烤', status: 1, createdAt: '2024-06-22 19:00', password: '' },
  { id: 7, phone: '13800138007', nickname: '吴九', avatar: '', tastePreferences: '粤菜', status: 0, createdAt: '2024-07-01 08:15', password: '' },
  { id: 8, phone: '13800138008', nickname: '郑十', avatar: '', tastePreferences: '湘菜', status: 1, createdAt: '2024-07-10 13:40', password: '' },
  { id: 9, phone: '13800138009', nickname: '钱十一', avatar: '', tastePreferences: '鲁菜', status: 1, createdAt: '2024-07-15 17:25', password: '' },
  { id: 10, phone: '13800138010', nickname: '孙十二', avatar: '', tastePreferences: '浙菜', status: 1, createdAt: '2024-07-18 10:00', password: '' },
  { id: 11, phone: '13800138011', nickname: '李十三', avatar: '', tastePreferences: '闽菜', status: 1, createdAt: '2024-07-19 15:30', password: '' },
  { id: 12, phone: '13800138012', nickname: '王十四', avatar: '', tastePreferences: '徽菜', status: 0, createdAt: '2024-07-20 09:00', password: '' },
]

const users = ref([])

// 弹窗状态
const showModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const formData = ref({
  id: null,
  phone: '',
  nickname: '',
  password: '',
  tastePreferences: '',
})

// 删除弹窗状态
const showDeleteModal = ref(false)
const deletingUser = ref(null)
const deleting = ref(false)

function pickGradient(key) {
  const index = (typeof key === 'string' ? key.charCodeAt(0) : Number(key)) % gradients.length
  return gradients[index >= 0 ? index : 0]
}

function mapUser(item) {
  const nickname = item.nickname || ''
  const initial = nickname ? nickname.charAt(0) : (item.phone ? item.phone.charAt(0) : 'U')
  let createdAt = ''
  if (item.createdAt) {
    const d = new Date(item.createdAt)
    if (!isNaN(d.getTime())) {
      const pad = (n) => String(n).padStart(2, '0')
      createdAt = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    } else {
      createdAt = String(item.createdAt)
    }
  }
  return {
    ...item,
    status: typeof item.status === 'number' ? item.status : (item.status === 'active' || item.status === 1 ? 1 : 0),
    initial,
    avatarGradient: pickGradient(item.id != null ? item.id : nickname),
    createdAt,
  }
}

async function fetchUsers() {
  loading.value = true
  try {
    const data = await getUserList()
    if (Array.isArray(data) && data.length > 0) {
      users.value = data.map(mapUser)
    } else if (Array.isArray(data)) {
      // 接口成功但无数据，使用 mock 兜底
      users.value = mockUsers.map(mapUser)
    } else {
      users.value = mockUsers.map(mapUser)
    }
  } catch (e) {
    console.error('[fetchUsers error]', e)
    users.value = mockUsers.map(mapUser)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})

const filteredUsers = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  return users.value.filter(user => {
    const matchesSearch = !keyword ||
      (user.nickname && user.nickname.toLowerCase().includes(keyword)) ||
      (user.phone && user.phone.toLowerCase().includes(keyword))
    const matchesStatus =
      statusFilter.value === 'all' ||
      (statusFilter.value === 'active' && user.status === 1) ||
      (statusFilter.value === 'disabled' && user.status === 0)
    return matchesSearch && matchesStatus
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredUsers.value.length / pageSize)))

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else if (current <= 4) {
    for (let i = 1; i <= 5; i++) pages.push(i)
    pages.push('...')
    pages.push(total)
  } else if (current >= total - 3) {
    pages.push(1)
    pages.push('...')
    for (let i = total - 4; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    pages.push('...')
    for (let i = current - 1; i <= current + 1; i++) pages.push(i)
    pages.push('...')
    pages.push(total)
  }
  return pages
})

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredUsers.value.slice(start, end)
})

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

function resetForm() {
  formData.value = {
    id: null,
    phone: '',
    nickname: '',
    password: '',
    tastePreferences: '',
  }
}

function handleAdd() {
  isEditing.value = false
  resetForm()
  showModal.value = true
}

function handleEdit(user) {
  isEditing.value = true
  formData.value = {
    id: user.id,
    phone: user.phone || '',
    nickname: user.nickname || '',
    password: '',
    tastePreferences: user.tastePreferences || '',
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  resetForm()
}

async function handleSave() {
  saving.value = true
  try {
    if (isEditing.value) {
      const payload = {
        id: formData.value.id,
        phone: formData.value.phone,
        nickname: formData.value.nickname,
        tastePreferences: formData.value.tastePreferences,
      }
      if (formData.value.password) {
        payload.password = formData.value.password
      }
      await updateUser(formData.value.id, payload)
      // 本地同步更新
      const idx = users.value.findIndex(u => u.id === formData.value.id)
      if (idx > -1) {
        users.value[idx] = mapUser({ ...users.value[idx], ...payload })
      }
    } else {
      const payload = {
        phone: formData.value.phone,
        nickname: formData.value.nickname,
        password: formData.value.password,
        tastePreferences: formData.value.tastePreferences,
      }
      const created = await createUser(payload)
      const newItem = created && typeof created === 'object' ? created : payload
      users.value.unshift(mapUser({ ...newItem, id: created?.id || Date.now() }))
      users.value = users.value.map(mapUser)
      currentPage.value = 1
    }
    closeModal()
  } catch (e) {
    console.error('[handleSave error]', e)
    alert('保存失败：' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(user) {
  try {
    await toggleUserStatus(user.id)
    user.status = user.status === 1 ? 0 : 1
  } catch (e) {
    console.error('[handleToggleStatus error]', e)
    alert('操作失败：' + (e?.message || '未知错误'))
  }
}

function confirmDelete(user) {
  deletingUser.value = user
  showDeleteModal.value = true
}

async function handleDelete() {
  if (!deletingUser.value) return
  deleting.value = true
  try {
    await deleteUser(deletingUser.value.id)
    const id = deletingUser.value.id
    users.value = users.value.filter(u => u.id !== id)
    showDeleteModal.value = false
    if (currentPage.value > totalPages.value && currentPage.value > 1) {
      currentPage.value = totalPages.value
    }
  } catch (e) {
    console.error('[handleDelete error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  } finally {
    deleting.value = false
    deletingUser.value = null
  }
}
</script>
