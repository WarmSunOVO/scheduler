// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import axios from 'axios'; // 导入 Axios

import { useAuthStore } from './stores/authStore'; // 导入 authStore

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

// 请求拦截器
axios.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();
        if (authStore.isAuthenticated && authStore.token) {
            config.headers.Authorization = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);


// 响应拦截器 (可选，但推荐用于统一错误处理)
// 响应拦截器
axios.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        const authStore = useAuthStore(); // 必须在拦截器函数内部获取实例
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    // 避免在登录页本身因401错误（比如初始化时token无效）导致无限循环
                    if (router.currentRoute.value.name !== 'login') {
                        ElMessage.error(error.response.data.message || '您的会话已过期或无效，请重新登录。');
                        authStore.logout(); // logout 方法内部会处理路由跳转
                    } else {
                        // 在登录页收到401，通常是凭据错误，登录逻辑的catch块会处理 ElMessage
                    }
                    break;
                case 403:
                    ElMessage.error(error.response.data.message || '您没有权限执行此操作。');
                    break;
                // 可以添加其他错误码处理
                // default:
                //   ElMessage.error(error.response.data.message || `请求错误: ${error.response.status}`);
            }
        } else if (error.request) {
            ElMessage.error('网络连接错误，请检查您的网络。');
        } else {
            ElMessage.error(`请求发送失败: ${error.message}`);
        }
        return Promise.reject(error); // ★★★ 重要：必须将错误继续抛出
    }
);
// --- Axios 配置结束 ---

const authStore = useAuthStore(); // 获取 authStore 实例，以便在路由守卫之前使用
authStore.initializeAuth(); // 初始化认证状态


app.use(router)
app.use(ElementPlus)

app.mount('#app')

