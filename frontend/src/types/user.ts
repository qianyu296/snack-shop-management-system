/** 登录表单 */
export interface LoginForm {
  username: string
  password: string
}

/** 注册表单 */
export interface RegisterForm {
  username: string
  realName: string
  phone: string
  password: string
  confirmPassword: string
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  phone: string
  avatar: string
  roles: string[]
  permissions: string[]
}
