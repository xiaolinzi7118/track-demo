<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button v-permission="'system-role:add'" type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="roleList" border stripe>
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

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="loadRoles"
        >
          <template #total>
            共 {{ Math.ceil(total / pageSize) || 0 }} 页
          </template>
        </el-pagination>
      </div>
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
      <div v-if="permDialogVisible" class="perm-tree-container">
        <table class="perm-table">
          <thead>
            <tr>
              <th width="120">菜单目录</th>
              <th width="140">菜单</th>
              <th>操作权限</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="dir in directories" :key="dir.id">
              <tr v-for="(page, idx) in dir.children" :key="page.id">
                <td v-if="idx === 0" :rowspan="dir.children.length" class="perm-dir-cell">
                  <el-checkbox v-model="menuCheckedMap[dir.id]" @change="(val) => handleMenuCheck(dir, val)">
                    {{ dir.menuName }}
                  </el-checkbox>
                </td>
                <td>
                  <el-checkbox v-model="menuCheckedMap[page.id]" @change="(val) => handleMenuCheck(page, val)">
                    {{ page.menuName }}
                  </el-checkbox>
                </td>
                <td>
                  <template v-if="page.children && page.children.some(c => c.menuType === 3)">
                    <el-checkbox
                      v-for="btn in page.children.filter(c => c.menuType === 3)"
                      :key="btn.id"
                      v-model="menuCheckedMap[btn.id]"
                      style="margin-right: 12px;"
                    >
                      {{ btn.menuName }}
                    </el-checkbox>
                  </template>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getRoleMenus, updateRoleMenus } from '../../api/role'
import { getMenuTree } from '../../api/menu'

const roleList = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
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

const directories = computed(() => menuTree.value.filter(item => item.menuType === 1 && item.children && item.children.length))

const normalizeCheckedMenuIds = (menuData) => {
  if (!Array.isArray(menuData)) return []
  return menuData
    .map(item => {
      if (item && typeof item === 'object') {
        return item.id
      }
      return item
    })
    .filter(id => id !== null && id !== undefined && id !== '')
}

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      const data = res.data
      if (Array.isArray(data)) {
        total.value = data.length
        const start = (pageNum.value - 1) * pageSize.value
        roleList.value = data.slice(start, start + pageSize.value)
      } else {
        roleList.value = data?.content || []
        total.value = data?.totalElements || roleList.value.length
      }
    }
  } finally {
    loading.value = false
  }
}

const handlePageSizeChange = () => {
  pageNum.value = 1
  loadRoles()
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
    ElMessage.error(roleForm.id ? '更新角色失败' : '新增角色失败')
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
      ElMessage.error('删除角色失败')
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
    const checkedIds = normalizeCheckedMenuIds(menuRes.data)
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
      menuIds.push(id)
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
    ElMessage.error('更新角色权限失败')
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
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.perm-tree-container {
  max-height: 500px;
  overflow-y: auto;
}
.perm-table {
  width: 100%;
  border-collapse: collapse;
}
.perm-table th,
.perm-table td {
  border: 1px solid #ebeef5;
  padding: 10px 12px;
  text-align: left;
  vertical-align: middle;
}
.perm-table th {
  background-color: #f5f7fa;
  font-weight: 500;
  color: #606266;
}
.perm-dir-cell {
  text-align: center;
  font-weight: 500;
}
</style>
