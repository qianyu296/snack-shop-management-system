import request from '@/utils/request'
import type { LoginForm, RegisterForm } from '@/types/user'

/** 登录 */
export function loginApi(data: LoginForm) {
  return request.post('/auth/login', data)
}

/** 注册申请 */
export function registerApi(data: RegisterForm) {
  return request.post('/auth/register', data)
}

/** 获取当前用户信息 */
export function getUserInfoApi() {
  return request.get('/auth/user-info')
}

/** 退出登录 */
export function logoutApi() {
  return request.post('/auth/logout')
}
