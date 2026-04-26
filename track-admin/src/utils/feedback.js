import { ElMessage } from 'element-plus'

export const showActionError = (fallbackMessage) => {
  ElMessage.error(fallbackMessage || '操作失败')
}

export const showActionSuccess = (message) => {
  ElMessage.success(message || '操作成功')
}
