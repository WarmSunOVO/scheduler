// src/stores/authStore.js
import { defineStore } from 'pinia';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';

const API_AUTH_URL = '/api/auth';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('authToken') || null,
        user: JSON.parse(localStorage.getItem('authUser') || JSON.stringify({ roles: [] })),
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        userRoles: (state) => (state.user && Array.isArray(state.user.roles) ? state.user.roles : []),
        currentUsername: (state) => state.user?.username || '未登录',
        currentUserId: (state) => state.user?.id || null,
    },
    actions: {
        async login(username, password) {
            this.clearAuthData();
            try {
                const response = await axios.post(`${API_AUTH_URL}/login`, { username, password });
                const responseData = response.data;
                if (responseData && responseData.accessToken) {
                    this.token = responseData.accessToken;
                    this.user = {
                        id: responseData.userId,
                        username: responseData.username,
                        roles: Array.isArray(responseData.roles) ? responseData.roles : []
                    };
                    localStorage.setItem('authToken', this.token);
                    localStorage.setItem('authUser', JSON.stringify(this.user));
                    axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`;
                    return true;
                } else {
                    throw new Error('登录响应无效，未能获取到Token或用户信息');
                }
            } catch (error) {
                const errorMessage = error.response?.data?.message || error.response?.data?.error || error.message || '登录失败，请检查您的凭据或网络连接。';
                ElMessage.error(errorMessage);
                this.clearAuthData();
                throw error;
            }
        },
        logout() {
            this.clearAuthData();
            ElMessage.success('您已成功登出！');
            if (router.currentRoute.value.name !== 'login') {
                router.push({ name: 'login', query: { logged_out: 'true' } });
            }
        },
        clearAuthData() {
            this.token = null;
            this.user = { roles: [] };
            localStorage.removeItem('authToken');
            localStorage.removeItem('authUser');
            delete axios.defaults.headers.common['Authorization'];
        },
        initializeAuth() {
            const token = localStorage.getItem('authToken');
            const userJson = localStorage.getItem('authUser');
            if (token && userJson) {
                this.token = token;
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                try {
                    const parsedUser = JSON.parse(userJson);
                    this.user = { ...parsedUser, roles: Array.isArray(parsedUser.roles) ? parsedUser.roles : [] };
                } catch (e) {
                    this.clearAuthData();
                }
            } else {
                this.clearAuthData();
            }
        },
        hasRole(roleName) {
            if (!this.user || !Array.isArray(this.user.roles)) return false;
            return this.user.roles.includes(roleName);
        },
        hasAnyRole(rolesArray) {
            if (!this.user || !Array.isArray(this.user.roles) || this.user.roles.length === 0) return false;
            if (!Array.isArray(rolesArray) || rolesArray.length === 0) return true;
            return rolesArray.some(role => this.user.roles.includes(role));
        }
    },
});