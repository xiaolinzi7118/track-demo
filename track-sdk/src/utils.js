export function generateId() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export function getUUId() {
  let uuid = localStorage.getItem('track_uuid')
  if (!uuid) {
    uuid = generateId()
    localStorage.setItem('track_uuid', uuid)
  }
  return uuid
}

export function getSessionId() {
  let sessionId = sessionStorage.getItem('track_session_id')
  if (!sessionId) {
    sessionId = generateId()
    sessionStorage.setItem('track_session_id', sessionId)
  }
  return sessionId
}

export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }
  const clone = Array.isArray(obj) ? [] : {}
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      clone[key] = deepClone(obj[key])
    }
  }
  return clone
}

export function getPageUrl() {
  return window.location.href
}

export function getUserAgent() {
  return window.navigator.userAgent
}

export function getTimestamp() {
  return new Date().toISOString()
}

export function parseUrl(url) {
  const a = document.createElement('a')
  a.href = url
  return {
    protocol: a.protocol,
    hostname: a.hostname,
    port: a.port,
    pathname: a.pathname,
    search: a.search,
    hash: a.hash
  }
}

export function matchUrlPattern(url, pattern) {
  if (!pattern) return true
  try {
    const regex = new RegExp(pattern)
    return regex.test(url)
  } catch (e) {
    console.error('Invalid URL pattern:', pattern, e)
    return false
  }
}

export function throttle(fn, delay = 1000) {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

export function debounce(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}
