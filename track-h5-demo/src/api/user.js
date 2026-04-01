import request from '../utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo(userId) {
  return request.get('/auth/userinfo', { params: { userId } })
}
