import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/register.vue'),
    meta: { title: '注册申请', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      // 1. 首页看板
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页看板', icon: 'DataBoard' },
      },
      // 2. 菜品管理（分类 + 菜品）
      {
        path: 'dish',
        name: 'DishGroup',
        redirect: '/dish/list',
        meta: { title: '菜品管理', icon: 'Food' },
        children: [
          {
            path: 'category',
            name: 'DishCategory',
            component: () => import('@/views/dish/category.vue'),
            meta: { title: '菜品分类' },
          },
          {
            path: 'list',
            name: 'DishList',
            component: () => import('@/views/dish/list.vue'),
            meta: { title: '菜品管理' },
          },
        ],
      },
      // 3. 订单管理（列表 + 创建 + 详情）
      {
        path: 'order',
        name: 'OrderGroup',
        redirect: '/order/list',
        meta: { title: '订单管理', icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'OrderList',
            component: () => import('@/views/order/list.vue'),
            meta: { title: '订单列表' },
          },
          {
            path: 'create',
            name: 'OrderCreate',
            component: () => import('@/views/order/create.vue'),
            meta: { title: '创建订单' },
          },
          {
            path: ':id',
            name: 'OrderDetail',
            component: () => import('@/views/order/detail.vue'),
            meta: { title: '订单详情', hidden: true },
          },
        ],
      },
      // 4. 库存管理（原料 + 流水）
      {
        path: 'inventory',
        name: 'InventoryGroup',
        redirect: '/inventory/material',
        meta: { title: '库存管理', icon: 'Box' },
        children: [
          {
            path: 'material',
            name: 'MaterialList',
            component: () => import('@/views/inventory/material.vue'),
            meta: { title: '原料管理' },
          },
          {
            path: 'record',
            name: 'InventoryRecord',
            component: () => import('@/views/inventory/record.vue'),
            meta: { title: '库存流水' },
          },
        ],
      },
      // 5. 采购管理（供应商 + 采购单 + 详情）
      {
        path: 'purchase',
        name: 'PurchaseGroup',
        redirect: '/purchase/list',
        meta: { title: '采购管理', icon: 'ShoppingCart' },
        children: [
          {
            path: 'supplier',
            name: 'SupplierList',
            component: () => import('@/views/purchase/supplier.vue'),
            meta: { title: '供应商管理' },
          },
          {
            path: 'list',
            name: 'PurchaseList',
            component: () => import('@/views/purchase/list.vue'),
            meta: { title: '采购单管理' },
          },
          {
            path: ':id',
            name: 'PurchaseDetail',
            component: () => import('@/views/purchase/detail.vue'),
            meta: { title: '采购详情', hidden: true },
          },
        ],
      },
      // 6. 经营统计
      {
        path: 'statistics/business',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '经营统计', icon: 'TrendCharts' },
      },
      // 7. 系统管理（管理员专属）
      {
        path: 'system',
        name: 'SystemGroup',
        redirect: '/system/user',
        meta: { title: '系统管理', icon: 'Setting', roles: ['ADMIN'] },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/user.vue'),
            meta: { title: '用户管理' },
          },
          {
            path: 'audit',
            name: 'AuditManage',
            component: () => import('@/views/system/audit.vue'),
            meta: { title: '账号审核' },
          },
          {
            path: 'log',
            name: 'OperationLog',
            component: () => import('@/views/system/log.vue'),
            meta: { title: '操作日志' },
          },
        ],
      },
      // 个人中心（隐藏）
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', hidden: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '小吃店管理系统'} - SnackAdmin`
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (!requiresAuth && authStore.isLoggedIn && to.name === 'Login') {
    next({ name: 'Dashboard' })
    return
  }

  // 角色权限检查
  if (to.meta.roles) {
    const requiredRoles = to.meta.roles as string[]
    const hasRole = requiredRoles.some((role) => authStore.roles.includes(role))
    if (!hasRole) {
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router
