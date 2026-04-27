<template>
  <div class="requirement-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>需求详情</span>
          <el-button @click="handleClose">返回列表</el-button>
        </div>
      </template>

      <template v-if="detail">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="详情信息" name="detail">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="需求ID" :span="2">{{ detail.requirementId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="需求状态">
                <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="priorityTagType(detail.priority)">{{ priorityText(detail.priority) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="需求标题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
              <el-descriptions-item label="需求创建时间">{{ formatDateTime(detail.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(detail.updateTime) }}</el-descriptions-item>
              <el-descriptions-item label="所属业务线">{{ detail.businessLineName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属部门">{{ detail.department || '-' }}</el-descriptions-item>
              <el-descriptions-item label="需求提出人">{{ detail.proposerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="期望上线时间">{{ detail.expectedOnlineDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="负责开发团队" :span="2">{{ detail.devTeamName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="需求描述" :span="2">
                <div class="desc-block">{{ detail.description || '-' }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="需求截图" :span="2">
                <ImageUpload :model-value="detail.screenshotFileId" :readonly="true" />
              </el-descriptions-item>
            </el-descriptions>

            <el-divider>操作日志</el-divider>

            <el-table :data="detail.logs || []" border stripe>
              <el-table-column prop="operatorName" label="操作人" width="120" />
              <el-table-column prop="operateTime" label="操作时间" width="180">
                <template #default="{ row }">{{ formatDateTime(row.operateTime) }}</template>
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
          </el-tab-pane>

          <el-tab-pane v-if="canEdit" label="编辑重提" name="edit">
            <el-form
              ref="formRef"
              :model="form"
              :rules="formRules"
              label-width="120px"
              class="edit-form"
            >
              <el-form-item label="需求标题" prop="title">
                <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入需求标题" />
              </el-form-item>
              <el-form-item label="所属业务线" prop="businessLineCode">
                <el-select v-model="form.businessLineCode" placeholder="请选择业务线" style="width: 100%">
                  <el-option
                    v-for="item in businessLineOptions"
                    :key="item.itemCode"
                    :label="item.itemName"
                    :value="item.itemCode"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="优先级" prop="priority">
                <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
                  <el-option label="P0(高)" value="P0" />
                  <el-option label="P1(中)" value="P1" />
                  <el-option label="P2(低)" value="P2" />
                </el-select>
              </el-form-item>
              <el-form-item label="期望上线时间" prop="expectedOnlineDate">
                <el-date-picker
                  v-model="form.expectedOnlineDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择日期"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="负责开发团队" prop="devTeamCode">
                <el-select v-model="form.devTeamCode" placeholder="请选择开发团队" style="width: 100%">
                  <el-option
                    v-for="item in devTeamOptions"
                    :key="item.itemCode"
                    :label="item.itemName"
                    :value="item.itemCode"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="需求描述">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入需求的详细背景、目标和业务逻辑（非必填）"
                />
              </el-form-item>
              <el-form-item label="需求截图">
                <ImageUpload
                  v-model="form.screenshotFileId"
                  :accept="['jpg', 'jpeg', 'png']"
                  :max-size="2 * 1024 * 1024"
                />
              </el-form-item>
            </el-form>

            <div class="footer-actions">
              <el-button @click="activeTab = 'detail'">取消</el-button>
              <el-button type="primary" :loading="submitLoading" @click="handleSubmitForm">
                提交
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTabStore } from '../../store/tab'
import { getDictParamIdsList } from '../../api/dict-param'
import ImageUpload from '../../components/ImageUpload.vue'
import { getRequirementDetail, resubmitRequirement } from '../../api/requirement'
import { showActionError, showActionSuccess } from '../../utils/feedback'
import {
  actionTypeText,
  formatDateTime,
  priorityTagType,
  priorityText,
  statusTagType,
  statusText
} from './display'

const BUSINESS_LINE_PARAM_ID = 'DICT2026042200000001'
const DEPT_PARAM_ID = 'SYS_DEPT'
const DEV_TEAM_EXTRA_ATTR = '开发'

const route = useRoute()
const router = useRouter()
const tabStore = useTabStore()

const loading = ref(false)
const submitLoading = ref(false)
const detail = ref(null)
const activeTab = ref('detail')
const formRef = ref(null)
const originalFormSnapshot = ref(null)
const businessLineOptions = ref([])
const devTeamOptions = ref([])

const form = reactive({
  requirementId: '',
  title: '',
  businessLineCode: '',
  priority: '',
  expectedOnlineDate: '',
  devTeamCode: '',
  description: '',
  screenshotFileId: ''
})

const formRules = reactive({
  title: [
    { required: true, message: '请输入需求标题', trigger: 'blur' },
    { min: 1, max: 200, message: '长度限制 1-200 字符', trigger: 'blur' }
  ],
  businessLineCode: [{ required: true, message: '请选择所属业务线', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  expectedOnlineDate: [{ required: true, message: '请选择期望上线时间', trigger: 'change' }],
  devTeamCode: [{ required: true, message: '请选择负责开发团队', trigger: 'change' }]
})

const requestedMode = computed(() => String(route.query.mode || '').toLowerCase())
const canEdit = computed(() => (detail.value?.availableActions || []).some(item => item.actionType === 'EDIT'))

const normalizeFormPayload = (data) => ({
  title: (data.title || '').trim(),
  businessLineCode: data.businessLineCode || '',
  priority: data.priority || '',
  expectedOnlineDate: data.expectedOnlineDate || '',
  devTeamCode: data.devTeamCode || '',
  description: data.description ? data.description.trim() : null,
  screenshotFileId: data.screenshotFileId || null
})

const isFormChanged = () => {
  if (!originalFormSnapshot.value) return true
  const current = normalizeFormPayload(form)
  return JSON.stringify(current) !== JSON.stringify(originalFormSnapshot.value)
}

const fillEditForm = (data) => {
  form.requirementId = data.requirementId || ''
  form.title = data.title || ''
  form.businessLineCode = data.businessLineCode || ''
  form.priority = data.priority || ''
  form.expectedOnlineDate = data.expectedOnlineDate || ''
  form.devTeamCode = data.devTeamCode || ''
  form.description = data.description || ''
  form.screenshotFileId = data.screenshotFileId || ''
  originalFormSnapshot.value = normalizeFormPayload(form)
}

const syncActiveTab = () => {
  if (requestedMode.value === 'edit' && canEdit.value) {
    activeTab.value = 'edit'
    return
  }
  activeTab.value = 'detail'
}

const loadDictOptions = async () => {
  const res = await getDictParamIdsList([BUSINESS_LINE_PARAM_ID, DEPT_PARAM_ID])
  if (res.code !== 200) {
    showActionError('加载字典数据失败')
    return
  }
  const dictMap = new Map((res.data || []).map(item => [item.paramId, item]))
  businessLineOptions.value = ((dictMap.get(BUSINESS_LINE_PARAM_ID) || {}).items || []).filter(item => item.status === 0)
  devTeamOptions.value = ((dictMap.get(DEPT_PARAM_ID) || {}).items || [])
    .filter(item => item.status === 0 && item.extraAttr === DEV_TEAM_EXTRA_ATTR)
}

const loadDetail = async () => {
  const requirementId = route.query.requirementId
  if (!requirementId) {
    showActionError('缺少需求ID')
    router.replace('/requirement-manage')
    return
  }

  loading.value = true
  try {
    const res = await getRequirementDetail(requirementId)
    if (res.code !== 200) {
      showActionError('加载需求详情失败')
      return
    }
    detail.value = res.data
    fillEditForm(res.data)
    syncActiveTab()
    if (res.data?.title) {
      const titlePrefix = activeTab.value === 'edit' ? '需求编辑' : '需求详情'
      tabStore.updateTabTitle(route.fullPath, `${titlePrefix}-${res.data.title}`)
    }
  } catch (error) {
    showActionError('加载需求详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmitForm = async () => {
  if (!canEdit.value) {
    showActionError('当前需求不可编辑')
    return
  }
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!isFormChanged()) {
    ElMessage.warning('至少修改一个字段后才能提交')
    return
  }

  submitLoading.value = true
  try {
    const res = await resubmitRequirement({
      requirementId: form.requirementId,
      ...normalizeFormPayload(form)
    })
    if (res.code === 200) {
      showActionSuccess('编辑并提交成功')
      await loadDetail()
      return
    }
    showActionError('编辑提交失败')
  } catch (error) {
    showActionError('编辑提交失败')
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  router.push('/requirement-manage')
}

onMounted(async () => {
  await loadDictOptions()
  await loadDetail()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.desc-block {
  white-space: pre-wrap;
  word-break: break-word;
}

.edit-form {
  max-width: 760px;
  margin-top: 8px;
}

.footer-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

