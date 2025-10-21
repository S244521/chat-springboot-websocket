import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式
import router from './router'

// 链式调用时，直接连续使用 .use() 即可
createApp(App)
  .use(router)       // 先使用路由
  .use(ElementPlus)  // 再使用 ElementPlus
  .mount('#app')     // 最后挂载应用