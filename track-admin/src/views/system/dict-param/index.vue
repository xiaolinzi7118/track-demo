<template>
  <div class="dict-param-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>参数维护</span>
          <el-button type="primary" @click="handleAdd">新增参数</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="字典名称">
          <el-input
            v-model="searchForm.keyword"
            clearable
            placeholder="请输入字典名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="paramId" label="参数ID" min-width="190" />
        <el-table-column label="参数名称" min-width="180">
          <template #default="{ row }">
            <el-button type="primary" link class="name-link" @click="handleView(row)">
              {{ row.paramName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateBy" label="更新人" width="120" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <!-- <el-button link type="danger" @click="handleDelete(row)">删除</el-button> -->
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
          @current-change="fetchList"
        >
          <template #total>
            共 {{ Math.ceil(total / pageSize) || 0 }} 页
          </template>
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteDictParam, getDictParamList } from '../../../api/dict-param'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  keyword: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const res = await getDictParamList(params)
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) {
        total.value = data.length
        const start = (pageNum.value - 1) * pageSize.value
        tableData.value = data.slice(start, start + pageSize.value)
      } else {
        tableData.value = data?.content || []
        total.value = data?.totalElements || tableData.value.length
      }
    } else {
      ElMessage.error('加载失败')
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openDetailPage = (action, row) => {
  const query = {}
  if (row?.id) {
    query.id = row.id
  }
  if (row?.paramName) {
    query.paramName = row.paramName
  }

  router.push({
    path: `/system/dict-param/${action}`,
    query
  })
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const handleReset = () => {
  searchForm.keyword = ''
  pageNum.value = 1
  fetchList()
}

const handlePageSizeChange = () => {
  pageNum.value = 1
  fetchList()
}

const handleAdd = () => {
  openDetailPage('add')
}

const handleEdit = (row) => {
  openDetailPage('edit', row)
}

const handleView = (row) => {
  openDetailPage('view', row)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除参数“${row.paramName}”？`, '提示', { type: 'warning' })
    const res = await deleteDictParam(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    // cancelled
  }
}

onMounted(() => {
  fetchList()
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

.name-link {
  color: #1f6feb;
  padding: 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
