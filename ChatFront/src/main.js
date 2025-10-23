import {
	createApp
} from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式
import router from './router'

// 引入 Animate.css
import 'animate.css';

// 引入粒子特效插件
import Particles from 'particles.vue3'

// 链式调用时，直接连续使用 .use() 即可
createApp(App)
	.use(Particles)
	.use(router) // 先使用路由
	.use(ElementPlus) // 再使用 ElementPlus
	.mount('#app') // 最后挂载应用