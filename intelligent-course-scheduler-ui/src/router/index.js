// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
// import HomeView from '../views/HomeView.vue' // 如果有，可以保留或删除
import ClassroomManagement from '../views/ClassroomManagement.vue' // <--- 导入教室管理视图
import TeacherManagement from '../views/TeacherManagement.vue' // <--- 导入教师管理视图
const routes = [
    {
        path: '/',
        redirect: '/management/classrooms' // 保持之前的重定向或修改为新的默认页
    },
    {
        path: '/management/classrooms',
        name: 'classroomManagement',
        component: ClassroomManagement,
        meta: { title: '教室管理' }
    },
    { // <--- 为教师管理添加路由
        path: '/management/teachers',
        name: 'teacherManagement',
        component: TeacherManagement,
        meta: { title: '教师管理' }
    }
    // ... 其他路由
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// (可选) 全局前置守卫，用于更新页面标题
router.beforeEach((to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 智能AI高校排课系统` : '智能AI高校排课系统';
    next();
});

export default router