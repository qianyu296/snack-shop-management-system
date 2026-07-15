import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginForm, UserInfo } from '@/types/user'
import { loginApi, getUserInfoApi } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)

    const isLoggedIn = computed(() => !!token.value)
    const username = computed(() => userInfo.value?.username || '')
    const realName = computed(() => userInfo.value?.realName || '')
    const roles = computed(() => userInfo.value?.roles || [])
    const isAdmin = computed(() => roles.value.includes('ADMIN'))
    const permissions = computed(() => userInfo.value?.permissions || [])

    async function login(form: LoginForm) {
      const res = await loginApi(form)
      token.value = res.data.token
      await fetchUserInfo()
    }

    async function fetchUserInfo() {
      const res = await getUserInfoApi()
      userInfo.value = res.data
    }

    function logout() {
      token.value = ''
      userInfo.value = null
      router.push('/login')
    }

    function hasPermission(perm: string): boolean {
      return permissions.value.includes(perm)
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      username,
      realName,
      roles,
      isAdmin,
      permissions,
      login,
      fetchUserInfo,
      logout,
      hasPermission,
    }
  },
  {
    persist: {
      key: 'snack-admin-auth',
      pick: ['token'],
    },
  }
)
