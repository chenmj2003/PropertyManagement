import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, 'src')
    }
  },
  server: {
    port: 3000,          // 保证和浏览器访问的端口一致
    proxy: {
      '/api': {          // 拦截 /api/xxx 的请求
        target: 'http://localhost:8080',
        changeOrigin: true
        // 不要写 rewrite，因为你后端路径本身就带 /api
      }
    }
  }
})