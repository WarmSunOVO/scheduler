import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import axios from 'axios'; // 确保导入 Axios
import { useAuthStore } from './stores/authStore'; // 确保导入 authStore
import { ElMessage } from 'element-plus'; // 确保导入 ElMessage，响应拦截器会用到

const app = createApp(App);

// 1. 创建 Pinia 实例
const pinia = createPinia();
app.use(pinia); // 先使用 Pinia

// 2. Axios 全局配置 和 拦截器
// axios.defaults.baseURL = '/api'; // 可选，如果你的API都有 /api 前缀

// 请求拦截器
axios.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore(); // 在拦截器函数内部获取实例
        if (authStore.isAuthenticated && authStore.token) {
            config.headers.Authorization = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
axios.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        const authStore = useAuthStore(); // 在拦截器函数内部获取实例
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    if (router.currentRoute.value.name !== 'login') {
                        ElMessage.error(error.response.data?.message || '您的会话已过期或无效，请重新登录。');
                        authStore.logout(); // logout 方法内部会处理路由跳转
                    } else {
                        // 在登录页收到401，通常是凭据错误，登录逻辑的catch块会处理ElMessage
                        // 可以选择在这里也弹出一个通用的消息，或者依赖登录页自己的处理
                        // ElMessage.error(error.response.data?.message || '用户名或密码错误。');
                    }
                    break;
                case 403:
                    ElMessage.error(error.response.data?.message || '您没有权限执行此操作。');
                    break;
                // 可以根据需要处理其他错误码
                default:
                    // 对于非401/403的服务器错误，可以给一个通用提示
                    // if (!error.config.url.includes('/api/auth/login')) { // 避免在登录失败时重复提示
                    //   ElMessage.error(error.response.data?.message || `请求错误: ${error.response.status}`);
                    // }
                    break;
            }
        } else if (error.request) {
            ElMessage.error('网络连接错误，请检查您的网络。');
        } else {
            ElMessage.error(`请求发送失败: ${error.message}`);
        }
        return Promise.reject(error); // 必须将错误继续抛出
    }
);

// 3. 初始化 Auth Store (在 Pinia use 之后)
// 注意：useAuthStore() 必须在 app.use(pinia) 之后调用
const authStore = useAuthStore();
authStore.initializeAuth();

// 4. 使用 Vue Router
app.use(router);

// 5. 使用 ElementPlus
app.use(ElementPlus);

// 6. 挂载应用
app.mount('#app');