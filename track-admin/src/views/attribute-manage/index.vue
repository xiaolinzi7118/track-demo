<template>
  <div class="attribute-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>属性管理</span>
          <div class="header-actions">
            <el-button v-permission="'attribute:view'" @click="handleDownloadTemplate">下载模板</el-button>
            <el-button
              v-permission="'attribute:add'"
              :loading="importLoading"
              @click="handleTriggerImport"
            >
              导入属性
            </el-button>
            <el-button v-permission="'attribute:add'" type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增属性
            </el-button>
            <input
              ref="importInputRef"
              class="hidden-file-input"
              type="file"
              accept=".xlsx"
              @change="handleImportChange"
            >
          </div>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="属性名称/属性字段"
              clearable
              style="width: 220px"
            />
          </el-form-item>
          <el-form-item label="属性类型">
            <el-select v-model="searchForm.attributeType" clearable placeholder="请选择" style="width: 160px">
              <el-option label="用户属性" value="user" />
              <el-option label="系统属性" value="system" />
              <el-option label="个性化属性" value="custom" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="attributeName" label="属性名称" min-width="130" />
        <el-table-column prop="attributeField" label="属性字段" min-width="120" />
        <el-table-column prop="attributeType" label="属性类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="attributeTypeTagType(row.attributeType)">
              {{ attributeTypeLabel(row.attributeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="SDK取值路径" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            {{ buildSdkPath(row) }}
          </template>
        </el-table-column>
        <el-table-column label="来源类型" width="120">
          <template #default="{ row }">
            {{ sourceTypeLabel(row.sourceType) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default>
            <el-tag size="small" type="success">生效</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'attribute:edit'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="'attribute:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
          @size-change="handleSearch"
          @current-change="handleSearch"
        >
          <template #total>
            共 {{ Math.ceil(total / pageSize) || 0 }} 页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑属性' : '新增属性'" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="属性名称" prop="attributeName">
              <el-input v-model="form.attributeName" maxlength="100" placeholder="请输入属性名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="属性字段" prop="attributeField">
              <el-input v-model="form.attributeField" maxlength="100" placeholder="请输入属性字段" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="属性类型" prop="attributeType">
              <el-select v-model="form.attributeType" placeholder="请选择" @change="onAttributeTypeChange">
                <el-option label="用户属性" value="user" />
                <el-option label="系统属性" value="system" />
                <el-option label="个性化属性" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="form.attributeType === 'custom'">
          <el-divider content-position="left">个性化属性配置</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="来源类型" prop="sourceType">
                <el-select v-model="form.sourceType" placeholder="请选择来源类型" @change="onSourceTypeChange">
                  <el-option label="节点内容" value="node_content" />
                  <el-option label="接口数据" value="api_data" />
                  <el-option label="全局对象" value="global_object" />
                  <el-option label="本地缓存" value="local_cache" />
                  <el-option label="静态值" value="static_value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.sourceType === 'api_data'">
              <el-form-item label="接口选择" prop="interfaceId">
                <el-select v-model="form.interfaceId" clearable filterable placeholder="请选择接口" @change="updateInterfacePath">
                  <el-option
                    v-for="item in interfaceOptions"
                    :key="item.id"
                    :label="`${item.name} (${item.path})`"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12" v-if="form.sourceType !== 'node_content'">
              <el-form-item label="变量路径/值" prop="sourceValue">
                <el-input v-model="form.sourceValue" :placeholder="sourceValuePlaceholder(form.sourceType)" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="默认值">
                <el-input v-model="form.defaultValue" placeholder="取值失败时兜底值(选填)" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllApiInterfaces } from '../../api/track'
import { addAttribute, deleteAttribute, getAttributeList, importAttribute, updateAttribute } from '../../api/attribute'

const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const importInputRef = ref(null)

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const interfaceOptions = ref([])

const searchForm = reactive({
  keyword: '',
  attributeType: ''
})

const form = reactive({
  id: undefined,
  attributeName: '',
  attributeField: '',
  attributeType: '',
  sourceType: '',
  sourceValue: '',
  interfaceId: null,
  interfacePath: '',
  defaultValue: ''
})

const rules = reactive({
  attributeName: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  attributeField: [{ required: true, message: '请输入属性字段', trigger: 'blur' }],
  attributeType: [{ required: true, message: '请选择属性类型', trigger: 'change' }],
  sourceType: [{
    validator: (_, value, callback) => {
      if (form.attributeType !== 'custom') {
        callback()
        return
      }
      if (!value) {
        callback(new Error('请选择来源类型'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  interfaceId: [{
    validator: (_, value, callback) => {
      if (form.attributeType === 'custom' && form.sourceType === 'api_data' && !value) {
        callback(new Error('请选择接口'))
        return
      }
      callback()
    },
    trigger: 'change'
  }],
  sourceValue: [{
    validator: (_, value, callback) => {
      if (form.attributeType === 'custom' && form.sourceType !== 'node_content' && !value) {
        callback(new Error('请输入变量路径/值'))
        return
      }
      callback()
    },
    trigger: 'blur'
  }]
})

const fetchInterfaces = async () => {
  try {
    const res = await getAllApiInterfaces()
    if (res.code === 200) {
      interfaceOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取接口列表失败', error)
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getAttributeList({
      ...searchForm,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.attributeType = ''
  pageNum.value = 1
  handleSearch()
}

const resetForm = () => {
  form.id = undefined
  form.attributeName = ''
  form.attributeField = ''
  form.attributeType = ''
  form.sourceType = ''
  form.sourceValue = ''
  form.interfaceId = null
  form.interfacePath = ''
  form.defaultValue = ''
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleDownloadTemplate = () => {
  const link = document.createElement('a')
  link.href = encodeURI('/mockData/属性导入模板.xlsx')
  link.download = '属性导入模板.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleTriggerImport = () => {
  if (importLoading.value) return
  if (!importInputRef.value) return
  importInputRef.value.value = ''
  importInputRef.value.click()
}

const renderImportFailMessage = (failList) => {
  if (!Array.isArray(failList) || failList.length === 0) return ''
  return failList
    .slice(0, 10)
    .map(item => `第${item.rowNum}行：${item.message}`)
    .join('\n')
}

const handleImportChange = async (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return
  if (!file.name.toLowerCase().endsWith('.xlsx')) {
    ElMessage.error('仅支持导入 .xlsx 文件')
    return
  }

  importLoading.value = true
  try {
    const res = await importAttribute(file)
    if (res.code !== 200) {
      ElMessage.error(res.message || '导入失败')
      return
    }

    const data = res.data || {}
    const totalCount = data.totalCount || 0
    const successCount = data.successCount || 0
    const failCount = data.failCount || 0
    const failList = data.failList || []

    if (failCount > 0) {
      const failMessage = renderImportFailMessage(failList)
      await ElMessageBox.alert(
        `共 ${totalCount} 条，成功 ${successCount} 条，失败 ${failCount} 条。${failMessage ? `\n\n${failMessage}` : ''}`,
        '导入结果',
        { type: 'warning' }
      )
    } else {
      ElMessage.success(`导入完成，共 ${totalCount} 条，成功 ${successCount} 条`)
    }

    pageNum.value = 1
    await handleSearch()
  } finally {
    importLoading.value = false
    if (importInputRef.value) {
      importInputRef.value.value = ''
    }
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.attributeName = row.attributeName
  form.attributeField = row.attributeField
  form.attributeType = row.attributeType
  form.sourceType = row.sourceType || ''
  form.sourceValue = row.sourceValue || ''
  form.interfaceId = row.interfaceId ?? null
  form.interfacePath = row.interfacePath || ''
  form.defaultValue = row.defaultValue || ''
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除属性「${row.attributeName}」？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await deleteAttribute(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      handleSearch()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  })
}

const onAttributeTypeChange = () => {
  if (form.attributeType !== 'custom') {
    form.sourceType = ''
    form.sourceValue = ''
    form.interfaceId = null
    form.interfacePath = ''
    form.defaultValue = ''
  }
}

const onSourceTypeChange = () => {
  if (form.sourceType !== 'api_data') {
    form.interfaceId = null
    form.interfacePath = ''
  } else {
    updateInterfacePath()
  }
  if (form.sourceType === 'node_content') {
    form.sourceValue = ''
  }
}

const updateInterfacePath = () => {
  const iface = interfaceOptions.value.find(item => item.id === form.interfaceId)
  form.interfacePath = iface ? iface.path : ''
}

const sourceValuePlaceholder = (sourceType) => {
  const map = {
    api_data: '如 data.productName',
    global_object: '如 userInfo.id',
    local_cache: '如 token',
    static_value: '如 male'
  }
  return map[sourceType] || ''
}

const attributeTypeLabel = (type) => {
  const map = {
    user: '用户属性',
    system: '系统属性',
    custom: '个性化属性'
  }
  return map[type] || type
}

const attributeTypeTagType = (type) => {
  const map = {
    user: 'success',
    system: 'info',
    custom: 'warning'
  }
  return map[type] || ''
}

const sourceTypeLabel = (type) => {
  const map = {
    node_content: '节点内容',
    api_data: '接口数据',
    global_object: '全局对象',
    local_cache: '本地缓存',
    static_value: '静态值'
  }
  return map[type] || ''
}

const buildSdkPath = (row) => {
  if (row.attributeType === 'user') {
    return `window.userInfo.${row.attributeField}`
  }
  if (row.attributeType === 'system') {
    return `window.system.${row.attributeField}`
  }
  if (row.sourceType === 'node_content') return '点击节点文本'
  if (row.sourceType === 'api_data') return `${row.interfacePath || '接口响应'} -> ${row.sourceValue || ''}`
  if (row.sourceType === 'global_object') return `window.${row.sourceValue || ''}`
  if (row.sourceType === 'local_cache') return `localStorage['${row.sourceValue || ''}']`
  if (row.sourceType === 'static_value') return `静态值: ${row.sourceValue || ''}`
  return '-'
}

const buildPayload = () => {
  const payload = {
    id: form.id,
    attributeName: form.attributeName,
    attributeField: form.attributeField,
    attributeType: form.attributeType
  }

  if (form.attributeType === 'custom') {
    payload.sourceType = form.sourceType
    payload.sourceValue = form.sourceType === 'node_content' ? null : form.sourceValue
    payload.interfaceId = form.sourceType === 'api_data' ? form.interfaceId : null
    payload.defaultValue = form.defaultValue || null
  } else {
    payload.sourceType = null
    payload.sourceValue = null
    payload.interfaceId = null
    payload.defaultValue = null
  }
  return payload
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const payload = buildPayload()
      const res = isEdit.value ? await updateAttribute(payload) : await addAttribute(payload)
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        handleSearch()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } finally {
      submitLoading.value = false
    }
  })
}

fetchInterfaces()
handleSearch()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.hidden-file-input {
  display: none;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
