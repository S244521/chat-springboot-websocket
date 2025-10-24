<template>
	<!-- 登录页面的主容器，代替原来的 body -->
	<div class="login-container">
		<!-- 鼠标跟随特效容器 -->
		<div class="cursor">
			<!-- 使用 v-for 循环生成每一个字母的 span -->
			<!-- 动态绑定 style 来实现原有的 hue-rotate 和位置偏移效果 -->
			<span v-for="(char, index) in textContent" :key="index" class="text" :style="{
	      '--i': index + 1,
	      'left': (index * 0.9) + 'em',
	      'filter': `hue-rotate(${index * 10}deg)`
	    }">
				{{ char }}
			</span>
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		onMounted,
		onUnmounted
	} from 'vue';
	import gsap from 'gsap';

	// 定义要显示的文字
	// const textContent = ref("animate text trail effect".split('')); //0.6
	const textContent = ref("欢迎来到，智哥聊天室".split(''));

	// 鼠标移动事件处理函数
	const handleMouseMove = (e) => {
		gsap.to(".text", {
			x: e.clientX-695,
			y: e.clientY-192,
			stagger: 0.1, // 每个字符之间的动画延迟，形成拖尾效果
			duration: 0.5, // 稍微增加一点持续时间让效果更柔和（可选）
			ease: "back.out(1.7)" // 添加一个缓动效果让动画更生动（可选）
		});
	};

	// 组件挂载完成后
	onMounted(() => {
		// 监听整个窗口的鼠标移动
		window.addEventListener("mousemove", handleMouseMove);
	});

	// 组件卸载前
	onUnmounted(() => {
		// 务必移除事件监听，防止内存泄漏
		window.removeEventListener("mousemove", handleMouseMove);
	});
</script>

<style scoped>
	* {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
		font-family: consolas;
	}

	.login-container {
		background-color: #222;
		overflow: hidden;
		background-image: linear-gradient(to right, #333 1px, transparent 1px), linear-gradient(to bottom, #333 1px, transparent 1px);
		background-size: 4vh 4vh;
		display: flex;
		justify-content: center;
		align-items: center;
		min-height: 100vh;
	}

	.cursor {
		/* 		position: absolute;
		top: 0;
		left: 20px;
		pointer-events: none; */

		position: absolute;
		top: 20%;
		left: 42%;
		transform: translateX(-50%);
		pointer-events: none;
	}

	.cursor .text {
		/* 		background-color: antiquewhite;
		position: absolute;
		font-size: 2em;
		color: #00ff9a;
		text-shadow: 0 0 15px #00ff9a, 0 0 50px #00ff9a;
		text-transform: uppercase;
		display: flex;
		justify-content: center;
		align-items: center; */


		position: absolute;
		font-size: 2em;
		color: #00ff9a;
		text-shadow: 0 0 15px #00ff9a, 0 0 50px #00ff9a;
		text-transform: uppercase;
		display: flex;
		justify-content: center;
		align-items: center;
		
	}
</style>