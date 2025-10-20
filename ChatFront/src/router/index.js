import {
	createRouter,
	createWebHistory
} from 'vue-router'
// 导入页面组件（后续创建）
import Login from '../page/login.vue'
import Chat from '../page/chat.vue'
import File from '../page/file.vue'
import Root from '../page/root.vue'

const routes = [{
		path: '/',
		name: 'Login',
		component: Login
	},
	{
		path: '/Chat',
		name: 'Chat',
		component: Chat
	},
	{
		path: '/File',
		name: 'File',
		component: File
	},
	{
		path: '/Root',
		name: 'Root',
		component: Root
	},
]

const router = createRouter({
	history: createWebHistory(),
	routes
})

// 全局前置守卫：路由跳转前拦截
router.beforeEach((to, from, next) => {
	// 1. 获取登录状态（优先从 sessionStorage 取用户信息，或直接取 localStorage 的 token）
	const userStr = sessionStorage.getItem("user");
	const hasLogin = !!userStr; // 转成布尔值：有值则为 true（已登录），无值则为 false（未登录）
	const rootStr = sessionStorage.getItem("root");
	// 2. 判断路由：未登录且要去的不是登录页 → 拦截到登录页（/）
	if (!hasLogin && to.path !== '/') {
		next('/'); // 强制跳转到登录页
		return;
	}

	// 3. 已登录，但要访问 /Root 页面 → 校验 shang 权限
	if (to.path === '/Root') {
		if (rootStr!=null&&rootStr.length>20) {// TODO 目前不定长没找到核对方法
			next(); // 有权限，允许进入
		} else {
			// 无权限，可跳回首页或提示
			next('/Chat'); // 或 next('/') 跳回登录页
			alert('无权限访问该页面（需root权限）');
		}
		return; // 终止后续逻辑
	}

	// 4. 其他情况（已登录访问非Root页，或未登录访问登录页）→ 正常放行
	next();
})

export default router