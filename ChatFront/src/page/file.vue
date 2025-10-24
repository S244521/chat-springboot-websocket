<template>
	<div class="back-button" @click="gotoChat">⮌️</div>
	<div class="file-manager">
		<!-- 头部 -->
		<div class="header">
			<div class="header-front">
				<h1>文件管理</h1>
				<button class="upload-btn" @click="showUploadModal = true">
					<span>📤</span> 上传文件
				</button>
			</div>
			<div class="header-backend">
				<div class="search-container">
					<!-- 1. 文件名搜索 -->
					<div class="search-item">
						<label class="search-label">文件名：</label>
						<input type="text" class="search-input" placeholder="输入文件名关键词..." v-model="searchForm.filename">
					</div>

					<!-- 2. 时间范围搜索（开始时间 + 结束时间） -->
					<div class="search-item">
						<label class="search-label">在这之后</label>
						<input type="datetime-local" class="search-input search-time" v-model="searchForm.startTime">
						<label class="search-label">在这之前</label>
						<input type="datetime-local" class="search-input search-time" v-model="searchForm.endTime">
					</div>

					<!-- 3. 数量排序选择 -->
					<div class="search-item">
						<label class="search-label">排序：</label>
						<select class="search-select" v-model="searchForm.isNum">
							<option :value="false">默认排序</option>
							<option :value="true">按下载数量降序</option>
						</select>
					</div>

					<!-- 4. 搜索按钮 -->
					<button class="search-btn" @click="fetchFiles">搜索</button>
					<!-- 5. 重置按钮（可选，快速清空搜索条件） -->
					<button class="reset-btn" @click="handleResetSearch">重置</button>
				</div>
			</div>

		</div>

		<!-- 文件列表 -->
		<div class="file-list">
			<table>
				<thead>
					<tr>
						<th style="width: 10%;">ID</th>
						<th style="width: 40%;">文件名</th>
						<th style="width: 30%;">下载数量</th>
						<th style="width: 30%;">上传时间</th>
						<th style="width: 10%;">操作</th>
					</tr>
				</thead>
				<tbody>
					<tr v-if="files.length === 0">
						<td colspan="5" class="empty">暂无文件</td>
					</tr>
					<tr v-for="file in files" :key="file.id" class="file-row">
						<td style="width: 8%;">{{ file.id }}</td>
						<td class="filename" style="width: 35%;">
							<span class="icon">📄</span>
							{{ file.filename }}
						</td>
						<td style="width: 22%;">{{ file.num }}</td>
						<td style="width: 25%;">{{ formatDate(file.uploadtime) }}</td>
						<td class="actions" style="width: 100%;">
							<button class="download-btn" @click="downloadFile(file)">下载</button>
							<button class="delete-btn" @click="deleteFile(file.id)">删除</button>
						</td>
					</tr>
				</tbody>
			</table>
		</div>

		<!-- 分页 -->
		<div class="pagination">
			<button :disabled="currentPage === 1" @click="currentPage--">
				上一页
			</button>
			<span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
			<button :disabled="currentPage === totalPages" @click="currentPage++">
				下一页
			</button>
		</div>

		<!-- 上传模态框 -->
		<div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
			<div class="modal" @click.stop>
				<div class="modal-header">
					<h2>文件分块上传</h2>
					<button class="close-btn" @click="closeUploadModal">×</button>
				</div>
				<div class="modal-body">
					<!-- 文件选择区 -->
					<div class="upload-area" @dragover.prevent="isDragging = true" @dragleave="isDragging = false"
						@drop.prevent="handleDrop" :class="{ dragging: isDragging }">
						<input type="file" ref="fileInput" @change="handleFileChange" style="display: none" />
						<span class="upload-icon">📁</span>
						<p v-if="!selectedFile">点击选择文件或拖拽文件到此</p>
						<div v-if="selectedFile" class="file-preview small">
							<p>已选择: <strong>{{ selectedFile.name }}</strong></p>
							<p>大小: {{ formatFileSize(selectedFile.size) }}</p>
						</div>
						<button class="select-btn" @click="$refs.fileInput.click()">
							重新选择
						</button>
					</div>

					<!-- 分块大小选择 -->
					<div class="chunk-size-selector">
						<label for="chunkSize">选择分块大小:</label>
						<select id="chunkSize" v-model.number="selectedChunkSizeMB">
							<option v-for="size in availableChunkSizes" :key="size" :value="size">{{ size }} MB</option>
						</select>
					</div>

					<!-- 进度条 -->
					<div v-if="uploadProgress > 0" class="progress-bar-container">
						<div class="progress-bar" :style="{ width: uploadProgress + '%' }">
							{{ uploadProgress.toFixed(2) }}%
						</div>
					</div>

					<!-- 上传状态 -->
					<div v-if="uploadStatus" :class="['status-message', uploadStatusType]">
						{{ uploadStatus }}
					</div>

					<!-- 上传成功链接 -->
					<div v-if="uploadedFileUrl" class="uploaded-link">
						<p>文件上传成功！</p>
						<a :href="uploadedFileUrl" target="_blank">{{ uploadedFileUrl }}</a>
					</div>

					<!-- 错误信息 -->
					<div v-if="uploadError" class="status-message error">
						<p>错误：{{ uploadError }}</p>
					</div>
				</div>

				<div class="modal-footer">
					<button class="cancel-btn" @click="closeUploadModal">关闭</button>
					<button class="confirm-btn" @click="startUpload" :disabled="!selectedFile || isUploading">
						{{ isUploading ? '上传中...' : '开始上传' }}
					</button>
				</div>
			</div>
		</div>

		<!-- 原始的提示信息组件 -->
		<div v-if="message" :class="['message', message.type]">
			{{ message.text }}
		</div>
	</div>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted,
		watch
	} from 'vue'
	import api from '../util/request.js'
	import {
		useRouter
	} from 'vue-router';

	const router = useRouter(); // 创建路由实例

	// --- 原有文件管理的状态 ---
	const files = ref([])
	const currentPage = ref(1)
	const pageSize = 10
	const totalCount = ref(0)
	const showUploadModal = ref(false)
	const message = ref(null)

	const totalPages = computed(() => Math.ceil(totalCount.value / pageSize))

	// --- 新增的分块上传状态 ---
	const fileInput = ref(null) // 文件输入框的引用
	const selectedFile = ref(null) // 已选择的文件
	const isDragging = ref(false) // 是否正在拖拽
	const isUploading = ref(false) // 是否正在上传
	const uploadProgress = ref(0) // 上传进度
	const uploadStatus = ref('') // 上传状态文字
	const uploadStatusType = ref('info') // 状态类型: info, success, error
	const uploadError = ref('') // 上传错误信息
	const uploadedFileUrl = ref('') // 上传成功后的文件URL
	const selectedChunkSizeMB = ref(5) // 默认分块大小 5MB
	const availableChunkSizes = ref([2, 5, 8, 10, 15]) // 可选的分块大小
	const chunkSize = computed(() => selectedChunkSizeMB.value * 1024 * 1024) // 转换为字节
	const searchForm = ref({
		filename: '', // 文件名关键词（默认空）
		startTime: '', // 开始时间（默认空，注意：datetime-local绑定需转成ISO格式）
		endTime: '', // 结束时间（默认空）
		isNum: false // 是否按数量排序（默认false=不按数量）
	});

	// --- 通用函数 ---

	// 重置数据
	const handleResetSearch = () => {
		searchForm.value = {
			filename: '', // 文件名关键词（默认空）
			startTime: '', // 开始时间（默认空，注意：datetime-local绑定需转成ISO格式）
			endTime: '', // 结束时间（默认空）
			isNum: false // 是否按数量排序（默认false=不按数量）
		};
	}

	const gotoChat = () => {
		router.push("/Chat")
	}
	// 格式化日期
	const formatDate = (dateStr) => {
		if (!dateStr) return ''
		return dateStr.split('.')[0].replace('T', ' ')
	}

	// 格式化文件大小
	const formatFileSize = (bytes) => {
		if (bytes === 0) return '0 B'
		const k = 1024
		const sizes = ['B', 'KB', 'MB', 'GB']
		const i = Math.floor(Math.log(bytes) / Math.log(k))
		return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
	}

	// 显示通用提示信息（右下角）
	const showMessage = (text, type = 'error') => {
		message.value = {
			text,
			type
		}
		setTimeout(() => {
			message.value = null
		}, 3000)
	}

	// --- 文件列表相关函数 ---

	// 获取文件列表
	const fetchFiles = () => {
		api({
			url: '/file/query',
			method: 'post',
			params: {
				pageNum: currentPage.value,
				pageSize: pageSize
			},
			data: searchForm.value
		}).then(response => {
			// console.log(response)
			files.value = response.records
			// currentPage.value = response.current
			totalCount.value = response.total
		}).catch(error => {
			showMessage('获取文件列表失败')
			console.error('获取文件列表失败:', error)
		})
	}

	// 下载文件
	const downloadFile = async (file) => {
		// 先请求文件，确保下载完成后再执行后续操作
		try {
			// 用 fetch 请求文件（也可以用 XMLHttpRequest）
			const response = await fetch(file.fileurl);
			if (!response.ok) {
				throw new Error('文件请求失败');
			}

			// 转换为 blob 并创建下载链接
			const blob = await response.blob();
			const url = URL.createObjectURL(blob);
			const link = document.createElement('a');
			link.href = url;
			link.download = file.filename;
			document.body.appendChild(link);
			link.click();

			// 清理资源
			document.body.removeChild(link);
			URL.revokeObjectURL(url);

			// 下载完成后执行
			showMessage('下载完成: ' + file.filename, 'success');
			fetchFiles(); // 确保在下载完成后执行
		} catch (err) {
			showMessage('下载失败: ' + file.filename, 'error');
			console.error('下载错误:', err);
		}
	};

	// 删除文件
	const deleteFile = (fileId) => {
		if (!confirm('确定要删除此文件吗?')) return

		const root =localStorage.getItem('Root')
		if (root==null) {
			showMessage('暂无删除权限', 'error')
			return;
		}

		api({
			url: `/file/deleteFileById`, // 假设这是删除接口
			method: 'post',
			params:{
				fileId:fileId
			}
		}).then(response => {
			
			showMessage(`文件删除成功${response}`, 'success');
			fetchFiles(); // 重新加载列表
		}).catch(error => {
			showMessage('删除文件失败');
			console.error('删除文件失败:', error);
		})
	}

	// --- 分块上传相关函数 ---

	// 处理文件选择（点击或拖拽）
	const handleFileChange = (event) => {
		const fileList = event.target.files || event.dataTransfer.files
		if (fileList.length > 0) {
			selectedFile.value = fileList[0]
			resetUploadStatus()
		}
	}

	const handleDrop = (event) => {
		isDragging.value = false;
		handleFileChange(event);
	}

	// 重置上传状态
	const resetUploadStatus = () => {
		isUploading.value = false;
		uploadProgress.value = 0;
		uploadStatus.value = '';
		uploadStatusType.value = 'info';
		uploadError.value = '';
		uploadedFileUrl.value = '';
	}

	// 关闭并重置上传模态框
	const closeUploadModal = () => {
		showUploadModal.value = false;
		selectedFile.value = null;
		resetUploadStatus();
	}

	// 开始上传
	const startUpload = async () => {
		if (!selectedFile.value) {
			uploadError.value = '请先选择一个文件。';
			return;
		}

		isUploading.value = true;
		resetUploadStatus(); // 重置状态以开始新的上传
		uploadStatus.value = '准备上传...';
		isUploading.value = true; // 立即设置为true

		const file = selectedFile.value;
		// 使用文件名、大小和最后修改时间生成唯一标识符
		const identifier = `${file.name}-${file.size}-${file.lastModified}`;
		const totalChunks = Math.ceil(file.size / chunkSize.value);
		let chunksUploaded = 0;

		try {
			for (let i = 0; i < totalChunks; i++) {
				const start = i * chunkSize.value;
				const end = Math.min(start + chunkSize.value, file.size);
				const chunk = file.slice(start, end);

				const formData = new FormData();
				formData.append('file', chunk, `${file.name}.chunk${i}`);
				formData.append('fileIdentifier', identifier);
				formData.append('chunkNumber', i);
				formData.append('totalChunks', totalChunks);
				formData.append('originalFilename', file.name);

				uploadStatus.value = `正在上传分块 ${i + 1}/${totalChunks}...`;

				// !注意: 请替换为您的后端上传地址和认证Token
				const UPLOAD_URL = 'http://localhost:9999/file/upload';
				const AUTH_TOKEN =
					'Shang eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290IiwidXNlcklkIjoxLCJuYW1lIjoi5LykIiwiY3JlYXRlZEF0IjoiMjAyNS0xMC0yM1QxMDozODozMy4zMzU0MTIiLCJpYXQiOjE3NjExODcxMTMsImV4cCI6MTc2MTI3MzUxM30.jybKFAzSGEWba5x7A0118rg4R9jZiFAyt9HgFWNiuqw';



				// 使用 await 等待 api 调用完成
				const response = await api({
					url: '/file/upload',
					method: 'post',
					headers: {
						'Content-Type': 'multipart/form-data',
						'Authorization': AUTH_TOKEN
					},
					data: formData
				});
				console.log(response)
				// 检查后端返回结果
				if (typeof response !== 'string') {
					// 如果后端返回的不是预期的字符串，说明可能出错了
					throw new Error('后端响应格式不正确');
				}

				// 判断是否是最终合并成功的响应
				if (response.startsWith('http')) {
					// 后端返回了文件URL，说明已全部上传并合并成功
					chunksUploaded++;
					uploadProgress.value = 100; // 直接设置为100%
					uploadedFileUrl.value = response.split('文件上传并合并成功')[0].trim(); // 提取纯URL
					uploadStatus.value = '文件上传并合并成功！';
					uploadStatusType.value = 'success';
					isUploading.value = false;
					fetchFiles(); // 刷新文件列表
					return; // 任务完成，退出函数
				}
				// 判断是否是普通分块上传成功的响应
				else if (response.includes('块上传成功')) {
					// 只是一个分块成功，继续下一个
					chunksUploaded++;
					uploadProgress.value = (chunksUploaded / totalChunks) * 100;
				}
				// 其他未知情况，视为错误
				else {
					throw new Error(response || '未知的后端响应');
				}
			}

			// 所有分块上传完成（循环正常结束）
			if (chunksUploaded === totalChunks) {
				uploadStatus.value = '所有分块上传完成！'
				uploadStatusType.value = 'success'
				isUploading.value = false
				fetchFiles() // 刷新文件列表
			}
		} catch (err) {
			console.error('上传过程中发生错误:', err);
			uploadError.value = '上传失败: ' + (err.response?.data?.message || err.message || '未知错误');
			uploadStatus.value = '上传失败';
			uploadStatusType.value = 'error';
			isUploading.value = false;
		}
	}


	// --- 生命周期和侦听器 ---

	// 监听分页变化
	watch(currentPage, () => {
		fetchFiles()
	})

	// 初始化时获取文件
	onMounted(() => {
		fetchFiles()
	})
</script>


<style scoped>
	@import url('../css/page-file/file-querybox.css');

	/* --- 原有样式 --- */
	* {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
	}


	.file-manager {
		padding: 20px;
		background-color: #f5f7fa;
		max-height: 100vh;
		height: 100vh;
		font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
	}

	.header {
		display: inline-block;
		width: 100%;
		margin-bottom: 30px;
		background: white;
		padding: 20px;
		border-radius: 8px;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
		height: 170px;
	}

	.header-front {
		width: 100%;
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-bottom: 20px;
	}



	.header h1 {
		font-size: 24px;
		color: #333;
	}


	.upload-btn {
		background-color: #409eff;
		color: white;
		border: none;
		padding: 10px 20px;
		border-radius: 4px;
		cursor: pointer;
		font-size: 14px;
		transition: background-color 0.3s;
	}

	.upload-btn:hover {
		background-color: #66b1ff;
	}

	.file-list {
		height: 520px;
		background: white;
		border-radius: 8px;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
		margin-bottom: 20px;
		overflow: hidden;
		position: relative;
	}

	table {
		width: 100%;
		border-collapse: collapse;
		table-layout: fixed;
	}

	thead {
		background-color: #c8cacc;
		border-bottom: 2px solid #e4e7eb;
		display: table;
		width: 100%;
		padding-right: 15px;
		box-sizing: border-box;
	}

	tbody {
		display: block;
		/* 关键：将 tbody 转为块级元素，支持固定高度 */
		height: 462px;
		/* 继承父容器高度（500px），或直接写 500px */
		overflow-y: auto;
		/* 垂直方向溢出时显示滚动条 */
		width: 100%;
	}

	tr {
		display: table;
		width: 100%;
		table-layout: fixed;
	}

	th {
		padding: 16px;
		text-align: left;
		font-weight: 600;
		color: #333;
	}

	td {
		padding: 16px;
		border-bottom: 1px solid #e4e7eb;
		text-align: left;
		/* text-align: center; */
		color: #606266;
	}


	.file-row:hover {
		background-color: #f5f7fa;
	}

	.filename {
		padding: 16px;

		gap: 8px;
	}

	.icon {
		font-size: 18px;
	}

	.empty {
		text-align: center;
		color: #909399;
		padding: 40px 16px !important;
	}

	.actions {
		display: flex;
		gap: 10px;
	}

	.download-btn,
	.delete-btn {
		padding: 6px 12px;
		border: none;
		border-radius: 4px;
		cursor: pointer;
		font-size: 13px;
		transition: all 0.3s;
	}

	.download-btn {
		background-color: #67c23a;
		color: white;
	}

	.download-btn:hover {
		background-color: #85ce61;
	}

	.delete-btn {
		background-color: #f56c6c;
		color: white;
	}

	.delete-btn:hover {
		background-color: #f78989;
	}

	.pagination {
		display: flex;
		justify-content: center;
		align-items: center;
		gap: 20px;
		background: white;
		padding: 20px;
		border-radius: 8px;
		box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
		margin-bottom: 20px;
	}

	.pagination button {
		padding: 8px 16px;
		border: 1px solid #dcdfe6;
		background: white;
		border-radius: 4px;
		cursor: pointer;
		transition: all 0.3s;
	}

	.pagination button:hover:not(:disabled) {
		color: #409eff;
		border-color: #409eff;
	}

	.pagination button:disabled {
		color: #c0c4cc;
		cursor: not-allowed;
	}

	.page-info {
		color: #606266;
		min-width: 150px;
		text-align: center;
	}

	.modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.7);
		display: flex;
		justify-content: center;
		align-items: center;
		z-index: 1000;
	}

	.modal {
		background: white;
		border-radius: 8px;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
		width: 90%;
		max-width: 600px;
		/* 稍微加宽以容纳新元素 */
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20px;
		border-bottom: 1px solid #e4e7eb;
	}

	.modal-header h2 {
		font-size: 18px;
		color: #333;
	}

	.close-btn {
		background: none;
		border: none;
		font-size: 28px;
		color: #909399;
		cursor: pointer;
		padding: 0;
	}

	.modal-body {
		padding: 20px 30px;
	}

	.upload-area {
		border: 2px dashed #dcdfe6;
		border-radius: 6px;
		padding: 30px;
		text-align: center;
		cursor: pointer;
		transition: all 0.3s;
		margin-bottom: 20px;
	}

	.upload-area:hover,
	.upload-area.dragging {
		border-color: #409eff;
		background-color: #f5f7fa;
	}

	.upload-icon {
		font-size: 48px;
		display: block;
		margin-bottom: 10px;
	}

	.upload-area p {
		color: #606266;
		margin-bottom: 15px;
	}

	.select-btn {
		background-color: #409eff;
		color: white;
		border: none;
		padding: 10px 20px;
		border-radius: 4px;
		cursor: pointer;
		transition: background-color 0.3s;
	}

	.select-btn:hover {
		background-color: #66b1ff;
	}

	.file-preview.small {
		margin-top: 0;
		padding: 0;
		background-color: transparent;
		border-radius: 0;
		text-align: center;
		margin-bottom: 15px;
	}

	.file-preview.small p {
		margin: 4px 0;
	}

	.modal-footer {
		display: flex;
		justify-content: flex-end;
		gap: 10px;
		padding: 20px;
		border-top: 1px solid #e4e7eb;
	}

	.cancel-btn,
	.confirm-btn {
		padding: 8px 16px;
		border: 1px solid #dcdfe6;
		background: white;
		border-radius: 4px;
		cursor: pointer;
		transition: all 0.3s;
	}

	.cancel-btn:hover {
		color: #409eff;
		border-color: #409eff;
	}

	.confirm-btn {
		background-color: #409eff;
		color: white;
		border-color: #409eff;
	}

	.confirm-btn:hover:not(:disabled) {
		background-color: #66b1ff;
	}

	.confirm-btn:disabled {
		background-color: #a0cfff;
		cursor: not-allowed;
	}

	.message {
		position: fixed;
		bottom: 20px;
		right: 20px;
		padding: 16px 20px;
		border-radius: 4px;
		color: white;
		font-size: 14px;
		z-index: 2000;
		animation: slideIn 0.3s ease-out;
	}

	.message.success {
		background-color: #67c23a;
	}

	.message.error {
		background-color: #f56c6c;
	}

	@keyframes slideIn {
		from {
			transform: translateX(400px);
			opacity: 0;
		}

		to {
			transform: translateX(0);
			opacity: 1;
		}
	}

	/* --- 新增和调整的样式 --- */
	.chunk-size-selector {
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 20px;
		gap: 10px;
	}

	.chunk-size-selector label {
		color: #555;
		font-size: 14px;
	}

	.chunk-size-selector select {
		padding: 8px 12px;
		border: 1px solid #ccc;
		border-radius: 5px;
		font-size: 14px;
		cursor: pointer;
		outline: none;
		transition: border-color 0.3s;
	}

	.chunk-size-selector select:focus {
		border-color: #409eff;
	}

	.progress-bar-container {
		width: 100%;
		background-color: #e0e0e0;
		border-radius: 5px;
		margin: 20px auto;
		overflow: hidden;
		height: 25px;
		box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
	}

	.progress-bar {
		height: 100%;
		background-color: #409eff;
		width: 0%;
		border-radius: 5px;
		text-align: center;
		color: white;
		line-height: 25px;
		transition: width 0.4s ease-out;
		white-space: nowrap;
		overflow: hidden;
	}

	.status-message {
		padding: 10px 15px;
		margin: 15px 0;
		border-radius: 5px;
		font-size: 14px;
		border: 1px solid transparent;
		text-align: center;
	}

	.status-message.info {
		background-color: #e0f7fa;
		color: #00796b;
		border-color: #00bcd4;
	}

	.status-message.success {
		background-color: #e6ffed;
		color: #1a7e2b;
		border-color: #28a745;
	}

	.status-message.error {
		background-color: #ffe0e0;
		color: #d32f2f;
		border-color: #dc3545;
	}

	.uploaded-link {
		margin-top: 15px;
		padding: 15px;
		background-color: #f0f9eb;
		border-left: 5px solid #67c23a;
		text-align: left;
		word-wrap: break-word;
	}

	.uploaded-link p {
		margin-top: 0;
		color: #333;
		font-weight: bold;
		margin-bottom: 5px;
	}

	.uploaded-link a {
		color: #409eff;
		text-decoration: none;
	}

	.uploaded-link a:hover {
		text-decoration: underline;
	}

	.back-button {
		position: fixed;
		right: 50px;
		bottom: 40px;
		width: 50px;
		height: 50px;
		border-radius: 50%;
		background-color: #59aefa;
		font-size: 30px;
		color: white;
		border: none;
		cursor: pointer;
		box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
		display: flex;
		/* align-items: center; */
		justify-content: center;
		transition: all 0.3s ease;
	}

	.back-button:hover {
		background-color: #aab0ff;
		transform: scale(1.1);
	}
</style>