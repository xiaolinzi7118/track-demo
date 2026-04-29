<template>
  <div class="requirement-manage-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>需求管理</span>
          <el-button v-if="canAdd" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增需求
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="需求标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入需求标题"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="需求状态">
          <el-select
            v-model="searchForm.statusList"
            multiple
            collapse-tags
            collapse-tags-tooltip
            clearable
            placeholder="请选择状态"
            style="width: 220px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select
            v-model="searchForm.priority"
            clearable
            placeholder="请选择优先级"
            style="width: 140px"
          >
            <el-option label="P0(高)" value="P0" />
            <el-option label="P1(中)" value="P1" />
            <el-option label="P2(低)" value="P2" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求提出人">
          <el-input
            v-model="searchForm.proposerName"
            placeholder="请输入提出人"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="所属业务线">
          <el-select
            v-model="searchForm.businessLineCode"
            clearable
            placeholder="请选择业务线"
            style="width: 200px"
          >
            <el-option
              v-for="item in businessLineOptions"
              :key="item.itemCode"
              :label="item.itemName"
              :value="item.itemCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input
            v-model="searchForm.department"
            placeholder="请输入所属部门"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="requirementId" label="需求ID" min-width="220" />
        <el-table-column label="需求标题" min-width="220">
          <template #default="{ row }">
            <el-button type="primary" link class="title-link" @click="openDetailTab(row)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="需求状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="110" sortable="custom">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)">
              {{ priorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="proposerName" label="需求提出人" width="120" />
        <el-table-column prop="businessLineName" label="所属业务线" width="140" />
        <el-table-column prop="department" label="所属部门" width="140" />
        <el-table-column prop="createTime" label="创建时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="getStatusActions(row).length > 1">
              <el-dropdown @command="(targetStatus) => handleStatusCommand(row, targetStatus)">
                <el-button type="primary" link>
                  变更状态
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="action in getStatusActions(row)"
                      :key="action.targetStatus"
                      :command="action.targetStatus"
                    >
                      {{ getStatusActionLabel(action) }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else-if="getStatusActions(row).length === 1">
              <el-button type="primary" link @click="triggerStatusAction(row, getStatusActions(row)[0])">
                {{ getStatusActionLabel(getStatusActions(row)[0]) }}
              </el-button>
            </template>
            <el-button
              v-if="hasEditAction(row)"
              type="primary"
              link
              @click="openEditTab(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="loadList"
        >
          <template #total>
            共 {{ Math.ceil(total / pageSize) || 0 }} 页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <el-dialog
      v-model="formDialogVisible"
      title="新增需求"
      width="760px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
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
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitForm">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store/user'
import { getDictParamIdsList } from '../../api/dict-param'
import ImageUpload from '../../components/ImageUpload.vue'
import {
  addRequirement,
  changeRequirementStatus,
  getRequirementList
} from '../../api/requirement'
import { showActionError, showActionSuccess } from '../../utils/feedback'
import {
  formatDateTime,
  priorityTagType,
  priorityText,
  requirementStatusOptions,
  statusTagType,
  statusText
} from './display'

const BUSINESS_LINE_PARAM_ID = 'DICT2026042200000001'
const DEPT_PARAM_ID = 'SYS_DEPT'
const DEV_TEAM_EXTRA_ATTR = '开发'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const sortField = ref('createTime')
const sortOrder = ref('desc')

const businessLineOptions = ref([])
const devTeamOptions = ref([])
const statusOptions = requirementStatusOptions

const searchForm = reactive({
  title: '',
  statusList: [],
  priority: '',
  proposerName: '',
  businessLineCode: '',
  department: ''
})

const formDialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
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

const canAdd = computed(() => userStore.hasPermission('requirement-manage:add'))

const getStatusActions = (row) => (row.availableActions || []).filter(item => item.actionType === 'CHANGE_STATUS')
const hasEditAction = (row) => (row.availableActions || []).some(item => item.actionType === 'EDIT')
const findStatusAction = (row, targetStatus) => getStatusActions(row).find(item => item.targetStatus === targetStatus)
const getTargetStatusText = (action) => statusText(action?.targetStatus) || action?.targetStatus || ''
const getStatusActionLabel = (action) => {
  const targetStatusText = getTargetStatusText(action)
  return targetStatusText ? `变更为${targetStatusText}` : '变更状态'
}

const normalizeFormPayload = (data) => ({
  title: (data.title || '').trim(),
  businessLineCode: data.businessLineCode || '',
  priority: data.priority || '',
  expectedOnlineDate: data.expectedOnlineDate || '',
  devTeamCode: data.devTeamCode || '',
  description: data.description ? data.description.trim() : null,
  screenshotFileId: data.screenshotFileId || null
})

const clearForm = () => {
  form.title = ''
  form.businessLineCode = ''
  form.priority = ''
  form.expectedOnlineDate = ''
  form.devTeamCode = ''
  form.description = ''
  form.screenshotFileId = ''
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

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      title: searchForm.title || undefined,
      statusList: searchForm.statusList.length ? searchForm.statusList.join(',') : undefined,
      priority: searchForm.priority || undefined,
      proposerName: searchForm.proposerName || undefined,
      businessLineCode: searchForm.businessLineCode || undefined,
      department: searchForm.department || undefined,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const res = await getRequirementList(params)
    if (res.code === 200) {
      tableData.value = res.data.content || []
      total.value = res.data.totalElements || 0
      return
    }
    showActionError('加载需求列表失败')
  } catch (error) {
    showActionError('加载需求列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadList()
}

const handlePageSizeChange = () => {
  pageNum.value = 1
  loadList()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.statusList = []
  searchForm.priority = ''
  searchForm.proposerName = ''
  searchForm.businessLineCode = ''
  searchForm.department = ''
  sortField.value = 'createTime'
  sortOrder.value = 'desc'
  pageNum.value = 1
  loadList()
}

const handleSortChange = ({ prop, order }) => {
  if (!prop || !order) {
    sortField.value = 'createTime'
    sortOrder.value = 'desc'
  } else {
    sortField.value = prop
    sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  }
  pageNum.value = 1
  loadList()
}

const openRequirementTab = (row, mode) => {
  router.push({
    path: '/requirement-manage/detail',
    query: {
      requirementId: row.requirementId,
      mode
    }
  })
}

const openDetailTab = (row) => {
  openRequirementTab(row, 'detail')
}

const openEditTab = (row) => {
  openRequirementTab(row, 'edit')
}

const handleAdd = () => {
  clearForm()
  formDialogVisible.value = true
}

const triggerStatusAction = async (row, action) => {
  if (!action) return
  if (action.needOpinion) {
    try {
      const { value } = await ElMessageBox.prompt(
        `请填写“${row.title}”变更为“${getTargetStatusText(action)}”的原因`,
        '审核不通过原因',
        {
          inputType: 'textarea',
          inputPlaceholder: '请输入原因',
          inputValidator: (input) => {
            if (!input || !input.trim()) return '原因不能为空'
            return true
          }
        }
      )
      await submitStatusChange(row, action.targetStatus, value)
    } catch (error) {
      // canceled
    }
    return
  }

  try {
    await ElMessageBox.confirm(
      `请确认是否将“${row.title}”变更为“${getTargetStatusText(action)}”？`,
      '状态变更确认',
      { type: 'warning' }
    )
    await submitStatusChange(row, action.targetStatus, null)
  } catch (error) {
    // canceled
  }
}

const handleStatusCommand = (row, targetStatus) => {
  const action = findStatusAction(row, targetStatus)
  triggerStatusAction(row, action)
}

const submitStatusChange = async (row, targetStatus, opinion) => {
  const res = await changeRequirementStatus({
    requirementId: row.requirementId,
    targetStatus,
    opinion
  })
  if (res.code === 200) {
    showActionSuccess('状态变更成功')
    loadList()
    return
  }
  showActionError('状态变更失败')
}

const handleSubmitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const res = await addRequirement(normalizeFormPayload(form))
    if (res.code === 200) {
      showActionSuccess('新增需求成功')
      formDialogVisible.value = false
      loadList()
      return
    }
    showActionError('新增需求失败')
  } catch (error) {
    showActionError('新增需求失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadDictOptions()
  await loadList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 18px;
}

.title-link {
  color: #1f6feb;
  padding: 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

