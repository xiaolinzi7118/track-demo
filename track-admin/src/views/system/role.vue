<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
        </div>
      </template>

      <el-table :data="roleList" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditPermission(row)" v-permission="'system:role:edit'">
              编辑权限
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)" v-permission="'system:role:edit'">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" v-permission="'system:role:edit'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑角色弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="editForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" v-if="!isEdit">
          <el-input v-model="editForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限编辑弹窗 -->
    <el-dialog v-model="permissionDialogVisible" title="编辑权限" width="500px">
      <div style="margin-bottom: 10px; color: #909399; font-size: 13px;">
        当前角色：<el-tag>{{ currentRole.roleName }}</el-tag>
      </div>
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedKeys"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getPermissionTree, assignPermissions } from '../../api/role'

const roleList = ref([])
const editDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const editForm = ref({})
const currentRole = ref({})
const permissionTree = ref([])
const checkedKeys = ref([])
const treeRef = ref(null)

const fetchRoles = async () => {
  const res = await getRoleList()
  if (res.code === 200) {
    roleList.value = res.data
  }
}

const handleEdit = (row) => {
  isEdit.value = true
  editForm.value = { ...row }
  editDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除角色 "${row.roleName}" 吗？`, '提示', { type: 'warning' })
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchRoles()
    } else {
      ElMessage.error(res.message)
    }
  } catch {}
}

const submitEdit = async () => {
  const res = isEdit.value ? await updateRole(editForm.value) : await addRole(editForm.value)
  if (res.code === 200) {
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    editDialogVisible.value = false
    fetchRoles()
  } else {
    ElMessage.error(res.message)
  }
}

const handleEditPermission = async (row) => {
  currentRole.value = row
  const res = await getPermissionTree(row.id)
  if (res.code === 200) {
    permissionTree.value = res.data
    // Collect leaf node IDs that are checked
    const keys = []
    const collectChecked = (nodes) => {
      for (const node of nodes) {
        if (node.checked && (!node.children || node.children.length === 0)) {
          keys.push(node.id)
        }
        if (node.children) {
          collectChecked(node.children)
        }
      }
    }
    collectChecked(res.data)
    checkedKeys.value = keys
    permissionDialogVisible.value = true
  }
}

const submitPermission = async () => {
  const checkedNodes = treeRef.value.getCheckedKeys(false)
  const halfCheckedNodes = treeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedNodes, ...halfCheckedNodes]
  const res = await assignPermissions(currentRole.value.id, allKeys)
  if (res.code === 200) {
    ElMessage.success('权限更新成功')
    permissionDialogVisible.value = false
  } else {
    ElMessage.error(res.message)
  }
}

onMounted(() => {
  fetchRoles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
