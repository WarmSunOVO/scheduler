import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';

// 视图组件导入
import LoginView from '../views/LoginView.vue';
import MainLayout from '../layouts/MainLayout.vue';
// --- 保持您现有的直接从 views 导入的风格 ---
import ClassroomManagement from '../views/ClassroomManagement.vue';
import TeacherManagement from '../views/TeacherManagement.vue';
import UserManagement from '../views/UserManagement.vue';
import CourseManagement from '../views/CourseManagement.vue';
import SemesterManagement from '../views/SemesterManagement.vue';
import TeachingTaskManagement from '../views/TeachingTaskManagement.vue';
import TeacherUnavailabilityManagement from '../views/TeacherUnavailabilityManagement.vue';
import ClassGroupUnavailabilityManagement from '../views/ClassGroupUnavailabilityManagement.vue';
import RoomUnavailabilityManagement from '../views/RoomUnavailabilityManagement.vue';
import ConstraintRuleManagement from '../views/ConstraintRuleManagement.vue';
// --- scheduling 包下的视图 ---
import AutomatedSchedulingPage from '../views/scheduling/AutomatedSchedulingPage.vue';
// import ScheduleDisplayPage   from "@/views/scheduling/ScheduleDisplayPage.vue"; // ScheduleDisplayPage 暂时不用，因为功能合并了

// 引入 Element Plus Icons
import {
    Cpu,                    // 用于自动排课
    Setting,                // 用于通用约束规则管理
    School,
    User as UserIcon,
    UserFilled,
    HomeFilled,
    Collection as CollectionIcon,
    Calendar as CalendarIcon,
    Tickets as TicketsIcon,
    Lock as LockIcon,
    House as HouseIcon,
    View as ViewIcon
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
        redirect: '/management/classrooms', // 或者您希望的其他默认页
        meta: {
            requiresAuth: true
        },
        children: [
            // --- 基础数据管理 (假设这些都在 /management/ 路径下) ---
            {
                path: 'management/semesters', // 为了统一，建议都带上前缀，或都不带
                name: 'semesterManagement',
                component: SemesterManagement,
                meta: {
                    title: '学期管理',
                    icon: CalendarIcon,
                    roles: ['ROLE_ADMIN']
                }
            },
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
                path: 'management/courses',
                name: 'courseManagement',
                component: CourseManagement,
                meta: {
                    title: '课程管理',
                    icon: CollectionIcon,
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
            // --- 核心业务 ---
            {
                path: 'teaching-tasks', // 这个路径没有 'management/' 前缀
                name: 'teachingTaskManagement',
                component: TeachingTaskManagement,
                meta: {
                    title: '教学任务管理',
                    icon: TicketsIcon,
                    roles: ['ROLE_ADMIN', 'ROLE_TEACHER']
                }
            },
            // --- 约束管理 (假设都在 /constraints/ 路径下) ---
            {
                path: 'constraints/teacher-unavailability',
                name: 'teacherUnavailabilityManagement',
                component: TeacherUnavailabilityManagement,
                meta: {
                    title: '教师不可用时间',
                    icon: LockIcon,
                    roles: ['ROLE_ADMIN', 'ROLE_MANAGER']
                }
            },
            {
                path: 'constraints/class-group-unavailability',
                name: 'classGroupUnavailabilityManagement',
                component: ClassGroupUnavailabilityManagement,
                meta: {
                    title: '班级不可用时间',
                    icon: UserFilled,
                    roles: ['ROLE_ADMIN', 'ROLE_MANAGER']
                }
            },
            {
                path: 'constraints/room-unavailability',
                name: 'roomUnavailabilityManagement',
                component: RoomUnavailabilityManagement,
                meta: {
                    title: '教室不可用时间',
                    icon: HouseIcon,
                    roles: ['ROLE_ADMIN', 'ROLE_MANAGER']
                }
            },
            {
                path: 'constraints/rules',
                name: 'constraintRuleManagement',
                component: ConstraintRuleManagement,
                meta: {
                    title: '通用约束规则',
                    icon: Setting,
                    roles: ['ROLE_ADMIN', 'ROLE_MANAGER']
                }
            },
            // --- 排课功能 (整合为一个) ---
            {
                path: 'scheduling/auto', // 保留这个路径
                name: 'autoScheduling',    // 名称可以简化
                component: AutomatedSchedulingPage,
                meta: {
                    title: '自动排课',      // 统一标题
                    icon: Cpu,
                    roles: ['ROLE_ADMIN', 'ROLE_MANAGER']
                }
            }
            // '/scheduling/auto-display' 路由已被移除
        ]
    },

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

// 全局前置路由守卫 (保持您原有的逻辑)
router.beforeEach((to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 智能AI高校排课系统` : '智能AI高校排课系统';
    const authStore = useAuthStore();
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiredRoles = to.meta.roles;

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
                    console.warn(`路由守卫: 用户 "${authStore.user?.username}" 没有权限访问 "${to.fullPath}". 需要角色: ${requiredRoles.join(',')}. 用户角色: ${authStore.userRoles?.join(',') || '无'}`);
                    ElMessage.warning('您没有权限访问此页面。');
                    if (from.name === 'login' || !from.fullPath || from.fullPath === to.fullPath) {
                        next('/');
                    } else {
                        next(from.fullPath);
                    }
                }
            } else {
                next();
            }
        }
    } else if (to.name === 'login' && authStore.isAuthenticated) {
        if (from.name === 'login' || !from.fullPath || from.fullPath === '/login') {
            next('/');
        } else {
            next(from.fullPath);
        }
    } else {
        next();
    }
});

export default router;