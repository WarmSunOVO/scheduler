import { createRouter, createWebHistory } from 'vue-router';

// 视图组件导入
import LoginView from '../views/LoginView.vue';
import MainLayout from '../layouts/MainLayout.vue'; // 确保路径正确，通常是 @/layouts/MainLayout.vue
import ClassroomManagement from '../views/ClassroomManagement.vue';
import TeacherManagement from '../views/TeacherManagement.vue';
// (可选) 如果你有仪表盘或404页面，也在这里导入或使用懒加载
// import DashboardView from '../views/DashboardView.vue';
// const NotFoundView = () => import('../views/NotFoundView.vue');

// 引入 Pinia store
import { useAuthStore } from '@/stores/authStore'; // 确保路径正确

const routes = [
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: {
            title: '系统登录',
            requiresAuth: false // 登录页不需要认证
        }
    },
    {
        path: '/', // 应用的根路径，将使用 MainLayout
        component: MainLayout,
        redirect: '/management/classrooms', // 默认重定向到 MainLayout 下的教室管理页面
        meta: {
            requiresAuth: true // 访问此布局及其子路由都需要认证
        },
        children: [
            // 示例：仪表盘/首页 (如果需要)
            // {
            //   path: 'dashboard', // 最终路径: /dashboard
            //   name: 'dashboard',
            //   component: DashboardView,
            //   meta: { title: '仪表盘' } // 父路由已要求认证
            // },
            {
                path: 'management/classrooms', // 最终路径: /management/classrooms
                name: 'classroomManagement',
                component: ClassroomManagement,
                meta: { title: '教室管理' }
            },
            {
                path: 'management/teachers', // 最终路径: /management/teachers
                name: 'teacherManagement',
                component: TeacherManagement,
                meta: { title: '教师管理' }
            },
            // 在这里添加其他需要在 MainLayout 中显示的受保护页面路由
            // 例如：课程管理，学生管理等
        ]
    },
    // (可选) 404 页面 - 应该放在所有有效路由之后
    // {
    //   path: '/:pathMatch(.*)*', // 匹配所有未匹配到的路径
    //   name: 'NotFound',
    //   component: NotFoundView,
    //   meta: { title: '页面未找到', requiresAuth: false }
    // }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL), // Vite 项目通常用这个
    routes,
    scrollBehavior(to, from, savedPosition) {
        // 路由切换时滚动到页面顶部
        if (savedPosition) {
            return savedPosition;
        } else {
            return { top: 0 };
        }
    }
});

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
    // 动态设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 智能AI高校排课系统` : '智能AI高校排课系统';

    const authStore = useAuthStore(); // 在守卫函数内部获取 store 实例

    // to.matched.some(record => record.meta.requiresAuth) 检查路由链中是否有任何一个记录标记了需要认证
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth && !authStore.isAuthenticated) {
        // 如果目标路由需要认证，但用户未通过认证
        console.log(`路由守卫: 路径 "${to.fullPath}" 需要认证，但用户未登录。重定向到登录页。`);
        next({
            name: 'login', // 跳转到登录页的路由名称
            query: { redirect: to.fullPath } // 保存用户原本想访问的路径，以便登录后重定向回去
        });
    } else if (to.name === 'login' && authStore.isAuthenticated) {
        // 如果用户已认证，但尝试访问登录页
        console.log(`路由守卫: 用户已登录，尝试访问登录页。重定向到来源页或首页。`);
        // 尝试重定向到他们刚离开的页面（如果不是登录页），否则到根路径
        const redirectTo = from.name !== 'login' && from.fullPath !== '/' ? from.fullPath : '/';
        next({ path: redirectTo });
    } else {
        // 其他情况 (访问无需认证的页面，或者已认证用户访问需要认证的页面)
        next(); // 允许路由继续
    }
});

export default router;