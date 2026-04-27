<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button v-permission="'system-user:add'" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="role in (row.roleNames || [])" :key="role">{{ role }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column prop="primaryDeptName" label="主部门" width="150" />
        <el-table-column label="数据部门" min-width="220">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag
                v-for="dept in (row.dataDeptNames || [])"
                :key="`${row.id}-${dept}`"
                type="success"
                effect="plain"
              >
                {{ dept }}
              </el-tag>
            </el-space>
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
            <el-button v-permission="'system-user:edit'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="'system-user:edit'" link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button
              v-permission="'system-user:delete'"
              link
              type="danger"
              :disabled="row.isSuperAdmin === true"
              @click="handleDelete(row)"
            >
              删除
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
          @current-change="loadUsers"
        >
          <template #total>
            共 {{ Math.ceil(total / pageSize) || 0 }} 页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form :model="userForm" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!userForm.id" label="密码">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.roleIds" multiple collapse-tags placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主部门">
          <el-select v-model="userForm.primaryDeptId" placeholder="请选择主部门" style="width: 100%">
            <el-option
              v-for="dept in primaryDeptOptions"
              :key="dept.id"
              :label="dept.itemName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数据部门">
          <el-select
            v-model="userForm.dataDeptIds"
            multiple
            collapse-tags
            placeholder="请选择数据部门"
            style="width: 100%"
          >
            <el-option
              v-for="dept in deptOptions"
              :key="dept.id"
              :label="dept.itemName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUser">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, updatePassword } from '../../api/system'
import { getRoleList } from '../../api/role'
import { getDeptOptions } from '../../api/dict-param'

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const roleList = ref([])
const deptOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  roleIds: [],
  primaryDeptId: null,
  dataDeptIds: [],
  status: 1
})

const DEV_ROLE_CODE = 'developer'
const DEV_DEPT_EXTRA_ATTR = '开发'

const buildFormDefaults = () => ({
  id: null,
  username: '',
  password: '',
  nickname: '',
  roleIds: [],
  primaryDeptId: null,
  dataDeptIds: [],
  status: 1
})

const isDeveloperRoleSelected = computed(() => {
  const selectedRoleIds = new Set((userForm.roleIds || []).filter(Boolean))
  return (roleList.value || []).some(role => {
    if (!selectedRoleIds.has(role.id)) return false
    return String(role.roleCode || '').toLowerCase() === DEV_ROLE_CODE
  })
})

const primaryDeptOptions = computed(() => {
  if (!isDeveloperRoleSelected.value) {
    return deptOptions.value
  }
  return (deptOptions.value || []).filter(dept => dept.extraAttr === DEV_DEPT_EXTRA_ATTR)
})

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const res = await getUserList(params)
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) {
        const users = data.filter(item => item.isSuperAdmin !== true)
        total.value = users.length
        const start = (pageNum.value - 1) * pageSize.value
        tableData.value = users.slice(start, start + pageSize.value)
      } else {
        const content = (data?.content || []).filter(item => item.isSuperAdmin !== true)
        tableData.value = content
        total.value = data?.totalElements || content.length
      }
    }
  } finally {
    loading.value = false
  }
}

const handlePageSizeChange = () => {
  pageNum.value = 1
  loadUsers()
}

const loadRoles = async () => {
  const res = await getRoleList({ pageNum: 1, pageSize: 1000 })
  if (res.code === 200) {
    const data = res.data
    roleList.value = Array.isArray(data) ? data : (data?.content || [])
  }
}

const loadDepts = async () => {
  const res = await getDeptOptions()
  if (res.code === 200) {
    deptOptions.value = res.data || []
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(userForm, buildFormDefaults())
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (row.isSuperAdmin === true) {
    ElMessage.warning('内置超级管理员不可编辑')
    return
  }
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname,
    roleIds: row.roleIds || (row.roleId ? [row.roleId] : []),
    primaryDeptId: row.primaryDeptId || null,
    dataDeptIds: row.dataDeptIds || [],
    status: row.status
  })
  dialogVisible.value = true
}

const normalizeUserPayload = () => {
  const roleIds = Array.from(new Set((userForm.roleIds || []).filter(Boolean)))
  const dataDeptIds = Array.from(new Set((userForm.dataDeptIds || []).filter(Boolean)))
  if (userForm.primaryDeptId && !dataDeptIds.includes(userForm.primaryDeptId)) {
    dataDeptIds.push(userForm.primaryDeptId)
  }
  return {
    id: userForm.id,
    username: userForm.username,
    password: userForm.password,
    nickname: userForm.nickname,
    roleIds,
    primaryDeptId: userForm.primaryDeptId,
    dataDeptIds,
    status: userForm.status
  }
}

const submitUser = async () => {
  if (!userForm.id && !userForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!userForm.id && !userForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!userForm.primaryDeptId) {
    ElMessage.warning('请选择主部门')
    return
  }

  const payload = normalizeUserPayload()
  let res
  if (userForm.id) {
    res = await updateUser(payload)
  } else {
    res = await addUser(payload)
  }
  if (res.code === 200) {
    ElMessage.success(userForm.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadUsers()
  } else {
    ElMessage.error(userForm.id ? '更新用户失败' : '创建用户失败')
  }
}

const handleDelete = async (row) => {
  if (row.isSuperAdmin === true) {
    ElMessage.warning('内置超级管理员不可删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error('删除用户失败')
    }
  } catch {
    // cancelled
  }
}

const handleResetPassword = async (row) => {
  if (row.isSuperAdmin === true) {
    ElMessage.warning('内置超级管理员不可修改密码')
    return
  }
  try {
    await ElMessageBox.prompt('请输入新密码', '重置密码', {
      inputValue: '123456',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    }).then(async ({ value }) => {
      const res = await updatePassword({ id: row.id, password: value })
      if (res.code === 200) {
        ElMessage.success('密码重置成功')
      } else {
        ElMessage.error('重置密码失败')
      }
    })
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadUsers()
  loadRoles()
  loadDepts()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

watch(
  () => [userForm.roleIds, deptOptions.value],
  () => {
    if (!userForm.primaryDeptId) return
    const exists = primaryDeptOptions.value.some(dept => dept.id === userForm.primaryDeptId)
    if (!exists) {
      userForm.primaryDeptId = null
    }
  },
  { deep: true }
)

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
