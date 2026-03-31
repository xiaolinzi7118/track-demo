<template>
  <div class="track-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>埋点配置列表</span>
          <el-button type="primary" @click="handleAdd">
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
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
      width="800px"
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
              <el-table-column prop="name" label="参数名称" />
              <el-table-column prop="type" label="参数类型">
                <template #default="{ row }">
                  <el-select v-model="row.type" size="small" style="width: 100%;">
                    <el-option label="字符串" value="string" />
                    <el-option label="数字" value="number" />
                    <el-option label="布尔值" value="boolean" />
                    <el-option label="对象" value="object" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="required" label="是否必填" width="100">
                <template #default="{ row }">
                  <el-switch v-model="row.required" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
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
import { getTrackConfigList, addTrackConfig, updateTrackConfig, deleteTrackConfig } from '../../api/track'

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
    name: '',
    type: 'string',
    required: false,
    description: ''
  })
}

const removeParam = (index) => {
  paramsList.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      form.params = JSON.stringify(paramsList.value)
      
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
</style>
