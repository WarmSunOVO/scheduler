// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// 导入你的组件 (稍后我们会创建这些示例组件)
// import HomeView from '../views/HomeView.vue'
// import LoginView from '../views/LoginView.vue'

const routes = [
    // {
    //   path: '/',
    //   name: 'home',
    //   component: HomeView
    // },
    // {
    //   path: '/login',
    //   name: 'login',
    //   component: LoginView
    // }
    // 暂时留空，或者添加一个最简单的根路由指向 App.vue 里的内容
    {
        path: '/',
        name: 'default',
        component: () => import('../App.vue') // 或者一个专门的布局/首页组件
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL), // 使用 history 模式
    routes
})

export default router