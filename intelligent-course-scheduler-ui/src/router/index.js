import { createRouter, createWebHistory } from 'vue-router';

// 视图组件导入
import LoginView from '../views/LoginView.vue';
import MainLayout from '../layouts/MainLayout.vue';
import ClassroomManagement from '../views/ClassroomManagement.vue';
import TeacherManagement from '../views/TeacherManagement.vue';
import UserManagement from '../views/UserManagement.vue';
import CourseManagement from '../views/CourseManagement.vue'; // <--- 导入课程管理视图
import SemesterManagement from '../views/SemesterManagement.vue'; // <--- 导入学期管理视图
import { Calendar as CalendarIcon } from '@element-plus/icons-vue'; // <--- 假设用这个图标
import TeachingTaskManagement from '../views/TeachingTaskManagement.vue'; // <--- 导入
import { Tickets as TicketsIcon } from '@element-plus/icons-vue'; // <--- 假设用这个图标

// 引入 Element Plus Icons
import {
    Setting,
    School,
    User as UserIcon,
    UserFilled,
    HomeFilled,
    Collection as CollectionIcon // <--- 用于课程管理的图标
} from '@element-plus/icons-vue';

// 引入 Pinia store
import { useAuthStore } from '@/stores/authStore';

const routes = [
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: {
            title: '系统登录',
            requiresAuth: false
        }
    },
    {
        path: '/',
        component: MainLayout,
        redirect: '/management/classrooms', // 默认重定向
        meta: {
            requiresAuth: true
        },
        children: [
            {
                path: 'management/classrooms',
                name: 'classroomManagement',
                component: ClassroomManagement,
                meta: {
                    title: '教室管理',
                    icon: School,
                    roles: ['ROLE_ADMIN', 'ROLE_TEACHER']
                }
            },
            {
                path: 'management/teachers',
                name: 'teacherManagement',
                component: TeacherManagement,
                meta: {
                    title: '教师管理',
                    icon: UserIcon,
                    roles: ['ROLE_ADMIN', 'ROLE_TEACHER']
                }
            },
            {
                path: 'management/users',
                name: 'userManagement',
                component: UserManagement,
                meta: {
                    title: '用户管理',
                    icon: UserFilled,
                    roles: ['ROLE_ADMIN']
                }
            },
            { // <--- 新增课程管理路由 --->
                path: 'management/courses',
                name: 'courseManagement',
                component: CourseManagement,
                meta: {
                    title: '课程管理',
                    icon: CollectionIcon, // 指定图标
                    roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] // 假设管理员和教师可访问
                }
            },
            { // <--- 新增学期管理路由 --->
                path: 'management/semesters',
                name: 'semesterManagement',
                component: SemesterManagement,
                meta: {
                    title: '学期管理',
                    icon: CalendarIcon, // 指定图标
                    roles: ['ROLE_ADMIN'] // 通常只有管理员能管理学期
                }
            },
            {
                path: 'teaching-tasks', // <--- 路径可以不放在 'management'下，如果它更核心
                name: 'teachingTaskManagement',
                component: TeachingTaskManagement,
                meta: {
                    title: '教学任务管理',
                    icon: TicketsIcon, // 指定图标
                    roles: ['ROLE_ADMIN', 'ROLE_TEACHER'] // 假设管理员和教师可操作
                }
            }
            // 在这里可以继续添加其他子路由，例如学期管理等
            // {
            //   path: 'management/semesters',
            //   name: 'semesterManagement',
            //   component: () => import('@/views/SemesterManagement.vue'),
            //   meta: { title: '学期管理', icon: SomeIcon, roles: ['ROLE_ADMIN'] }
            // },
        ]
    },
    // 404 页面 (可选)
    // {
    //   path: '/:pathMatch(.*)*',
    //   name: 'NotFound',
    //   component: () => import('../views/NotFoundView.vue'),
    //   meta: { title: '页面未找到 - 404', requiresAuth: false }
    // }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition;
        } else {
            return { top: 0, behavior: 'smooth' };
        }
    }
});

// 全局前置路由守卫 (保持不变)
router.beforeEach((to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 智能AI高校排课系统` : '智能AI高校排课系统';
    const authStore = useAuthStore();
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiredRoles = to.meta.roles; // 获取当前目标路由定义的角色

    if (requiresAuth) {
        if (!authStore.isAuthenticated) {
            next({
                name: 'login',
                query: { redirect: to.fullPath }
            });
        } else {
            if (requiredRoles && Array.isArray(requiredRoles) && requiredRoles.length > 0) {
                if (authStore.hasAnyRole(requiredRoles)) {
                    next();
                } else {
                    console.warn(`路由守卫: 用户 "${authStore.user?.username}" 没有权限访问 "${to.fullPath}". 需要角色: ${requiredRoles.join(',')}. 用户角色: ${authStore.userRoles.join(',')}`);
                    next({ path: from.fullPath && from.name !== 'login' ? from.fullPath : '/' });
                }
            } else {
                next();
            }
        }
    } else if (to.name === 'login' && authStore.isAuthenticated) {
        next({ path: from.fullPath && from.name !== 'login' && from.fullPath !== '/login' ? from.fullPath : '/' });
    } else {
        next();
    }
});

export default router;