<template>
  <div class="h-full flex flex-col">
    <div class="mb-6">
      <h1 class="text-2xl font-semibold text-foreground">AI 厨房助手</h1>
      <p class="text-sm text-muted-foreground mt-1">智能食谱推荐、烹饪技巧问答、食材分析</p>
    </div>

    <div class="flex gap-4 mb-6">
      <button v-for="tab in tabs" :key="tab.key"
              @click="activeTab = tab.key"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200"
              :class="activeTab === tab.key ? 'bg-orange-500 text-white shadow-md' : 'bg-muted text-muted-foreground hover:bg-muted/80'">
        <component :is="tab.icon" class="w-4 h-4 inline-block mr-2" />
        {{ tab.label }}
      </button>
    </div>

    <div class="flex-1 bg-white rounded-xl border border-border overflow-hidden shadow-sm">
      <div v-if="activeTab === 'chat'" class="h-full flex flex-col">
        <div class="flex-1 p-6 overflow-y-auto space-y-4" ref="chatContainer">
          <div v-for="msg in chatMessages" :key="msg.id"
               class="flex gap-3" :class="{ 'flex-row-reverse': msg.role === 'user' }">
            <div class="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
                 :style="msg.role === 'user' ? 'background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-dark));' : 'background: #f3f4f6;'">
              <User v-if="msg.role === 'user'" class="w-4 h-4 text-white" />
              <Bot v-else class="w-4 h-4 text-gray-600" />
            </div>
            <div class="max-w-[70%]">
              <div class="px-4 py-3 rounded-2xl"
                   :class="msg.role === 'user' ? 'bg-orange-500 text-white rounded-tr-sm' : 'bg-muted text-foreground rounded-tl-sm'">
                <div v-if="msg.type === 'recipe'" class="space-y-3">
                  <div class="font-semibold text-lg">{{ msg.content.name }}</div>
                  <div class="text-sm opacity-80">{{ msg.content.description }}</div>
                  <div class="flex flex-wrap gap-2">
                    <span class="px-2 py-1 rounded text-xs" style="background: rgba(255,255,255,0.2);">{{ msg.content.prepTime }}</span>
                    <span class="px-2 py-1 rounded text-xs" style="background: rgba(255,255,255,0.2);">{{ msg.content.cookTime }}</span>
                    <span class="px-2 py-1 rounded text-xs" style="background: rgba(255,255,255,0.2);">{{ msg.content.servings }}</span>
                    <span class="px-2 py-1 rounded text-xs" :style="msg.content.difficulty === '简单' ? 'background: #10b981;' : msg.content.difficulty === '中等' ? 'background: #f59e0b;' : 'background: #ef4444;'">{{ msg.content.difficulty }}</span>
                  </div>
                  <div>
                    <div class="font-medium text-sm">食材：</div>
                    <ul class="list-disc list-inside text-sm opacity-80 mt-1">
                      <li v-for="(ing, idx) in msg.content.ingredients" :key="idx">{{ ing.name }} {{ ing.quantity }}</li>
                    </ul>
                  </div>
                  <div>
                    <div class="font-medium text-sm">步骤：</div>
                    <ol class="list-decimal list-inside text-sm opacity-80 mt-1 space-y-1">
                      <li v-for="(step, idx) in msg.content.steps" :key="idx">{{ step }}</li>
                    </ol>
                  </div>
                  <div v-if="msg.content.tips" class="pt-2 border-t" style="border-color: rgba(255,255,255,0.2);">
                    <div class="text-sm font-medium">小贴士：</div>
                    <div class="text-sm opacity-80">{{ msg.content.tips }}</div>
                  </div>
                </div>
                <div v-else-if="msg.type === 'analysis'" class="space-y-2">
                  <div class="font-medium">营养分析：</div>
                  <div class="text-sm opacity-80">{{ msg.content.nutrition }}</div>
                  <div class="font-medium">风味特点：</div>
                  <div class="text-sm opacity-80">{{ msg.content.flavor }}</div>
                  <div class="font-medium">推荐搭配：</div>
                  <div class="text-sm opacity-80">{{ msg.content.pairing }}</div>
                  <div class="font-medium">储存建议：</div>
                  <div class="text-sm opacity-80">{{ msg.content.storage }}</div>
                </div>
                <div v-else-if="msg.role === 'user'" class="whitespace-pre-wrap">{{ msg.content }}</div>
                <div v-else class="md-body" v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>
          </div>
          <div v-if="chatLoading" class="flex gap-3">
            <div class="w-8 h-8 rounded-full flex items-center justify-center bg-gray-100">
              <Bot class="w-4 h-4 text-gray-600" />
            </div>
            <div class="px-4 py-3 rounded-2xl bg-gray-100 rounded-tl-sm">
              <div class="flex gap-1">
                <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms;"></div>
                <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms;"></div>
                <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms;"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="p-4 border-t border-border">
          <div class="flex gap-3">
            <input v-model="chatInput" @keyup.enter="sendChat" type="text" 
                   placeholder="输入您的问题，例如：番茄炒蛋怎么做？"
                   class="flex-1 input-field">
            <button @click="sendChat" :disabled="chatLoading || !chatInput.trim()" class="btn-primary">
              <Send class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'generate'" class="p-6">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">可用食材</label>
            <textarea v-model="recipeForm.ingredients" rows="3" placeholder="请输入可用食材，用逗号分隔，例如：番茄,鸡蛋,洋葱"
                      class="input-field resize-none"></textarea>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium mb-2 text-foreground">烹饪风格</label>
              <select v-model="recipeForm.style" class="input-field">
                <option value="">不限</option>
                <option value="中式">中式</option>
                <option value="西式">西式</option>
                <option value="日式">日式</option>
                <option value="韩式">韩式</option>
                <option value="川菜">川菜</option>
                <option value="粤菜">粤菜</option>
                <option value="清淡">清淡</option>
                <option value="香辣">香辣</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-2 text-foreground">饮食限制</label>
              <select v-model="recipeForm.dietary" class="input-field">
                <option value="">不限</option>
                <option value="素食">素食</option>
                <option value="低脂">低脂</option>
                <option value="低糖">低糖</option>
                <option value="无麸质">无麸质</option>
              </select>
            </div>
          </div>
          <button @click="generateRecipe" :disabled="recipeLoading" class="btn-primary w-full">
            <Sparkles v-if="recipeLoading" class="w-4 h-4 mr-2 animate-spin" />
            {{ recipeLoading ? '生成中...' : '生成食谱' }}
          </button>

          <div v-if="generatedRecipe" class="mt-6 p-4 bg-muted/50 rounded-lg">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-lg font-semibold">{{ generatedRecipe.name }}</h3>
              <button @click="saveGeneratedRecipe" class="text-sm text-orange-500 hover:text-orange-600">保存到食谱库</button>
            </div>
            <p class="text-sm text-muted-foreground mb-3">{{ generatedRecipe.description }}</p>
            <div class="flex flex-wrap gap-2 mb-4">
              <span class="px-2 py-1 bg-muted rounded text-xs">{{ generatedRecipe.prepTime }}</span>
              <span class="px-2 py-1 bg-muted rounded text-xs">{{ generatedRecipe.cookTime }}</span>
              <span class="px-2 py-1 bg-muted rounded text-xs">{{ generatedRecipe.servings }}</span>
              <span class="px-2 py-1 rounded text-xs" :style="generatedRecipe.difficulty === '简单' ? 'background: #d1fae5; color: #065f46;' : generatedRecipe.difficulty === '中等' ? 'background: #fef3c7; color: #92400e;' : 'background: #fee2e2; color: #991b1b;'">{{ generatedRecipe.difficulty }}</span>
              <span class="px-2 py-1 bg-muted rounded text-xs">{{ generatedRecipe.calories }} 卡路里</span>
            </div>
            <div class="mb-4">
              <h4 class="text-sm font-medium mb-2">食材清单</h4>
              <ul class="space-y-1">
                <li v-for="(ing, idx) in generatedRecipe.ingredients" :key="idx" class="text-sm flex justify-between">
                  <span>{{ ing.name }}</span>
                  <span class="text-muted-foreground">{{ ing.quantity }}</span>
                </li>
              </ul>
            </div>
            <div class="mb-4">
              <h4 class="text-sm font-medium mb-2">烹饪步骤</h4>
              <ol class="space-y-2">
                <li v-for="(step, idx) in generatedRecipe.steps" :key="idx" class="text-sm flex gap-2">
                  <span class="font-medium w-6 flex-shrink-0">{{ idx + 1 }}.</span>
                  <span>{{ step }}</span>
                </li>
              </ol>
            </div>
            <div v-if="generatedRecipe.tips" class="p-3 bg-orange-50 rounded-lg">
              <h4 class="text-sm font-medium mb-1">烹饪小贴士</h4>
              <p class="text-sm">{{ generatedRecipe.tips }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'analyze'" class="p-6">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">食材名称</label>
            <textarea v-model="analyzeForm.ingredients" rows="3" placeholder="请输入要分析的食材，用逗号分隔"
                      class="input-field resize-none"></textarea>
          </div>
          <button @click="analyzeIngredients" :disabled="analyzeLoading" class="btn-primary w-full">
            <Search class="w-4 h-4 mr-2" />
            {{ analyzeLoading ? '分析中...' : '分析食材' }}
          </button>

          <div v-if="ingredientAnalysis" class="mt-6 space-y-4">
            <div class="p-4 bg-blue-50 rounded-lg">
              <div class="flex items-center gap-2 mb-2">
                <Heart class="w-4 h-4 text-blue-500" />
                <span class="font-medium">营养分析</span>
              </div>
              <p class="text-sm">{{ ingredientAnalysis.nutrition }}</p>
            </div>
            <div class="p-4 bg-green-50 rounded-lg">
              <div class="flex items-center gap-2 mb-2">
                <Flame class="w-4 h-4 text-green-500" />
                <span class="font-medium">风味特点</span>
              </div>
              <p class="text-sm">{{ ingredientAnalysis.flavor }}</p>
            </div>
            <div class="p-4 bg-purple-50 rounded-lg">
              <div class="flex items-center gap-2 mb-2">
                <Link class="w-4 h-4 text-purple-500" />
                <span class="font-medium">推荐搭配</span>
              </div>
              <p class="text-sm">{{ ingredientAnalysis.pairing }}</p>
            </div>
            <div class="p-4 bg-yellow-50 rounded-lg">
              <div class="flex items-center gap-2 mb-2">
                <Package class="w-4 h-4 text-yellow-500" />
                <span class="font-medium">储存建议</span>
              </div>
              <p class="text-sm">{{ ingredientAnalysis.storage }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'tips'" class="p-6">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">菜品名称</label>
            <input v-model="tipsForm.dishName" type="text" placeholder="请输入菜品名称，例如：红烧肉"
                   class="input-field">
          </div>
          <button @click="getCookingTips" :disabled="tipsLoading" class="btn-primary w-full">
            <Lightbulb class="w-4 h-4 mr-2" />
            {{ tipsLoading ? '获取中...' : '获取烹饪技巧' }}
          </button>

          <div v-if="cookingTipsContent" class="mt-6 p-4 bg-muted/50 rounded-lg">
            <div class="flex items-center gap-2 mb-3">
              <Lightbulb class="w-5 h-5 text-orange-500" />
              <h3 class="font-semibold">烹饪技巧</h3>
            </div>
            <div class="whitespace-pre-wrap text-sm">{{ cookingTipsContent }}</div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'knowledge'" class="p-6">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">向知识库提问</label>
            <textarea v-model="kbForm.question" rows="3" placeholder="例如：西红柿炒鸡蛋为什么出水？鸡蛋怎么保存？"
                      class="input-field resize-none"></textarea>
          </div>
          <button @click="askKnowledge" :disabled="kbLoading || !kbForm.question.trim()" class="btn-primary w-full">
            <BookOpen v-if="kbLoading" class="w-4 h-4 mr-2 animate-spin" />
            {{ kbLoading ? '查询中...' : '知识库问答' }}
          </button>

          <div v-if="kbAnswer" class="mt-6 p-4 bg-muted/50 rounded-lg">
            <div class="flex items-center gap-2 mb-3">
              <BookOpen class="w-5 h-5 text-orange-500" />
              <h3 class="font-semibold">知识库回答</h3>
              <span v-if="kbGrounded" class="text-xs px-2 py-0.5 rounded bg-green-100 text-green-700">已引用知识库</span>
              <span v-else class="text-xs px-2 py-0.5 rounded bg-gray-100 text-gray-500">常识回答</span>
            </div>
            <div class="whitespace-pre-wrap text-sm">{{ kbAnswer }}</div>
            <div v-if="kbSources && kbSources.length" class="mt-3 pt-3 border-t border-border">
              <div class="text-xs font-medium text-muted-foreground mb-1">引用来源：</div>
              <div class="flex flex-wrap gap-2">
                <span v-for="(s, idx) in kbSources" :key="idx"
                      class="text-xs px-2 py-1 rounded bg-orange-50 text-orange-600">
                  {{ s.category }} · {{ s.title }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { 
  MessageCircle, Sparkles, Search, Lightbulb, User, Bot, Send, 
  Heart, Flame, Link, Package, BookOpen
} from 'lucide-vue-next'
import { chat, generateRecipe as fetchRecipe, analyzeIngredients as analyzeIngredientsApi, cookingTips, askKnowledge as askKnowledgeApi } from '@/api/assistant'
import { createRecipe } from '@/api/recipe'
import { renderMarkdown } from '@/utils/markdown'

const tabs = [
  { key: 'chat', label: '智能问答', icon: MessageCircle },
  { key: 'generate', label: '食谱生成', icon: Sparkles },
  { key: 'analyze', label: '食材分析', icon: Search },
  { key: 'tips', label: '烹饪技巧', icon: Lightbulb },
  { key: 'knowledge', label: '知识库', icon: BookOpen },
]

const activeTab = ref('chat')
const chatContainer = ref(null)

const chatMessages = ref([
  { id: 1, role: 'assistant', type: 'text', content: '您好！我是您的AI厨房助手。请问有什么可以帮到您的？您可以问我关于烹饪技巧、食谱推荐、食材搭配等问题。' }
])
const chatInput = ref('')
const chatLoading = ref(false)

async function sendChat() {
  if (!chatInput.value.trim() || chatLoading.value) return
  
  const msg = {
    id: Date.now(),
    role: 'user',
    type: 'text',
    content: chatInput.value
  }
  chatMessages.value.push(msg)
  chatInput.value = ''
  chatLoading.value = true
  
  await nextTick()
  chatContainer.value?.scrollTo({ top: chatContainer.value.scrollHeight, behavior: 'smooth' })
  
  try {
    const data = await chat({ message: msg.content })
    let reply = data?.reply || ''
    
    if (msg.content.includes('生成食谱') || msg.content.includes('帮我写个食谱')) {
      const recipeData = await generateRecipe({ ingredients: msg.content })
      const recipe = recipeData?.recipe || {}
      chatMessages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        type: 'recipe',
        content: recipe
      })
    } else if (msg.content.includes('分析') || msg.content.includes('营养')) {
      const analysisData = await analyzeIngredients({ ingredients: msg.content })
      const analysis = analysisData?.analysis || {}
      chatMessages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        type: 'analysis',
        content: analysis
      })
    } else {
      chatMessages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        type: 'text',
        content: reply
      })
    }
  } catch (err) {
    chatMessages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      type: 'text',
      content: '抱歉，我暂时无法回答这个问题。请稍后再试。'
    })
  } finally {
    chatLoading.value = false
    await nextTick()
    chatContainer.value?.scrollTo({ top: chatContainer.value.scrollHeight, behavior: 'smooth' })
  }
}

const recipeForm = reactive({
  ingredients: '',
  style: '',
  dietary: ''
})
const recipeLoading = ref(false)
const generatedRecipe = ref(null)

async function generateRecipe() {
  if (!recipeForm.ingredients.trim()) {
    alert('请输入可用食材')
    return
  }
  recipeLoading.value = true
  try {
    const data = await fetchRecipe({
      ingredients: recipeForm.ingredients,
      style: recipeForm.style,
      dietary: recipeForm.dietary
    })
    generatedRecipe.value = data?.recipe || data?.rawContent
  } catch (err) {
    alert('食谱生成失败：' + (err?.message || '未知错误'))
  } finally {
    recipeLoading.value = false
  }
}

async function saveGeneratedRecipe() {
  if (!generatedRecipe.value) return
  try {
    const recipe = generatedRecipe.value
    const ingredients = Array.isArray(recipe.ingredients) 
      ? recipe.ingredients.map(i => `${i.name} ${i.quantity}`).join(', ')
      : ''
    const steps = Array.isArray(recipe.steps) ? recipe.steps.join('\n') : ''
    
    await createRecipe({
      title: recipe.name,
      description: recipe.description,
      category: recipeForm.style || 'AI生成',
      cookTime: recipe.cookTime || '30分钟',
      difficulty: recipe.difficulty || '中等',
      calories: parseInt(recipe.calories) || 300,
      ingredientsJson: JSON.stringify(recipe.ingredients || []),
      ingredients,
      stepsJson: JSON.stringify(recipe.steps || []),
      steps,
      imageUrl: '',
      isHero: false
    })
    alert('食谱保存成功！')
  } catch (err) {
    alert('保存失败：' + (err?.message || '未知错误'))
  }
}

const analyzeForm = reactive({
  ingredients: ''
})
const analyzeLoading = ref(false)
const ingredientAnalysis = ref(null)

async function analyzeIngredients() {
  if (!analyzeForm.ingredients.trim()) {
    alert('请输入要分析的食材')
    return
  }
  analyzeLoading.value = true
  try {
    const data = await analyzeIngredientsApi({ ingredients: analyzeForm.ingredients })
    ingredientAnalysis.value = data?.analysis || data?.rawContent
  } catch (err) {
    alert('分析失败：' + (err?.message || '未知错误'))
  } finally {
    analyzeLoading.value = false
  }
}

const tipsForm = reactive({
  dishName: ''
})
const tipsLoading = ref(false)
const cookingTipsContent = ref(null)

async function getCookingTips() {
  if (!tipsForm.dishName.trim()) {
    alert('请输入菜品名称')
    return
  }
  tipsLoading.value = true
  try {
    const data = await cookingTips({ dishName: tipsForm.dishName })
    cookingTipsContent.value = data?.tips
  } catch (err) {
    alert('获取失败：' + (err?.message || '未知错误'))
  } finally {
    tipsLoading.value = false
  }
}

const kbForm = reactive({
  question: ''
})
const kbLoading = ref(false)
const kbAnswer = ref('')
const kbGrounded = ref(false)
const kbSources = ref([])

async function askKnowledge() {
  if (!kbForm.question.trim()) {
    alert('请输入问题')
    return
  }
  kbLoading.value = true
  kbAnswer.value = ''
  kbSources.value = []
  try {
    const data = await askKnowledgeApi({ question: kbForm.question })
    kbAnswer.value = data?.answer || '暂无回答'
    kbGrounded.value = !!data?.grounded
    kbSources.value = data?.sources || []
  } catch (err) {
    alert('查询失败：' + (err?.message || '未知错误'))
  } finally {
    kbLoading.value = false
  }
}
</script>

<style scoped>
.animate-bounce {
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ---- AI 回复的 Markdown 排版（renderMarkdown 产出的 HTML） ----
   注意：这些标签是 v-html 动态插入的，不带 scoped 的 data-v 属性，
   因此必须用 :deep() 才能命中。 */
.md-body {
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
.md-body :deep(> *:first-child) { margin-top: 0; }
.md-body :deep(> *:last-child) { margin-bottom: 0; }

.md-body :deep(.md-h1),
.md-body :deep(.md-h2),
.md-body :deep(.md-h3),
.md-body :deep(.md-h4),
.md-body :deep(.md-h5),
.md-body :deep(.md-h6) {
  font-weight: 600;
  line-height: 1.4;
  margin: 14px 0 8px;
}
.md-body :deep(.md-h1) { font-size: 20px; }
.md-body :deep(.md-h2) { font-size: 18px; }
.md-body :deep(.md-h3) { font-size: 16px; }
.md-body :deep(.md-h4),
.md-body :deep(.md-h5),
.md-body :deep(.md-h6) { font-size: 14px; }

.md-body :deep(.md-p) { margin: 8px 0; }

.md-body :deep(.md-ul),
.md-body :deep(.md-ol) {
  margin: 8px 0;
  padding-left: 22px;
}
.md-body :deep(.md-ul) { list-style: disc; }
.md-body :deep(.md-ol) { list-style: decimal; }
.md-body :deep(.md-ul > li),
.md-body :deep(.md-ol > li) { margin: 4px 0; }
/* 嵌套列表：缩小间距 + 用空心圆点区分层级 */
.md-body :deep(.md-ul .md-ul) { list-style: circle; }
.md-body :deep(.md-ul .md-ul .md-ul) { list-style: square; }
.md-body :deep(.md-ul .md-ul),
.md-body :deep(.md-ol .md-ol),
.md-body :deep(.md-ul .md-ol),
.md-body :deep(.md-ol .md-ul) {
  margin: 4px 0;
  padding-left: 20px;
}

.md-body :deep(.md-code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.88em;
  padding: 1px 5px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.07);
}

.md-body :deep(.md-hr) {
  border: none;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin: 12px 0;
}

.md-body :deep(.md-quote) {
  margin: 8px 0;
  padding: 6px 12px;
  border-left: 3px solid var(--brand-primary, #f97316);
  background: rgba(249, 115, 22, 0.06);
  border-radius: 0 6px 6px 0;
  color: rgba(0, 0, 0, 0.65);
}
</style>
