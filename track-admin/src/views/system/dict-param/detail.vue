<template>
  <div class="dict-param-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="参数ID">
          <el-input :model-value="form.paramId || '保存后自动生成'" disabled />
        </el-form-item>

        <el-form-item label="参数名称" prop="paramName">
          <el-input
            v-model="form.paramName"
            :disabled="isViewMode"
            maxlength="100"
            placeholder="请输入参数名称"
          />
        </el-form-item>

        <el-form-item label="参数项">
          <div class="item-box">
            <el-button v-if="!isViewMode" type="primary" plain @click="addItem">新增参数项</el-button>
            <el-table :data="itemList" border style="margin-top: 10px">
              <el-table-column label="编码" min-width="220">
                <template #default="{ row }">
                  <el-input
                    v-model="row.itemCode"
                    :disabled="isViewMode"
                    maxlength="100"
                    placeholder="请输入编码"
                  />
                </template>
              </el-table-column>
              <el-table-column label="名称" min-width="220">
                <template #default="{ row }">
                  <el-input
                    v-model="row.itemName"
                    :disabled="isViewMode"
                    maxlength="100"
                    placeholder="请输入名称"
                  />
                </template>
              </el-table-column>
              <el-table-column v-if="!isViewMode" label="操作" width="100" fixed="right">
                <template #default="{ row, $index }">
                  <el-button
                    :disabled="row.status === 1"
                    link
                    type="danger"
                    @click="removeItem(row, $index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>

      <div class="footer-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button v-if="!isViewMode" type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addDictParam, getDictParamDetail, updateDictParam } from '../../../api/dict-param'
import { useTabStore } from '../../../store/tab'

const route = useRoute()
const router = useRouter()
const tabStore = useTabStore()

const formRef = ref(null)
const saving = ref(false)
const deletedItemIds = ref([])

const form = reactive({
  id: null,
  paramId: '',
  paramName: ''
})

const itemList = ref([])

const action = computed(() => route.params.action || 'view')
const isViewMode = computed(() => action.value === 'view')
const isEditMode = computed(() => action.value === 'edit')
const isAddMode = computed(() => action.value === 'add')

const actionLabelMap = {
  add: '新增',
  edit: '编辑',
  view: '查看'
}

const pageTitle = computed(() => {
  if (isAddMode.value) return '新增参数'
  if (isEditMode.value) return '编辑参数'
  return '查看参数'
})

const rules = reactive({
  paramName: [{ required: true, message: '参数名称不能为空', trigger: 'blur' }]
})

const getDisplayName = () => {
  const fromForm = (form.paramName || '').trim()
  if (fromForm) return fromForm

  const fromQuery = (route.query.paramName || '').toString().trim()
  if (fromQuery) return fromQuery

  return '未命名'
}

const buildTabTitle = () => `${actionLabelMap[action.value] || '查看'}-${getDisplayName()}参数`

const syncTabTitle = () => {
  tabStore.updateTabTitle(route.fullPath, buildTabTitle())
}

const loadDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('缺少参数ID')
    router.push('/system/dict-param')
    return
  }

  const res = await getDictParamDetail(id)
  if (res.code !== 200 || !res.data) {
    ElMessage.error(res.message || '加载失败')
    router.push('/system/dict-param')
    return
  }

  form.id = res.data.id
  form.paramId = res.data.paramId
  form.paramName = res.data.paramName
  itemList.value = (res.data.items || []).map(item => ({
    id: item.id,
    itemCode: item.itemCode,
    itemName: item.itemName,
    status: item.status
  }))

  syncTabTitle()
}

const addItem = () => {
  itemList.value.push({
    id: null,
    itemCode: '',
    itemName: '',
    status: 0
  })
}

const removeItem = (row, index) => {
  if (row.id) {
    deletedItemIds.value.push(row.id)
  }
  itemList.value.splice(index, 1)
}

const validateItems = () => {
  const codeSet = new Set()
  for (const item of itemList.value) {
    const code = (item.itemCode || '').trim()
    const name = (item.itemName || '').trim()

    if (!code) {
      ElMessage.warning('参数项编码不能为空')
      return false
    }
    if (!name) {
      ElMessage.warning('参数项名称不能为空')
      return false
    }
    if (codeSet.has(code)) {
      ElMessage.warning(`参数项编码重复: ${code}`)
      return false
    }

    codeSet.add(code)
  }
  return true
}

const handleSave = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!validateItems()) return

  saving.value = true
  const payload = {
    id: form.id,
    paramName: form.paramName,
    items: itemList.value.map(item => ({
      id: item.id,
      itemCode: (item.itemCode || '').trim(),
      itemName: (item.itemName || '').trim()
    })),
    deletedItemIds: [...new Set(deletedItemIds.value)]
  }

  try {
    const res = isAddMode.value ? await addDictParam(payload) : await updateDictParam(payload)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      closeCurrentTab()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const closeCurrentTab = () => {
  tabStore.removeTab(route.fullPath)
  router.push('/system/dict-param')
}

const handleCancel = () => {
  closeCurrentTab()
}

watch(
  () => form.paramName,
  () => {
    syncTabTitle()
  }
)

onMounted(async () => {
  syncTabTitle()
  if (!isAddMode.value) {
    await loadDetail()
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-box {
  width: 100%;
}

.footer-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
