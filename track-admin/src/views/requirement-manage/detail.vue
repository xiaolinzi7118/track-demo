<template>
  <div class="requirement-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>需求详情</span>
        </div>
      </template>

      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="需求ID" :span="2">{{ detail.requirementId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="需求状态">
          <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">{{ priorityText(detail.priority) }}</el-descriptions-item>
        <el-descriptions-item label="需求标题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="需求创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="所属业务线">{{ detail.businessLineName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="需求提出人">{{ detail.proposerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="期望上线时间">{{ detail.expectedOnlineDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责开发团队" :span="2">{{ detail.devTeamName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">
          <div class="desc-block">{{ detail.description || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider>操作日志</el-divider>

      <el-table :data="detail?.logs || []" border stripe>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operateTime" label="操作时间" width="180">
          <template #default="{ row }">{{ formatTime(row.operateTime) }}</template>
        </el-table-column>
        <el-table-column prop="actionType" label="操作类型" width="140">
          <template #default="{ row }">{{ actionTypeText(row.actionType) }}</template>
        </el-table-column>
        <el-table-column label="变更前状态" width="130">
          <template #default="{ row }">{{ statusText(row.fromStatus) }}</template>
        </el-table-column>
        <el-table-column label="变更后状态" width="130">
          <template #default="{ row }">{{ statusText(row.toStatus) }}</template>
        </el-table-column>
        <el-table-column prop="opinion" label="操作意见" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.opinion || '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="footer-actions">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTabStore } from '../../store/tab'
import { getRequirementDetail } from '../../api/requirement'

const route = useRoute()
const router = useRouter()
const tabStore = useTabStore()

const loading = ref(false)
const detail = ref(null)

const statusMap = {
  PENDING_REVIEW: '待审核',
  SCHEDULING: '排期中',
  DEVELOPING: '开发中',
  TESTING: '测试中',
  ONLINE: '已上线',
  OFFLINE: '已下线',
  REJECTED: '审核不通过'
}

const actionTypeMap = {
  CREATE: '提交创建',
  STATUS_CHANGE: '状态变更',
  EDIT_RESUBMIT: '编辑重提'
}

const statusText = (status) => {
  if (!status) return '-'
  return statusMap[status] || status
}

const actionTypeText = (type) => {
  if (!type) return '-'
  return actionTypeMap[type] || type
}

const priorityText = (priority) => {
  if (priority === 'P0') return 'P0(高)'
  if (priority === 'P1') return 'P1(中)'
  if (priority === 'P2') return 'P2(低)'
  return priority || '-'
}

const statusTagType = (status) => {
  if (status === 'PENDING_REVIEW') return 'warning'
  if (status === 'SCHEDULING') return 'info'
  if (status === 'DEVELOPING') return ''
  if (status === 'TESTING') return 'success'
  if (status === 'ONLINE') return 'success'
  if (status === 'OFFLINE') return 'danger'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  const str = String(time).replace('T', ' ')
  return str.length > 19 ? str.slice(0, 19) : str
}

const loadDetail = async () => {
  const requirementId = route.query.requirementId
  if (!requirementId) {
    ElMessage.error('缺少需求ID')
    router.replace('/requirement-manage')
    return
  }

  loading.value = true
  try {
    const res = await getRequirementDetail(requirementId)
    if (res.code === 200) {
      detail.value = res.data
      if (res.data?.title) {
        tabStore.updateTabTitle(route.fullPath, `需求详情-${res.data.title}`)
      }
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (error) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  router.push('/requirement-manage')
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.desc-block {
  white-space: pre-wrap;
  word-break: break-word;
}

.footer-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

