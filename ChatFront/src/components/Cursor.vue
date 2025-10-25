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
	const textContent = ref("欢迎来到，智哥聊天室".split(''));

	// 鼠标移动事件处理函数
	const handleMouseMove = (e) => {
		// e.clientX 和 e.clientY 就是鼠标相对于浏览器视口的坐标
		// 我们直接使用这个坐标，GSAP会处理元素的定位
		gsap.to(".text", {
			x: e.clientX + 35,
			y: e.clientY - 10,
			stagger: 0.05, // 可以稍微调整延迟，让跟随更紧密或更松散
			duration: 0.5,
			ease: "power2.out" // 换一个更平滑的缓动函数
		});
	};

	onMounted(() => {
		// --- 新增代码：设置文字的初始位置 ---

		// 1. 计算视口的中心点 X 坐标
		const initialX = window.innerWidth / 2-170;
		// 2. 计算视口顶部向下 15% 的 Y 坐标
		const initialY = window.innerHeight * 0.15;

		// 3. 使用 gsap.set() 立即将所有 .text 元素放置到计算出的初始位置
		//    这里加上 handleMouseMove 中的偏移量，是为了让初始效果和跟随效果的对齐方式保持一致
		gsap.set(".text", {
			x: initialX + 35,
			y: initialY - 10,
		});

		// --- 结束新增代码 ---


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
		/* 
		 * 移除 position: absolute 和 top/left/transform
		 * .text 元素将直接通过 GSAP 的 transform 来定位
		 */
		pointer-events: none;
	}

	.cursor .text {
		position: fixed;
		/* 使用 fixed 定位，使其相对于视口 */
		top: 0;
		left: 0;
		font-size: 2em;
		color: #00ff9a;
		text-shadow: 0 0 15px #00ff9a, 0 0 50px #00ff9a;
		text-transform: uppercase;

		/* 关键：通过 transform 将元素的中心点对准鼠标 */
		transform: translate(-50%, -50%);
	}
</style>