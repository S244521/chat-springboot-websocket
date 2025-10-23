<template>
	<div class="login-container">
		<canvas ref="canvasRef" class="background-canvas"></canvas>

		<div class="login-box">
			<div class="login-header">
				<h2 class="title">Root权限获取</h2>
			</div>

			<form>
				<div>
					<div class="user-box">
						<input type="text" name="username" v-model="loginForm.username" required="">
						<label>用户名</label>
					</div>
					<div class="user-box">
						<input type="password" name="password" v-model="loginForm.password" required="">
						<label>密码</label>
					</div>
				</div>
				<div class="submit" @click="login">
					获取
				</div>
			</form>
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		reactive,
		onMounted,
		onBeforeUnmount
	} from 'vue'
	import {
		useRouter,
		useRoute
	} from 'vue-router'
	import {
		ElMessage
	} from 'element-plus'
	import api from '../util/request.js'

	const router = useRouter()
	const route = useRoute()

	const loginFormRef = ref()
	const canvasRef = ref()


	const loginForm = ref({
		username: '',
		password: ''
	})



	// 俄罗斯方块形状定义
	const SHAPES = {
		I: [
			[1, 1, 1, 1]
		],
		O: [
			[1, 1],
			[1, 1]
		],
		T: [
			[0, 1, 0],
			[1, 1, 1]
		],
		L: [
			[1, 0],
			[1, 0],
			[1, 1]
		],
		J: [
			[0, 1],
			[0, 1],
			[1, 1]
		],
		S: [
			[0, 1, 1],
			[1, 1, 0]
		],
		Z: [
			[1, 1, 0],
			[0, 1, 1]
		]
	}

	const login = () => {
		api({
			url: '/root/login',
			method: 'post',
			data: loginForm.value
		}).then(response => {
			console.log('登录成功:', response)
			localStorage.setItem('Root', "Shang " + response.token)
			router.push("/Chat")
		})
		.catch(error => {
			// 失败处理
			console.error('登录失败:', error)
			alert('登录失败: ' + (error.msg || error.message || '未知错误'))
		})
	}

	const COLORS = [
		'#00f2fe', // 青色 (I)
		'#fee140', // 黄色 (O)
		'#764ba2', // 紫色 (T)
		'#fa709a', // 橙色 (L)
		'#4facfe', // 蓝色 (J)
		'#43e97b', // 绿色 (S)
		'#f093fb' // 红色 (Z)
	]

	// 获取指定列的最高点（从底部开始有方块的最高行）
	const getColumnHeight = (col) => {
		for (let row = 0; row < ROWS; row++) {
			if (grid[row][col] !== null) {
				return ROWS - row // 返回从这一行到底部的距离
			}
		}
		return 0 // 整列为空，高度为0
	}

	// 智能选择形状（根据当前列和周围的情况）
	const selectBestShape = (targetCol) => {
		// 获取当前列和左右各1列的高度
		const leftHeight = targetCol > 0 ? getColumnHeight(targetCol - 1) : 0
		const currentHeight = getColumnHeight(targetCol)
		const rightHeight = targetCol < COLS - 1 ? getColumnHeight(targetCol + 1) : 0

		const avgHeight = (leftHeight + currentHeight + rightHeight) / 3

		// 根据高度选择形状
		if (currentHeight > avgHeight + 2) {
			// 当前列偏高，选择宽形状（I横放或O）
			const wideShapes = ['I', 'O', 'S', 'Z']
			return wideShapes[Math.floor(Math.random() * wideShapes.length)]
		} else if (currentHeight < avgHeight - 2) {
			// 当前列偏低，选择窄形状（I竖放、L、J、T）
			const tallShapes = ['L', 'J', 'T']
			return tallShapes[Math.floor(Math.random() * tallShapes.length)]
		} else {
			// 高度适中，随机选择
			const shapeKeys = Object.keys(SHAPES)
			return shapeKeys[Math.floor(Math.random() * shapeKeys.length)]
		}
	}

	// 创建新方块（按顺序在指定列，并智能选择形状）
	const createPiece = () => {
		// 使用智能选择，根据当前列的高度选择合适的形状
		const shapeKey = selectBestShape(currentColumn)
		const shapeKeys = Object.keys(SHAPES)
		const shapeIndex = shapeKeys.indexOf(shapeKey)
		const shape = SHAPES[shapeKey]

		// 计算方块宽度
		const shapeWidth = shape[0].length

		// 起始位置就是当前列
		let startX = currentColumn

		// 边界检查：如果会超出右边界，重置到左边开始
		if (startX + shapeWidth > COLS) {
			startX = 0
			currentColumn = 0
		}

		// 下一个方块的起始列 = 当前列 + 当前方块宽度
		currentColumn = startX + shapeWidth

		// 如果下一列超出范围，从头开始
		if (currentColumn >= COLS) {
			currentColumn = 0
		}

		return {
			shape: shape,
			color: COLORS[shapeIndex],
			x: startX,
			y: 0,
			speed: 0
		}
	}

	// 检查碰撞
	const checkCollision = (piece, offsetX = 0, offsetY = 0) => {
		for (let row = 0; row < piece.shape.length; row++) {
			for (let col = 0; col < piece.shape[row].length; col++) {
				if (piece.shape[row][col]) {
					const newX = piece.x + col + offsetX
					const newY = piece.y + row + offsetY

					// 检查边界
					if (newX < 0 || newX >= COLS || newY >= ROWS) {
						return true
					}

					// 检查是否碰到已有方块
					if (newY >= 0 && grid[newY][newX] !== null) {
						return true
					}
				}
			}
		}
		return false
	}

	// 旋转方块（顺时针90度）
	const rotateShape = (shape) => {
		const rows = shape.length
		const cols = shape[0].length
		const rotated = []

		for (let col = 0; col < cols; col++) {
			rotated[col] = []
			for (let row = rows - 1; row >= 0; row--) {
				rotated[col][rows - 1 - row] = shape[row][col]
			}
		}

		return rotated
	}

	// 旋转当前方块
	const rotatePiece = () => {
		if (!currentPiece) return

		const rotated = rotateShape(currentPiece.shape)
		const originalShape = currentPiece.shape

		// 尝试旋转
		currentPiece.shape = rotated

		// 检查旋转后是否碰撞
		if (checkCollision(currentPiece, 0, 0)) {
			// 如果碰撞，尝试左移
			if (!checkCollision(currentPiece, -1, 0)) {
				currentPiece.x -= 1
			}
			// 尝试右移
			else if (!checkCollision(currentPiece, 1, 0)) {
				currentPiece.x += 1
			}
			// 尝试左移2格
			else if (!checkCollision(currentPiece, -2, 0)) {
				currentPiece.x -= 2
			}
			// 都不行，恢复原形状
			else {
				currentPiece.shape = originalShape
			}
		}
	}

	// 快速下落
	const dropPiece = () => {
		if (!currentPiece) return

		// 一直下移直到碰撞
		while (!checkCollision(currentPiece, 0, 1)) {
			currentPiece.y++
		}

		// 立即固定
		lockPiece(currentPiece)
		clearLines()

		// 检查游戏结束
		if (checkGameOver()) {
			initGrid()
		}

		currentPiece = null
	}

	// 将方块固定到网格
	const lockPiece = (piece) => {
		for (let row = 0; row < piece.shape.length; row++) {
			for (let col = 0; col < piece.shape[row].length; col++) {
				if (piece.shape[row][col]) {
					const gridY = piece.y + row
					const gridX = piece.x + col
					if (gridY >= 0 && gridY < ROWS && gridX >= 0 && gridX < COLS) {
						grid[gridY][gridX] = piece.color
					}
				}
			}
		}
	}

	// 检查是否游戏结束（顶部被占用）
	const checkGameOver = () => {
		// 检查前3行是否有方块
		for (let row = 0; row < 3; row++) {
			for (let col = 0; col < COLS; col++) {
				if (grid[row][col] !== null) {
					return true
				}
			}
		}
		return false
	}

	// 检查并消除满行
	const clearLines = () => {
		const linesToClear = []

		for (let row = 0; row < ROWS; row++) {
			let isFull = true
			for (let col = 0; col < COLS; col++) {
				if (grid[row][col] === null) {
					isFull = false
					break
				}
			}
			if (isFull) {
				linesToClear.push(row)
			}
		}

		if (linesToClear.length > 0) {
			// 闪烁动画（简化版）
			setTimeout(() => {
				// 删除满行
				for (const row of linesToClear) {
					grid.splice(row, 1)
				}

				// 在顶部添加新的空行
				for (let i = 0; i < linesToClear.length; i++) {
					const newRow = []
					for (let col = 0; col < COLS; col++) {
						newRow.push(null)
					}
					grid.unshift(newRow)
				}
			}, 200)
		}

		return linesToClear.length
	}

	let animationId = null
	let gridSize = 30
	let frameCount = 0
	let dropInterval = 90 // 每90帧掉落一个方块
	const ROWS = 20 // 固定20行
	const COLS = 35 // 固定35列
	let gameAreaOffsetX = 0 // 游戏区域X偏移
	let gameAreaOffsetY = 0 // 游戏区域Y偏移
	let gameAreaWidth = 0 // 游戏区域宽度
	let gameAreaHeight = 0 // 游戏区域高度

	// 游戏网格 - 二维数组，null表示空，否则存储颜色
	let grid = []

	// 当前下落的方块
	let currentPiece = null

	// 当前列索引（按顺序）
	let currentColumn = 0

	// 初始化网格
	const initGrid = () => {
		grid = []
		for (let row = 0; row < ROWS; row++) {
			grid[row] = []
			for (let col = 0; col < COLS; col++) {
				grid[row][col] = null
			}
		}
		currentColumn = 0 // 重置为从第一列开始
	}

	// 初始化Canvas
	const initCanvas = () => {
		const canvas = canvasRef.value
		if (!canvas) return

		canvas.width = window.innerWidth
		canvas.height = window.innerHeight

		const ctx = canvas.getContext('2d')

		// 重置状态
		initGrid()
		currentPiece = null
		frameCount = 0

		// 计算固定游戏区域尺寸和位置（居中）
		gameAreaWidth = COLS * gridSize
		gameAreaHeight = ROWS * gridSize
		gameAreaOffsetX = (canvas.width - gameAreaWidth) / 2
		gameAreaOffsetY = (canvas.height - gameAreaHeight) / 2

		// 绘制网格
		const drawGrid = () => {
			for (let row = 0; row < ROWS; row++) {
				for (let col = 0; col < COLS; col++) {
					if (grid[row][col] !== null) {
						const x = gameAreaOffsetX + col * gridSize
						const y = gameAreaOffsetY + row * gridSize

						// 主体填充
						ctx.fillStyle = grid[row][col]
						ctx.fillRect(x + 1, y + 1, gridSize - 2, gridSize - 2)

						// 边框
						ctx.strokeStyle = 'rgba(0, 0, 0, 0.3)'
						ctx.lineWidth = 2
						ctx.strokeRect(x + 1, y + 1, gridSize - 2, gridSize - 2)

						// 内部高光
						ctx.fillStyle = 'rgba(255, 255, 255, 0.2)'
						ctx.fillRect(x + 4, y + 4, gridSize - 8, 4)
					}
				}
			}
		}

		// 绘制当前方块
		const drawPiece = (piece) => {
			if (!piece) return

			for (let row = 0; row < piece.shape.length; row++) {
				for (let col = 0; col < piece.shape[row].length; col++) {
					if (piece.shape[row][col]) {
						const x = gameAreaOffsetX + (piece.x + col) * gridSize
						const y = gameAreaOffsetY + (piece.y + row) * gridSize

						// 主体填充
						ctx.fillStyle = piece.color
						ctx.fillRect(x + 1, y + 1, gridSize - 2, gridSize - 2)

						// 边框
						ctx.strokeStyle = 'rgba(0, 0, 0, 0.3)'
						ctx.lineWidth = 2
						ctx.strokeRect(x + 1, y + 1, gridSize - 2, gridSize - 2)

						// 内部高光
						ctx.fillStyle = 'rgba(255, 255, 255, 0.2)'
						ctx.fillRect(x + 4, y + 4, gridSize - 8, 4)
					}
				}
			}
		}

		// 动画循环
		const animate = () => {
			frameCount++

			// 深色背景
			ctx.fillStyle = '#0a0e27'
			ctx.fillRect(0, 0, canvas.width, canvas.height)

			// 绘制游戏区域边框
			ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)'
			ctx.lineWidth = 2
			ctx.strokeRect(gameAreaOffsetX, gameAreaOffsetY, gameAreaWidth, gameAreaHeight)

			// 绘制游戏区域背景
			ctx.fillStyle = 'rgba(0, 0, 0, 0.2)'
			ctx.fillRect(gameAreaOffsetX, gameAreaOffsetY, gameAreaWidth, gameAreaHeight)

			// 绘制网格线
			ctx.strokeStyle = 'rgba(255, 255, 255, 0.03)'
			ctx.lineWidth = 1
			for (let i = 0; i <= COLS; i++) {
				ctx.beginPath()
				ctx.moveTo(gameAreaOffsetX + i * gridSize, gameAreaOffsetY)
				ctx.lineTo(gameAreaOffsetX + i * gridSize, gameAreaOffsetY + gameAreaHeight)
				ctx.stroke()
			}
			for (let i = 0; i <= ROWS; i++) {
				ctx.beginPath()
				ctx.moveTo(gameAreaOffsetX, gameAreaOffsetY + i * gridSize)
				ctx.lineTo(gameAreaOffsetX + gameAreaWidth, gameAreaOffsetY + i * gridSize)
				ctx.stroke()
			}

			// 生成新方块
			if (!currentPiece) {
				currentPiece = createPiece()
			}

			// 更新当前方块
			if (currentPiece) {
				currentPiece.speed += 0.05 // 每帧累积速度（加快3倍）

				if (currentPiece.speed >= 1) {
					currentPiece.speed = 0

					// 尝试向下移动
					if (!checkCollision(currentPiece, 0, 1)) {
						currentPiece.y++
					} else {
						// 无法下移，固定方块
						lockPiece(currentPiece)
						clearLines()

						// 检查是否堆到顶部
						if (checkGameOver()) {
							// 清空所有方块，重新开始
							initGrid()
						}

						currentPiece = null
					}
				}
			}

			// 绘制已固定的方块
			drawGrid()

			// 绘制当前下落的方块
			drawPiece(currentPiece)

			animationId = requestAnimationFrame(animate)
		}

		animate()
	}

	// 处理窗口大小变化
	const handleResize = () => {
		if (animationId) {
			cancelAnimationFrame(animationId)
		}
		initCanvas()
	}

	// 处理键盘事件
	const handleKeyDown = (event) => {
		if (!currentPiece) return

		switch (event.key) {
			case 'ArrowUp':
				event.preventDefault()
				rotatePiece()
				break
			case 'ArrowDown':
				event.preventDefault()
				dropPiece()
				break
			case 'ArrowLeft':
				event.preventDefault()
				// 左移
				if (!checkCollision(currentPiece, -1, 0)) {
					currentPiece.x -= 1
				}
				break
			case 'ArrowRight':
				event.preventDefault()
				// 右移
				if (!checkCollision(currentPiece, 1, 0)) {
					currentPiece.x += 1
				}
				break
		}
	}


	onMounted(() => {
		initCanvas()
		window.addEventListener('resize', handleResize)
		window.addEventListener('keydown', handleKeyDown)
	})

	onBeforeUnmount(() => {
		if (animationId) {
			cancelAnimationFrame(animationId)
		}
		window.removeEventListener('resize', handleResize)
		window.removeEventListener('keydown', handleKeyDown)
	})
</script>

<style lang="scss" scoped>
	@import url('../css/components-root/rootlogin-form.css');

	.login-container {
		position: relative;
		width: 100vw;
		height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
	}

	.background-canvas {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		z-index: 1;
		pointer-events: none;
	}

	.login-box {
		position: relative;
		z-index: 2;
		width: 420px;
		padding: 50px 45px;
		background: rgba(255, 255, 255, 0.1);
		backdrop-filter: blur(20px);
		-webkit-backdrop-filter: blur(20px);
		border-radius: 20px;
		border: 1px solid rgba(255, 255, 255, 0.2);
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
		animation: fadeInUp 0.8s ease-out;
	}

	@keyframes fadeInUp {
		from {
			opacity: 0;
			transform: translateY(30px);
		}

		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.login-header {
		text-align: center;
		margin-bottom: 40px;

		.title {
			font-size: 32px;
			font-weight: 700;
			color: #ffffff;
			margin: 0 0 10px 0;
			text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
			letter-spacing: 1px;
		}

		.subtitle {
			font-size: 14px;
			color: rgba(255, 255, 255, 0.8);
			margin: 0;
			font-weight: 300;
		}
	}

	.login-form {
		margin-top: 20px;

		:deep(.el-form-item) {
			margin-bottom: 24px;
		}

		:deep(.el-input__wrapper) {
			background: rgba(255, 255, 255, 0.15);
			backdrop-filter: blur(10px);
			-webkit-backdrop-filter: blur(10px);
			border: 1px solid rgba(255, 255, 255, 0.3);
			box-shadow: none;
			transition: all 0.3s ease;

			&:hover,
			&.is-focus {
				background: rgba(255, 255, 255, 0.25);
				border-color: rgba(255, 255, 255, 0.5);
				box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
			}
		}

		:deep(.el-input__inner) {
			color: #ffffff;
			font-size: 15px;

			&::placeholder {
				color: rgba(255, 255, 255, 0.6);
			}
		}

		:deep(.el-input__prefix) {
			color: rgba(255, 255, 255, 0.7);
		}

		:deep(.el-input__suffix) {
			color: rgba(255, 255, 255, 0.7);
		}

		:deep(.el-input__icon) {
			color: rgba(255, 255, 255, 0.7);
		}

		.login-button {
			width: 100%;
			height: 48px;
			font-size: 16px;
			font-weight: 600;
			letter-spacing: 2px;
			background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
			border: none;
			border-radius: 24px;
			box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
			transition: all 0.3s ease;

			&:hover {
				transform: translateY(-2px);
				box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
			}

			&:active {
				transform: translateY(0);
			}
		}

		:deep(.el-form-item__error) {
			color: #ff6b6b;
			font-weight: 500;
			text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
		}
	}
</style>