<template>
  <div class="min-h-screen flex items-center justify-center p-4"
       style="background: linear-gradient(160deg, var(--brand-primary-lightest) 0%, var(--ak-background) 40%, var(--brand-primary-lightest) 100%);">
    <main class="w-full max-w-[400px]">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 mb-4"
             style="background: var(--brand-primary-lighter); border-radius: var(--radius-full);">
          <Flame class="w-8 h-8" style="color: var(--ak-primary);" />
        </div>
        <h1 class="text-2xl font-semibold mb-1 text-foreground">AI厨房料理助手</h1>
        <p class="text-base text-muted-foreground">管理端平台</p>
      </div>

      <div class="p-6 sm:p-8 card shadow-md">
        <form @submit.prevent="handleLogin">
          <div class="mb-4">
            <label for="account" class="block text-sm font-medium mb-2 text-foreground">账号</label>
            <input id="account" v-model="form.account" type="text" placeholder="请输入账号" 
                   autocomplete="username" class="input-field">
          </div>

          <div class="mb-4">
            <label for="password" class="block text-sm font-medium mb-2 text-foreground">密码</label>
            <div class="relative">
              <input id="password" v-model="form.password" :type="showPassword ? 'text' : 'password'" 
                     placeholder="请输入密码" autocomplete="current-password" 
                     class="input-field pr-10">
              <button type="button" @click="showPassword = !showPassword"
                      class="absolute right-3 top-1/2 -translate-y-1/2 inline-flex items-center justify-center p-1 transition-colors duration-200 cursor-pointer text-muted-foreground bg-none border-none">
                <Eye v-if="!showPassword" class="w-4 h-4" />
                <EyeOff v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="flex items-center gap-2 mb-6">
            <input id="remember" v-model="form.remember" type="checkbox" 
                   class="w-4 h-4 rounded-sm cursor-pointer"
                   style="accent-color: var(--ak-primary);">
            <label for="remember" class="text-sm text-muted-foreground cursor-pointer select-none">记住密码</label>
          </div>

          <button type="submit" :disabled="loading"
                  class="w-full btn-primary disabled:opacity-60 disabled:cursor-not-allowed">
            <Loader2 v-if="loading" class="w-4 h-4 mr-2 animate-spin" />
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
      </div>

      <p class="text-center text-xs mt-6 text-muted-foreground">AI Kitchen Assistant Admin Platform</p>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Flame, Eye, EyeOff, Loader2 } from 'lucide-vue-next'
import { login } from '@/api/user'

const router = useRouter()

const form = reactive({
  account: '',
  password: '',
  remember: true
})

const showPassword = ref(false)
const loading = ref(false)
const errorMsg = ref('')

// 管理员白名单：仅这些手机号可登录管理端
const ADMIN_PHONES = ['13800138000']

async function handleLogin() {
  if (!form.account || !form.password) {
    errorMsg.value = '请输入账号和密码'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    // 调用后端登录接口（后端使用 phone + password）
    const data = await login({
      phone: form.account,
      password: form.password,
    })

    // 后端返回 { token, user }
    const token = data?.token || ''
    const user = data?.user || {}

    // 管理员白名单校验，普通 App 账号不允许进入管理端
    if (!ADMIN_PHONES.includes(String(user.phone || ''))) {
      errorMsg.value = '该账号无管理员权限'
      return
    }

    localStorage.setItem('admin_token', token)
    localStorage.setItem('admin_logged_in', 'true')
    localStorage.setItem('admin_user', JSON.stringify(user))

    if (form.remember) {
      localStorage.setItem('admin_remember_account', form.account)
    } else {
      localStorage.removeItem('admin_remember_account')
    }

    router.push('/dashboard')
  } catch (err) {
    errorMsg.value = err.message || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}
</script>
