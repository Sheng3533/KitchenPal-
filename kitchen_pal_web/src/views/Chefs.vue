<template>
  <div>
    <!-- 顶部：标题 + 添加按钮 + 搜索框 -->
    <div class="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <h1 class="text-xl font-bold text-foreground">大厨管理</h1>
      <div class="flex items-center gap-3">
        <div class="relative w-64">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索大厨姓名..."
            class="w-full pl-10 pr-4 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
            style="border-color: var(--border);"
            @focus="searchFocus = true"
            @blur="searchFocus = false"
            :style="{ borderColor: searchFocus ? 'var(--brand-primary)' : 'var(--border)', boxShadow: searchFocus ? '0 0 0 3px rgba(249, 115, 22, 0.1)' : 'none' }"
          />
        </div>
        <button
          @click="handleAdd"
          class="inline-flex items-center gap-2 px-4 py-2 text-white text-sm font-medium rounded-lg transition-colors shrink-0"
          :style="{ background: hoverAddBtn ? 'var(--brand-primary-dark)' : 'var(--brand-primary)' }"
          @mouseenter="hoverAddBtn = true"
          @mouseleave="hoverAddBtn = false"
        >
          <Plus class="w-4 h-4" />
          添加大厨
        </button>
      </div>
    </div>

    <!-- 大厨卡片网格 -->
    <div v-if="filteredChefs.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="chef in filteredChefs"
        :key="chef.id"
        class="card p-5 transition-shadow hover:shadow-md"
      >
        <!-- 头部：头像 + 姓名/头衔 + 在线状态 -->
        <div class="flex items-start gap-3 mb-4">
          <div
            class="w-12 h-12 rounded-full flex items-center justify-center text-lg font-bold text-white shrink-0"
            :style="{ background: chef.avatarGradient }"
          >
            {{ chef.initial }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <h3 class="text-base font-semibold text-foreground truncate">{{ chef.name }}</h3>
              <span
                class="w-2.5 h-2.5 rounded-full shrink-0"
                :style="{ backgroundColor: chef.status === 1 ? 'var(--state-success)' : 'var(--brand-neutral-400)' }"
                :title="chef.status === 1 ? '在线' : '离线'"
              ></span>
            </div>
            <p class="text-sm text-muted-foreground truncate">{{ chef.title || '大厨' }}</p>
          </div>
        </div>

        <!-- 擅长菜系标签 -->
        <div v-if="chef.specialty" class="mb-3">
          <div class="flex flex-wrap gap-1.5">
            <span
              v-for="tag in parseSpecialty(chef.specialty)"
              :key="tag"
              class="inline-flex items-center px-2 py-0.5 text-xs font-medium rounded-full"
              style="background: var(--brand-primary-lighter); color: var(--brand-primary-dark);"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 信息行 -->
        <div class="space-y-1.5 mb-3 text-sm">
          <div v-if="chef.signatureDish" class="flex items-center gap-2 text-muted-foreground">
            <Utensils class="w-3.5 h-3.5 shrink-0" style="color: var(--brand-primary);" />
            <span class="truncate">招牌菜：{{ chef.signatureDish }}</span>
          </div>
          <div v-if="chef.experienceYears" class="flex items-center gap-2 text-muted-foreground">
            <Clock class="w-3.5 h-3.5 shrink-0" style="color: var(--brand-primary);" />
            <span>从业 {{ chef.experienceYears }} 年</span>
          </div>
        </div>

        <!-- 简介 -->
        <p v-if="chef.description" class="text-sm text-muted-foreground mb-4 line-clamp-2">
          {{ chef.description }}
        </p>

        <!-- 操作按钮 -->
        <div class="flex items-center gap-1 pt-3 border-t" style="border-color: var(--border);">
          <button
            @click="openChat(chef)"
            class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md transition-colors text-white"
            style="background: var(--brand-primary);"
            title="与大厨聊天"
          >
            <MessageCircle class="w-3.5 h-3.5" />
            聊天
          </button>
          <button
            @click="handleEdit(chef)"
            class="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
            title="编辑"
          >
            <Pencil class="w-4 h-4" />
          </button>
          <button
            @click="confirmDelete(chef)"
            class="p-1.5 rounded-md text-muted-foreground hover:text-state-error hover:bg-state-error-light transition-colors"
            title="删除"
          >
            <Trash2 class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="card p-12 text-center">
      <Search class="w-12 h-12 text-muted-foreground/40 mx-auto mb-3" />
      <p class="text-sm text-muted-foreground">{{ loading ? '加载中...' : '没有找到匹配的大厨' }}</p>
    </div>

    <!-- 聊天弹窗 -->
    <Teleport to="body">
      <div v-if="showChatModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeChat"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-3xl h-[70vh] overflow-hidden flex">
          <!-- 左侧：大厨信息卡片 -->
          <div class="w-64 shrink-0 border-r p-5 flex flex-col" style="border-color: var(--border);">
            <button @click="closeChat" class="self-end p-1 rounded-md hover:bg-muted transition-colors mb-3">
              <X class="w-4 h-4 text-muted-foreground" />
            </button>
            <div class="flex flex-col items-center text-center">
              <div
                class="w-16 h-16 rounded-full flex items-center justify-center text-xl font-bold text-white mb-3"
                :style="{ background: chatChef?.avatarGradient }"
              >
                {{ chatChef?.initial }}
              </div>
              <h3 class="text-base font-semibold text-foreground">{{ chatChef?.name }}</h3>
              <p class="text-sm text-muted-foreground mt-1">{{ chatChef?.title || '大厨' }}</p>
              <span
                class="mt-2 inline-flex items-center gap-1.5 px-2.5 py-0.5 text-xs font-medium rounded-full"
                :style="chatChef?.status === 1
                  ? { background: 'var(--state-success-light)', color: 'var(--state-success)' }
                  : { background: 'var(--ak-muted)', color: 'var(--ak-muted-foreground)' }"
              >
                <span
                  class="w-1.5 h-1.5 rounded-full"
                  :style="{ backgroundColor: chatChef?.status === 1 ? 'var(--state-success)' : 'var(--ak-muted-foreground)' }"
                ></span>
                {{ chatChef?.status === 1 ? '在线' : '离线' }}
              </span>
            </div>
            <div class="mt-4 space-y-2 text-sm text-muted-foreground text-left">
              <div v-if="chatChef?.specialty" class="flex items-center gap-2">
                <Utensils class="w-3.5 h-3.5 shrink-0" />
                <span class="truncate">{{ chatChef.specialty }}</span>
              </div>
              <div v-if="chatChef?.signatureDish" class="flex items-center gap-2">
                <Flame class="w-3.5 h-3.5 shrink-0" />
                <span class="truncate">{{ chatChef.signatureDish }}</span>
              </div>
              <div v-if="chatChef?.experienceYears" class="flex items-center gap-2">
                <Clock class="w-3.5 h-3.5 shrink-0" />
                <span>从业 {{ chatChef.experienceYears }} 年</span>
              </div>
            </div>
          </div>

          <!-- 右侧：聊天区域 -->
          <div class="flex-1 flex flex-col min-w-0">
            <!-- 消息区域 -->
            <div ref="chatContainer" class="flex-1 overflow-y-auto p-4 space-y-4 content-scroll">
              <div v-if="chatMessages.length === 0" class="flex flex-col items-center justify-center h-full text-muted-foreground">
                <MessageCircle class="w-10 h-10 mb-2 opacity-40" />
                <p class="text-sm">向 {{ chatChef?.name }} 提问烹饪技巧吧</p>
              </div>
              <div
                v-for="(msg, idx) in chatMessages"
                :key="idx"
                class="flex"
                :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
              >
                <div
                  class="max-w-[75%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed"
                  :class="msg.role === 'user'
                    ? 'text-white rounded-br-md'
                    : 'text-foreground rounded-bl-md'"
                  :style="msg.role === 'user'
                    ? { background: 'var(--brand-primary)' }
                    : { background: 'var(--ak-muted)' }"
                >
                  {{ msg.content }}
                </div>
              </div>
              <!-- 加载中动画 -->
              <div v-if="chatLoading" class="flex justify-start">
                <div class="px-4 py-3 rounded-2xl rounded-bl-md" style="background: var(--ak-muted);">
                  <div class="flex items-center gap-1">
                    <span class="w-2 h-2 rounded-full animate-bounce" style="background: var(--ak-muted-foreground); animation-delay: 0ms;"></span>
                    <span class="w-2 h-2 rounded-full animate-bounce" style="background: var(--ak-muted-foreground); animation-delay: 150ms;"></span>
                    <span class="w-2 h-2 rounded-full animate-bounce" style="background: var(--ak-muted-foreground); animation-delay: 300ms;"></span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 输入框 -->
            <div class="p-3 border-t" style="border-color: var(--border);">
              <div class="flex items-center gap-2">
                <input
                  v-model="chatInput"
                  type="text"
                  placeholder="输入消息..."
                  class="flex-1 px-4 py-2.5 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="chatInputFocus = true"
                  @blur="chatInputFocus = false"
                  :style="{ borderColor: chatInputFocus ? 'var(--brand-primary)' : 'var(--border)' }"
                  @keydown.enter="sendMessage"
                  :disabled="chatLoading"
                />
                <button
                  @click="sendMessage"
                  :disabled="chatLoading || !chatInput.trim()"
                  class="inline-flex items-center justify-center w-10 h-10 rounded-lg text-white transition-colors disabled:opacity-50"
                  style="background: var(--brand-primary);"
                >
                  <Send class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 添加/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeFormModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <h2 class="text-lg font-semibold text-foreground">{{ isEditing ? '编辑大厨' : '添加大厨' }}</h2>
            <button @click="closeFormModal" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
            <form @submit.prevent="handleSave" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">姓名 *</label>
                <input
                  v-model="formData.name"
                  type="text"
                  required
                  placeholder="请输入大厨姓名"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField('name')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'name' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">头衔 *</label>
                <input
                  v-model="formData.title"
                  type="text"
                  required
                  placeholder="例如：川菜大师"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField('title')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'title' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">擅长菜系 *</label>
                <input
                  v-model="formData.specialty"
                  type="text"
                  required
                  placeholder="例如：川菜、粤菜（逗号分隔）"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField('specialty')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'specialty' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">烹饪风格</label>
                <input
                  v-model="formData.style"
                  type="text"
                  placeholder="例如：麻辣鲜香"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField('style')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'style' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">招牌菜</label>
                <input
                  v-model="formData.signatureDish"
                  type="text"
                  placeholder="例如：水煮鱼"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField('signatureDish')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'signatureDish' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">从业年限</label>
                <input
                  v-model.number="formData.experienceYears"
                  type="number"
                  min="0"
                  placeholder="请输入从业年限"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                  @focus="focusField('experienceYears')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'experienceYears' ? 'var(--brand-primary)' : 'var(--border)' }"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">简介</label>
                <textarea
                  v-model="formData.description"
                  rows="3"
                  placeholder="请输入大厨简介"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground resize-none"
                  style="border-color: var(--border);"
                  @focus="focusField('description')"
                  @blur="blurField"
                  :style="{ borderColor: focusedField === 'description' ? 'var(--brand-primary)' : 'var(--border)' }"
                ></textarea>
              </div>

              <div class="flex items-center justify-end gap-3 pt-4">
                <button
                  type="button"
                  @click="closeFormModal"
                  class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors"
                >
                  取消
                </button>
                <button
                  type="submit"
                  :disabled="saving"
                  class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50"
                  style="background: var(--brand-primary);"
                >
                  <span v-if="saving">保存中...</span>
                  <span v-else>{{ isEditing ? '保存修改' : '添加大厨' }}</span>
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
            <p class="text-sm text-muted-foreground">确定要删除大厨「{{ deletingChef?.name }}」吗？此操作无法撤销。</p>
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import {
  Search, Plus, MessageCircle, Pencil, Trash2, X, Send,
  Utensils, Clock, Flame, AlertCircle
} from 'lucide-vue-next'
import { getChefList, createChef, updateChef, deleteChef, chatWithChef } from '@/api/chef'

// --- 列表相关 ---
const searchQuery = ref('')
const searchFocus = ref(false)
const hoverAddBtn = ref(false)
const loading = ref(false)
const chefs = ref([])

const gradients = [
  'linear-gradient(135deg, #F97316, #EA580C)',
  'linear-gradient(135deg, #FB923C, #F97316)',
  'linear-gradient(135deg, #F59E0B, #D97706)',
  'linear-gradient(135deg, #EF4444, #DC2626)',
  'linear-gradient(135deg, #10B981, #059669)',
  'linear-gradient(135deg, #3B82F6, #2563EB)',
  'linear-gradient(135deg, #8B5CF6, #7C3AED)',
  'linear-gradient(135deg, #EC4899, #DB2777)',
]

function pickGradient(key) {
  const index = (typeof key === 'string' ? key.charCodeAt(0) : Number(key)) % gradients.length
  return gradients[index >= 0 ? index : 0]
}

function mapChef(item) {
  const name = item.name || ''
  const initial = name ? name.charAt(0) : '厨'
  return {
    ...item,
    initial,
    avatarGradient: pickGradient(item.id != null ? item.id : name),
    status: typeof item.status === 'number' ? item.status : (item.status === 1 || item.status === 'active' ? 1 : 0),
    specialty: item.specialty || '',
    title: item.title || '',
    signatureDish: item.signatureDish || '',
    experienceYears: item.experienceYears || 0,
    description: item.description || '',
    style: item.style || '',
  }
}

const mockChefs = [
  { id: 1, name: '陈建明', title: '川菜大师', specialty: '川菜,火锅', style: '麻辣鲜香', signatureDish: '水煮鱼', experienceYears: 25, description: '国家级川菜大师，擅长传统川菜与创新融合，曾获多项国际烹饪大奖。', status: 1 },
  { id: 2, name: '李慧芳', title: '粤菜名厨', specialty: '粤菜,点心', style: '清淡鲜美', signatureDish: '白切鸡', experienceYears: 18, description: '粤菜世家传人，精通广式点心与煲汤技艺，注重食材本味。', status: 1 },
  { id: 3, name: '王大海', title: '鲁菜传承人', specialty: '鲁菜,海鲜', style: '咸鲜醇厚', signatureDish: '葱烧海参', experienceYears: 30, description: '鲁菜非遗传承人，擅长海鲜烹饪与传统宴席制作。', status: 0 },
  { id: 4, name: '张雅琴', title: '甜品女王', specialty: '甜点,烘焙', style: '精致甜蜜', signatureDish: '提拉米苏', experienceYears: 12, description: '法国蓝带毕业，专注法式甜点与创意烘焙，作品多次登上美食杂志。', status: 1 },
  { id: 5, name: '刘建国', title: '湘菜大师', specialty: '湘菜,腊味', style: '香辣浓郁', signatureDish: '剁椒鱼头', experienceYears: 22, description: '湖南本地大厨，精通腊味制作与湘菜小炒，追求地道湖南味。', status: 1 },
  { id: 6, name: '赵美玲', title: '日料匠人', specialty: '日料,寿司', style: '极致鲜美', signatureDish: '金枪鱼刺身', experienceYears: 15, description: '东京修业十年，精通江户前寿司与怀石料理，追求食材极致鲜美。', status: 0 },
]

async function fetchChefs() {
  loading.value = true
  try {
    const data = await getChefList()
    if (Array.isArray(data)) {
      chefs.value = data.map(mapChef)
    } else {
      chefs.value = mockChefs.map(mapChef)
    }
  } catch (e) {
    console.error('[fetchChefs error]', e)
    chefs.value = mockChefs.map(mapChef)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchChefs()
})

function parseSpecialty(specialty) {
  if (!specialty) return []
  return specialty.split(/[,，、]/).map(s => s.trim()).filter(Boolean)
}

const filteredChefs = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return chefs.value
  return chefs.value.filter(chef =>
    chef.name.toLowerCase().includes(keyword) ||
    (chef.title && chef.title.toLowerCase().includes(keyword)) ||
    (chef.specialty && chef.specialty.toLowerCase().includes(keyword))
  )
})

// --- 表单弹窗相关 ---
const showFormModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const focusedField = ref('')

const formData = ref({
  id: null,
  name: '',
  title: '',
  specialty: '',
  style: '',
  signatureDish: '',
  experienceYears: null,
  description: '',
})

function resetForm() {
  formData.value = {
    id: null,
    name: '',
    title: '',
    specialty: '',
    style: '',
    signatureDish: '',
    experienceYears: null,
    description: '',
  }
}

function focusField(field) {
  focusedField.value = field
}

function blurField() {
  focusedField.value = ''
}

function handleAdd() {
  isEditing.value = false
  resetForm()
  showFormModal.value = true
}

function handleEdit(chef) {
  isEditing.value = true
  formData.value = {
    id: chef.id,
    name: chef.name || '',
    title: chef.title || '',
    specialty: chef.specialty || '',
    style: chef.style || '',
    signatureDish: chef.signatureDish || '',
    experienceYears: chef.experienceYears || null,
    description: chef.description || '',
  }
  showFormModal.value = true
}

function closeFormModal() {
  showFormModal.value = false
  resetForm()
}

async function handleSave() {
  saving.value = true
  try {
    const payload = { ...formData.value }
    if (isEditing.value) {
      await updateChef(formData.value.id, payload)
    } else {
      await createChef(payload)
    }
    closeFormModal()
    await fetchChefs()
  } catch (e) {
    console.error('[handleSave error]', e)
    alert('保存失败：' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// --- 删除弹窗相关 ---
const showDeleteModal = ref(false)
const deletingChef = ref(null)
const deleting = ref(false)

function confirmDelete(chef) {
  deletingChef.value = chef
  showDeleteModal.value = true
}

async function handleDelete() {
  if (!deletingChef.value) return
  deleting.value = true
  try {
    await deleteChef(deletingChef.value.id)
    chefs.value = chefs.value.filter(c => c.id !== deletingChef.value.id)
    showDeleteModal.value = false
  } catch (e) {
    console.error('[handleDelete error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  } finally {
    deleting.value = false
    deletingChef.value = null
  }
}

// --- 聊天弹窗相关 ---
const showChatModal = ref(false)
const chatChef = ref(null)
const chatMessages = ref([])
const chatInput = ref('')
const chatInputFocus = ref(false)
const chatLoading = ref(false)
const chatContainer = ref(null)

function openChat(chef) {
  chatChef.value = chef
  chatMessages.value = []
  chatInput.value = ''
  chatLoading.value = false
  showChatModal.value = true
}

function closeChat() {
  showChatModal.value = false
  chatChef.value = null
  chatMessages.value = []
  chatInput.value = ''
  chatLoading.value = false
}

async function sendMessage() {
  const msg = chatInput.value.trim()
  if (!msg || chatLoading.value) return

  chatMessages.value.push({ role: 'user', content: msg })
  chatInput.value = ''
  chatLoading.value = true

  await nextTick()
  scrollToBottom()

  try {
    const data = await chatWithChef(chatChef.value.id, { message: msg })
    const reply = typeof data === 'string' ? data : (data?.reply || data?.message || data?.content || JSON.stringify(data))
    chatMessages.value.push({ role: 'assistant', content: reply })
  } catch (e) {
    console.error('[chatWithChef error]', e)
    chatMessages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后再试。' })
  } finally {
    chatLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
