<template>
  <div class="image-upload">
    <div v-if="canUpload" class="upload-actions">
      <el-upload
        :show-file-list="false"
        :accept="acceptAttr"
        :http-request="handleUploadRequest"
        :before-upload="beforeUpload"
        :disabled="uploading || disabled"
      >
        <el-button type="primary" :loading="uploading">上传图片</el-button>
      </el-upload>
      <el-button v-if="modelValue" @click="clearImage">移除</el-button>
    </div>

    <div v-if="previewUrl" class="preview-wrap">
      <img class="thumb" :src="previewUrl" alt="preview" @click="openPreview" />
      <el-button link type="primary" @click="openPreview">查看</el-button>
    </div>
    <div v-else class="empty-text">未上传图片</div>

    <div class="tip-text">{{ tipText }}</div>

    <el-dialog v-model="previewVisible" title="图片预览" width="760px" append-to-body>
      <div class="dialog-image-wrap">
        <img class="dialog-image" :src="previewUrl" alt="preview" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getImagePreviewBlob, uploadImage } from '../api/file'

const DEFAULT_MAX_SIZE = 2 * 1024 * 1024
const MIME_TO_EXT = {
  'image/jpeg': 'jpg',
  'image/jpg': 'jpg',
  'image/png': 'png'
}

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  accept: {
    type: [Array, String],
    default: () => ['jpg', 'png']
  },
  maxSize: {
    type: Number,
    default: DEFAULT_MAX_SIZE
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const uploading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')

const normalizedAccept = computed(() => {
  const raw = Array.isArray(props.accept) ? props.accept : String(props.accept || '').split(',')
  const values = raw
    .map((item) => String(item || '').trim().toLowerCase())
    .filter(Boolean)
    .map((item) => {
      if (item.startsWith('.')) return item.slice(1)
      if (item.startsWith('image/')) return MIME_TO_EXT[item] || ''
      return item
    })
    .filter(Boolean)

  if (!values.length) {
    return ['jpg', 'png']
  }
  return [...new Set(values)]
})

const acceptAttr = computed(() => normalizedAccept.value.map((item) => `.${item}`).join(','))
const canUpload = computed(() => !props.readonly && !props.disabled)

const tipText = computed(() => {
  const maxSize = props.maxSize > 0 ? props.maxSize : DEFAULT_MAX_SIZE
  return `支持格式: ${normalizedAccept.value.join('/')}，最大 ${(maxSize / 1024 / 1024).toFixed(1)}MB`
})

const revokePreviewUrl = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

const normalizeExt = (name) => {
  const text = String(name || '').trim().toLowerCase()
  if (!text) return ''
  if (text === 'jpeg') return 'jpg'
  if (text.startsWith('.')) return normalizeExt(text.slice(1))
  return text
}

const readBlobAsText = (blob) =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = reject
    reader.readAsText(blob)
  })

const createPreview = (blob) => {
  revokePreviewUrl()
  previewUrl.value = URL.createObjectURL(blob)
}

const loadRemotePreview = async (fileId) => {
  if (!fileId) {
    revokePreviewUrl()
    return
  }
  try {
    const blob = await getImagePreviewBlob(fileId)
    if (!(blob instanceof Blob)) {
      revokePreviewUrl()
      return
    }
    if ((blob.type || '').includes('application/json')) {
      const text = await readBlobAsText(blob).catch(() => '')
      try {
        const data = JSON.parse(text)
        ElMessage.error(data.message || '加载图片失败')
      } catch {
        ElMessage.error('加载图片失败')
      }
      revokePreviewUrl()
      return
    }
    createPreview(blob)
  } catch (error) {
    revokePreviewUrl()
  }
}

watch(
  () => props.modelValue,
  (fileId) => {
    loadRemotePreview(fileId)
  },
  { immediate: true }
)

const beforeUpload = (file) => {
  const ext = normalizeExt(String(file.name || '').split('.').pop())
  const maxSize = props.maxSize > 0 ? props.maxSize : DEFAULT_MAX_SIZE
  if (!ext || !normalizedAccept.value.includes(ext)) {
    ElMessage.warning(`仅支持 ${normalizedAccept.value.join('/')} 格式`)
    return false
  }
  if (file.size > maxSize) {
    ElMessage.warning(`图片大小不能超过 ${(maxSize / 1024 / 1024).toFixed(1)}MB`)
    return false
  }
  return true
}

const handleUploadRequest = async (options) => {
  uploading.value = true
  try {
    const res = await uploadImage(options.file)
    if (res.code !== 200 || !res.data?.fileId) {
      const msg = res.message || '上传失败'
      ElMessage.error(msg)
      options.onError?.(new Error(msg))
      return
    }
    emit('update:modelValue', res.data.fileId)
    emit('change', res.data.fileId)
    createPreview(options.file)
    options.onSuccess?.(res)
    ElMessage.success('上传成功')
  } catch (error) {
    options.onError?.(error)
  } finally {
    uploading.value = false
  }
}

const clearImage = () => {
  emit('update:modelValue', '')
  emit('change', '')
  revokePreviewUrl()
}

const openPreview = () => {
  if (!previewUrl.value) return
  previewVisible.value = true
}

onBeforeUnmount(() => {
  revokePreviewUrl()
})
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-wrap {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.thumb {
  width: 140px;
  height: 88px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
}

.empty-text {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.tip-text {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.dialog-image-wrap {
  width: 100%;
  display: flex;
  justify-content: center;
}

.dialog-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}
</style>
