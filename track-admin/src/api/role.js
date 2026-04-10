import request from '../utils/request'

export function getRoleList() {
  return request({
    url: '/role/list',
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/role/add',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/role/update',
    method: 'post',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: '/role/delete',
    method: 'post',
    data: { id }
  })
}

export function getPermissionTree(roleId) {
  return request({
    url: '/role/permission-tree',
    method: 'get',
    params: { roleId }
  })
}

export function assignPermissions(roleId, menuIds) {
  return request({
    url: '/role/assign-permissions',
    method: 'post',
    data: { roleId, menuIds }
  })
}
