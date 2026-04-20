<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button v-permission="'system-role:add'" type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      <el-table :data="roleList" border stripe>
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'system-role:edit'" type="primary" link @click="handleEditPermission(row)">编辑权限</el-button>
            <el-button v-permission="'system-role:edit'" type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="'system-role:delete'" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色编码" v-if="!roleForm.id">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限编辑弹窗 -->
    <el-dialog v-model="permDialogVisible" title="编辑权限" width="700px" @opened="loadPermissionData">
      <div v-if="permDialogVisible" style="max-height: 500px; overflow-y: auto;">
        <el-table :data="menuTree" border row-key="id" default-expand-all
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
          <el-table-column label="菜单名称" width="200">
            <template #default="{ row }">
              <el-checkbox
                v-if="row.menuType === 1 || row.menuType === 2"
                v-model="menuCheckedMap[row.id]"
                @change="(val) => handleMenuCheck(row, val)"
              >
                {{ row.menuName }}
              </el-checkbox>
              <span v-else style="padding-left: 24px;">{{ row.menuName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作权限">
            <template #default="{ row }">
              <template v-if="row.children && row.children.length && row.children.some(c => c.menuType === 3)">
                <el-checkbox
                  v-for="btn in row.children.filter(c => c.menuType === 3)"
                  :key="btn.id"
                  v-model="menuCheckedMap[btn.id]"
                  style="margin-right: 12px;"
                >
                  {{ btn.menuName }}
                </el-checkbox>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getRoleMenus, updateRoleMenus } from '../../api/role'
import { getMenuTree } from '../../api/menu'

const roleList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const roleForm = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1
})

const permDialogVisible = ref(false)
const currentRole = ref(null)
const menuTree = ref([])
const menuCheckedMap = reactive({})

const loadRoles = async () => {
  const res = await getRoleList()
  if (res.code === 200) {
    roleList.value = res.data || []
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  Object.assign(roleForm, { id: null, roleCode: '', roleName: '', description: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(roleForm, { ...row })
  dialogVisible.value = true
}

const submitRole = async () => {
  if (!roleForm.roleCode && !roleForm.id) {
    ElMessage.warning('请输入角色编码')
    return
  }
  if (!roleForm.roleName) {
    ElMessage.warning('请输入角色名称')
    return
  }
  let res
  if (roleForm.id) {
    res = await updateRole(roleForm)
  } else {
    res = await addRole(roleForm)
  }
  if (res.code === 200) {
    ElMessage.success(roleForm.id ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadRoles()
  } else {
    ElMessage.error(res.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除角色"${row.roleName}"？`, '提示', { type: 'warning' })
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadRoles()
    } else {
      ElMessage.error(res.message)
    }
  } catch { /* cancelled */ }
}

const handleEditPermission = async (row) => {
  currentRole.value = row
  permDialogVisible.value = true
}

const loadPermissionData = async () => {
  const treeRes = await getMenuTree()
  if (treeRes.code === 200) {
    menuTree.value = treeRes.data || []
  }
  const menuRes = await getRoleMenus(currentRole.value.id)
  if (menuRes.code === 200) {
    const checkedIds = menuRes.data || []
    // 重置
    Object.keys(menuCheckedMap).forEach(k => delete menuCheckedMap[k])
    checkedIds.forEach(id => {
      menuCheckedMap[id] = true
    })
  }
}

const handleMenuCheck = (row, val) => {
  // 联动子节点
  if (row.children && row.children.length) {
    row.children.forEach(child => {
      menuCheckedMap[child.id] = val
      if (child.children && child.children.length) {
        child.children.forEach(btn => {
          menuCheckedMap[btn.id] = val
        })
      }
    })
  }
}

const submitPermission = async () => {
  const menuIds = []
  Object.keys(menuCheckedMap).forEach(id => {
    if (menuCheckedMap[id]) {
      menuIds.push(Number(id))
    }
  })
  const res = await updateRoleMenus({
    roleId: currentRole.value.id,
    menuIds
  })
  if (res.code === 200) {
    ElMessage.success('权限更新成功')
    permDialogVisible.value = false
  } else {
    ElMessage.error(res.message)
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-container {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
