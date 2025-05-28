<template>
  <div id="app-container">
    <!-- <h1>智能AI高校排课系统 - 前端</h1> -->
    <!-- 可以放一个全局的导航栏在这里 -->
    <router-view /> <!-- 路由出口 -->
  </div>
    <div id="app-container">
      <el-button type="primary">Element Plus 按钮</el-button>
      <router-view />
    </div>
</template>

<script setup>
// 这里可以写一些全局的逻辑
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
</style>