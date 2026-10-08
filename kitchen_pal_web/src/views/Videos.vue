<template>
  <div>
    <!-- 顶部标题 -->
    <div class="mb-6">
      <h1 class="text-xl font-bold text-foreground">视频教学</h1>
      <p class="text-sm text-muted-foreground mt-1">管理烹饪教学视频，发布后客户端可观看</p>
    </div>

    <!-- 分类筛选 Tab -->
    <div class="flex items-center gap-1 mb-4 p-1 bg-muted/50 rounded-lg w-fit flex-wrap">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="activeTab = tab.value"
        class="px-4 py-1.5 text-sm font-medium rounded-md transition-colors"
        :class="activeTab === tab.value ? 'bg-white text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 操作栏 -->
    <div class="card p-4 mb-4">
      <div class="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div class="relative flex-1 w-full">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索视频标题..."
            class="input-field pl-10"
          />
        </div>
        <button
          @click="handleAdd"
          class="btn-primary inline-flex items-center gap-2 shrink-0"
        >
          <Plus class="w-4 h-4" />
          添加视频
        </button>
      </div>
    </div>

    <!-- 视频卡片网格 -->
    <div v-if="filteredList.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="video in filteredList"
        :key="video.id"
        class="card overflow-hidden transition-shadow hover:shadow-md"
      >
        <!-- 封面区域 -->
        <div class="relative aspect-video bg-muted overflow-hidden">
          <img
            v-if="video.coverImage"
            :src="video.coverImage"
            :alt="video.title"
            class="w-full h-full object-cover"
          />
          <div
            v-else
            class="w-full h-full flex items-center justify-center"
            :style="{ background: video.coverGradient }"
          >
            <VideoIcon class="w-12 h-12 text-white/60" />
          </div>
          <!-- 播放按钮叠加 -->
          <div class="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 hover:opacity-100 transition-opacity cursor-pointer">
            <div class="w-12 h-12 rounded-full bg-white/90 flex items-center justify-center shadow-lg">
              <Play class="w-5 h-5 text-foreground ml-0.5" />
            </div>
          </div>
          <!-- 时长标签 -->
          <span
            v-if="video.duration"
            class="absolute bottom-2 right-2 px-2 py-0.5 text-xs font-medium text-white bg-black/70 rounded"
          >
            {{ video.duration }}
          </span>
          <!-- 状态标识 -->
          <span
            class="absolute top-2 left-2 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
            :style="video.status === 'published'
              ? { backgroundColor: 'rgba(34,197,94,0.9)', color: '#fff' }
              : { backgroundColor: 'rgba(156,163,175,0.9)', color: '#fff' }"
          >
            {{ video.status === 'published' ? '已发布' : '已下架' }}
          </span>
        </div>

        <!-- 信息区域 -->
        <div class="p-4">
          <h3 class="text-base font-semibold text-foreground mb-2 truncate">{{ video.title }}</h3>

          <div class="flex items-center gap-2 mb-2 flex-wrap">
            <!-- 分类标签 -->
            <span
              class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
              :style="getCategoryStyle(video.category)"
            >
              {{ video.category }}
            </span>
            <!-- 难度标签 -->
            <span
              class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium"
              :style="getDifficultyStyle(video.difficulty)"
            >
              {{ video.difficulty }}
            </span>
          </div>

          <div class="flex items-center gap-3 text-sm text-muted-foreground mb-2">
            <div class="flex items-center gap-1">
              <Clock class="w-3.5 h-3.5" />
              {{ video.duration || '--' }}
            </div>
            <div class="flex items-center gap-1">
              <Eye class="w-3.5 h-3.5" />
              {{ video.viewCount ?? 0 }}
            </div>
          </div>

          <div v-if="video.chefName" class="flex items-center gap-1.5 text-sm text-muted-foreground mb-3">
            <User class="w-3.5 h-3.5" style="color: var(--brand-primary);" />
            <span>{{ video.chefName }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center gap-1 pt-3 border-t" style="border-color: var(--border);">
            <button
              @click="handleEdit(video)"
              class="p-1.5 rounded-lg transition-colors hover:bg-muted"
              title="编辑"
            >
              <Pencil class="w-4 h-4 text-muted-foreground" />
            </button>
            <button
              @click="handleToggleStatus(video)"
              class="p-1.5 rounded-lg transition-colors hover:bg-muted"
              :title="video.status === 'published' ? '下架' : '发布'"
            >
              <component :is="video.status === 'published' ? EyeOff : Eye" class="w-4 h-4 text-muted-foreground" />
            </button>
            <button
              @click="confirmDelete(video)"
              class="p-1.5 rounded-lg transition-colors hover:bg-state-error-light"
              title="删除"
            >
              <Trash2 class="w-4 h-4" style="color: var(--state-error);" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="card p-12 text-center">
      <VideoIcon class="w-12 h-12 text-muted-foreground/40 mx-auto mb-3" />
      <p class="text-sm text-muted-foreground">{{ loading ? '加载中...' : '没有找到匹配的视频' }}</p>
    </div>

    <!-- 添加/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeFormModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <h2 class="text-lg font-semibold text-foreground">{{ isEditing ? '编辑视频' : '添加视频' }}</h2>
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
                  placeholder="请输入视频标题"
                  class="input-field"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">描述</label>
                <textarea
                  v-model="formData.description"
                  rows="3"
                  placeholder="请输入视频描述"
                  class="input-field resize-none"
                ></textarea>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">视频地址URL *</label>
                  <input
                    v-model="formData.videoUrl"
                    type="url"
                    required
                    placeholder="请输入视频地址"
                    class="input-field"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">封面图URL</label>
                  <input
                    v-model="formData.coverImage"
                    type="url"
                    placeholder="请输入封面图URL"
                    class="input-field"
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">分类 *</label>
                  <select
                    v-model="formData.category"
                    required
                    class="input-field cursor-pointer"
                  >
                    <option value="">请选择分类</option>
                    <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">关联大厨</label>
                  <select
                    v-model="formData.chefId"
                    class="input-field cursor-pointer"
                  >
                    <option value="">请选择大厨</option>
                    <option v-for="chef in chefList" :key="chef.id" :value="chef.id">{{ chef.name }}</option>
                  </select>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">时长</label>
                  <input
                    v-model="formData.duration"
                    type="text"
                    placeholder="例如：12:30"
                    class="input-field"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">难度 *</label>
                  <select
                    v-model="formData.difficulty"
                    required
                    class="input-field cursor-pointer"
                  >
                    <option value="">请选择难度</option>
                    <option value="简单">简单</option>
                    <option value="中等">中等</option>
                    <option value="困难">困难</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">状态</label>
                  <select
                    v-model="formData.status"
                    class="input-field cursor-pointer"
                  >
                    <option value="published">发布</option>
                    <option value="unpublished">下架</option>
                  </select>
                </div>
              </div>

              <div class="flex items-center justify-end gap-3 pt-4">
                <button type="button" @click="closeFormModal" class="btn-secondary">取消</button>
                <button type="submit" :disabled="saving" class="btn-primary disabled:opacity-50">
                  <span v-if="saving">保存中...</span>
                  <span v-else>{{ isEditing ? '保存修改' : '添加视频' }}</span>
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
              <div class="w-10 h-10 rounded-full flex items-center justify-center" style="background: var(--state-error-light);">
                <AlertCircle class="w-5 h-5" style="color: var(--state-error);" />
              </div>
              <h3 class="text-lg font-semibold text-foreground">确认删除</h3>
            </div>
            <p class="text-sm text-muted-foreground">确定要删除视频「{{ deletingItem?.title }}」吗？此操作无法撤销。</p>
          </div>
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t" style="border-color: var(--border);">
            <button @click="showDeleteModal = false" class="btn-secondary">取消</button>
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
import { getVideoList, createVideo, updateVideo, deleteVideo } from '@/api/video'
import { getChefList } from '@/api/chef'
import {
  Search,
  Plus,
  Play,
  Pencil,
  Trash2,
  X,
  Eye,
  EyeOff,
  Clock,
  User,
  AlertCircle,
  Video as VideoIcon
} from 'lucide-vue-next'

// Tab 配置
const tabs = [
  { label: '全部', value: 'all' },
  { label: '基础技巧', value: '基础技巧' },
  { label: '家常菜', value: '家常菜' },
  { label: '西餐', value: '西餐' },
  { label: '烘焙', value: '烘焙' },
  { label: '创意料理', value: '创意料理' },
]

const categories = ['基础技巧', '家常菜', '西餐', '烘焙', '创意料理']

const coverGradients = [
  'linear-gradient(135deg, #F97316, #EA580C)',
  'linear-gradient(135deg, #F59E0B, #D97706)',
  'linear-gradient(135deg, #10B981, #059669)',
  'linear-gradient(135deg, #3B82F6, #2563EB)',
  'linear-gradient(135deg, #8B5CF6, #7C3AED)',
  'linear-gradient(135deg, #EC4899, #DB2777)',
]

function pickGradient(key) {
  const index = (typeof key === 'string' ? key.charCodeAt(0) : Number(key)) % coverGradients.length
  return coverGradients[index >= 0 ? index : 0]
}

// 分类样式
const categoryStyleMap = {
  '基础技巧': { color: '#2563EB', bgColor: '#DBEAFE' },
  '家常菜':   { color: '#EA580C', bgColor: '#FED7AA' },
  '西餐':     { color: '#16A34A', bgColor: '#DCFCE7' },
  '烘焙':     { color: '#DB2777', bgColor: '#FCE7F3' },
  '创意料理': { color: '#9333EA', bgColor: '#F3E8FF' },
}

function getCategoryStyle(category) {
  const style = categoryStyleMap[category]
  return style
    ? { color: style.color, backgroundColor: style.bgColor }
    : { color: '#6B7280', backgroundColor: '#F3F4F6' }
}

// 难度样式
const difficultyStyleMap = {
  '简单': { color: '#16A34A', bgColor: '#DCFCE7' },
  '中等': { color: '#CA8A04', bgColor: '#FEF9C3' },
  '困难': { color: '#DC2626', bgColor: '#FEE2E2' },
}

function getDifficultyStyle(difficulty) {
  const style = difficultyStyleMap[difficulty]
  return style
    ? { color: style.color, backgroundColor: style.bgColor }
    : { color: '#6B7280', backgroundColor: '#F3F4F6' }
}

// 状态
const activeTab = ref('all')
const searchQuery = ref('')
const loading = ref(false)
const videoList = ref([])
const chefList = ref([])

// 弹窗状态
const showFormModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)

const showDeleteModal = ref(false)
const deletingItem = ref(null)
const deleting = ref(false)

// 表单
const formData = ref(getEmptyForm())

function getEmptyForm() {
  return {
    id: null,
    title: '',
    description: '',
    videoUrl: '',
    coverImage: '',
    category: '',
    chefId: '',
    duration: '',
    difficulty: '',
    status: 'published',
  }
}

// Mock 数据
const mockVideos = [
  { id: 1, title: '刀工基础：切丝与切片技巧', description: '从零开始学习厨房最基本的刀工技巧', videoUrl: '', coverImage: '', category: '基础技巧', chefId: 1, chefName: '陈建明', duration: '15:30', difficulty: '简单', status: 'published', viewCount: 1280 },
  { id: 2, title: '红烧肉的秘诀', description: '掌握红烧肉的火候与调味，做出入口即化的经典味道', videoUrl: '', coverImage: '', category: '家常菜', chefId: 1, chefName: '陈建明', duration: '22:10', difficulty: '中等', status: 'published', viewCount: 2350 },
  { id: 3, title: '意大利面的正宗做法', description: '从面团到摆盘，学习制作正宗意大利面', videoUrl: '', coverImage: '', category: '西餐', chefId: 2, chefName: '李慧芳', duration: '28:45', difficulty: '中等', status: 'published', viewCount: 960 },
  { id: 4, title: '法式可颂完全指南', description: '酥脆可颂的制作秘诀，从折叠到烘烤全程演示', videoUrl: '', coverImage: '', category: '烘焙', chefId: 4, chefName: '张雅琴', duration: '35:20', difficulty: '困难', status: 'published', viewCount: 1870 },
  { id: 5, title: '分子料理入门', description: '探索分子料理的基本技法，制作创意前菜', videoUrl: '', coverImage: '', category: '创意料理', chefId: 3, chefName: '王大海', duration: '18:50', difficulty: '困难', status: 'published', viewCount: 720 },
  { id: 6, title: '家常炒菜火候掌握', description: '掌握中式炒菜的核心技巧，让每道菜都锅气十足', videoUrl: '', coverImage: '', category: '家常菜', chefId: 5, chefName: '刘建国', duration: '12:00', difficulty: '简单', status: 'unpublished', viewCount: 450 },
]

const mockChefs = [
  { id: 1, name: '陈建明' },
  { id: 2, name: '李慧芳' },
  { id: 3, name: '王大海' },
  { id: 4, name: '张雅琴' },
  { id: 5, name: '刘建国' },
]

// 数据映射
function mapVideo(item) {
  return {
    id: item.id,
    title: item.title || '',
    description: item.description || '',
    videoUrl: item.videoUrl || '',
    coverImage: item.coverImage || '',
    category: item.category || '',
    chefId: item.chefId || '',
    chefName: item.chefName || '',
    duration: item.duration || '',
    difficulty: item.difficulty || '中等',
    status: item.status || 'unpublished',
    viewCount: item.viewCount ?? 0,
    coverGradient: pickGradient(item.id != null ? item.id : item.title),
  }
}

// 筛选
const filteredList = computed(() => {
  return videoList.value.filter(video => {
    const matchesSearch = (video.title || '').toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesTab = activeTab.value === 'all' || video.category === activeTab.value
    return matchesSearch && matchesTab
  })
})

watch([searchQuery, activeTab], () => {
  // 触发重新筛选（computed 自动响应）
})

// API 调用
async function fetchVideos() {
  loading.value = true
  try {
    const params = {}
    if (activeTab.value !== 'all') params.category = activeTab.value
    if (searchQuery.value) params.keyword = searchQuery.value
    const data = await getVideoList(params)
    if (Array.isArray(data)) {
      videoList.value = data.map(mapVideo)
    } else {
      videoList.value = mockVideos.map(mapVideo)
    }
  } catch (e) {
    console.error('[fetchVideos error]', e)
    videoList.value = mockVideos.map(mapVideo)
  } finally {
    loading.value = false
  }
}

async function fetchChefs() {
  try {
    const data = await getChefList()
    if (Array.isArray(data) && data.length > 0) {
      chefList.value = data.map(c => ({ id: c.id, name: c.name || '' }))
    } else {
      chefList.value = [...mockChefs]
    }
  } catch (e) {
    console.error('[fetchChefs error]', e)
    chefList.value = [...mockChefs]
  }
}

// 弹窗操作
function handleAdd() {
  isEditing.value = false
  formData.value = getEmptyForm()
  showFormModal.value = true
}

function handleEdit(video) {
  isEditing.value = true
  formData.value = {
    id: video.id,
    title: video.title || '',
    description: video.description || '',
    videoUrl: video.videoUrl || '',
    coverImage: video.coverImage || '',
    category: video.category || '',
    chefId: video.chefId || '',
    duration: video.duration || '',
    difficulty: video.difficulty || '',
    status: video.status || 'unpublished',
  }
  showFormModal.value = true
}

function closeFormModal() {
  showFormModal.value = false
  formData.value = getEmptyForm()
}

function confirmDelete(video) {
  deletingItem.value = video
  showDeleteModal.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const chef = chefList.value.find(c => c.id === formData.value.chefId)
    const submitData = {
      ...formData.value,
      chefName: chef ? chef.name : '',
    }

    if (isEditing.value) {
      await updateVideo(formData.value.id, submitData)
    } else {
      await createVideo(submitData)
    }

    closeFormModal()
    await fetchVideos()
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
    await deleteVideo(deletingItem.value.id)
    showDeleteModal.value = false
    await fetchVideos()
  } catch (e) {
    console.error('[handleDelete error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  } finally {
    deleting.value = false
    deletingItem.value = null
  }
}

async function handleToggleStatus(video) {
  try {
    const newStatus = video.status === 'published' ? 'unpublished' : 'published'
    await updateVideo(video.id, { ...video, status: newStatus })
    await fetchVideos()
  } catch (e) {
    console.error('[handleToggleStatus error]', e)
    alert('状态切换失败：' + (e?.message || '未知错误'))
  }
}

onMounted(() => {
  fetchChefs()
  fetchVideos()
})
</script>
