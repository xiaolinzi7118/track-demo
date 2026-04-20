<template>
  <div class="track-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>埋点配置列表</span>
          <el-button v-permission="'track-config:add'" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="事件类型">
            <el-select v-model="searchForm.eventType" clearable placeholder="请选择">
              <el-option label="页面曝光" value="page_view" />
              <el-option label="点击交互" value="click" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="事件名称/事件编码"
              clearable
              style="width: 200px"
            />
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
        <el-table-column prop="eventName" label="事件名称" />
        <el-table-column prop="eventCode" label="事件编码" />
        <el-table-column prop="eventType" label="事件类型">
          <template #default="{ row }">
            <el-tag :type="row.eventType === 'page_view' ? 'success' : 'primary'">
              {{ row.eventType === 'page_view' ? '页面曝光' : '点击交互' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="urlPattern" label="生效页面" show-overflow-tooltip />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'track-config:edit'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="'track-config:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑配置' : '新增配置'"
      width="850px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="事件名称" prop="eventName">
              <el-input v-model="form.eventName" placeholder="请输入事件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事件编码" prop="eventCode">
              <el-input v-model="form.eventCode" placeholder="请输入事件编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="事件类型" prop="eventType">
              <el-select v-model="form.eventType" placeholder="请选择">
                <el-option label="页面曝光" value="page_view" />
                <el-option label="点击交互" value="click" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="生效页面URL正则">
          <el-input
            v-model="form.urlPattern"
            placeholder="如：^https://example.com/page/.* （空则表示所有页面）"
          />
        </el-form-item>
        <el-form-item label="参数配置">
          <div class="params-config">
            <el-button size="small" type="primary" @click="addParam">
              <el-icon><Plus /></el-icon>
              添加参数
            </el-button>
            <el-table :data="paramsList" border size="small" style="margin-top: 10px;">
              <el-table-column prop="paramName" label="参数名称" width="120">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" placeholder="参数名" />
                </template>
              </el-table-column>
              <el-table-column prop="sourceType" label="来源类型" width="140">
                <template #default="{ row }">
                  <el-select v-model="row.sourceType" size="small" style="width: 100%;">
                    <el-option label="节点内容" value="node_content" :disabled="form.eventType !== 'click'" />
                    <el-option label="接口数据" value="api_data" :disabled="form.eventType !== 'click'" />
                    <el-option label="全局对象" value="global_object" />
                    <el-option label="本地缓存" value="local_cache" />
                    <el-option label="静态值" value="static_value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="sourceValue" label="变量路径/值" width="260">
                <template #default="{ row }">
                  <template v-if="row.sourceType === 'api_data'">
                    <el-select v-model="row.interfaceId" size="small" style="width: 100%; margin-bottom: 4px;" placeholder="选择接口" clearable>
                      <el-option v-for="item in interfaceOptions" :key="item.id" :label="item.name + ' (' + item.path + ')'" :value="item.id" />
                    </el-select>
                    <el-input v-model="row.sourceValue" size="small" placeholder="如: data.productName" />
                  </template>
                  <el-input
                    v-else-if="row.sourceType !== 'node_content'"
                    v-model="row.sourceValue"
                    size="small"
                    :placeholder="getSourcePlaceholder(row.sourceType)"
                  />
                  <span v-else class="hint-text">自动获取</span>
                </template>
              </el-table-column>
              <el-table-column prop="defaultValue" label="默认值" width="120">
                <template #default="{ row }">
                  <el-input v-model="row.defaultValue" size="small" placeholder="获取失败时使用" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" width="150">
                <template #default="{ row }">
                  <el-input v-model="row.description" size="small" placeholder="描述" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="removeParam($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTrackConfigList, addTrackConfig, updateTrackConfig, deleteTrackConfig, getAllApiInterfaces } from '../../api/track'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  eventType: '',
  keyword: ''
})

const form = reactive({
  eventName: '',
  eventCode: '',
  eventType: '',
  description: '',
  urlPattern: '',
  status: 1,
  params: ''
})

const paramsList = ref([])
const interfaceOptions = ref([])

const fetchInterfaces = async () => {
  try {
    const res = await getAllApiInterfaces()
    if (res.code === 200) {
      interfaceOptions.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to fetch interfaces:', error)
  }
}
fetchInterfaces()

const rules = reactive({
  eventName: [
    { required: true, message: '请输入事件名称', trigger: 'blur' }
  ],
  eventCode: [
    { required: true, message: '请输入事件编码', trigger: 'blur' }
  ],
  eventType: [
    { required: true, message: '请选择事件类型', trigger: 'change' }
  ]
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getTrackConfigList({
      ...searchForm,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data.content || res.data
      total.value = res.data.totalElements || res.data.length || 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.eventType = ''
  searchForm.keyword = ''
  pageNum.value = 1
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = undefined
  form.eventName = ''
  form.eventCode = ''
  form.eventType = ''
  form.description = ''
  form.urlPattern = ''
  form.status = 1
  form.params = ''
  paramsList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.eventName = row.eventName
  form.eventCode = row.eventCode
  form.eventType = row.eventType
  form.description = row.description
  form.urlPattern = row.urlPattern
  form.status = row.status

  try {
    paramsList.value = row.params ? JSON.parse(row.params) : []
  } catch {
    paramsList.value = []
  }

  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该配置吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteTrackConfig(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        handleSearch()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const addParam = () => {
  paramsList.value.push({
    paramName: '',
    sourceType: 'static_value',
    sourceValue: '',
    interfaceId: null,
    defaultValue: '',
    description: ''
  })
}

const removeParam = (index) => {
  paramsList.value.splice(index, 1)
}

const getSourcePlaceholder = (sourceType) => {
  const placeholders = {
    global_object: '如: userInfo.id',
    local_cache: '如: auth_token',
    static_value: '如: 1.0.0',
    api_data: '响应数据字段路径'
  }
  return placeholders[sourceType] || ''
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true

      // 为 api_data 类型参数补充 interfacePath
      const enrichedParams = paramsList.value.map(p => {
        if (p.sourceType === 'api_data' && p.interfaceId) {
          const iface = interfaceOptions.value.find(i => i.id === p.interfaceId)
          return { ...p, interfacePath: iface ? iface.path : '' }
        }
        return { ...p }
      })
      form.params = JSON.stringify(enrichedParams)

      try {
        let res
        if (isEdit.value) {
          res = await updateTrackConfig(form)
        } else {
          res = await addTrackConfig(form)
        }

        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          handleSearch()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

handleSearch()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.params-config {
  width: 100%;
}

.hint-text {
  color: #909399;
  font-size: 12px;
}
</style>
