<template>
	<div class="page-background">
		<!-- 应用主容器 -->
		<div class="app-container">
			<!-- 左侧聊天列表面板 -->
			<aside class="chat-list-panel">
				<div class="search-bar">
					<span class="search-icon" @click="search()">🔍</span>
					<input type="text" placeholder="搜索..." v-model="key"/>
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
							<span class="search-icon" style="font-size: 24px;" @click.stop="deleteById(chat.id)">🗑</span>	
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
							<p class="chat-subtitle">{{ selectedChat.members }}</p>
						</div>
					</div>
				</header>

				<!-- 消息显示区域 -->
				<main class="chat-messages">
					<div class="time-separator">2024.05.26</div>
					<div v-for="message in messages" :key="message.id" class="message-wrapper" :class="message.type">
						<img :src="message.avatar" alt="avatar" class="avatar" />
						<div class="message-content">
							<div v-if="message.type === 'received'" class="message-sender">{{ message.sender }}</div>
							<div class="message-bubble">
								<p>{{ message.text }}</p>
							</div>
							<div class="message-timestamp">{{ message.timestamp }}</div>
						</div>
					</div>
				</main>

				<!-- 底部输入区域 -->
				<footer class="chat-footer">
					<input type="text" class="message-input" placeholder="请输入消息..." />
					<span class="icon" @click="showAttachments = !showAttachments">📎</span>
					<button class="send-button">发送</button>
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
		onMounted
	} from 'vue';
	import api from '../util/request';
	import {
		useRouter
	} from 'vue-router';
	
	
	// --- 状态管理 ---
	const showAttachments = ref(false);
	
	// 模拟的聊天列表数据
	const chatListItems = ref([{
			id: 1,
			name: '聊天聊天室',
			avatar: 'https://i.pravatar.cc/40?u=group1',
			lastMessage: '自己 fonnan mestag...',
			timestamp: '9:05',
			members: '聊天室 成员数'
		}
	]);
	const key=ref("");
	
	// 模拟的聊天消息数据 (实际项目中应根据 selectedChat 动态加载)
	const messages = ref([{
			id: 1,
			type: 'sent',
			sender: 'Me',
			text: '自己 fonnan mestag...',
			avatar: 'https://i.pravatar.cc/40?u=a',
			timestamp: '9:05 SS'
		},
		{
			id: 2,
			type: 'received',
			sender: '眠呢',
			text: '收到的消息示例',
			avatar: 'https://i.pravatar.cc/40?u=b',
			timestamp: '2024 65:25'
		},
		{
			id: 3,
			type: 'received',
			sender: '咱人',
			text: '这是另一条收到的消息。',
			avatar: 'https://i.pravatar.cc/40?u=c',
			timestamp: '2024 45-35'
		},
		{
			id: 4,
			type: 'sent',
			sender: 'Me',
			text: 'Withla mestag...',
			avatar: 'https://i.pravatar.cc/40?u=a',
			timestamp: '9:05 SS'
		},
		{
			id: 5,
			type: 'received',
			sender: '咱人',
			text: '你好！Vue 3 真棒！',
			avatar: 'https://i.pravatar.cc/40?u=d',
			timestamp: '2024 45:45'
		},
	]);
	
	
	
	



	// 初始化聊天列表，TODO 初始化实时查询数据
	const chatlist = () => {
		api({
			url: '/conversation/getself',
			method: 'get'
		}).then(response => {
			console.log(response)
			chatListItems.value=response;
			console.log(chatListItems.value)
		}).catch(error => {
			// 失败处理
			console.error('获取聊天列表失败:', error)
			alert('获取聊天列表失败: ' + (error.msg || error.message || '未知错误'))
		})
	}

	// 当前选中的聊天，默认为第一个
	const selectedChat = ref(chatListItems.value[0]);

	// 点击切换聊天的函数
	const selectChat = (chat) => {
		selectedChat.value = chat;
	};
	
	// 退出会话
	const deleteById = (id) => {
		api({
			url: '/conversation/leave',
			method: 'post',
			params:{
				id:id
			}
		}).then(response => {
			console.log(response)
		}).catch(error => {
			// 失败处理
			console.error('删除会话失败:', error)
			alert('删除会话失败: ' + (error.msg || error.message || '未知错误'))
		})
	}

	// 实时查询
	const search =()=> {
		alert(key.value)
	}
	
	
	// 注册 mounted 钩子，DOM 挂载后自动执行
	onMounted(chatlist);
</script>

<style scoped>
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

	.search-bar {
		padding: 20px;
		display: flex;
		align-items: center;
		gap: 10px;
		border-bottom: 1px solid rgba(0, 224, 255, 0.2);
	}

	.search-icon {
		color: var(--glow-cyan);
		cursor: pointer;
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

	.chat-list-item .avatar {
		width: 50px;
		height: 50px;
		border-radius: 50%;
		margin-right: 15px;
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

	/* .header-left {background-color: #007bff;} */
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

	/* --- 消息区域 (样式与之前版本相同) --- */
	.chat-messages {
		flex-grow: 1;
		padding: 20px;
		overflow-y: auto;
		display: flex;
		flex-direction: column;
		gap: 20px;
	}

	.chat-messages::-webkit-scrollbar {
		width: 6px;
	}

	.chat-messages::-webkit-scrollbar-track {
		background: transparent;
	}

	.chat-messages::-webkit-scrollbar-thumb {
		background-color: var(--glow-cyan);
		border-radius: 10px;
	}

	.time-separator {
		text-align: center;
		font-size: 12px;
		color: #888;
		margin-bottom: 10px;
	}

	.message-wrapper {
		display: flex;
		gap: 10px;
		max-width: 70%;
	}

	.avatar {
		width: 40px;
		height: 40px;
		border-radius: 50%;
		border: 1px solid rgba(0, 255, 156, 0.3);
	}

	.message-content {
		display: flex;
		flex-direction: column;
	}

	.message-bubble {
		padding: 10px 15px;
		border-radius: 15px;
		font-size: 14px;
	}

	.message-sender {
		font-size: 13px;
		color: #ccc;
		margin-bottom: 5px;
	}

	.message-timestamp {
		font-size: 10px;
		color: #777;
		margin-top: 5px;
	}

	.message-wrapper.received {
		align-self: flex-start;
	}

	.message-wrapper.received .message-bubble {
		background-color: rgba(30, 45, 40, 0.8);
		border-top-left-radius: 0;
	}

	.message-wrapper.received .message-timestamp {
		align-self: flex-start;
	}

	.message-wrapper.sent {
		align-self: flex-end;
		flex-direction: row-reverse;
	}

	.message-wrapper.sent .message-content {
		align-items: flex-end;
	}

	.message-wrapper.sent .message-bubble {
		background-color: var(--glow-green);
		color: #000;
		font-weight: 500;
		border-top-right-radius: 0;
		box-shadow: 0 0 8px var(--glow-green), 0 0 15px rgba(0, 255, 156, 0.7);
	}

	.message-wrapper.sent .message-timestamp {
		align-self: flex-end;
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
		background-color: #03a7ff;
	}

	.send-button:hover {
		/* transform: scale(1.05); */
		box-shadow: 0 0 12px #00ff9c;
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