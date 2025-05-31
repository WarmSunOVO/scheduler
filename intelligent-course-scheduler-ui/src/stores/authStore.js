// src/stores/authStore.js
import { defineStore } from 'pinia';
import axios from 'axios'; // 确保axios已安装并按需配置（例如baseURL）
import { ElMessage } from 'element-plus';
import router from '@/router'; // 引入Vue Router实例

// 后端认证API的基础路径 (确保与后端Controller的@RequestMapping一致)
const API_AUTH_URL = '/api/auth';

export const useAuthStore = defineStore('auth', {
    // 状态：存储认证相关的数据
    state: () => ({
        token: localStorage.getItem('authToken') || null, // JWT Token
        user: JSON.parse(localStorage.getItem('authUser')) || null, // 当前登录用户信息 (例如: { id, username, fullName, email, roles: ['ROLE_ADMIN'] })
        // returnUrl: null, // (可选) 用于存储登录后要重定向的URL
    }),

    // Getters：计算属性，方便获取衍生的状态
    getters: {
        /**
         * 判断用户是否已认证 (通过检查token是否存在)
         * @returns {boolean}
         */
        isAuthenticated: (state) => !!state.token,

        /**
         * 获取当前用户名
         * @returns {string | null}
         */
        currentUsername: (state) => state.user?.username || null,

        /**
         * 获取当前用户ID
         * @returns {number | null}
         */
        currentUserId: (state) => state.user?.id || null,

        /**
         * 获取当前用户角色列表 (假设user对象中有一个roles数组)
         * @returns {Array<string>}
         */
        userRoles: (state) => state.user?.roles || [],

        // 你可以根据需要添加更多 getters，例如：
        // isAdmin: (state) => state.user?.roles?.includes('ROLE_ADMIN') || false,
    },

    // Actions：定义修改状态的方法（通常是异步的）
    actions: {
        /**
         * 用户登录
         * @param {string} username - 用户名
         * @param {string} password - 密码
         * @returns {Promise<boolean>} - 返回一个Promise，解析为true表示登录成功
         * @throws {Error} - 如果登录失败或发生错误
         */
        async login(username, password) {
            try {
                console.log('AuthStore: Attempting login with', { username });
                const response = await axios.post(`${API_AUTH_URL}/login`, {
                    username,
                    password,
                });

                // 假设后端成功时返回的数据格式为：
                // { accessToken: "...", userId: 1, username: "admin", roles: ["ROLE_ADMIN"] }
                const { accessToken, userId, username: loggedInUsername, roles } = response.data;

                if (accessToken) {
                    this.token = accessToken;
                    this.user = {
                        id: userId,
                        username: loggedInUsername,
                        // 如果后端返回了角色，也存储起来
                        roles: roles || [], // 确保roles是一个数组
                    };

                    // 持久化到 localStorage
                    localStorage.setItem('authToken', accessToken);
                    localStorage.setItem('authUser', JSON.stringify(this.user));

                    // 为后续的 Axios 请求设置默认的 Authorization header
                    axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;

                    console.log('AuthStore: Login successful, token and user set.');
                    return true; // 表示登录成功
                } else {
                    // 这种情况理论上不应该发生，如果后端API设计良好，失败会通过HTTP错误码返回
                    throw new Error('登录失败，未能从服务器获取有效的Token。');
                }
            } catch (error) {
                console.error('AuthStore: Login API call failed.', error.response || error);
                const errorMessage =
                    error.response?.data?.message || // 后端自定义的错误消息
                    error.response?.data?.error || // Spring Boot 默认的错误字段
                    (error.response?.status === 401 ? '用户名或密码错误。' : null) || // 针对401的特定消息
                    error.message || // 其他网络或请求错误
                    '登录时发生未知错误，请稍后重试。';

                ElMessage.error(errorMessage);
                this.clearAuthData(); // 登录失败时也清理状态，确保一致性
                throw new Error(errorMessage); // 将处理过的错误消息继续抛出
            }
        },

        /**
         * 用户登出
         * 清理认证信息并重定向到登录页
         */
        logout() {
            console.log('AuthStore: Logging out.');
            this.clearAuthData();
            // 确保在非登录页才执行跳转，避免在登录页重复跳转
            if (router.currentRoute.value.name !== 'login') {
                router.push({ name: 'login' });
            }
        },

        /**
         * 内部方法：清理认证相关的状态和 localStorage
         */
        clearAuthData() {
            this.token = null;
            this.user = null;
            localStorage.removeItem('authToken');
            localStorage.removeItem('authUser');
            // 清除 Axios 的默认 Authorization header
            delete axios.defaults.headers.common['Authorization'];
            console.log('AuthStore: Auth data cleared.');
        },

        /**
         * 应用初始化时调用，尝试从 localStorage 恢复认证状态
         */
        initializeAuth() {
            console.log('AuthStore: Initializing auth state from localStorage.');
            const token = localStorage.getItem('authToken');
            if (token) {
                const userJson = localStorage.getItem('authUser');
                if (userJson) {
                    try {
                        this.user = JSON.parse(userJson);
                        this.token = token; // 只有当用户信息也成功恢复时才设置token，或在下面验证token有效性
                        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                        console.log('AuthStore: Auth state restored from localStorage for user:', this.user?.username);
                        // (可选) 在这里可以添加一个API调用来验证token的有效性
                        // 如果token无效，则调用 this.logout();
                        // 例如: this.validateTokenOnLoad();
                    } catch (e) {
                        console.error("AuthStore: Failed to parse stored user info, logging out.", e);
                        this.logout(); // 如果存储的用户信息损坏，则执行登出
                    }
                } else {
                    // 有token但没有用户信息，这可能是一个不一致的状态，清理掉
                    console.warn("AuthStore: Token found but no user info, logging out.");
                    this.logout();
                }
            } else {
                // 没有token，确保所有状态都是清理干净的
                this.clearAuthData();
            }
        },

        /**
         * (可选) 示例：如果需要在应用加载时验证token并获取最新用户信息
         * 这个方法可以被 initializeAuth 调用
         */
        // async validateTokenOnLoad() {
        //   if (!this.token) return;
        //   try {
        //     // 假设你有一个 /api/auth/me 或类似的端点来验证token并返回当前用户信息
        //     const response = await axios.get('/api/auth/me'); // 确保这个API会使用 Authorization header
        //     this.user = response.data; // 更新用户信息
        //     localStorage.setItem('authUser', JSON.stringify(this.user));
        //     console.log('AuthStore: Token validated and user info refreshed on load.');
        //   } catch (error) {
        //     console.warn('AuthStore: Token validation failed or /api/auth/me failed on load:', error.response || error);
        //     this.logout(); // 如果token无效或获取用户信息失败，则执行登出
        //   }
        // }
    },
});