export function getUserId() {
  return localStorage.getItem('userId')
}

export function setUserId(id) {
  localStorage.setItem('userId', String(id))
}

export function getUserInfo() {
  const raw = localStorage.getItem('userInfo')
  return raw ? JSON.parse(raw) : null
}

export function setUserInfo(info) {
  localStorage.setItem('userInfo', JSON.stringify(info))
}

export function clearAuth() {
  localStorage.removeItem('userId')
  localStorage.removeItem('userInfo')
}

export function isLoggedIn() {
  return !!localStorage.getItem('userId')
}
