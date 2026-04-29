<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>接口管理</span>
        <div class="header-actions">
          <el-button
            v-permission="'api-interface:add'"
            :loading="importLoading"
            @click="handleTriggerImport"
          >
            导入接口
          </el-button>
          <el-button v-permission="'api-interface:add'" type="primary" @click="handleAdd">新增接口</el-button>
          <input
            ref="importInputRef"
            class="hidden-file-input"
            type="file"
            accept=".txt"
            @change="handleImportChange"
          >
        </div>
      </div>
    </template>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="keyword" placeholder="接口名称/路径" clearable @clear="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="name" label="接口名称" />
      <el-table-column prop="path" label="接口路径" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="引用状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.referenced ? 'warning' : 'info'">
            {{ row.referenced ? '已被引用' : '未引用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button v-permission="'api-interface:edit'" link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-permission="'api-interface:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      >
        <template #total>
          共 {{ Math.ceil(total / pageSize) || 0 }} 页
        </template>
      </el-pagination>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑接口' : '新增接口'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="接口路径" prop="path">
          <el-input v-model="form.path" placeholder="如: /api/financial-product/list" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApiInterfaceList, addApiInterface, updateApiInterface, deleteApiInterface, importApiInterface } from '../../api/track'

const tableData = ref([])
const loading = ref(false)
const importLoading = ref(false)
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const importInputRef = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  path: '',
  description: ''
})

const rules = reactive({
  name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入接口路径', trigger: 'blur' }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getApiInterfaceList({
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      const data = res.data
      tableData.value = data.content || data || []
      total.value = data.totalElements || tableData.value.length
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

const handleReset = () => {
  keyword.value = ''
  pageNum.value = 1
  pageSize.value = 10
  fetchData()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.path = ''
  form.description = ''
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
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
    .map(item => `第${item.rowNum}条：${item.message}`)
    .join('\n')
}

const handleImportChange = async (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return
  if (!file.name.toLowerCase().endsWith('.txt')) {
    ElMessage.error('仅支持导入 .txt 文件')
    return
  }

  importLoading.value = true
  try {
    const res = await importApiInterface(file)
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
    await fetchData()
  } finally {
    importLoading.value = false
    if (importInputRef.value) {
      importInputRef.value.value = ''
    }
  }
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.path = row.path
  form.description = row.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()

  try {
    const res = isEdit.value
      ? await updateApiInterface({ ...form })
      : await addApiInterface({ ...form })
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该接口吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteApiInterface(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error('删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
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
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
