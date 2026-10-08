<template>
  <div class="max-w-4xl mx-auto">
    <div class="mb-6">
      <h1 class="text-2xl font-semibold text-foreground">系统设置</h1>
      <p class="text-sm text-muted-foreground mt-1">管理您的个人偏好和系统配置</p>
    </div>

    <div class="space-y-6">
      <div class="p-6 card">
        <div class="flex items-center gap-3 mb-5">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: var(--brand-primary-lightest);">
            <Settings class="w-5 h-5" style="color: var(--brand-primary);" />
          </div>
          <div>
            <h2 class="text-lg font-semibold text-foreground">基本设置</h2>
            <p class="text-sm text-muted-foreground">配置站点基本信息</p>
          </div>
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">站点名称</label>
            <input v-model="basicForm.siteName" type="text" class="input-field">
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">站点描述</label>
            <textarea v-model="basicForm.siteDescription" rows="3" class="input-field resize-none"></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">Logo 上传</label>
            <div class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors hover:border-orange-400"
                 style="border-color: var(--ak-border); background: var(--ak-muted);"
                 @click="triggerLogoUpload"
                 @dragover.prevent
                 @drop.prevent="handleLogoDrop">
              <input ref="logoInputRef" type="file" accept="image/*" class="hidden" @change="handleLogoChange">
              <div v-if="!logoPreview" class="flex flex-col items-center">
                <Upload class="w-10 h-10 mb-3 text-muted-foreground" />
                <p class="text-sm font-medium text-foreground">点击或拖拽上传 Logo</p>
                <p class="text-xs text-muted-foreground mt-1">支持 JPG、PNG、SVG 格式，建议尺寸 200x200</p>
              </div>
              <div v-else class="flex flex-col items-center">
                <img :src="logoPreview" alt="Logo Preview" class="w-20 h-20 object-contain mb-3 rounded-lg">
                <p class="text-sm font-medium text-foreground">点击更换 Logo</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="p-6 card">
        <div class="flex items-center gap-3 mb-5">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: var(--brand-primary-lightest);">
            <Bot class="w-5 h-5" style="color: var(--brand-primary);" />
          </div>
          <div>
            <h2 class="text-lg font-semibold text-foreground">AI 模型设置</h2>
            <p class="text-sm text-muted-foreground">配置 AI 模型参数</p>
          </div>
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">API Base URL</label>
            <input v-model="aiForm.apiBaseUrl" type="text" class="input-field">
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">API Key</label>
            <div class="relative">
              <input v-model="aiForm.apiKey" :type="showApiKey ? 'text' : 'password'" class="input-field pr-10">
              <button type="button" @click="showApiKey = !showApiKey"
                      class="absolute right-3 top-1/2 -translate-y-1/2 inline-flex items-center justify-center p-1 transition-colors duration-200 cursor-pointer text-muted-foreground bg-none border-none">
                <Eye v-if="!showApiKey" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">模型名称</label>
            <div class="flex gap-2">
              <select v-model="aiForm.modelName" class="input-field cursor-pointer flex-1">
                <option v-for="model in modelOptions" :key="model.value" :value="model.value">
                  {{ model.label }}
                </option>
              </select>
              <button type="button" @click="fetchOllamaModels" 
                      class="btn-secondary px-3 py-2 flex items-center gap-1"
                      :disabled="fetchingModels">
                <RefreshCw v-if="fetchingModels" class="w-4 h-4 animate-spin" />
                <RefreshCw v-else class="w-4 h-4" />
              </button>
            </div>
            <p v-if="ollamaError" class="text-xs text-red-500 mt-1">{{ ollamaError }}</p>
          </div>
          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="text-sm font-medium text-foreground">温度参数</label>
              <span class="text-sm font-medium" style="color: var(--brand-primary);">{{ aiForm.temperature }}</span>
            </div>
            <input v-model.number="aiForm.temperature" type="range" min="0" max="1" step="0.1"
                   class="w-full h-2 rounded-lg appearance-none cursor-pointer slider-orange">
            <div class="flex justify-between text-xs text-muted-foreground mt-1">
              <span>精确 (0)</span>
              <span>创意 (1)</span>
            </div>
          </div>
        </div>
      </div>

      <div class="p-6 card">
        <div class="flex items-center gap-3 mb-5">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: var(--brand-primary-lightest);">
            <Bell class="w-5 h-5" style="color: var(--brand-primary);" />
          </div>
          <div>
            <h2 class="text-lg font-semibold text-foreground">通知设置</h2>
            <p class="text-sm text-muted-foreground">管理通知偏好</p>
          </div>
        </div>
        <div class="space-y-4">
          <div class="flex items-center justify-between py-2">
            <div class="flex-1 pr-4">
              <div class="text-sm font-medium text-foreground">系统通知</div>
              <div class="text-xs text-muted-foreground mt-0.5">接收系统更新和重要公告</div>
            </div>
            <div class="switch-wrapper" @click="notificationForm.system = !notificationForm.system">
              <div class="switch-track" :class="{ 'switch-on': notificationForm.system }">
                <div class="switch-thumb"></div>
              </div>
            </div>
          </div>
          <div class="border-t" style="border-color: var(--ak-border);"></div>
          <div class="flex items-center justify-between py-2">
            <div class="flex-1 pr-4">
              <div class="text-sm font-medium text-foreground">邮件通知</div>
              <div class="text-xs text-muted-foreground mt-0.5">通过电子邮件接收重要提醒</div>
            </div>
            <div class="switch-wrapper" @click="notificationForm.email = !notificationForm.email">
              <div class="switch-track" :class="{ 'switch-on': notificationForm.email }">
                <div class="switch-thumb"></div>
              </div>
            </div>
          </div>
          <div class="border-t" style="border-color: var(--ak-border);"></div>
          <div class="flex items-center justify-between py-2">
            <div class="flex-1 pr-4">
              <div class="text-sm font-medium text-foreground">推送通知</div>
              <div class="text-xs text-muted-foreground mt-0.5">接收浏览器桌面推送通知</div>
            </div>
            <div class="switch-wrapper" @click="notificationForm.push = !notificationForm.push">
              <div class="switch-track" :class="{ 'switch-on': notificationForm.push }">
                <div class="switch-thumb"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="p-6 card">
        <div class="flex items-center gap-3 mb-5">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" style="background: var(--brand-primary-lightest);">
            <Shield class="w-5 h-5" style="color: var(--brand-primary);" />
          </div>
          <div>
            <h2 class="text-lg font-semibold text-foreground">安全设置</h2>
            <p class="text-sm text-muted-foreground">保护您的账户安全</p>
          </div>
        </div>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">当前密码</label>
            <div class="relative">
              <input v-model="securityForm.currentPassword" :type="showCurrentPassword ? 'text' : 'password'" class="input-field pr-10">
              <button type="button" @click="showCurrentPassword = !showCurrentPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 inline-flex items-center justify-center p-1 transition-colors duration-200 cursor-pointer text-muted-foreground bg-none border-none">
                <Eye v-if="!showCurrentPassword" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">新密码</label>
            <div class="relative">
              <input v-model="securityForm.newPassword" :type="showNewPassword ? 'text' : 'password'" class="input-field pr-10">
              <button type="button" @click="showNewPassword = !showNewPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 inline-flex items-center justify-center p-1 transition-colors duration-200 cursor-pointer text-muted-foreground bg-none border-none">
                <Eye v-if="!showNewPassword" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">确认新密码</label>
            <div class="relative">
              <input v-model="securityForm.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'" class="input-field pr-10">
              <button type="button" @click="showConfirmPassword = !showConfirmPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 inline-flex items-center justify-center p-1 transition-colors duration-200 cursor-pointer text-muted-foreground bg-none border-none">
                <Eye v-if="!showConfirmPassword" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>
          <div class="border-t pt-4" style="border-color: var(--ak-border);">
            <div class="flex items-center justify-between py-2">
              <div class="flex-1 pr-4">
                <div class="text-sm font-medium text-foreground">两步验证</div>
                <div class="text-xs text-muted-foreground mt-0.5">使用身份验证器应用增强账户安全</div>
              </div>
              <div class="switch-wrapper" @click="securityForm.twoFactor = !securityForm.twoFactor">
                <div class="switch-track" :class="{ 'switch-on': securityForm.twoFactor }">
                  <div class="switch-thumb"></div>
                </div>
              </div>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium mb-2 text-foreground">会话超时时间</label>
            <select v-model="securityForm.sessionTimeout" class="input-field cursor-pointer">
              <option value="30">30 分钟</option>
              <option value="60">1 小时</option>
              <option value="120">2 小时</option>
              <option value="1440">24 小时</option>
            </select>
          </div>
        </div>
      </div>

      <div class="flex justify-end gap-3 pb-6">
        <button class="btn-secondary" @click="handleReset">重置</button>
        <button class="btn-primary" @click="handleSave" :disabled="saving">
          <Loader2 v-if="saving" class="w-4 h-4 mr-2 animate-spin" />
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Settings, Bot, Bell, Shield, Upload, Eye, EyeOff, Loader2, RefreshCw } from 'lucide-vue-next'
import { changePassword } from '@/api/user'

const logoInputRef = ref(null)
const logoPreview = ref(null)
const showApiKey = ref(false)
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const saving = ref(false)
const fetchingModels = ref(false)
const ollamaError = ref('')

const modelOptions = ref([
  { label: 'qwen2.5-coder:1.5b', value: 'qwen2.5-coder:1.5b' },
  { label: 'qwen3.7-plus', value: 'qwen3.7-plus' },
  { label: 'gpt-4o', value: 'gpt-4o' },
  { label: 'claude-3-opus', value: 'claude-3-opus' }
])

const basicForm = reactive({
  siteName: 'AI厨房料理助手',
  siteDescription: '智能食材识别与食谱推荐平台'
})

const aiForm = reactive({
  apiBaseUrl: 'http://localhost:11434/v1',
  apiKey: '',
  modelName: 'qwen2.5-coder:1.5b',
  temperature: 0.7
})

const notificationForm = reactive({
  system: true,
  email: true,
  push: false
})

const securityForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
  twoFactor: false,
  sessionTimeout: '60'
})

async function fetchOllamaModels() {
  fetchingModels.value = true
  ollamaError.value = ''
  try {
    const baseUrl = aiForm.apiBaseUrl.replace('/v1', '')
    const response = await fetch(`${baseUrl}/v1/models`)
    if (!response.ok) {
      throw new Error('无法获取模型列表')
    }
    const data = await response.json()
    if (data.data && Array.isArray(data.data)) {
      modelOptions.value = data.data.map(model => ({
        label: model.id,
        value: model.id
      }))
      if (!modelOptions.value.find(m => m.value === aiForm.modelName) && modelOptions.value.length > 0) {
        aiForm.modelName = modelOptions.value[0].value
      }
    }
  } catch (err) {
    ollamaError.value = '无法连接到 Ollama，请确保服务已启动'
    console.error('fetchOllamaModels error:', err)
  } finally {
    fetchingModels.value = false
  }
}

function triggerLogoUpload() {
  logoInputRef.value?.click()
}

function handleLogoChange(e) {
  const file = e.target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => {
      logoPreview.value = event.target?.result
    }
    reader.readAsDataURL(file)
  }
}

function handleLogoDrop(e) {
  const file = e.dataTransfer?.files?.[0]
  if (file && file.type.startsWith('image/')) {
    const reader = new FileReader()
    reader.onload = (event) => {
      logoPreview.value = event.target?.result
    }
    reader.readAsDataURL(file)
  }
}

async function handleSave() {
  // 如果填了密码字段，调用后端修改密码
  if (securityForm.currentPassword || securityForm.newPassword || securityForm.confirmPassword) {
    if (!securityForm.currentPassword) {
      alert('请输入当前密码')
      return
    }
    if (!securityForm.newPassword) {
      alert('请输入新密码')
      return
    }
    if (securityForm.newPassword.length < 6) {
      alert('新密码长度不能少于6位')
      return
    }
    if (securityForm.newPassword !== securityForm.confirmPassword) {
      alert('两次输入的新密码不一致')
      return
    }

    saving.value = true
    try {
      // 从 localStorage 获取当前登录的管理员信息
      const userStr = localStorage.getItem('admin_user') || '{}'
      const user = JSON.parse(userStr)
      const userId = user.id || 1

      await changePassword({
        userId,
        currentPassword: securityForm.currentPassword,
        newPassword: securityForm.newPassword
      })

      alert('密码修改成功！')
      securityForm.currentPassword = ''
      securityForm.newPassword = ''
      securityForm.confirmPassword = ''
    } catch (err) {
      console.error('[changePassword error]', err)
      const msg = err?.response?.data?.message || err?.message || '密码修改失败'
      alert(msg)
    } finally {
      saving.value = false
    }
    return
  }

  // 没有填密码字段，保存其他设置
  saving.value = true
  try {
    // 保存到 localStorage（基本设置、AI设置、通知设置）
    localStorage.setItem('admin_settings', JSON.stringify({
      basic: { ...basicForm },
      ai: { ...aiForm },
      notification: { ...notificationForm }
    }))
    setTimeout(() => {
      saving.value = false
      alert('设置保存成功！')
    }, 300)
  } catch (err) {
    saving.value = false
    alert('设置保存失败：' + (err?.message || '未知错误'))
  }
}

function handleReset() {
  if (confirm('确定要重置所有设置吗？')) {
    basicForm.siteName = 'AI厨房料理助手'
    basicForm.siteDescription = '智能食材识别与食谱推荐平台'
    logoPreview.value = null
    aiForm.apiBaseUrl = 'http://localhost:11434/v1'
    aiForm.apiKey = ''
    aiForm.modelName = 'qwen2.5-coder:1.5b'
    aiForm.temperature = 0.7
    notificationForm.system = true
    notificationForm.email = true
    notificationForm.push = false
    securityForm.currentPassword = ''
    securityForm.newPassword = ''
    securityForm.confirmPassword = ''
    securityForm.twoFactor = false
    securityForm.sessionTimeout = '60'
  }
}

// 加载已保存的设置
try {
  const saved = JSON.parse(localStorage.getItem('admin_settings') || '{}')
  if (saved.basic) {
    Object.assign(basicForm, saved.basic)
  }
  if (saved.ai) {
    Object.assign(aiForm, saved.ai)
  }
  if (saved.notification) {
    Object.assign(notificationForm, saved.notification)
  }
} catch (e) {
  console.error('加载保存的设置失败', e)
}
</script>

<style scoped>
.switch-wrapper {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.switch-track {
  position: relative;
  width: 44px;
  height: 24px;
  border-radius: 9999px;
  background-color: var(--ak-input);
  transition: background-color 0.2s ease;
}

.switch-track.switch-on {
  background-color: var(--brand-primary);
}

.switch-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 9999px;
  background-color: white;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease;
}

.switch-track.switch-on .switch-thumb {
  transform: translateX(20px);
}

.slider-orange {
  -webkit-appearance: none;
  appearance: none;
  background: var(--ak-input);
  outline: none;
}

.slider-orange::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--brand-primary);
  cursor: pointer;
  border: 2px solid white;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.2);
  transition: transform 0.15s ease;
}

.slider-orange::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

.slider-orange::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--brand-primary);
  cursor: pointer;
  border: 2px solid white;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.2);
}

select {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%23737373' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 0.75rem center;
  background-repeat: no-repeat;
  background-size: 1.25em 1.25em;
  padding-right: 2.5rem;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}
</style>
