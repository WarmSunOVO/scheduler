<template>
  <router-view />
</template>

<script setup>
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router'; // 如果登出后需要编程式导航，但通常authStore.logout会处理

const authStore = useAuthStore();
const router = useRouter(); // 如果需要

const handleLogout = () => {
  authStore.logout(); // 调用 store 中的 logout action
  // logout action 内部应该已经包含了 router.push({ name: 'login' })
  ElMessage.success('您已成功登出！');
};
</script>
<style>
/* 你可以在这里写一些全局样式，或者在 src/assets/main.css (如果Vite生成了这个文件) */
#app-container {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 10px; /* 根据需要调整 */
}
/* 你的全局或布局样式 */
.el-header {
  display: flex;
  align-items: center;
  justify-content: flex-end; /* 示例：让内容靠右 */
  padding: 0 20px;
  background-color: #B3C0D1; /* 示例颜色 */
}
</style>