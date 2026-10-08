<template>
  <div>
    <div class="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <h1 class="text-xl font-bold text-foreground">食谱管理</h1>
      <button
        @click="handleAddRecipe"
        class="inline-flex items-center gap-2 px-4 py-2 text-white text-sm font-medium rounded-lg transition-colors"
        style="background: var(--brand-primary);"
        @mouseenter="hoverAddBtn = true"
        @mouseleave="hoverAddBtn = false"
        :style="{ background: hoverAddBtn ? 'var(--brand-primary-dark)' : 'var(--brand-primary)' }"
      >
        <Plus class="w-4 h-4" />
        新增食谱
      </button>
    </div>

    <div class="p-4 card mb-4">
      <div class="flex flex-col lg:flex-row gap-3">
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索食谱名称..."
            class="w-full pl-10 pr-4 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
            style="border-color: var(--border);"
            @focus="searchFocus = true"
            @blur="searchFocus = false"
            :style="{ borderColor: searchFocus ? 'var(--brand-primary)' : 'var(--border)', boxShadow: searchFocus ? '0 0 0 3px rgba(249, 115, 22, 0.1)' : 'none' }"
          />
        </div>

        <div class="relative min-w-[160px]">
          <select
            v-model="selectedCategory"
            class="w-full appearance-none pl-4 pr-10 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground cursor-pointer"
            style="border-color: var(--border);"
          >
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground pointer-events-none" />
        </div>

        <div class="flex items-center gap-2 flex-wrap">
          <span class="text-sm text-muted-foreground shrink-0">难度：</span>
          <button
            v-for="diff in difficulties"
            :key="diff.value"
            @click="toggleDifficulty(diff.value)"
            class="px-3 py-1.5 text-xs font-medium rounded-full transition-colors"
            :class="selectedDifficulties.includes(diff.value) ? 'text-white' : 'bg-muted text-muted-foreground hover:bg-muted/80'"
            :style="selectedDifficulties.includes(diff.value) ? { backgroundColor: diff.color } : {}"
          >
            {{ diff.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[900px]">
          <thead>
            <tr class="border-b" style="border-color: var(--border);">
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">食谱</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">分类</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">难度</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">烹饪时间</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">卡路里</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">收藏量</th>
              <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">状态</th>
              <th class="text-right text-xs font-medium text-muted-foreground uppercase tracking-wider px-4 py-3">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y" style="border-color: var(--border);">
            <tr
              v-for="recipe in paginatedRecipes"
              :key="recipe.id"
              class="transition-colors hover:bg-muted/30"
            >
              <td class="px-4 py-3">
                <div class="flex items-center gap-3">
                  <div
                    class="w-10 h-10 rounded-full flex items-center justify-center shrink-0"
                    :style="{ backgroundColor: recipe.bgColor }"
                  >
                    <component :is="recipe.icon" class="w-5 h-5" :style="{ color: recipe.iconColor }" />
                  </div>
                  <span class="font-medium text-foreground text-sm">{{ recipe.name }}</span>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-muted-foreground">{{ recipe.category }}</td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="{ backgroundColor: getDifficultyBadge(recipe.difficulty).bg, color: getDifficultyBadge(recipe.difficulty).text }"
                >
                  {{ recipe.difficulty }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5 text-sm text-muted-foreground">
                  <Clock class="w-3.5 h-3.5" />
                  {{ recipe.cookTime }}
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5 text-sm text-muted-foreground">
                  <Flame class="w-3.5 h-3.5" style="color: var(--state-error);" />
                  {{ recipe.calories }}
                </div>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-1.5 text-sm text-muted-foreground">
                  <Heart class="w-3.5 h-3.5" style="color: var(--state-error);" />
                  {{ recipe.favorites.toLocaleString() }}
                </div>
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :style="recipe.status === '上架' ? { backgroundColor: 'var(--state-success-light)', color: 'var(--state-success)' } : { backgroundColor: 'var(--muted)', color: 'var(--muted-foreground)' }"
                >
                  {{ recipe.status }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-end gap-1">
                  <button
                    @click="toggleRecommend(recipe)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    :title="recipe.recommended ? '取消推荐' : '设为推荐'"
                  >
                    <Star
                      class="w-4 h-4"
                      :class="recipe.recommended ? 'fill-current' : ''"
                      :style="recipe.recommended ? { color: '#F59E0B' } : { color: 'var(--muted-foreground)' }"
                    />
                  </button>
                  <button
                    @click="openComments(recipe)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    title="查看评价"
                  >
                    <MessageSquare class="w-4 h-4 text-muted-foreground" />
                  </button>
                  <button
                    @click="handleEdit(recipe)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-muted"
                    title="编辑"
                  >
                    <Pencil class="w-4 h-4 text-muted-foreground" />
                  </button>
                  <button
                    @click="confirmDelete(recipe)"
                    class="p-1.5 rounded-lg transition-colors hover:bg-state-error-light"
                    title="删除"
                  >
                    <Trash2 class="w-4 h-4" style="color: var(--state-error);" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredRecipes.length === 0">
              <td colspan="8" class="px-4 py-12 text-center">
                <div class="flex flex-col items-center gap-2">
                  <Search class="w-10 h-10 text-muted-foreground/50" />
                  <p class="text-sm text-muted-foreground">没有找到匹配的食谱</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        v-if="filteredRecipes.length > 0"
        class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t"
        style="border-color: var(--border);"
      >
        <div class="text-sm text-muted-foreground">
          共 {{ totalRecipes }} 条，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="flex items-center gap-1">
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
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeModal"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <h2 class="text-lg font-semibold text-foreground">{{ isEditing ? '编辑食谱' : '新增食谱' }}</h2>
            <button @click="closeModal" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>
          <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)]">
            <form @submit.prevent="handleSave" class="space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">食谱名称 *</label>
                  <input
                    v-model="formData.title"
                    type="text"
                    required
                    placeholder="请输入食谱名称"
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                    style="border-color: var(--border);"
                    @focus="focusField('title')"
                    @blur="blurField('title')"
                    :style="{ borderColor: focusedField === 'title' ? 'var(--brand-primary)' : 'var(--border)' }"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">副标题</label>
                  <input
                    v-model="formData.subtitle"
                    type="text"
                    placeholder="请输入副标题"
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                    style="border-color: var(--border);"
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">分类 *</label>
                  <select
                    v-model="formData.category"
                    required
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground cursor-pointer"
                    style="border-color: var(--border);"
                  >
                    <option value="">请选择分类</option>
                    <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">难度 *</label>
                  <select
                    v-model="formData.difficulty"
                    required
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground cursor-pointer"
                    style="border-color: var(--border);"
                  >
                    <option value="">请选择难度</option>
                    <option value="简单">简单</option>
                    <option value="中等">中等</option>
                    <option value="困难">困难</option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">状态 *</label>
                  <select
                    v-model="formData.status"
                    required
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground cursor-pointer"
                    style="border-color: var(--border);"
                  >
                    <option value="上架">上架</option>
                    <option value="下架">下架</option>
                  </select>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">烹饪时间(分钟) *</label>
                  <input
                    v-model.number="formData.cookTimeMin"
                    type="number"
                    required
                    min="1"
                    placeholder="请输入烹饪时间"
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                    style="border-color: var(--border);"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">卡路里(kcal) *</label>
                  <input
                    v-model.number="formData.calories"
                    type="number"
                    required
                    min="1"
                    placeholder="请输入卡路里"
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                    style="border-color: var(--border);"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-muted-foreground mb-1.5">评分</label>
                  <input
                    v-model.number="formData.rating"
                    type="number"
                    min="0"
                    max="5"
                    step="0.5"
                    placeholder="0-5"
                    class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                    style="border-color: var(--border);"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">图片URL</label>
                <input
                  v-model="formData.imageUrl"
                  type="url"
                  placeholder="请输入图片URL"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">标签（逗号分隔）</label>
                <input
                  v-model="formData.tagsInput"
                  type="text"
                  placeholder="例如：家常菜,健康,快手"
                  class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                  style="border-color: var(--border);"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">食材</label>
                <div class="space-y-2">
                  <div v-for="(ingredient, idx) in formData.ingredients" :key="idx" class="flex items-center gap-2">
                    <span class="w-6 h-6 rounded-full flex items-center justify-center text-xs text-white" style="background: var(--brand-primary);">{{ idx + 1 }}</span>
                    <input
                      v-model="formData.ingredients[idx]"
                      type="text"
                      placeholder="请输入食材名称"
                      class="flex-1 px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground"
                      style="border-color: var(--border);"
                    />
                    <button type="button" @click="removeIngredient(idx)" class="p-1.5 rounded-lg hover:bg-state-error-light transition-colors">
                      <Trash2 class="w-4 h-4" style="color: var(--state-error);" />
                    </button>
                  </div>
                  <button type="button" @click="addIngredient" class="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
                    <Plus class="w-4 h-4" />
                    添加食材
                  </button>
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-muted-foreground mb-1.5">烹饪步骤</label>
                <div class="space-y-2">
                  <div v-for="(step, idx) in formData.steps" :key="idx" class="flex items-start gap-2">
                    <span class="w-6 h-6 rounded-full flex items-center justify-center text-xs text-white shrink-0 mt-1" style="background: var(--brand-primary);">{{ idx + 1 }}</span>
                    <textarea
                      v-model="formData.steps[idx]"
                      rows="2"
                      placeholder="请输入步骤描述"
                      class="flex-1 px-3 py-2 text-sm border rounded-lg outline-none transition-colors bg-background text-foreground placeholder:text-muted-foreground resize-none"
                      style="border-color: var(--border);"
                    ></textarea>
                    <button type="button" @click="removeStep(idx)" class="p-1.5 rounded-lg hover:bg-state-error-light transition-colors shrink-0 mt-1">
                      <Trash2 class="w-4 h-4" style="color: var(--state-error);" />
                    </button>
                  </div>
                  <button type="button" @click="addStep" class="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
                    <Plus class="w-4 h-4" />
                    添加步骤
                  </button>
                </div>
              </div>

              <div class="flex items-center justify-end gap-3 pt-4">
                <button type="button" @click="closeModal" class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted rounded-lg transition-colors">
                  取消
                </button>
                <button type="submit" :disabled="saving" class="px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50" style="background: var(--brand-primary);">
                  <span v-if="saving">保存中...</span>
                  <span v-else>{{ isEditing ? '保存修改' : '新增食谱' }}</span>
                </button>
              </div>
            </form>
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
            <p class="text-sm text-muted-foreground">确定要删除食谱「{{ deletingRecipe?.name }}」吗？此操作无法撤销。</p>
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

    <!-- 评价（评论 + 评分）弹窗 -->
    <Teleport to="body">
      <div v-if="showCommentsModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="closeComments"></div>
        <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-lg max-h-[85vh] overflow-hidden flex flex-col">
          <div class="flex items-center justify-between px-6 py-4 border-b" style="border-color: var(--border);">
            <div>
              <h2 class="text-lg font-semibold text-foreground">用户评价</h2>
              <p class="text-xs text-muted-foreground mt-0.5">{{ commentsRecipe?.title || commentsRecipe?.name }}</p>
            </div>
            <button @click="closeComments" class="p-1.5 rounded-lg hover:bg-muted transition-colors">
              <X class="w-5 h-5 text-muted-foreground" />
            </button>
          </div>

          <div class="px-6 py-4 border-b" style="border-color: var(--border);">
            <div class="flex items-center gap-3">
              <span class="text-3xl font-bold text-foreground">{{ avgRating.toFixed(1) }}</span>
              <div>
                <div class="flex items-center gap-0.5">
                  <Star v-for="i in 5" :key="i" class="w-4 h-4"
                    :style="{ color: i <= Math.round(avgRating) ? '#F59E0B' : 'var(--border)' }"
                    :class="i <= Math.round(avgRating) ? 'fill-current' : ''" />
                </div>
                <p class="text-xs text-muted-foreground mt-1">{{ ratings.length }} 人评分 · {{ comments.length }} 条评论</p>
              </div>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto px-6 py-3">
            <div v-if="commentsLoading" class="py-10 text-center text-sm text-muted-foreground">加载中...</div>
            <div v-else-if="comments.length === 0" class="py-10 text-center text-sm text-muted-foreground">暂无评价</div>
            <div v-else class="divide-y" style="border-color: var(--border);">
              <div v-for="c in comments" :key="c.id" class="py-3 flex gap-3">
                <div class="w-9 h-9 rounded-full flex items-center justify-center shrink-0 text-sm font-semibold text-white" style="background: var(--brand-primary);">
                  {{ (c.userName || '用').slice(0, 1) }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between">
                    <span class="text-sm font-medium text-foreground">{{ c.userName || '匿名用户' }}</span>
                    <button @click="removeComment(c.id)" class="text-xs text-muted-foreground hover:text-foreground">删除</button>
                  </div>
                  <p class="text-xs text-muted-foreground">{{ (c.createdAt || '').replace('T', ' ').slice(0, 16) }}</p>
                  <p class="text-sm text-foreground mt-1">{{ c.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  getRecipeList,
  getCategories,
  createRecipe,
  updateRecipe,
  deleteRecipe,
  toggleRecipeHero
} from '@/api/recipe'
import {
  Search,
  Plus,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  Clock,
  Flame,
  Heart,
  Star,
  Pencil,
  Trash2,
  Utensils,
  Salad,
  Soup,
  CakeSlice,
  Beef,
  Coffee,
  X,
  AlertCircle,
  MessageSquare
} from 'lucide-vue-next'
import { getComments, deleteComment, getRatings } from '@/api/comment'

const hoverAddBtn = ref(false)
const searchFocus = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedDifficulties = ref([])
const currentPage = ref(1)
const pageSize = 5
const loading = ref(false)
const error = ref('')

const showModal = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const focusedField = ref('')

const showDeleteModal = ref(false)
const deletingRecipe = ref(null)
const deleting = ref(false)

// 评价弹窗
const showCommentsModal = ref(false)
const commentsRecipe = ref(null)
const comments = ref([])
const ratings = ref([])
const commentsLoading = ref(false)
const avgRating = computed(() => {
  if (!ratings.value.length) {
    const r = commentsRecipe.value?.rating
    return typeof r === 'number' ? r : 0
  }
  return ratings.value.reduce((s, x) => s + (x.rating || 0), 0) / ratings.value.length
})

const difficulties = [
  { label: '简单', value: '简单', color: '#16A34A' },
  { label: '中等', value: '中等', color: '#CA8A04' },
  { label: '困难', value: '困难', color: '#DC2626' }
]

const categoryStyleMap = {
  '中式': { icon: Beef, bgColor: '#FED7AA', iconColor: '#EA580C' },
  '西式': { icon: Salad, bgColor: '#DCFCE7', iconColor: '#16A34A' },
  '日式': { icon: Utensils, bgColor: '#DBEAFE', iconColor: '#2563EB' },
  '甜点': { icon: CakeSlice, bgColor: '#FCE7F3', iconColor: '#DB2777' },
  '汤品': { icon: Soup, bgColor: '#FEF9C3', iconColor: '#CA8A04' },
  '家常菜': { icon: Coffee, bgColor: '#FFEDD5', iconColor: '#EA580C' },
  '家常热菜': { icon: Beef, bgColor: '#FED7AA', iconColor: '#EA580C' },
  '汤煲': { icon: Soup, bgColor: '#FEF9C3', iconColor: '#CA8A04' },
  '川菜': { icon: Beef, bgColor: '#FEF2F2', iconColor: '#DC2626' },
  '海鲜': { icon: Utensils, bgColor: '#DBEAFE', iconColor: '#2563EB' },
  '主食': { icon: Coffee, bgColor: '#FED7AA', iconColor: '#EA580C' },
  '凉菜': { icon: Salad, bgColor: '#DCFCE7', iconColor: '#16A34A' }
}
const defaultStyle = { icon: Coffee, bgColor: '#FFEDD5', iconColor: '#EA580C' }

function getStyleByCategory(category) {
  return categoryStyleMap[category] || defaultStyle
}

const mockRecipes = [
  {
    id: 1,
    name: '红烧狮子头',
    category: '中式',
    difficulty: '中等',
    cookTime: '45分钟',
    calories: '380kcal',
    favorites: 1284,
    status: '上架',
    icon: Beef,
    bgColor: '#FED7AA',
    iconColor: '#EA580C',
    recommended: true
  },
  {
    id: 2,
    name: '凯撒沙拉',
    category: '西式',
    difficulty: '简单',
    cookTime: '15分钟',
    calories: '220kcal',
    favorites: 956,
    status: '上架',
    icon: Salad,
    bgColor: '#DCFCE7',
    iconColor: '#16A34A',
    recommended: false
  },
  {
    id: 3,
    name: '味噌拉面',
    category: '日式',
    difficulty: '中等',
    cookTime: '60分钟',
    calories: '520kcal',
    favorites: 2103,
    status: '上架',
    icon: Utensils,
    bgColor: '#DBEAFE',
    iconColor: '#2563EB',
    recommended: true
  },
  {
    id: 4,
    name: '提拉米苏',
    category: '甜点',
    difficulty: '困难',
    cookTime: '120分钟',
    calories: '450kcal',
    favorites: 3872,
    status: '上架',
    icon: CakeSlice,
    bgColor: '#FCE7F3',
    iconColor: '#DB2777',
    recommended: true
  },
  {
    id: 5,
    name: '番茄蛋花汤',
    category: '汤品',
    difficulty: '简单',
    cookTime: '10分钟',
    calories: '85kcal',
    favorites: 4215,
    status: '下架',
    icon: Soup,
    bgColor: '#FEF9C3',
    iconColor: '#CA8A04',
    recommended: false
  },
  {
    id: 6,
    name: '宫保鸡丁',
    category: '中式',
    difficulty: '中等',
    cookTime: '30分钟',
    calories: '310kcal',
    favorites: 2767,
    status: '上架',
    icon: Coffee,
    bgColor: '#FFEDD5',
    iconColor: '#EA580C',
    recommended: false
  }
]
const mockCategories = ['中式', '西式', '日式', '甜点', '汤品', '家常菜', '家常热菜', '汤煲', '川菜', '海鲜', '主食', '凉菜']

const categories = ref([...mockCategories])
const recipes = ref([...mockRecipes])

const formData = ref({
  id: null,
  title: '',
  subtitle: '',
  category: '',
  difficulty: '',
  status: '上架',
  cookTimeMin: null,
  calories: null,
  rating: null,
  imageUrl: '',
  tags: [],
  tagsInput: '',
  ingredients: [],
  steps: [],
  isHero: false,
  matchPercent: null
})

function mapRecipe(item) {
  const style = getStyleByCategory(item.category)
  return {
    id: item.id,
    name: item.title || '',
    category: item.category || '',
    difficulty: item.difficulty || '中等',
    cookTime: item.cookTimeMin != null ? `${item.cookTimeMin}分钟` : '--',
    calories: item.calories != null ? `${item.calories}kcal` : '--',
    favorites: item.favorites != null ? item.favorites : 0,
    status: item.status || '上架',
    icon: style.icon,
    bgColor: style.bgColor,
    iconColor: style.iconColor,
    recommended: !!item.isHero
  }
}

async function fetchRecipes() {
  loading.value = true
  error.value = ''
  try {
    const params = {}
    if (selectedCategory.value) params.category = selectedCategory.value
    if (searchQuery.value) params.keyword = searchQuery.value
    const data = await getRecipeList(params)
    if (Array.isArray(data)) {
      recipes.value = data.map(mapRecipe)
    } else {
      recipes.value = [...mockRecipes]
    }
  } catch (e) {
    console.error('[fetchRecipes error]', e)
    error.value = e?.message || '获取食谱列表失败，已使用本地数据兜底'
    recipes.value = [...mockRecipes]
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const data = await getCategories()
    if (Array.isArray(data) && data.length > 0) {
      categories.value = data
    }
  } catch (e) {
    console.error('[fetchCategories error]', e)
  }
}

let searchTimer = null
watch(searchQuery, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchRecipes()
  }, 300)
})

watch(selectedCategory, () => {
  currentPage.value = 1
  fetchRecipes()
})

onMounted(() => {
  fetchCategories()
  fetchRecipes()
})

const filteredRecipes = computed(() => {
  return recipes.value.filter(recipe => {
    const matchSearch = recipe.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchCategory = selectedCategory.value === '' || recipe.category === selectedCategory.value
    const matchDifficulty = selectedDifficulties.value.length === 0 || selectedDifficulties.value.includes(recipe.difficulty)
    return matchSearch && matchCategory && matchDifficulty
  })
})

const totalRecipes = computed(() => filteredRecipes.value.length)
const totalPages = computed(() => Math.ceil(totalRecipes.value / pageSize) || 1)

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

const paginatedRecipes = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredRecipes.value.slice(start, end)
})

function getDifficultyBadge(difficulty) {
  const map = {
    '简单': { bg: 'var(--state-success-light)', text: 'var(--state-success)' },
    '中等': { bg: 'var(--state-warning-light)', text: 'var(--state-warning)' },
    '困难': { bg: 'var(--state-error-light)', text: 'var(--state-error)' }
  }
  return map[difficulty] || map['中等']
}

function toggleDifficulty(value) {
  const index = selectedDifficulties.value.indexOf(value)
  if (index > -1) {
    selectedDifficulties.value.splice(index, 1)
  } else {
    selectedDifficulties.value.push(value)
  }
  currentPage.value = 1
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

function goToPage(page) {
  if (page !== '...' && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function focusField(field) {
  focusedField.value = field
}

function blurField(field) {
  focusedField.value = ''
}

function resetForm() {
  formData.value = {
    id: null,
    title: '',
    subtitle: '',
    category: '',
    difficulty: '',
    status: '上架',
    cookTimeMin: null,
    calories: null,
    rating: null,
    imageUrl: '',
    tags: [],
    tagsInput: '',
    ingredients: [],
    steps: [],
    isHero: false,
    matchPercent: null
  }
}

function openModal(mode, data = null) {
  isEditing.value = mode === 'edit'
  if (isEditing.value && data) {
    formData.value = {
      id: data.id,
      title: data.name,
      subtitle: '',
      category: data.category,
      difficulty: data.difficulty,
      status: data.status,
      cookTimeMin: parseInt(data.cookTime) || null,
      calories: parseInt(data.calories) || null,
      rating: null,
      imageUrl: '',
      tags: [],
      tagsInput: '',
      ingredients: [],
      steps: [],
      isHero: data.recommended,
      matchPercent: null
    }
  } else {
    resetForm()
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  resetForm()
}

function addIngredient() {
  formData.value.ingredients.push('')
}

function removeIngredient(index) {
  formData.value.ingredients.splice(index, 1)
}

function addStep() {
  formData.value.steps.push('')
}

function removeStep(index) {
  formData.value.steps.splice(index, 1)
}

async function handleSave() {
  saving.value = true
  try {
    const submitData = { ...formData.value }
    submitData.tags = formData.value.tagsInput.split(',').map(t => t.trim()).filter(Boolean)
    submitData.ingredientsJson = JSON.stringify(formData.value.ingredients)
    submitData.stepsJson = JSON.stringify(formData.value.steps)
    delete submitData.tagsInput
    delete submitData.ingredients
    delete submitData.steps

    if (isEditing.value) {
      await updateRecipe(formData.value.id, submitData)
    } else {
      await createRecipe(submitData)
    }

    closeModal()
    await fetchRecipes()
  } catch (e) {
    console.error('[handleSave error]', e)
    alert('保存失败：' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function toggleRecommend(recipe) {
  try {
    await toggleRecipeHero(recipe.id)
    recipe.recommended = !recipe.recommended
  } catch (e) {
    console.error('[toggleRecommend error]', e)
    alert('操作失败：' + (e?.message || '未知错误'))
  }
}

// ---------- 评价弹窗 ----------

async function openComments(recipe) {
  commentsRecipe.value = recipe
  showCommentsModal.value = true
  comments.value = []
  ratings.value = []
  commentsLoading.value = true
  try {
    const [c, r] = await Promise.all([
      getComments(recipe.id).catch(() => []),
      getRatings(recipe.id).catch(() => [])
    ])
    comments.value = Array.isArray(c) ? c : (c?.data || [])
    ratings.value = Array.isArray(r) ? r : (r?.data || [])
  } catch (e) {
    console.error('[openComments error]', e)
  } finally {
    commentsLoading.value = false
  }
}

function closeComments() {
  showCommentsModal.value = false
  commentsRecipe.value = null
}

async function removeComment(id) {
  if (!confirm('确定删除这条评论吗？')) return
  try {
    await deleteComment(id)
    comments.value = comments.value.filter(c => c.id !== id)
  } catch (e) {
    console.error('[removeComment error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  }
}

function handleAddRecipe() {
  openModal('add')
}

function handleEdit(recipe) {
  openModal('edit', recipe)
}

function confirmDelete(recipe) {
  deletingRecipe.value = recipe
  showDeleteModal.value = true
}

async function handleDelete() {
  deleting.value = true
  try {
    await deleteRecipe(deletingRecipe.value.id)
    showDeleteModal.value = false
    await fetchRecipes()
    if (currentPage.value > totalPages.value && currentPage.value > 1) {
      currentPage.value = totalPages.value
    }
  } catch (e) {
    console.error('[handleDelete error]', e)
    alert('删除失败：' + (e?.message || '未知错误'))
  } finally {
    deleting.value = false
    deletingRecipe.value = null
  }
}
</script>