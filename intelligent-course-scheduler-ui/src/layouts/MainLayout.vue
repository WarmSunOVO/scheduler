<template>
  <el-container class="main-layout-container">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="layout-logo-container" @click="goToHome" title="返回首页">
        <img src="@/assets/logo.png" alt="Logo" class="layout-logo"/>
        <span v-if="!isCollapse" class="layout-system-title">排课系统</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-custom"
      router
      :collapse="isCollapse"
      :collapse-transition="false"
      background-color="transparent"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      >
      <el-sub-menu index="management">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span v-if="!isCollapse">基础数据管理</span>
        </template>
        <el-menu-item index="/management/classrooms" route="/management/classrooms">
          <el-icon><School /></el-icon>
          <template #title><span>教室管理</span></template>
        </el-menu-item>
        <el-menu-item index="/management/teachers" route="/management/teachers">
          <el-icon><User /></el-icon>
          <template #title><span>教师管理</span></template>
        </el-menu-item>
      </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="main-header">
        <div>
          <el-button @click="toggleSidebar" :icon="isCollapse ? Expand : Fold" circle class="collapse-btn"></el-button>
          <span>{{ currentPageTitle }}</span>
        </div>
        <div class="user-info">
          <el-dropdown @command="handleUserCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="30" style="margin-right: 8px;">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              {{ authStore.user?.username || '未登录' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
// 你的 <script setup> 部分保持不变
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import {
  Setting,
  School,
  User,
  HomeFilled, // 如果顶部下拉菜单用得到
  UserFilled,
  ArrowDown,
  Expand,
  Fold
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';


const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const isCollapse = ref(false);

const activeMenu = computed(() => {
  return route.path;
});

const currentPageTitle = computed(() => {
  return route.meta.title || '智能AI高校排课系统';
});

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value;
}

const goToHome = () => {
  router.push('/');
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    authStore.logout();
    ElMessage.success('您已成功登出！');
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能暂未开放');
  } else if (command === 'settings') {
    ElMessage.info('设置功能暂未开放');
  }
};
</script>

<style scoped>
.main-layout-container {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: #2b333e; /* 更深一点的统一侧边栏背景色 */
  color: #bfcbd9;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1); /* 平滑过渡 */
  box-shadow: 2px 0 6px rgba(0,0,0,0.1); /* 给侧边栏一点右侧阴影 */
}

.layout-logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center; /* 折叠时图标也居中 */
  padding: 0 10px;
  /* background-color: #262f3b; */ /* 可以与sidebar背景色一致或略深 */
  cursor: pointer;
  overflow: hidden; /* 防止折叠时文字溢出 */
}
.layout-logo {
  height: 32px;
  width: 32px;
  margin-right: 0; /* 折叠时图标居中 */
  object-fit: contain; /* 保持图片比例 */
  transition: margin-right 0.28s; /* 与侧边栏宽度过渡同步 */
}
/* 当侧边栏展开时，给logo和文字之间添加间距 */
.sidebar:not(.el-aside--collapse) .layout-logo {
  margin-right: 10px;
}

.layout-system-title {
  color: white;
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap; /* 防止文字换行 */
  opacity: 1; /* 控制文字的显示/隐藏 */
  transition: opacity 0.2s 0.1s; /* 稍微延迟文字的出现/消失 */
}
/* 当侧边栏折叠时，隐藏文字 */
.sidebar.el-aside--collapse .layout-system-title {
  opacity: 0;
  width: 0; /* 确保不占位 */
  display:none; /* 彻底隐藏 */
}

.el-menu-vertical-custom { /* 给el-menu一个自定义类名 */
  border-right: none !important; /* 强制移除边框 */
  /* background-color: transparent !important; /* 已在template中设置 */
  /* text-color 和 active-text-color 已在template中设置 */
  height: calc(100% - 60px); /* 减去logo区域高度 */
  overflow-y: auto; /* 内容多时可滚动 */
  overflow-x: hidden;
}

/* 滚动条美化 (可选) */
.el-menu-vertical-custom::-webkit-scrollbar { width: 6px; }
.el-menu-vertical-custom::-webkit-scrollbar-thumb { background: #5a5a5a; border-radius: 3px; }
.el-menu-vertical-custom::-webkit-scrollbar-track { background: transparent; }


/* 统一设置菜单项和子菜单标题 */
.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-sub-menu__title) {
  /* color: #bfcbd9 !important; /* 已在template中设置 text-color */
  height: 50px; /* 统一高度 */
  line-height: 50px;
  /* transition: background-color 0.3s, color 0.3s; */ /* 平滑过渡 */
}

/* 鼠标悬浮效果 */
.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background-color: #3a4b5f !important; /* 更明显的悬浮背景色 */
  color: #ffffff !important; /* 悬浮时文字变白，更突出 */
}

/* 当前激活的菜单项 */
.sidebar :deep(.el-menu-item.is-active) {
  /* color: #409EFF !important; /* 已在template中设置 active-text-color */
  background-color: #0070f3 !important; /* 更鲜明、现代的蓝色作为激活背景 */
  color: #ffffff !important; /* 激活时文字也为白色，对比更强 */
  border-left: 4px solid #ffffff; /* 左侧添加白色高亮边框 */
  padding-left: calc(20px - 4px) !important; /* 调整内边距以适应边框 (el-menu-item默认padding-left是20px) */
  position: relative; /* 为了可能的伪元素 */
}


/* 激活的子菜单的父菜单标题 (当子项被选中时，父菜单也高亮) */
.sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #ffffff !important; /* 激活的父菜单文字也变白 */
  /* background-color: #3a4b5f !important; */ /* 可以不改变背景，或者用一个稍浅的高亮色 */
}
/* 当子菜单展开时，父菜单的样式 */
.sidebar :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background-color: #323c4a !important; /* 展开的父菜单背景色 */
}


/* 折叠时，菜单项的 tooltip 触发区域调整 */
.sidebar.el-aside--collapse :deep(.el-menu-item),
.sidebar.el-aside--collapse :deep(.el-sub-menu__title) {
  padding: 0 !important; /* 移除内边距 */
  display: flex;
  align-items: center;
  justify-content: center; /* 图标居中 */
}
.sidebar.el-aside--collapse :deep(.el-menu-item.is-active) {
  padding-left: calc(50% - 11px - 2px) !important; /* 尝试让边框居中一点，11px是图标大致宽度的一半 */
}


/* 顶部栏样式 */
.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid #e6e6e6;
}
.collapse-btn {
  margin-right: 15px;
  font-size: 20px; /* 让折叠按钮图标大一点 */
  color: #606266;
}
.user-info {
  cursor: pointer;
}
.el-dropdown-link {
  display: flex;
  align-items: center;
  color: #303133;
}
.el-dropdown-link .el-avatar {
  background-color: #409EFF; /* 用户头像背景色 */
}


.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px); /* 减去顶部栏高度 */
}

/* 路由切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .5s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>