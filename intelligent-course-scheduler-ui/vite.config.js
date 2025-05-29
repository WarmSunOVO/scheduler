import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173, // 你可以指定前端开发服务器的端口
    proxy: {
      // 字符串简写写法
      // '/foo': 'http://localhost:4567',
      // 带选项写法
      '/api': { // 当请求路径以 /api 开头时，会触发此代理规则
        target: 'http://localhost:8080', // 后端API服务器的地址
        changeOrigin: true, // 需要虚拟主机站点
        // rewrite: (path) => path.replace(/^\/api/, '') // 如果后端API路径本身不带/api，则需要重写
        // 在我们的例子中，后端路径已经是 /api/classrooms，所以不需要 rewrite
      },
      // 正则表达式写法
      // '^/fallback/.*': {
      //   target: 'http://jsonplaceholder.typicode.com',
      //   changeOrigin: true,
      //   rewrite: (path) => path.replace(/^\/fallback/, '')
      // }
    }
  }
})