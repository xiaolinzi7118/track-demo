import request from '../utils/request'

export function getMenuTree() {
  return request({
    url: '/menu/tree',
    method: 'get'
  })
}
