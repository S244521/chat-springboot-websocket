import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
	baseURL: '/backend', // 基础路径，根据实际后端地址配置
	timeout: 50000
})

// 请求拦截器（统一处理 token 等）
request.interceptors.request.use(
	config => {
		// 从本地存储获取 token（登录后存储）
		const token = localStorage.getItem('token')
		if (token && config.headers) {
			// 统一设置 Authorization 请求头（除登录接口外）
			if (config.url !== '/login') {
				config.headers.Authorization = token
			}
		}
		return config
	},
	error => {
		return Promise.reject(error)
	}
)

// 响应拦截器（统一处理返回结果）
request.interceptors.response.use(
	response => {
		// 假设后端统一返回格式为 { code: 200, data: ..., msg: '' }
		const res = response.data
		if (res.code !== 0) {
			// 错误处理（如弹窗提示）
			console.error(res.msg || '请求失败')
			return Promise.reject(res)
		}
		return res.data
	},
	error => {
		console.error('网络错误:', error.message)
		return Promise.reject(error)
	}
)

// 接口封装
const api = {
	// 登录接口（无 token）
	login: (userData) => {
		return request({
			url: '/user/login',
			method: 'post',
			data: userData // 请求体携带用户信息
		})
	},

	// 查询用户
	selectUser: (pageNum, pageSize, key, shangHeader = '') => {
		return request({
			url: '/user/selectUser', // 后端接口路径
			method: 'get', // 接口是 @GetMapping，对应 get 方法
			params: {
				pageNum, // 分页页码（默认1）
				pageSize, // 每页条数（默认10）
				key // 搜索关键词（可选，默认空）
			},
			headers: {
				// 携带 Shang 请求头（可选，后端 required = false）
				Shang: shangHeader
				// Authorization 头会由 request 拦截器自动添加（无需手动写）
			}
		})
	}

	// 更新用户接口（带 Authorization 头）
	// updateUser: (userData) => {
	//   return request({
	//     url: '/update',
	//     method: 'post',
	//     data: userData // 请求体携带更新信息
	//   })
	// },

	// // 删除用户接口（带 root 头 + 路径参数）
	// deleteUserById: (id, rootToken) => {
	//   return request({
	//     url: '/deleteUserById',
	//     method: 'post',
	//     params: { id }, // url 参数携带 id
	//     headers: { root: rootToken } // 单独设置 root 请求头
	//   })
	// },

	// // 查询接口（带 Authorization 头 + 混合参数）
	// queryFiles: (pageNum, pageSize, fileQuery) => {
	//   return request({
	//     url: '/query',
	//     method: 'post',
	//     params: { pageNum, pageSize }, // url 参数
	//     data: fileQuery // 请求体参数
	//   })
	// },

	// // 文件上传接口（带 Authorization 头 + FormData）
	// uploadChunk: (formData) => {
	//   return request({
	//     url: '/upload',
	//     method: 'post',
	//     data: formData,
	//     headers: {
	//       'Content-Type': 'multipart/form-data' // 表单数据类型
	//     }
	//   })
	// }
}

export default api