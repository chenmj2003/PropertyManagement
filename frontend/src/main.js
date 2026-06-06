import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 如果你需要 Ant Design Vue，请先 npm install ant-design-vue，并取消下一行注释
// import Antd from 'ant-design-vue'
// import 'ant-design-vue/dist/reset.css'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
// app.use(Antd)      // 如果安装了 ant-design-vue
app.mount('#app')