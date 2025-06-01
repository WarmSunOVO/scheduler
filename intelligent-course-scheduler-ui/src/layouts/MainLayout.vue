<template>
  <el-container class="main-layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="layout-logo-container" @click="goToHome" title="返回首页">
        <img src="@/assets/logo.png" alt="Logo" class="layout-logo"/>
        <span v-if="!isCollapse" class="layout-system-title">智能排课系统</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-custom"
          router
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#2b333e"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <!-- 直接遍历 visibleMenuItems -->
        <template v-for="menuItem in visibleMenuItems" :key="menuItem.path">
          <el-sub-menu
              v-if="menuItem.children && menuItem.children.length > 0"
              :index="menuItem.path"
          >
            <template #title>
              <el-icon v-if="menuItem.meta?.icon"><component :is="menuItem.meta.icon" /></el-icon>
              <!-- 在折叠时，如果只有图标，标题可以不显示或特殊处理 -->
              <span v-if="!isCollapse || !menuItem.meta?.icon">{{ menuItem.meta?.title }}</span>
            </template>
            <el-menu-item
                v-for="childItem in menuItem.children"
                :key="childItem.name"
                :index="childItem.path"
                :route="{ name: childItem.name }"
            >
              <el-icon v-if="childItem.meta?.icon"><component :is="childItem.meta.icon" /></el-icon>
              <template #title><span>{{ childItem.meta?.title }}</span></template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item
              v-else
              :index="menuItem.path"
              :route="{ path: menuItem.path }"
          >
            <el-icon v-if="menuItem.meta?.icon"><component :is="menuItem.meta.icon" /></el-icon>
            <template #title><span>{{ menuItem.meta?.title }}</span></template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="content-wrapper">
      <el-header class="main-header-simplified">
        <div class="header-left">
          <el-button
              @click="toggleSidebar"
              :icon="isCollapse ? Expand : Fold"
              text
              circle
              title="折叠/展开侧边栏"
              class="sidebar-toggle-btn"
          />
        </div>
        <div class="header-right">
          <el-dropdown @command="handleUserCommand" trigger="click" placement="bottom-end">
            <span class="el-dropdown-link user-dropdown-trigger">
              <el-avatar :size="32" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username-text">{{ authStore.currentUsername }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="dashboard" :icon="HomeFilled">系统首页</el-dropdown-item>
                <el-dropdown-item command="profile" :icon="AvatarIcon">个人中心</el-dropdown-item>
                <el-dropdown-item command="changePassword" :icon="LockIcon">修改密码</el-dropdown-item>
                <el-dropdown-item command="settings" :icon="SettingHeaderIcon">系统设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided :icon="SwitchButtonIcon">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="page-title-container" v-if="currentPageTitle">
          <h1>{{ currentPageTitle }}</h1>
          <el-divider v-if="currentPageTitle" />
        </div>
        <router-view v-slot="{ Component, route }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, markRaw } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
// 导入所有在 router/index.js 中为 meta.icon 指定的图标组件
import {
  Setting, School, User as UserIconInMenu, UserFilled, HomeFilled, ArrowDown, Expand, Fold,
  Avatar as AvatarIcon, Lock as LockIconInHeader, SwitchButton as SwitchButtonIcon, Setting as SettingHeaderIcon,
  Collection as CollectionIcon, Calendar as CalendarIcon, Tickets as TicketsIcon, Lock as LockIcon, House as HouseIcon,
  View as ViewIcon // 确保 ViewIcon 也导入了，虽然不在菜单直接用，但 router/index.js 可能有
} from '@element-plus/icons-vue'; // LockIcon 可能重复，确保只导入一次或用不同别名
import { ElMessage } from 'element-plus';

defineOptions({
  name: 'MainLayout'
});

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const isCollapse = ref(false);
const activeMenu = computed(() => route.meta.activeMenu || route.path);
const currentPageTitle = computed(() => route.meta.title || '');

// 辅助函数：检查菜单项是否应该显示
const shouldShowMenuItem = (menuItem) => {
  if (!menuItem.meta || menuItem.meta.hiddenInMenu || menuItem.meta.hidden) { // 增加 hiddenInMenu 判断
    return false;
  }
  if (menuItem.meta.roles && Array.isArray(menuItem.meta.roles) && menuItem.meta.roles.length > 0) {
    return authStore.hasAnyRole(menuItem.meta.roles);
  }
  return true; // 如果没有定义 roles，则默认显示 (如果需要认证，路由守卫会处理)
};

// 递归处理子路由并构建菜单项
const processRoutesForMenu = (routesToProcess, basePath = '') => {
  const menu = [];
  for (const r of routesToProcess) {
    if (!shouldShowMenuItem(r)) {
      continue;
    }

    const currentPath = r.path.startsWith('/') ? r.path : (basePath ? `${basePath}/${r.path}` : `/${r.path}`);
    // 确保路径是唯一的，并且对于 router-link 的 index 是有效的
    // router/index.js 中子路由的 path 通常不带前导 '/'，除非是绝对路径

    const menuItem = {
      path: currentPath,
      name: r.name,
      meta: { // 直接传递 meta 对象，图标组件引用在 meta.icon 中
        ...r.meta,
        // 如果 meta.icon 是一个已导入的组件，就直接使用它。
        // Vue 模板中的 <component :is="xxx" /> 会正确处理组件对象。
        // 不需要在这里 markRaw，因为 router/index.js 中导入的组件已经是 markRaw 的效果了
        // 或者说，组件对象本身就不应该被 Vue 深度代理。
        icon: r.meta?.icon // 直接传递路由中定义的图标组件
      },
      children: []
    };

    if (r.children && r.children.length > 0 && !r.meta?.alwaysShowAsRoot) {
      menuItem.children = processRoutesForMenu(r.children, currentPath);
      // 如果子菜单过滤后为空，则不应显示为父菜单 (除非父菜单自身也作为链接)
      if (menuItem.children.length === 0 && !r.component) { // 如果没有子项且父级不是一个可点击的路由页面
        continue;
      }
    }
    menu.push(menuItem);
  }
  return menu;
};


const visibleMenuItems = computed(() => {
  console.log('Recalculating visibleMenuItems...');
  const mainLayoutRoute = router.options.routes.find(r => r.path === '/' && r.component?.name === 'MainLayout');

  if (!mainLayoutRoute || !mainLayoutRoute.children) {
    console.warn('MainLayout route or its children not found in router configuration.');
    return [];
  }
  // console.log('MainLayout children from router:', JSON.parse(JSON.stringify(mainLayoutRoute.children)));

  // 直接使用原始的 children 数组进行处理，不再手动分组 "基础数据管理"
  // 你的 router/index.js 中的结构已经是分组的了 (通过路径前缀 'management/' 和 'constraints/')
  // 如果需要严格按照 "基础数据管理", "核心业务", "约束管理" 这样的分组，
  // 那么 router/index.js 中就应该定义这样的父级路由，或者在这里进行更复杂的重组。
  // 当前简化为直接处理 MainLayout 的 children。

  const processedMenu = processRoutesForMenu(mainLayoutRoute.children);
  // console.log('Final processedMenu for template:', JSON.parse(JSON.stringify(processedMenu)));
  return processedMenu;
});


const toggleSidebar = () => { isCollapse.value = !isCollapse.value; };
const goToHome = () => { router.push('/'); };
const handleUserCommand = (command) => {
  switch (command) {
    case 'logout': authStore.logout(); break;
    case 'profile': ElMessage.info('个人中心功能暂未开放'); router.push('/user/profile'); break; // 示例跳转
    case 'changePassword': ElMessage.info('修改密码功能暂未开放'); router.push('/user/change-password'); break; // 示例跳转
    case 'settings': ElMessage.info('系统设置功能暂未开放'); router.push('/system/settings'); break; // 示例跳转
    case 'dashboard': router.push('/'); break;
    default: break;
  }
};
</script>

<style scoped>
/* 您原有的样式保持不变 */
.main-layout-container { height: 100vh; overflow: hidden; }
.sidebar { background-color: #2b333e; color: #bfcbd9; transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15); z-index: 1001; }
.layout-logo-container { height: 50px; display: flex; align-items: center; justify-content: center; padding: 0 10px; cursor: pointer; overflow: hidden; }
.layout-logo { height: 30px; width: 30px; margin-right: 0; object-fit: contain; transition: margin-right 0.28s; }
.sidebar:not(.el-aside--collapse) .layout-logo { margin-right: 10px; }
.layout-system-title { color: white; font-size: 16px; font-weight: 600; white-space: nowrap; opacity: 1; transition: opacity 0.2s 0.1s; }
.sidebar.el-aside--collapse .layout-system-title { opacity: 0; width: 0; display:none; }
.el-menu-vertical-custom { border-right: none !important; height: calc(100% - 50px); overflow-y: auto; overflow-x: hidden; }
.el-menu-vertical-custom::-webkit-scrollbar { width: 6px; }
.el-menu-vertical-custom::-webkit-scrollbar-thumb { background: #5a5a5a; border-radius: 3px; }
.el-menu-vertical-custom::-webkit-scrollbar-track { background: transparent; }
.sidebar :deep(.el-menu-item), .sidebar :deep(.el-sub-menu__title) { color: #bfcbd9 !important; height: 50px; line-height: 50px; }
.sidebar :deep(.el-menu-item .el-icon), .sidebar :deep(.el-sub-menu__title .el-icon) { vertical-align: middle; margin-right: 5px; width: 24px; text-align: center; font-size: 18px;}
.sidebar.el-aside--collapse :deep(.el-sub-menu__title .el-icon) { margin-left: 0; }
.sidebar.el-aside--collapse :deep(.el-menu-item .el-icon) { margin-right: 0; }
.sidebar :deep(.el-menu-item:hover), .sidebar :deep(.el-sub-menu__title:hover) { background-color: #3a4b5f !important; color: #ffffff !important; }
.sidebar :deep(.el-menu-item.is-active) { color: #409EFF !important; background-color: #202a36 !important; border-left: 3px solid #409EFF; padding-left: calc(20px - 3px) !important; position: relative; }
.sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title), .sidebar :deep(.el-sub-menu.is-opened > .el-sub-menu__title) { color: #ffffff !important; background-color: #3a4b5f !important; }
.sidebar.el-aside--collapse :deep(.el-tooltip__trigger) { padding: 0 !important; display: flex; align-items: center; justify-content: center; height: 100%; }
.sidebar.el-aside--collapse :deep(.el-menu-item.is-active) { padding-left: 0 !important; justify-content: center; }
.sidebar.el-aside--collapse :deep(.el-menu-item.is-active)::before { content: ''; position: absolute; left: 0; top: 15px; bottom: 15px; width: 3px; background-color: #409EFF;}

.content-wrapper { transition: margin-left 0.28s; }
.main-header-simplified { display: flex; justify-content: space-between; align-items: center; padding: 0 15px 0 0; height: 50px; background-color: #fff; box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08); border-bottom: 1px solid #f0f0f0; z-index: 1000; }
.header-left{ padding-left: 5px; }
.header-right { display: flex; align-items: center; }
.sidebar-toggle-btn { font-size: 20px; color: #5a5e66; padding: 0 10px; height: 100%; cursor: pointer; }
.sidebar-toggle-btn:hover { background-color: rgba(0,0,0,0.025); }
.el-divider--vertical { height: 1.5em; margin: 0 8px; border-left: 1px solid #dcdfe6; }
.user-dropdown-trigger { display: flex; align-items: center; cursor: pointer; padding: 0 10px; height: 100%; transition: background-color 0.3s; }
.user-dropdown-trigger:hover { background-color: #f5f5f5; }
.user-avatar { background-color: #409EFF; margin-right: 8px; }
.username-text { font-size: 14px; color: #303133; margin-right: 5px; }
.main-content { background-color: #f0f2f5; padding: 20px; overflow-y: auto; height: calc(100vh - 50px); position: relative; }
.page-title-container { margin-bottom: 18px; }
.page-title-container h1 { font-size: 18px; font-weight: 500; color: #303133; margin: 0 0 12px 0; }
.page-title-container .el-divider { margin: 0 0 18px 0; }
.fade-transform-leave-active, .fade-transform-enter-active { transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1); }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }
</style>