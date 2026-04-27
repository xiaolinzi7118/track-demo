import request from '../utils/request'

export function getRoleList(params) {
  return request({ url: '/role/list', method: 'get', params })
}

export function getRoleDetail(id) {
  return request({ url: '/role/detail', method: 'get', params: { id } })
}

export function addRole(data) {
  return request({ url: '/role/add', method: 'post', data })
}

export function updateRole(data) {
  return request({ url: '/role/update', method: 'post', data })
}

export function deleteRole(id) {
  return request({ url: '/role/delete', method: 'post', data: { id } })
}

export function getRoleMenus(id) {
  return request({ url: '/role/menus', method: 'get', params: { id } })
}

export function updateRoleMenus(data) {
  return request({ url: '/role/update-menus', method: 'post', data })
}
