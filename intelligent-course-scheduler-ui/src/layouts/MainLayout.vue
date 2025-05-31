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
        <template v-for="menuItem in visibleMenuItems" :key="menuItem.path">
          <el-sub-menu
              v-if="menuItem.children && menuItem.children.length > 0 && !menuItem.meta?.alwaysShowAsRoot && menuItem.children.some(c => shouldShowMenuItem(c))"
              :index="menuItem.path"
          >
            <template #title>
              <el-icon v-if="menuItem.meta?.icon"><component :is="menuItem.meta.icon" /></el-icon>
              <span v-if="!isCollapse || !menuItem.meta?.icon">{{ menuItem.meta?.title }}</span>
            </template>
            <el-menu-item
                v-for="childItem in menuItem.children.filter(c => shouldShowMenuItem(c))"
                :key="childItem.name"
                :index="childItem.path"
                :route="{ name: childItem.name }"
            >
              <el-icon v-if="childItem.meta?.icon"><component :is="childItem.meta.icon" /></el-icon>
              <template #title><span>{{ childItem.meta?.title }}</span></template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item
              v-else-if="!menuItem.meta?.hideInMenu"
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
import {
  Setting, School, User as UserIconInMenu, UserFilled, HomeFilled, ArrowDown, Expand, Fold,
  Avatar as AvatarIcon, Lock as LockIcon, SwitchButton as SwitchButtonIcon, Setting as SettingHeaderIcon,
} from '@element-plus/icons-vue';
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

const iconComponents = { Setting, School, UserIconInMenu, UserFilled, HomeFilled, AvatarIcon, LockIcon, SwitchButtonIcon, SettingHeaderIcon };

const shouldShowMenuItem = (menuItem) => {
  if (!menuItem.meta || menuItem.meta.hidden) {
    return false;
  }
  if (menuItem.meta.roles && Array.isArray(menuItem.meta.roles) && menuItem.meta.roles.length > 0) {
    return authStore.hasAnyRole(menuItem.meta.roles);
  }
  return true;
};

const visibleMenuItems = computed(() => {
  console.log('Recalculating visibleMenuItems...');
  const mainLayoutRoute = router.options.routes.find(r => r.path === '/' && r.component?.name === 'MainLayout');
  console.log('MainLayout Route found:', mainLayoutRoute ? JSON.parse(JSON.stringify(mainLayoutRoute)) : undefined);

  if (!mainLayoutRoute || !mainLayoutRoute.children) {
    console.log('No MainLayout route or no children found in router.options.routes for path "/"');
    return [];
  }

  const menuStructure = []; // 最终的菜单结构

  // 1. 处理 "基础数据管理" 子菜单
  const managementChildren = mainLayoutRoute.children
      .filter(childRoute => {
        const isManagementRoute = childRoute.path.startsWith('management/'); // 注意，router.js中定义的是 'management/xxx'
        const hasMetaAndTitle = childRoute.meta && childRoute.meta.title;
        const shouldShow = shouldShowMenuItem(childRoute);
        // console.log(`Child: ${childRoute.name}, Path: ${childRoute.path}, IsMgmt: ${isManagementRoute}, HasMetaTitle: ${hasMetaAndTitle}, ShouldShow: ${shouldShow}, Meta:`, childRoute.meta ? JSON.parse(JSON.stringify(childRoute.meta)) : undefined, `UserRoles: ${authStore.userRoles.join(',')}`);
        return isManagementRoute && hasMetaAndTitle && shouldShow;
      })
      .map(routeItem => {
        let iconComponent = null;
        if (routeItem.meta?.icon) {
          iconComponent = typeof routeItem.meta.icon === 'string' && iconComponents[routeItem.meta.icon]
              ? markRaw(iconComponents[routeItem.meta.icon])
              : markRaw(routeItem.meta.icon);
        }
        return {
          ...routeItem,
          // path 已经是 /management/xxx，不需要额外处理
          name: routeItem.name,
          meta: { ...routeItem.meta, icon: iconComponent }
        };
      });

  if (managementChildren.length > 0) {
    menuStructure.push({
      path: '/management', // 虚拟父路径，用于 el-sub-menu 的 index
      meta: { title: '基础数据管理', icon: markRaw(Setting) }, // 父菜单的图标和标题
      children: managementChildren,
    });
  }

  // 2. 处理其他一级菜单项 (例如教学任务管理)
  mainLayoutRoute.children.forEach(childRoute => {
    // 确保它不是 management/ 开头的，并且应该显示，并且它没有子菜单（或者它的子菜单我们不在这里处理为嵌套）
    const isNotManagementRoute = !childRoute.path.startsWith('management/');
    const hasMetaAndTitle = childRoute.meta && childRoute.meta.title;
    const shouldShow = shouldShowMenuItem(childRoute);

    // console.log(`Checking other route: ${childRoute.name}, Path: ${childRoute.path}, IsNotMgmt: ${isNotManagementRoute}, HasMetaTitle: ${hasMetaAndTitle}, ShouldShow: ${shouldShow}`);

    if (isNotManagementRoute && hasMetaAndTitle && shouldShow) {
      // 检查这个一级菜单项是否已经作为某个子菜单的父级被处理了 (虽然目前我们的结构不会)
      // 简单起见，直接添加
      let iconComponent = null;
      if (childRoute.meta?.icon) {
        iconComponent = typeof childRoute.meta.icon === 'string' && iconComponents[childRoute.meta.icon]
            ? markRaw(iconComponents[childRoute.meta.icon])
            : markRaw(childRoute.meta.icon);
      }
      menuStructure.push({
        ...childRoute,
        // 确保 path 是可以直接用于导航的 (从你的router.js看，子路由的path已经是完整的了)
        path: childRoute.path.startsWith('/') ? childRoute.path : '/' + childRoute.path,
        meta: { ...childRoute.meta, icon: iconComponent },
        children: (childRoute.children && childRoute.children.length > 0) ?
            childRoute.children.filter(c => shouldShowMenuItem(c)).map(c_ => ({...c_, meta: {...c_.meta, icon: c_.meta.icon ? markRaw(c_.meta.icon) : null }}))
            : undefined // 如果它自身有子菜单且子菜单可见，也一并处理
      });
    }
  });

  console.log('Final menuStructure:', JSON.parse(JSON.stringify(menuStructure)));
  return menuStructure;
});

const toggleSidebar = () => { isCollapse.value = !isCollapse.value; };
const goToHome = () => { router.push('/'); };
const handleUserCommand = (command) => {
  switch (command) {
    case 'logout': authStore.logout(); break;
    case 'profile': ElMessage.info('个人中心功能暂未开放'); break;
    case 'changePassword': ElMessage.info('修改密码功能暂未开放'); break;
    case 'settings': ElMessage.info('系统设置功能暂未开放'); break;
    case 'dashboard': router.push('/'); break;
    default: break;
  }
};
</script>

<style scoped>
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