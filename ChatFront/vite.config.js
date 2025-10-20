import {
	defineConfig
} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
	plugins: [vue()],
	server: {
		// 配置跨域代理
		proxy: {
			'/backend':{
				target:'http://localhost:9999',
				changeOrigin:true,
				rewrite: (path) => {
					return path.replace(/^\/backend/,'')
				}
			}
		}
	}
})