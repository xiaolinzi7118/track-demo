import request from '../utils/request'

export function getMenuTree() {
  return request({ url: '/menu/tree', method: 'get' })
}

export function getUserMenus() {
  return request({ url: '/menu/user-menus', method: 'get' })
}

export function getUserPermissions() {
  return request({ url: '/menu/user-permissions', method: 'get' })
}
