<template>
	<div class="inputbox" v-if="showAddChatGroup">
		<!-- 标题和关闭按钮区域 -->
		<div class="title-box">
			<span>创建群聊</span>
			<span class="close-icon" @click="showAddChatGroup = false">✕</span>
		</div>

		<!-- 输入和添加按钮区域 -->
		<div class="input-box">
			<input type="text" v-model="ChatGroupName" placeholder="请输入群聊名称" />
			<button class="confirm-btn" @click="addgroupchat()">添加</button>
		</div>
	</div>

	<div class="self-box" v-if="showChangeSelf">
		<!-- 标题和关闭按钮区域 -->
		<div class="title-box">
			<span>修改个人信息</span>
			<span class="close-icon" @click="showChangeSelf = false">✕</span>
		</div>

		<!-- 输入和添加按钮区域 -->
		<div style="width: 100%;">
			<div class="form-grid">
				<!-- 左侧栏 -->
				<div class="form-column">
					<div>
						<div class="user-box">
							<input type="text" name="reg-username" v-model="username" required>
							<label>用户名</label>
						</div>
						<div class="user-box">
							<input type="text" name="reg-password" v-model="password" required>
							<label>密码（直接修改）</label>
						</div>
					</div>

					<div>
						<div class="user-box">
							<input type="text" name="reg-name" v-model="name" required>
							<label>名称</label>
						</div>
						<div class="user-box user-box-select">
							<select name="gender" v-model="sex" required>
								<!-- 占位符选项 -->
								<option value="2">未知</option>
								<option value="0">男</option>
								<option value="1">女</option>
							</select>
							<label>性别</label>
						</div>
					</div>

				</div>
			</div>
			<div class="btn-box">
				<div style="display: flex;justify-content: space-between;;gap: 30px;">
					<button class="self-btn" @click="logout()">注销</button>
					<button class="self-btn" @click="changeself()">修改</button>
				</div>
			</div>
		</div>
	</div>

	<div class="select-user" v-if="showSelectUser">
		<div class="title-box">
			<span>与他(她)私聊</span>
			<span class="close-icon" @click="showSelectUser = false">✕</span>
		</div>


		<!-- 用户列表 -->
		<div class="user-list">
			<div class="user-item" v-for="user in userPage.records" :key="user.id" @click="handleUserClick(user)">
				<div class="user-info">
					<span class="user-name">{{ user.name }}</span>
					<span class="user-username">({{ user.username }})</span>
				</div>
				<span class="user-sex">
					{{ user.sex === 1 ? '男' : user.sex === 2 ? '女' : '未知' }}
				</span>
			</div>
		</div>

		<!-- 分页控件 -->
		<div class="pagination-box">
			<el-pagination v-model:current-page="userPage.current" v-model:page-size="userPage.size"
				:total="userPage.total" :page-sizes="[5, 10, 15]" layout="total, sizes, prev, pager, next, jumper"
				@size-change="handlePageSizeChange" @current-change="handleCurrentPageChange" />
		</div>
	</div>

	<div class="page-background">
		<!-- 应用主容器 -->
		<div class="app-container">
			<!-- 左侧聊天列表面板 -->
			<aside class="chat-list-panel">
				<div class="search-bar">
					<select v-model="type" required>
						<!-- 占位符选项 -->
						<option value="0">私聊</option>
						<option value="1">群聊</option>
					</select>
					<input type="text" placeholder="搜索..." v-model="key" @focus="showSearchResults = true"
						@blur="hideSearchResults" @input="handleSearch" />
					<span class="search-icon" @click="search()">🔍</span>
					<ul v-if="type==1&&showSearchResults && filteredList.length > 0" class="search-results-list">
						<li v-for="item in filteredList" :key="item.id" @mousedown="handleSearchResultClick(item)">
							{{ item.name }}
						</li>
					</ul>
				</div>
				<ul class="chat-list">
					<li v-for="chat in chatListItems" :key="chat.id" class="chat-list-item"
						:class="{ active: selectedChat && selectedChat.id === chat.id }" @click="selectChat(chat)">
						<!-- <img :src="chat.avatar" alt="avatar" class="avatar" /> -->
						<div class="chat-info">
							<div class="chat-info-header">
								<span class="chat-name">{{ chat.name }}</span>
								<span class="chat-timestamp">{{ chat.timestamp }}</span>
							</div>
							<span class="search-icon" style="font-size: 24px;"
								@click.stop="deleteById(chat.id)" title="退出聊天">🗑</span>
							<!-- <p class="last-message">{{ chat.lastMessage }}</p> -->
						</div>
					</li>
				</ul>
			</aside>

			<!-- 右侧聊天室 -->
			<div class="chat-room-container" v-if="selectedChat">
				<!-- 头部区域 -->
				<header class="chat-header">
					<div class="header-left">
						<!-- 标题现在是动态的 -->
						<div>
							<h1 class="chat-title">{{ selectedChat.name }}</h1>
							<p class="chat-subtitle">{{ selectedChat.id }}</p>
						</div>

						<div class="chat-box">
							<span class="search-icon" style="font-size: 22px;color: aqua;margin-right: 5px;" @click="gotoroot" title="获取管理员权限" >♜</span>
							<span class="search-icon" style="font-size: 20px;color: aqua;margin-right: 8px;" @click="showAddChatGroup=true" title="创建群聊">✚</span>
							<span class="search-icon" style="font-size: 20px;color: aqua;" @click="showChangeSelf=true" title="修改个人信息">帥</span>
						</div>
					</div>
				</header>

				<!-- 消息显示区域 -->
				<main class="chat-messages">
					<div v-for="message in messages" :key="message.id" class="message-wrapper"
						:class="{ 'sent': myuser && myuser.name === message.sender, 'received': myuser && myuser.name !== message.sender }">
						<!-- <div v-for="message in messages" :key="message.id" class="message-wrapper" :class="{ 'sent': myuser && myuser.name === message.sender }"> -->
						<div class="message-content">
							<div class="message-sender">{{ message.sender }}</div>
							<div class="message-bubble">
								{{ message.text }}
							</div>
							<div class="message-timestamp">{{ message.timestamp }}</div>
						</div>
					</div>
				</main>

				<!-- 底部输入区域 -->
				<footer class="chat-footer">
					<input type="text" class="message-input" v-model="messageInput" placeholder="请输入消息..."
						@keyup.enter="sendMessage" />
					<span class="icon" @click="gotofile" title="文件上传">📎</span>
					<button class="send-button" @click="sendMessage">发送</button>
				</footer>

				<!-- 附件弹出窗口 -->
				<div v-if="showAttachments" class="attachments-popup">
					<div class="popup-header">
						<span class="popup-title">本地文件</span>
						<span class="popup-close" @click="showAttachments = false">×</span>
					</div>
					<div class="popup-grid">
						<div class="grid-item active">📄</div>
						<div class="grid-item">📁</div>
						<div class="grid-item">🖼️</div>
						<div class="grid-item">🎵</div>
						<div class="grid-item">📹</div>
						<div class="grid-item">✉️</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		onMounted,
		watch
	} from 'vue';
	import api from '../util/request';
	import {
		useRouter
	} from 'vue-router';
	import {
		ElPagination
	} from 'element-plus'; // 引入分页组件
	import {
		ElMessage
	} from 'element-plus';

	const router = useRouter(); // 创建路由实例

	// --- 状态管理 ---
	const showAttachments = ref(false);
	const showAddChatGroup = ref(false);
	const showChangeSelf = ref(false);
	const showSearchResults = ref(false); // 新增：控制搜索结果下拉列表的显示
	const showSelectUser = ref(false);


	const chatListItems = ref([]); // 模拟的聊天列表数据
	const key = ref(""); // 实时查询关键词
	const RealTimeList = ref([]); // 实时查询本地数据
	const filteredList = ref([]); // 新增：用于存放过滤后的搜索结果
	const ChatGroupName = ref(""); //  群聊名称
	const username = ref("");
	const password = ref("");
	const name = ref("");
	const sex = ref();
	const type = ref(1);
	const userPage = ref({
		records: [], // 初始化为空数组，避免 v-for 报错
		total: 0,
		size: 10,
		current: 1,
		pages: 0
	}); // 私聊查询的用户列表
	const selectedChat = ref(null); // 初始值设为 null
	const messages = ref([]); // 消息列表
	const messageInput = ref(""); // 用于绑定输入框的消息内容
	const ws = ref(null); // 用于持有 WebSocket 实例
	const myuser = ref(); // 用户信息

	//*******************websocket***********************//

	// 监听 selectedChat 的变化，以便在聊天切换时重新连接 WebSocket
	watch(selectedChat, (newChat, oldChat) => {
		// 如果旧的聊天存在，并且ws连接也存在，则断开旧的连接
		if (oldChat && ws.value) {
			ws.value.close();
			console.log(`已断开与聊天 [${oldChat.id}] 的 WebSocket 连接`);
		}

		// 如果选择了新的聊天
		if (newChat) {
			// 清空旧的消息列表
			messages.value = [];
			// 连接新的 WebSocket
			connectWebSocket(newChat.id);
		}
	});

	// WebSocket 连接函数
	const connectWebSocket = (chatId) => {
		// 确保 chatId 有效
		if (!chatId) return;

		// 请将 "localhost:8080" 替换为你的后端服务器地址和端口
		const wsUrl = `ws://localhost:9999/ws/${chatId}`;
		ws.value = new WebSocket(wsUrl);

		ws.value.onopen = () => {
			console.log(`成功连接到聊天 [${chatId}] 的 WebSocket 服务器`);
			// 可以在这里发送一条 "加入" 消息或获取历史消息
		};

		ws.value.onmessage = (event) => {
			console.log(`收到来自 [${chatId}] 的消息:`, event.data);
			// 这里假设后端发送的是 JSON 格式的消息
			// { id: ..., type: 'received', sender: '...', text: '...', avatar: '...', timestamp: '...' }
			// 你需要根据后端实际返回的数据格式来解析
			const receivedMessage = JSON.parse(event.data);
			messages.value.push(receivedMessage);
		};

		ws.value.onclose = () => {
			console.log(`与聊天 [${chatId}] 的 WebSocket 连接已关闭`);
		};

		ws.value.onerror = (error) => {
			console.error(`WebSocket 连接 [${chatId}] 出错:`, error);
		};
	};

	const formatCurrentTime = () => {
		const now = new Date();
		const year = now.getFullYear();
		const month = String(now.getMonth() + 1).padStart(2, '0');
		const day = String(now.getDate()).padStart(2, '0');
		const hours = String(now.getHours()).padStart(2, '0');
		const minutes = String(now.getMinutes()).padStart(2, '0');

		return `${year}/${month}/${day} ${hours}:${minutes}`;
	}

	// 发送消息的函数
	const sendMessage = () => {
		if (ws.value && ws.value.readyState === WebSocket.OPEN && messageInput.value.trim() !== '') {
			// 你需要构建一个消息对象，与后端和你自己的消息展示格式对齐
			// 例如，从 sessionStorage 获取当前用户信息来填充 sender 等字段
			const user = JSON.parse(sessionStorage.getItem("user"));
			const messageToSend = {
				sender: user.name, // 发送者名称
				text: messageInput.value,
				timestamp: formatCurrentTime()
			};
			api({
				url: '/history/addHistory',
				method: 'post',
				data: {
					conversationid: selectedChat.value.id,
					text: messageInput.value
				}
			}).then(response => {
				console.log(response)
				ws.value.send(JSON.stringify(messageToSend));
				// 清空输入框
				messageInput.value = "";
			}).catch(error => {
				// 失败处理
				ElMessage.error('发送消息失败:' + JSON.stringify(error))
			})

		} else {
			console.log("WebSocket 未连接或消息为空");
			ElMessage.warning('连接已断开或消息不能为空！');
		}
	};





	//********************************常规函数*****************************//

	const gotoroot = () => {
		router.push('/Root');
	}
	
	// 跳转file页面
	const gotofile = () => {
		router.push('/File');
	}

	// 点击用户项触发
	const handleUserClick = (user) => {
		// alert(`选中用户：\nID: ${user.id}\n姓名: ${user.name}\n用户名: ${user.username}`);
		// 可选：关闭弹窗或其他操作
		api({
			url: '/conversation/create',
			method: 'post',
			data: {
				type: 0,
				conversation: user.id
			}
		}).then(response => {
			console.log(response)
			initchatlist();
			showSelectUser.value = false;
		}).catch(error => {
			// 失败处理
			console.error('添加私聊失败:', error)
		})
	};

	// 每页条数改变时触发
	const handlePageSizeChange = (size) => {
		userPage.value.size = size;
		userPage.value.current = 1; // 重置为第一页
		loadUserData(1, size, key.value); // 重新加载数据（key根据实际场景传入）
	};

	// 当前页码改变时触发
	const handleCurrentPageChange = (page) => {
		userPage.value.current = page;
		loadUserData(page, userPage.value.size, key.value); // 重新加载数据
	};

	// 请求后端的私聊用户
	const loadUserData = (pageNum, pageSize, key) => {
		// 查询用户接口查到用创建会话接口
		api({
			url: '/user/selectUser',
			method: 'get',
			params: {
				pageNum: pageNum,
				pageSize: pageSize,
				key: key
			}
		}).then(response => {
			console.log(response)
			userPage.value = response;
			showSelectUser.value = true;
		}).catch(error => {
			// 失败处理
			console.error('查询用户:', error)
		})
	}


	// 实时搜索逻辑 现在会同时匹配 name 和 id
	const handleSearch = () => {
		if (key.value.trim() !== '') {
			const searchTerm = key.value.toLowerCase().trim();
			filteredList.value = RealTimeList.value.filter(item =>
				item.name.toLowerCase().includes(searchTerm) ||
				(item.id && item.id.toLowerCase().includes(searchTerm))
			);
		} else {
			filteredList.value = []; // 如果没有输入，则清空列表
		}
	};

	//点击搜索结果项的处理函数
	const handleSearchResultClick = (item) => {
		key.value = item.id; // 将id的值赋给key
		showSearchResults.value = false; // 隐藏下拉列表
	};

	// 隐藏搜索结果，并稍作延迟以允许点击事件触发
	const hideSearchResults = () => {
		setTimeout(() => {
			showSearchResults.value = false;
		}, 200); // 延迟200毫秒
	};

	// 初始化用户信息
	const inituser = () => {
		let user = sessionStorage.getItem("user");
		try {
			user = JSON.parse(user);
			username.value = user.username;
			name.value = user.name;
			sex.value = user.sex;
			console.log("user: " + username.value + name.value + sex.value);
			myuser.value = user;
		} catch (e) {
			console.error("解析 user 失败：", e);
		}
	}

	// 初始化实时数据
	const initrealtime = () => {
		api({
			url: '/conversation/realtime',
			method: 'get'
		}).then(response => {
			RealTimeList.value = response;
			console.log(RealTimeList.value)
		}).catch(error => {
			// 失败处理
			console.error('获取实时查询数据失败:', error)
		})
	}

	// 初始化聊天列表, 初始化实时查询数据
	const initchatlist = () => {
		api({
			url: '/conversation/getself',
			method: 'get'
		}).then(response => {
			console.log(1)
			chatListItems.value = response;

			console.log(chatListItems.value)
		}).catch(error => {
			// 失败处理
			console.error('获取聊天列表失败:', error)
		})
	}

	// 点击切换聊天的函数
	const selectChat = (chat) => {
		selectedChat.value = chat;
		// 获取会话的历史数据
		api({
			url: '/history/getHistoryByConversationId',
			method: 'post',
			data: {
				conversationId: chat.id
			}
		}).then(response => {
			// console.log("获取历史聊天记录");
			// console.log(response)
			response.forEach(item => {
				const messageinit = {
					sender: item.username,
					text: item.text,
					timestamp: item.createtime
				}
				// console.log(messageinit);
				messages.value.push(messageinit);
			})
		}).catch(error => {
			// 失败处理
			ElMessage.error('获取历史聊天记录失败:' + JSON.stringify(error))
		})
	};

	// 退出会话
	const deleteById = (id) => {
		api({
			url: '/conversation/leave',
			method: 'post',
			params: {
				id: id
			}
		}).then(response => {
			console.log(response)
			initchatlist();
		}).catch(error => {
			// 失败处理
			ElMessage.error('删除会话失败:' + JSON.stringify(error))
		})
	}

	// 添加群聊
	const addgroupchat = () => {
		api({
			url: '/conversation/create',
			method: 'post',
			data: {
				name: ChatGroupName.value,
				type: 1,
				conversation: ""
			}
		}).then(response => {
			console.log(response)
			showAddChatGroup.value = false;
			// 创建成功的弹窗
			ElMessage.success('创建群聊成功');
			// 重新获取数据
			initchatlist();
			initrealtime();
		}).catch(error => {
			// 失败处理
			ElMessage.error('创建群聊失败:' + JSON.stringify(error))
		})
	}

	// 实时查询
	const search = () => {
		console.log(type.value);
		if (type.value == 1) {
			// 用加入会话接口
			api({
				url: '/conversation/join',
				method: 'post',
				params: {
					id: key.value
				}
			}).then(response => {
				ElMessage.success(response)
				initchatlist();
			}).catch(error => {
				// 失败处理
				ElMessage.error('加入群聊失败:' + JSON.stringify(error))
			})
		} else if (type.value == 0) {
			// 查询用户接口查到用创建会话接口
			loadUserData(1, 10, key.value);
		}
	}

	//修改个人信息
	const changeself = () => {
		api({
			url: '/user/update',
			method: 'post',
			data: {
				username: username.value,
				name: name.value,
				sex: sex.value
			}
		}).then(response => {
			console.log(response)
			showChangeSelf.value = false;
			if (response.token) {
				sessionStorage.setItem("user", JSON.stringify(response));
				localStorage.setItem('token', "Bearer " + response.token)
				ElMessage.success("修改个人信息成功")
				router.push('/Chat');
			}
		}).catch(error => {
			// 失败处理
			ElMessage.error('修改信息失败:' + JSON.stringify(error))
		})
	}

	// 注销账号
	const logout = () => {
		// 显示确认弹窗，用户点击“确定”返回 true，“取消”返回 false
		if (confirm('确定要注销账号吗？')) {
			api({
				url: '/user/logout',
				method: 'get'
			}).then(response => {
				console.log(response)
				sessionStorage.removeItem("user");
				localStorage.removeItem("token");
				router.push('/');
			}).catch(error => {
				console.error('注销失败:', error)
			})
		} else {
			// 用户取消注销，可添加提示或不处理
			console.log('已取消注销');
		}
	};

	// 注册 mounted 钩子，DOM 挂载后自动执行
	onMounted(() => {
		inituser();
		initrealtime();
		initchatlist();
	});
</script>

<style scoped>
	/* 添加群聊样式 */
	@import url("../css/components-chat/chat-groupbox.css");
	/* 修改个人信息样式 */
	@import url("../css/components-chat/chat-selfbox.css");
	/* 实时搜素样式 */
	@import url("../css/components-chat/chat-realtime.css");
	/* 选择用户私聊样式 */
	@import url("../css/components-chat/chat-selectuser.css");
	/* 消息列表样式 */
	@import url("../css/components-chat/chat-main.css");

	/* 定义辉光颜色变量 */
	:root {
		--glow-green: #00ff9c;
		--glow-cyan: #00e0ff;
		--glow-blue: #007bff;
	}




	/* 页面背景 */
	.page-background {
		position: fixed;
		top: 0;
		left: 0;
		width: 100vw;
		height: 100vh;
		background-image: url('https://source.unsplash.com/random/1600x900?cyberpunk,city');
		background-size: cover;
		display: flex;
		justify-content: center;
		align-items: center;
		font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
	}

	/* 应用主容器 - 包含列表和聊天室 */
	.app-container {
		width: 900px;
		height: 600px;
		background: rgba(10, 20, 20, 0.6);
		backdrop-filter: blur(15px);
		-webkit-backdrop-filter: blur(15px);
		border-radius: 15px;
		border: 1px solid rgba(0, 255, 156, 0.2);
		box-shadow: 0 0 10px rgba(0, 255, 156, 0.3), 0 0 20px rgba(0, 255, 156, 0.2);
		display: flex;
		color: #e0e0e0;
		overflow: hidden;
	}

	/* --- 左侧聊天列表面板 --- */
	.chat-list-panel {
		width: 240px;
		flex-shrink: 0;
		border-right: 1px solid rgba(0, 224, 255, 0.2);
		box-shadow: 5px 0 15px -5px rgba(0, 224, 255, 0.1);
		display: flex;
		flex-direction: column;
	}

	/* 搜索栏容器 - 调整为相对定位以容纳搜索结果 */
	.search-bar {
		padding: 12px 16px;
		/* 优化内边距，减少上下空间 */
		display: flex;
		align-items: center;
		gap: 8px;
		/* 缩小元素间距，更紧凑 */
		border-bottom: 1px solid rgba(0, 224, 255, 0.2);
		position: relative;
		/* 为搜索结果定位 */
	}

	/* 聊天类型选择框 */
	.search-bar select {
		min-width: 55px;
		/* 固定最小宽度，避免内容撑开 */
		padding: 6px 8px;
		/* 调整内边距 */
		background: rgba(0, 224, 255, 0.05);
		border: 1px solid rgba(0, 224, 255, 0.3);
		border-radius: 6px;
		/* 略小的圆角，更精致 */
		color: #fff;
		outline: none;
		cursor: pointer;
		font-size: 13px;
		/* 调整字体大小 */
		appearance: none;
		/* 去除默认箭头 */
		background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='rgba(0,224,255,0.7)' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
		background-repeat: no-repeat;
		background-position: right 6px center;
		background-size: 12px;
		transition: border-color 0.2s;
	}

	.search-bar select:hover,
	.search-bar select:focus {
		border-color: rgba(0, 224, 255, 0.7);
		/* 聚焦时高亮边框 */
	}

	.search-bar select option {
		background-color: rgba(6, 18, 12, 1.0);
		color: #ffffff;
		/* 选项文字色 */
		padding: 6px 10px;
		/* 选项内边距 */
	}

	.search-bar select option:checked {
		background-color: rgba(6, 18, 12, 1.0);
		color: #fff;
	}



	.search-icon {
		color: var(--glow-cyan);
		cursor: pointer;
		font-size: 18px;
		/* 调整图标大小 */
		flex-shrink: 0;
		/* 防止图标被压缩 */
		transition: transform 0.2s;
	}

	.search-bar input {
		width: 100%;
		background: rgba(0, 224, 255, 0.05);
		border: 1px solid rgba(0, 224, 255, 0.3);
		border-radius: 8px;
		padding: 8px 12px;
		color: #fff;
		outline: none;
	}

	.search-bar input::placeholder {
		color: #888;
	}

	.chat-list {
		list-style: none;
		padding: 0;
		margin: 0;
		overflow-y: auto;
		flex-grow: 1;
	}

	.chat-list-item {
		display: flex;
		align-items: center;
		padding: 15px 20px;
		cursor: pointer;
		transition: background-color 0.3s ease;
		border-bottom: 1px solid rgba(255, 255, 255, 0.05);
	}

	.chat-list-item:hover {
		background-color: rgba(0, 224, 255, 0.1);
	}

	.chat-list-item.active {
		background-color: rgba(0, 224, 255, 0.2);
		border-left: 3px solid var(--glow-cyan);
		padding-left: 17px;
	}

	.chat-info {
		width: 100%;
		display: flex;
		justify-content: space-between;
		overflow: hidden;
	}

	.chat-info-header {
		width: 70%;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.chat-name {
		font-weight: bold;
		color: #fff;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.chat-timestamp {
		font-size: 12px;
		color: #888;
	}

	.last-message {
		font-size: 14px;
		color: #aaa;
		margin: 5px 0 0;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	/* --- 右侧聊天室 --- */
	.chat-room-container {
		flex-grow: 1;
		display: flex;
		flex-direction: column;
		position: relative;
		/* 用于附件弹窗定位 */
	}

	/* --- 头部 --- */
	.chat-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 15px 25px;
		border-bottom: 1px solid rgba(0, 255, 156, 0.1);
		flex-shrink: 0;
	}

	.header-left {
		width: 100%;
		display: flex;
		justify-content: space-between;
	}

	.chat-box {
		display: flex;
		font-size: 30px;
	}

	.chat-title {
		color: var(--glow-green);
		font-size: 20px;
		font-weight: bold;
		text-shadow: 0 0 5px var(--glow-green), 0 0 10px var(--glow-green);
		margin: 0;
	}

	.chat-subtitle {
		font-size: 12px;
		color: #aaa;
		margin: 2px 0 0;
		text-align: left;
		/* 新增属性，强制文字左对齐 */
	}

	.icon {
		font-size: 20px;
		cursor: pointer;
		color: var(--glow-cyan);
		text-shadow: 0 0 5px var(--glow-cyan);
		transition: all 0.3s ease;
	}

	.icon:hover {
		transform: scale(1.1);
		text-shadow: 0 0 8px var(--glow-cyan), 0 0 15px var(--glow-cyan);
	}



	/* --- 底部输入区 (样式与之前版本相同) --- */
	.chat-footer {
		display: flex;
		align-items: center;
		gap: 15px;
		padding: 15px 45px;
		border-top: 1px solid rgba(0, 255, 156, 0.1);
		flex-shrink: 0;
	}

	.message-input {
		flex-grow: 1;
		background: transparent;
		border: 1px solid var(--glow-cyan);
		border-radius: 20px;
		padding: 10px 15px;
		color: #fff;
		font-size: 14px;
		outline: none;
		box-shadow: 0 0px 10px rgba(155, 255, 255, 1.0), inset 0 0 10px rgba(115, 248, 255, 1.0);
		border: 2px solid rgba(0, 255, 255, 1);
		transition: box-shadow 0.3s ease;
	}

	.message-input::placeholder {
		color: #888;
	}

	.message-input:focus {
		box-shadow: 0 0 8px rgba(0, 224, 255, 0.8), inset 0 0 8px rgba(0, 224, 255, 0.5);
	}

	.send-button {
		color: #ffffff;
		width: 10%;
		height: 100%;
		border-radius: 12px;
		/* 增加圆角弧度，更柔和 */
		background: linear-gradient(135deg, #03a7ff 0%, #0077e6 100%);
		/* 渐变背景，提升质感 */
		border: none;
		/* 去掉默认边框 */
		font-size: 14px;
		font-weight: 500;
		cursor: pointer;
		transition: all 0.3s ease;
		/* 统一过渡动画，让交互更丝滑 */
		box-shadow: 0 4px 8px rgba(3, 167, 255, 0.3);
		/* 初始阴影，增强立体感 */
	}

	.send-button:hover {
		box-shadow: 0 0 15px #7bdcff, 0 4px 12px rgba(3, 167, 255, 0.5);
		/* 复合阴影，hover时更醒目 */
		transform: translateY(-2px);
		/* 轻微上浮，增强交互反馈 */
		background: linear-gradient(135deg, #00c2ff 0%, #0088ff 100%);
		/* hover时渐变加深 */
	}

	.send-button:active {
		transform: translateY(0);
		/* 点击时还原位置 */
		box-shadow: 0 2px 4px rgba(3, 167, 255, 0.3);
		/* 点击时阴影缩小，模拟按压感 */
	}

	/* --- 附件弹窗 (样式与之前版本相同) --- */
	.attachments-popup {
		position: absolute;
		bottom: 85px;
		right: 80px;
		width: 240px;
		padding: 10px;
		background: rgba(20, 35, 35, 0.7);
		backdrop-filter: blur(10px);
		-webkit-backdrop-filter: blur(10px);
		border-radius: 10px;
		border: 1px solid rgba(0, 224, 255, 0.3);
		box-shadow: 0 0 10px rgba(0, 224, 255, 0.3);
		z-index: 10;
	}

	.popup-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 5px;
		color: #eee;
	}

	.popup-title {
		font-weight: bold;
	}

	.popup-close {
		cursor: pointer;
		font-size: 20px;
	}

	.popup-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 10px;
		padding: 10px 5px;
	}

	.grid-item {
		background-color: rgba(0, 224, 255, 0.1);
		border: 1px solid rgba(0, 224, 255, 0.2);
		height: 50px;
		border-radius: 8px;
		display: flex;
		justify-content: center;
		align-items: center;
		font-size: 24px;
		cursor: pointer;
		transition: all 0.3s ease;
	}

	.grid-item:hover {
		background-color: rgba(0, 224, 255, 0.2);
		border-color: rgba(0, 224, 255, 0.5);
	}

	.grid-item.active {
		background-color: rgba(0, 224, 255, 0.3);
		border-color: var(--glow-cyan);
		box-shadow: 0 0 8px rgba(0, 224, 255, 0.7);
	}
</style>