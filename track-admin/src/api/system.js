import request from '../utils/request'

export function getUserList(params) {
  return request({ url: '/user/list', method: 'get', params })
}

export function getUserDetail(id) {
  return request({ url: '/user/detail', method: 'get', params: { id } })
}

export function addUser(data) {
  return request({ url: '/user/add', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/user/update', method: 'post', data })
}

export function deleteUser(id) {
  return request({ url: '/user/delete', method: 'post', data: { id } })
}

export function updatePassword(data) {
  return request({ url: '/user/update-password', method: 'post', data })
}
