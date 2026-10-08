<template>
  <div>
    <!-- 顶部标题 -->
    <div class="mb-6">
      <h1 class="text-xl font-bold text-foreground">内容与活动</h1>
      <p class="text-sm text-muted-foreground mt-1">管理文章、活动、公告，发布后客户端可见</p>
    </div>

    <!-- Tab 切换 -->
    <div class="flex items-center gap-1 mb-4 p-1 bg-muted/50 rounded-lg w-fit">
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
            placeholder="搜索标题..."
            class="input-field pl-10"
          />
        </div>
        <button
          @click="handleAdd"
          class="btn-primary inline-flex items-center gap-2 shrink-0"
        >
          <Plus class="w-4 h-4" />
          发布内容
        </button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[1000px]">
          <thead>
            <tr class="border-b" style="border-color: var(--ak-border);">
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">标题</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">类型</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">作者</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">状态</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">浏览量</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">创建时间</th>
              <th class="text-right text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y" style="--tw-divide-opacity: 1; border-color: var(--ak-border);">
            <tr
              v-for="item in paginatedList"
              :key="item.id"
              class="transition-colors hover:bg-muted/30"
            >
              <td class="px-4 py-3">
                <div class="font-medium text-foreground text-sm truncate max-w-[200px]">{{ item.title }}</div>
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="{ backgroundColor: typeConfig[item.type]?.bgColor, color: typeConfig[item.type]?.color }"
                >
                  {{ typeConfig[item.type]?.label || item.type }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ item.author || '-' }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="item.status === 'published'
                    ? { backgroundColor: 'var(--state-success-light)', color: 'var(--state-success)' }
                    : { backgroundColor: 'var(--state-warning-light)', color: 'var(--state-warning)' }"
                >
                  {{ item.status === 'published' ? '已发布' : '草稿' }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ item.viewCount ?? 0 }}</td>
              <td class="px-4 py-3 text-sm text-muted-foreground whitespace-nowrap">{{ item.createdAt || '-' }}</td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-end gap-1">
                  <button
                    @click="handlePreview(item)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    title="预览"
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
                    @click="handleToggleStatus(item)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    :title="item.status === 'published' ? '转为草稿' : '发布'"
                  >
                    <component :is="item.status === 'published' ? FilePenLine : Send" class="w-4 h-4 text-muted-foreground" />
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
            <tr v-if="filteredList.length === 0">
              <td colspan="7" class="px-4 py-12 text-center">
                <div class="flex flex-col items-center gap-2">
                  <Inbox class="w-10 h-10 text-muted-foreground/50" />
                  <p class="text-sm text-muted-foreground">暂无内容</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div
        v-if="filteredList.length > 0"
        class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t"
        style="border-color: var(--ak-border);"
      >
        <div class="text-sm text-muted-foreground">
          共 {{ total }} 条，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="flex items-center gap-1">
          <button
            @click="currentPage = 1"
            :disabled="currentPage === 1"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--ak-border);"
          >
            <ChevronsLeft class="w-4 h-4 text-foreground" />
          </button>
          <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--ak-border);"
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
            style="border-color: var(--ak-border);"
          >
            <ChevronRight class="w-4 h-4 text-foreground" />
          </button>
          <button
            @click="currentPage = totalPages"
            :disabled="currentPage === totalPages"
            class="p-2 rounded-lg border transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
            style="border-color: var(--ak-border);"
          >
            <ChevronsRight class="w-4 h-4 text-foreground" />
          </button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeFormModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--ak-border);">
            <h2 class="text-lg font-semibold text-foreground">{{ isEditing ? '编辑内容' : '发布内容' }}</h2>
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
                  placeholder="请输入标题"
                  class="input-field"
                />
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">类型 *</label>
                  <select
                    v-model="formData.type"
                    required
                    class="input-field cursor-pointer"
                  >
                    <option value="article">文章</option>
                    <option value="activity">活动</option>
                    <option value="promotion">促销</option>
                    <option value="announcement">公告</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">作者 *</label>
                  <input
                    v-model="formData.author"
                    type="text"
                    required
                    placeholder="请输入作者"
                    class="input-field"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">封面图URL</label>
                <input
                  v-model="formData.coverUrl"
                  type="text"
                  placeholder="请输入封面图URL"
                  class="input-field"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">正文内容 *</label>
                <textarea
                  v-model="formData.content"
                  rows="8"
                  required
                  placeholder="请输入正文内容"
                  class="input-field resize-none"
                ></textarea>
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">状态</label>
                <div class="flex items-center gap-4 mt-1">
                  <label class="inline-flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="formData.status"
                      type="radio"
                      value="published"
                      class="accent-[var(--brand-primary)]"
                    />
                    <span class="text-sm text-foreground">发布</span>
                  </label>
                  <label class="inline-flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="formData.status"
                      type="radio"
                      value="draft"
                      class="accent-[var(--brand-primary)]"
                    />
                    <span class="text-sm text-foreground">草稿</span>
                  </label>
                </div>
              </div>

              <div class="flex items-center justify-end gap-3 pt-4">
                <button type="button" @click="closeFormModal" class="btn-secondary">取消</button>
                <button type="submit" :disabled="saving" class="btn-primary disabled:opacity-50">
                  <span v-if="saving">保存中...</span>
                  <span v-else>{{ isEditing ? '保存修改' : '发布' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 预览弹窗 -->
    <Teleport to="body">
      <div v-if="showPreviewModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="showPreviewModal = false"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--ak-border);">
            <h2 class="text-lg font-semibold text-foreground">内容预览</h2>
            <button @click="showPreviewModal = false" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]" v-if="previewData">
            <div class="mb-5">
              <h3 class="text-lg font-bold text-foreground break-words">{{ previewData.title }}</h3>
              <div class="flex items-center gap-3 mt-2">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="{ backgroundColor: typeConfig[previewData.type]?.bgColor, color: typeConfig[previewData.type]?.color }"
                >
                  {{ typeConfig[previewData.type]?.label || previewData.type }}
                </span>
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="previewData.status === 'published'
                    ? { backgroundColor: 'var(--state-success-light)', color: 'var(--state-success)' }
                    : { backgroundColor: 'var(--state-warning-light)', color: 'var(--state-warning)' }"
                >
                  {{ previewData.status === 'published' ? '已发布' : '草稿' }}
                </span>
              </div>
            </div>

            <div class="grid grid-cols-3 gap-4 mb-5 py-4 border-y" style="border-color: var(--ak-border);">
              <div>
                <div class="text-xs text-muted-foreground mb-1">作者</div>
                <div class="text-sm font-medium text-foreground">{{ previewData.author || '-' }}</div>
              </div>
              <div>
                <div class="text-xs text-muted-foreground mb-1">浏览量</div>
                <div class="text-sm font-medium text-foreground">{{ previewData.viewCount ?? 0 }}</div>
              </div>
              <div>
                <div class="text-xs text-muted-foreground mb-1">创建时间</div>
                <div class="text-sm font-medium text-foreground">{{ previewData.createdAt || '-' }}</div>
              </div>
            </div>

            <div v-if="previewData.coverUrl" class="mb-5">
              <div class="text-xs text-muted-foreground mb-2">封面图</div>
              <img :src="previewData.coverUrl" alt="封面" class="w-full max-h-[240px] object-cover rounded-lg" />
            </div>

            <div class="mb-5">
              <div class="text-xs text-muted-foreground mb-2">正文内容</div>
              <div class="text-sm text-foreground leading-relaxed whitespace-pre-wrap bg-muted/30 rounded-lg p-4">
                {{ previewData.content || '（无内容）' }}
              </div>
            </div>

            <div class="flex items-center justify-end gap-3 pt-4 border-t" style="border-color: var(--ak-border);">
              <button @click="showPreviewModal = false" class="btn-secondary">关闭</button>
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
            <p class="text-sm text-muted-foreground">确定要删除「{{ deletingItem?.title }}」吗？此操作无法撤销。</p>
          </div>
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t" style="border-color: var(--ak-border);">
            <button @click="showDeleteModal = false" class="btn-secondary">取消</button>
            <button @click="handleDelete" :disabled="deleting" class="btn-danger disabled:opacity-50">
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
import { getContentList, createContent, updateContent, deleteContent } from '@/api/content'
import {
  Search,
  Plus,
  Eye,
  Pencil,
  Trash2,
  X,
  Send,
  FilePenLine,
  AlertCircle,
  Inbox,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight
} from 'lucide-vue-next'

// Tab 配置
const tabs = [
  { label: '全部', value: 'all' },
  { label: '文章', value: 'article' },
  { label: '活动', value: 'activity' },
  { label: '促销', value: 'promotion' },
  { label: '公告', value: 'announcement' },
]

// 类型配置：label + 颜色
const typeConfig = {
  article:      { label: '文章', color: '#2563EB', bgColor: '#DBEAFE' },
  activity:     { label: '活动', color: '#16A34A', bgColor: '#DCFCE7' },
  promotion:    { label: '促销', color: '#EA580C', bgColor: '#FED7AA' },
  announcement: { label: '公告', color: '#9333EA', bgColor: '#F3E8FF' },
}

// 状态
const activeTab = ref('all')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = 10

const contentList = ref([])

// 弹窗状态
const showFormModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)

const showPreviewModal = ref(false)
const previewData = ref(null)

const showDeleteModal = ref(false)
const deletingItem = ref(null)
const deleting = ref(false)

// 表单
const formData = ref(getEmptyForm())

function getEmptyForm() {
  return {
    id: null,
    title: '',
    type: 'article',
    author: '',
    coverUrl: '',
    content: '',
    status: 'draft',
  }
}

// Mock 数据兜底
const mockData = [
  { id: 1, title: '夏日清凉饮品制作指南', type: 'article', author: '小厨娘', coverUrl: '', content: '炎炎夏日，来一杯自制清凉饮品吧！本文将介绍5款简单易做的夏日饮品，包括柠檬薄荷水、西瓜汁、冰镇酸梅汤等，让你在家也能享受清凉。', status: 'published', viewCount: 320, createdAt: '2024-07-18 10:00' },
  { id: 2, title: '中秋美食节活动报名', type: 'activity', author: '运营组', coverUrl: '', content: '中秋美食节活动正式开启！参与线上烹饪比赛，分享你的中秋家宴，即有机会赢取精美厨房用品大奖。活动时间：9月10日-9月20日。', status: 'published', viewCount: 580, createdAt: '2024-07-17 14:30' },
  { id: 3, title: '限时8折优惠：厨房工具套装', type: 'promotion', author: '市场部', coverUrl: '', content: '本周特惠！精选厨房工具套装限时8折，包含刀具、砧板、量杯等必备工具，优惠截止至7月25日，先到先得！', status: 'published', viewCount: 210, createdAt: '2024-07-16 09:00' },
  { id: 4, title: '系统升级维护通知', type: 'announcement', author: '技术团队', coverUrl: '', content: '为提升服务品质，系统将于7月22日凌晨2:00-5:00进行升级维护，届时部分功能可能暂时不可用，敬请谅解。', status: 'published', viewCount: 150, createdAt: '2024-07-15 16:00' },
  { id: 5, title: '新手烹饪入门技巧', type: 'article', author: '美食达人', coverUrl: '', content: '刚开始学做饭？别担心！本文总结了10个新手必知的烹饪小技巧，从刀工到调味，帮你快速上手。', status: 'draft', viewCount: 0, createdAt: '2024-07-14 11:20' },
  { id: 6, title: '周末烘焙工坊线下活动', type: 'activity', author: '社区运营', coverUrl: '', content: '本周末在城市中心店举办线下烘焙工坊，专业烘焙师现场教学，带你制作法式可颂和提拉米苏！名额有限，先报先得。', status: 'draft', viewCount: 0, createdAt: '2024-07-13 08:45' },
  { id: 7, title: '会员专享福利：满100减30', type: 'promotion', author: '市场部', coverUrl: '', content: '会员专享福利来袭！下单满100元立减30元，可与积分叠加使用，活动持续一整月。', status: 'published', viewCount: 430, createdAt: '2024-07-12 10:00' },
  { id: 8, title: '隐私政策更新公告', type: 'announcement', author: '法务部门', coverUrl: '', content: '我们的隐私政策已更新，请查阅最新版本以了解我们如何保护您的个人信息。更新将于8月1日正式生效。', status: 'draft', viewCount: 0, createdAt: '2024-07-11 17:30' },
  { id: 9, title: '一周热门食谱推荐', type: 'article', author: '编辑部', coverUrl: '', content: '本周热门食谱排行榜出炉！红烧肉、番茄炒蛋、麻婆豆腐名列前三，快来看看详细做法吧。', status: 'published', viewCount: 670, createdAt: '2024-07-10 07:00' },
  { id: 10, title: '亲子厨房活动报名中', type: 'activity', author: '运营组', coverUrl: '', content: '带上孩子一起来做美食吧！亲子厨房活动面向5-12岁儿童家庭，专业老师指导，制作趣味造型饼干和水果沙拉。', status: 'published', viewCount: 290, createdAt: '2024-07-09 13:00' },
  { id: 11, title: '新用户注册立享9折', type: 'promotion', author: '市场部', coverUrl: '', content: '新用户注册即享首单9折优惠，还可获得50积分奖励，快来加入我们的美食社区吧！', status: 'published', viewCount: 360, createdAt: '2024-07-08 09:30' },
  { id: 12, title: '功能更新：新增菜谱收藏夹', type: 'announcement', author: '产品团队', coverUrl: '', content: '我们新增了菜谱收藏夹功能，你可以将喜欢的菜谱一键收藏，随时查看。同时优化了搜索体验，快来试试吧！', status: 'published', viewCount: 180, createdAt: '2024-07-07 15:00' },
]

// 数据映射
function mapItem(item) {
  return {
    id: item.id,
    title: item.title || '-',
    type: item.type || 'article',
    author: item.author || '-',
    coverUrl: item.coverUrl || '',
    content: item.content || '',
    status: item.status || 'draft',
    viewCount: item.viewCount ?? 0,
    createdAt: item.createdAt || '-',
  }
}

// 计算属性
const filteredList = computed(() => {
  return contentList.value.filter(item => {
    const matchesSearch = (item.title || '').toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesType = activeTab.value === 'all' || item.type === activeTab.value
    return matchesSearch && matchesType
  })
})

const total = computed(() => filteredList.value.length)
const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
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

// 分页操作
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

// 监听筛选条件变化重置分页
watch([searchQuery, activeTab], () => {
  currentPage.value = 1
})

// API 调用
async function fetchList() {
  try {
    const data = await getContentList()
    if (Array.isArray(data)) {
      contentList.value = data.map(mapItem)
    } else {
      contentList.value = mockData.map(mapItem)
    }
  } catch (e) {
    console.error('[fetchList error]', e)
    contentList.value = mockData.map(mapItem)
  }
}

// 弹窗操作
function handleAdd() {
  isEditing.value = false
  formData.value = getEmptyForm()
  showFormModal.value = true
}

function handleEdit(item) {
  isEditing.value = true
  formData.value = {
    id: item.id,
    title: item.title || '',
    type: item.type || 'article',
    author: item.author || '',
    coverUrl: item.coverUrl || '',
    content: item.content || '',
    status: item.status || 'draft',
  }
  showFormModal.value = true
}

function closeFormModal() {
  showFormModal.value = false
  formData.value = getEmptyForm()
}

function handlePreview(item) {
  previewData.value = { ...item }
  showPreviewModal.value = true
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
      type: formData.value.type,
      author: formData.value.author,
      coverUrl: formData.value.coverUrl || null,
      content: formData.value.content,
      status: formData.value.status,
    }

    if (isEditing.value) {
      await updateContent(formData.value.id, submitData)
    } else {
      await createContent(submitData)
    }

    closeFormModal()
    await fetchList()
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
    await deleteContent(deletingItem.value.id)
    showDeleteModal.value = false
    await fetchList()
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

async function handleToggleStatus(item) {
  try {
    const newStatus = item.status === 'published' ? 'draft' : 'published'
    await updateContent(item.id, { ...item, status: newStatus })
    await fetchList()
  } catch (e) {
    console.error('[handleToggleStatus error]', e)
    alert('状态切换失败：' + (e?.message || '未知错误'))
  }
}

onMounted(() => {
  fetchList()
})
</script>
