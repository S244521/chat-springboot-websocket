<!-- src/components/LoginCard.vue -->
<template>
	<div class="login-box">
		<!-- 标题根据当前视图动态切换 -->
		<h2>{{ isLoginView ? '登录' : '注册' }}</h2>
		<form>
			<!-- 登录表单 -->
			<div v-if="isLoginView">
				<div class="user-box">
					<input type="text" name="username" v-model="loginusername" required="">
					<label>用户名</label>
				</div>
				<div class="user-box">
					<input type="password" name="password" v-model="loginpassword" required="">
					<label>密码</label>
				</div>
			</div>

			<!-- 注册表单 -->
			<div v-else>
				<div class="form-grid">
					<!-- 左侧栏 -->
					<div class="form-column">
						<div class="user-box">
							<input type="text" name="reg-username" v-model="signusername" required>
							<label>用户名</label>
						</div>
						<div class="user-box">
							<input type="password" name="reg-password" v-model="signpassword" required>
							<label>设置密码</label>
						</div>
					</div>
					<!-- 右侧栏 -->
					<div class="form-column">
						<div class="user-box">
							<input type="text" name="reg-name" v-model="signname" required>
							<label>名称</label>
						</div>
						<div class="user-box user-box-select">
							<select name="gender" v-model="signsex" required>
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

			<!-- 提交按钮 -->
			<div class="submit" @click="request(isLoginView)">
				{{ isLoginView ? '登录' : '注册' }}
			</div>

			<!-- 切换链接 -->
			<a href="#" class="register-link" @click.prevent="toggleView">
				{{ isLoginView ? '没有账户？去注册' : '已有账户？去登录' }}
			</a>
		</form>
	</div>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import api from '../util/request';
	import {
		useRouter
	} from 'vue-router';

	// 创建路由实例
	const router = useRouter();

	const loginusername = ref('');
	const loginpassword = ref('');

	const signusername = ref('');
	const signpassword = ref('');
	const signname = ref('');
	const signsex = ref();

	// true 表示显示登录视图, false 表示显示注册视图
	const isLoginView = ref(true);

	// 定义一个方法来切换视图
	const toggleView = () => {
		isLoginView.value = !isLoginView.value;
	};


	const request = (isLoginView) => {
		if (isLoginView) {
			login();
		} else {
			sign();
		}
	}

	const login = () => {
		api({
				url: '/user/login',
				method: 'post',
				data: {
					username: loginusername.value,
					password: loginpassword.value
				}
			}).then(response => {
				// 成功处理
				console.log('登录成功:', response)
				if (response.token) {
					sessionStorage.setItem("user", response);
					localStorage.setItem('token', "Bearer " + response.token)
					router.push('/Chat');
				}
			})
			.catch(error => {
				// 失败处理
				console.error('登录失败:', error)
				alert('登录失败: ' + (error.msg || error.message || '未知错误'))
			})
	}
	const sign = () => {
		api({
				url: '/user/sign',
				method: 'post',
				data: {
					username: signusername.value,
					password: signpassword.value,
					name: signname.value,
					sex: signsex.value
				}
			}).then(response => {
				// 成功处理
				console.log('注册成功:', response)
				if (response.token) {
					sessionStorage.setItem("user", response);
					localStorage.setItem('token', "Bearer " + response.token)
					router.push('/Chat');
				}
			})
			.catch(error => {
				// 失败处理
				console.error('注册失败:', error)
				alert('注册失败: ' + (error.msg || error.message || '未知错误'))
			})
	}
</script>

<style scoped>
	/* 样式与之前版本完全相同，无需任何修改 */
	.login-box {
		position: absolute;
		top: 50%;
		left: 50%;
		width: 450px;
		padding: 40px;
		transform: translate(-50%, -50%);
		background: rgba(0, 0, 0, .5);
		box-sizing: border-box;
		box-shadow: 0 15px 25px rgba(0, 0, 0, .6);
		border-radius: 10px;
	}

	.login-box h2 {
		margin: 0 0 30px;
		padding: 0;
		color: #fff;
		text-align: center;
	}

	.login-box .user-box {
		position: relative;
	}

	.login-box .user-box input {
		width: 100%;
		padding: 10px 0;
		font-size: 16px;
		color: #fff;
		margin-bottom: 30px;
		border: none;
		border-bottom: 1px solid #fff;
		outline: none;
		background: transparent;
	}

	.login-box .user-box label {
		position: absolute;
		top: 0;
		left: 0;
		padding: 10px 0;
		font-size: 16px;
		color: #fff;
		pointer-events: none;
		transition: .5s;
	}

	.login-box .user-box input:focus~label,
	.login-box .user-box input:valid~label {
		top: -20px;
		left: 0;
		color: #03e9f4;
		font-size: 12px;
	}

	/* 注册表单网格布局 */
	.form-grid {
		display: flex;
		gap: 20px;
		/* 两列之间的间距 */
	}

	.form-column {
		flex: 1;
		/* 每列占据一半的空间 */
	}

	/* 为 select 元素定制样式，使其与 input 风格一致 */
	.login-box .user-box select {
		width: 100%;
		padding: 10px 0;
		font-size: 16px;
		color: #fff;
		margin-bottom: 30px;
		border: none;
		border-bottom: 1px solid #fff;
		outline: none;
		background: transparent;
		-webkit-appearance: none;
		/* 移除 Chrome/Safari 默认样式 */
		-moz-appearance: none;
		/* 移除 Firefox 默认样式 */
		appearance: none;
		/* 移除标准浏览器默认样式 */
		cursor: pointer;
	}

	/* 下拉选项的样式 */
	.login-box .user-box select option {
		background: #333;
		/* 下拉菜单的背景色 */
		color: #fff;
		border: none;
	}

	/* 让 select 的 label 动画生效 */
	.login-box .user-box select:focus~label,
	.login-box .user-box select:valid~label {
		top: -20px;
		left: 0;
		color: #03e9f4;
		font-size: 12px;
	}

	/* 自定义下拉箭头 */
	.user-box-select {
		position: relative;
	}

	.user-box-select::after {
		content: '▼';
		font-size: 12px;
		color: #fff;
		position: absolute;
		right: 5px;
		top: 10px;
		pointer-events: none;
		/* 确保可以点击到 select 本身 */
		transition: .5s;
	}

	/* 当下拉框激活时，箭头变色 */
	.user-box-select select:focus~label::after,
	.user-box-select select:valid~label::after {
		color: #03e9f4;
	}

	.login-box form a {
		position: relative;
		display: inline-block;
		padding: 10px 20px;
		color: #03e9f4;
		font-size: 16px;
		text-decoration: none;
		text-transform: uppercase;
		overflow: hidden;
		transition: .5s;
		margin-top: 40px;
		letter-spacing: 4px
	}

	.login-box form .submit {
		position: relative;
		display: inline-block;
		padding: 10px 20px;
		color: #03e9f4;
		font-size: 16px;
		text-decoration: none;
		text-transform: uppercase;
		overflow: hidden;
		transition: .5s;
		margin-top: 40px;
		letter-spacing: 4px;
		cursor: pointer;
	}

	.login-box .submit:hover {
		background: #03e9f4;
		color: #fff;
		border-radius: 5px;
		box-shadow: 0 0 5px #03e9f4,
			0 0 25px #03e9f4,
			0 0 50px #03e9f4,
			0 0 100px #03e9f4;
	}

	.login-box a span {
		position: absolute;
		display: block;
	}

	.login-box a span:nth-child(1) {
		top: 0;
		left: -100%;
		width: 100%;
		height: 2px;
		background: linear-gradient(90deg, transparent, #03e9f4);
		animation: btn-anim1 1s linear infinite;
	}

	@keyframes btn-anim1 {
		0% {
			left: -100%;
		}

		50%,
		100% {
			left: 100%;
		}
	}

	.login-box a span:nth-child(2) {
		top: -100%;
		right: 0;
		width: 2px;
		height: 100%;
		background: linear-gradient(180deg, transparent, #03e9f4);
		animation: btn-anim2 1s linear infinite;
		animation-delay: .25s
	}

	@keyframes btn-anim2 {
		0% {
			top: -100%;
		}

		50%,
		100% {
			top: 100%;
		}
	}

	.login-box a span:nth-child(3) {
		bottom: 0;
		right: -100%;
		width: 100%;
		height: 2px;
		background: linear-gradient(270deg, transparent, #03e9f4);
		animation: btn-anim3 1s linear infinite;
		animation-delay: .5s
	}

	@keyframes btn-anim3 {
		0% {
			right: -100%;
		}

		50%,
		100% {
			right: 100%;
		}
	}

	.login-box a span:nth-child(4) {
		bottom: -100%;
		left: 0;
		width: 2px;
		height: 100%;
		background: linear-gradient(360deg, transparent, #03e9f4);
		animation: btn-anim4 1s linear infinite;
		animation-delay: .75s
	}

	@keyframes btn-anim4 {
		0% {
			bottom: -100%;
		}

		50%,
		100% {
			bottom: 100%;
		}
	}

	.login-box .register-link {
		font-size: 14px;
		text-decoration: none;
		color: #aaa;
		margin-left: 20px;
	}

	.login-box .register-link:hover {
		color: #03e9f4;
		background: transparent;
		box-shadow: none;
	}
</style>