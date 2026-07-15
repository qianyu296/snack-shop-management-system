<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapsed" class="logo-text">SnackAdmin</span>
        <span v-else class="logo-text-mini">SA</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="item in menuItems" :key="item.path">
          <!-- 无子菜单 -->
          <el-menu-item v-if="!item.children" :index="item.path">
            <el-icon><component :is="item.meta.icon" /></el-icon>
            <template #title>{{ item.meta.title }}</template>
          </el-menu-item>
          <!-- 有子菜单 -->
          <el-sub-menu v-else :index="item.path">
            <template #title>
              <el-icon><component :is="item.meta.icon" /></el-icon>
              <span>{{ item.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.path"
              :index="child.path"
            >
              {{ child.meta.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="navbar">
        <div class="navbar-left">
          <el-icon class="collapse-btn" @click="toggleSidebar" :size="20">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="navbar-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ authStore.realName || authStore.username }}</span>
              <el-tag v-if="authStore.isAdmin" size="small" type="danger" class="role-tag">管理员</el-tag>
              <el-tag v-else size="small" type="primary" class="role-tag">店长</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { UserFilled, Fold, Expand } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)
const currentTitle = computed(() => route.meta.title as string | undefined)
const activeMenu = computed(() => route.path)

// 从路由自动生成菜单
const menuItems = computed(() => {
  const routes = router
    .getRoutes()
    .find((r) => r.path === '/')
  if (!routes?.children) return []

  return routes.children
    .filter((r) => !r.meta.hidden)
    .filter((r) => {
      // 角色过滤
      if (r.meta.roles) {
        const requiredRoles = r.meta.roles as string[]
        return requiredRoles.some((role) => authStore.roles.includes(role))
      }
      return true
    })
})

function toggleSidebar() {
  appStore.toggleSidebar()
}

async function handleLogout() {
  try {
    await import('@/api/auth').then((m) => m.logoutApi())
  } catch {
    // ignore
  }
  authStore.logout()
}

// 引用 router 用于菜单生成
import router from '@/router'
</script>

<style scoped lang="scss">
.main-layout {
  height: 100%;

  .sidebar {
    background-color: #304156;
    overflow-y: auto;
    overflow-x: hidden;
    transition: width 0.3s;

    .logo {
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #263445;
      .logo-text {
        color: #fff;
        font-size: 18px;
        font-weight: 700;
        letter-spacing: 1px;
      }
      .logo-text-mini {
        color: #fff;
        font-size: 16px;
        font-weight: 700;
      }
    }

    .el-menu {
      border-right: none;
    }
  }

  .main-container {
    flex-direction: column;
    overflow: hidden;

    .navbar {
      height: 56px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: #fff;
      border-bottom: 1px solid #e6e6e6;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
      padding: 0 20px;

      &-left {
        display: flex;
        align-items: center;
        gap: 12px;
        .collapse-btn {
          cursor: pointer;
          &:hover { color: var(--primary-color); }
        }
      }

      &-right {
        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          .username { font-size: 14px; }
          .role-tag { margin-left: 4px; }
        }
      }
    }

    .main-content {
      flex: 1;
      overflow-y: auto;
      padding: 16px 20px;
      background-color: #f0f2f5;
    }
  }
}
</style>
