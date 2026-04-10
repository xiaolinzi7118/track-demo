<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd" v-permission="'system:user:add'">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.roleName" :type="getRoleTagType(row.roleName)">{{ row.roleName }}</el-tag>
            <span v-else style="color: #909399">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" v-permission="'system:user:edit'">编辑</el-button>
            <el-button link type="warning" @click="handleResetPassword(row)" v-permission="'system:user:edit'">重置密码</el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-permission="'system:user:delete'" :disabled="row.username === 'admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="editForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!isEdit">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="editForm.password" type="password" placeholder="默认密码：123456" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="editForm.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser, resetPassword } from '../../api/user'
import { getRoleList } from '../../api/role'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editForm = ref({})
const roleOptions = ref([])
const formRef = ref(null)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleTagType = (roleName) => {
  const map = { '管理员': 'danger', '业务人员': 'warning', '开发人员': '' }
  return map[roleName] || ''
}

const fetchUsers = async () => {
  const res = await getUserList({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (res.code === 200) {
    tableData.value = res.data.content
    total.value = res.data.totalElements
  }
}

const fetchRoles = async () => {
  const res = await getRoleList()
  if (res.code === 200) {
    roleOptions.value = res.data
  }
}

const handleAdd = () => {
  isEdit.value = false
  editForm.value = { status: 1, password: '123456' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editForm.value = {
    id: row.id,
    nickname: row.nickname,
    roleId: row.roleId,
    status: row.status
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchUsers()
    } else {
      ElMessage.error(res.message)
    }
  } catch {}
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重置用户 "${row.username}" 的密码为 123456 吗？`, '提示', { type: 'warning' })
    const res = await resetPassword(row.id)
    if (res.code === 200) {
      ElMessage.success('密码已重置')
    } else {
      ElMessage.error(res.message)
    }
  } catch {}
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const res = isEdit.value ? await updateUser(editForm.value) : await addUser(editForm.value)
  if (res.code === 200) {
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchUsers()
  } else {
    ElMessage.error(res.message)
  }
}

const handlePageChange = (page) => {
  pageNum.value = page
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
