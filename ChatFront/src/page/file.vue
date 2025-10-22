<template>
  <div class="file-manager">
    <!-- 头部 -->
    <div class="header">
      <h1>文件管理</h1>
      <button class="upload-btn" @click="showUploadModal = true">
        <span>📤</span> 上传文件
      </button>
    </div>

    <!-- 文件列表 -->
    <div class="file-list">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>文件名</th>
            <th>上传时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="files.length === 0">
            <td colspan="4" class="empty">暂无文件</td>
          </tr>
          <tr v-for="file in files" :key="file.id" class="file-row">
            <td>{{ file.id }}</td>
            <td class="filename">
              <span class="icon">📄</span>
              {{ file.filename }}
            </td>
            <td>{{ formatDate(file.uploadtime) }}</td>
            <td class="actions">
              <button class="download-btn" @click="downloadFile(file)">下载</button>
              <button class="delete-btn" @click="deleteFile(file.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button 
        :disabled="currentPage === 1" 
        @click="currentPage--"
      >
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <button 
        :disabled="currentPage === totalPages" 
        @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 上传模态框 -->
    <div v-if="showUploadModal" class="modal-overlay" @click="showUploadModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>上传文件</h2>
          <button class="close-btn" @click="showUploadModal = false">×</button>
        </div>
        <div class="modal-body">
          <div 
            class="upload-area"
            @dragover.prevent="isDragging = true"
            @dragleave="isDragging = false"
            @drop.prevent="handleDrop"
            :class="{ dragging: isDragging }"
          >
            <input 
              type="file" 
              ref="fileInput" 
              @change="handleFileSelect"
              style="display: none"
            />
            <span class="upload-icon">📁</span>
            <p>点击选择文件或拖拽文件到此</p>
            <button class="select-btn" @click="$refs.fileInput.click()">
              选择文件
            </button>
          </div>
          <div v-if="selectedFile" class="file-preview">
            <p>已选择: <strong>{{ selectedFile.name }}</strong></p>
            <p>大小: {{ formatFileSize(selectedFile.size) }}</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showUploadModal = false">取消</button>
          <button 
            class="confirm-btn" 
            @click="uploadFile"
            :disabled="!selectedFile || isUploading"
          >
            {{ isUploading ? '上传中...' : '上传' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 提示信息 -->
    <div v-if="message" :class="['message', message.type]">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const files = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalCount = ref(0)
const showUploadModal = ref(false)
const selectedFile = ref(null)
const fileInput = ref(null)
const isDragging = ref(false)
const isUploading = ref(false)
const message = ref(null)

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize))

// 格式化日期
const formatDate = (dateStr) => {
  return dateStr.split('.')[0].replace('T', ' ')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 获取文件列表
const fetchFiles = async () => {
  try {
    const response = await fetch(`/api/files?page=${currentPage.value}&pageSize=${pageSize}`)
    const data = await response.json()
    files.value = data.data || []
    totalCount.value = data.total || 0
  } catch (error) {
    showMessage('获取文件列表失败', 'error')
    console.error(error)
  }
}

// 下载文件
const downloadFile = (file) => {
  const link = document.createElement('a')
  link.href = file.fileurl
  link.download = file.filename
  link.click()
  showMessage('开始下载: ' + file.filename, 'success')
}

// 删除文件
const deleteFile = async (fileId) => {
  if (!confirm('确定要删除此文件吗?')) return
  
  try {
    const response = await fetch(`/api/files/${fileId}`, { method: 'DELETE' })
    if (response.ok) {
      showMessage('文件删除成功', 'success')
      fetchFiles()
    }
  } catch (error) {
    showMessage('删除文件失败', 'error')
    console.error(error)
  }
}

// 处理文件选择
const handleFileSelect = (event) => {
  selectedFile.value = event.target.files[0]
}

// 处理拖拽
const handleDrop = (event) => {
  isDragging.value = false
  selectedFile.value = event.dataTransfer.files[0]
}

// 上传文件
const uploadFile = async () => {
  if (!selectedFile.value) return

  isUploading.value = true
  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const response = await fetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })
    
    if (response.ok) {
      showMessage('文件上传成功', 'success')
      showUploadModal.value = false
      selectedFile.value = null
      currentPage.value = 1
      fetchFiles()
    } else {
      showMessage('文件上传失败', 'error')
    }
  } catch (error) {
    showMessage('上传出错', 'error')
    console.error(error)
  } finally {
    isUploading.value = false
  }
}

// 显示提示信息
const showMessage = (text, type) => {
  message.value = { text, type }
  setTimeout(() => {
    message.value = null
  }, 3000)
}

// 监听分页变化
const handlePageChange = () => {
  fetchFiles()
}

// 初始化
onMounted(() => {
  fetchFiles()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.file-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
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
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background-color: #f5f7fa;
  border-bottom: 2px solid #e4e7eb;
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
  color: #606266;
}

.file-row:hover {
  background-color: #f5f7fa;
}

.filename {
  display: flex;
  align-items: center;
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
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
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
  max-width: 500px;
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
  padding: 30px 20px;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
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

.file-preview {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.file-preview p {
  color: #606266;
  font-size: 14px;
  margin: 5px 0;
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
</style>