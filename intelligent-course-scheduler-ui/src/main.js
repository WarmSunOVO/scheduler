import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { createPinia } from 'pinia' // <--- 引入 createPinia

const app = createApp(App)
const pinia = createPinia() // <--- 创建 Pinia 实例

app.use(ElementPlus) // 全局注册 Element Plus
app.use(router)
app.use(pinia)  // 稍后会取消这里的注释

app.mount('#app')