import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';

// 视图组件导入
import LoginView from '../views/LoginView.vue';
import MainLayout from '../layouts/MainLayout.vue';
import ClassroomManagement from '../views/ClassroomManagement.vue';
import TeacherManagement from '../views/TeacherManagement.vue';
import UserManagement from '../views/UserManagement.vue';
import CourseManagement from '../views/CourseManagement.vue';
import SemesterManagement from '../views/SemesterManagement.vue';
import TeachingTaskManagement from '../views/TeachingTaskManagement.vue';
import TeacherUnavailabilityManagement from '../views/TeacherUnavailabilityManagement.vue';
import ClassGroupUnavailabilityManagement from '../views/ClassGroupUnavailabilityManagement.vue';
import RoomUnavailabilityManagement from '../views/RoomUnavailabilityManagement.vue';
// 新增导入：通用约束规则管理视图
import ConstraintRuleManagement from '../views/ConstraintRuleManagement.vue';


// 引入 Element Plus Icons
import {
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
    View as ViewIcon        // ConstraintRuleManagement 中用到了 ViewIcon, 确保这里有 (虽然路由meta不用它)
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
        redirect: '/management/classrooms',
        meta: {
            requiresAuth: true
        },
        children: [
            // --- 基础数据管理 ---
            {
                path: 'management/semesters',
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
                path: 'teaching-tasks',
                name: 'teachingTaskManagement',
                component: TeachingTaskManagement,
                meta: {
                    title: '教学任务管理',
                    icon: TicketsIcon,
                    roles: ['ROLE_ADMIN', 'ROLE_TEACHER']
                }
            },
            // --- 约束管理 ---
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
                    icon: UserFilled, // 或者您选择的其他图标
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
            { // <--- 新增通用约束规则管理路由 --->
                path: 'constraints/rules',
                name: 'constraintRuleManagement',
                component: ConstraintRuleManagement,
                meta: {
                    title: '通用约束规则',
                    icon: Setting, // 使用 Setting 图标
                    roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] // 假设管理员和教务管理员可访问
                }
            }
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