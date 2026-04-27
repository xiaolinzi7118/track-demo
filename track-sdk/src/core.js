import { getUUId, getSessionId, getPageUrl, getUserAgent, getTimestamp, matchUrlPattern } from './utils'

class TrackCore {
  constructor(config) {
    this.config = config
    this.userId = ''
    this.userInfo = {}
    this.sessionId = getSessionId()
    this.uuid = getUUId()
    this.queue = []
    this.maxQueueSize = 20
    this.flushInterval = 5000
    this.timer = null
    this.pageEnterTime = Date.now()
    this.currentTrackConfig = null
    this.isVisible = true
    this.referencedPaths = []
    this._interceptionSetup = false
    this._pendingCache = new Map()
  }

  init() {
    this.setupNetworkInterception()
    this.fetchTrackConfig()
    this.fetchReferencedPaths()
    this.initAutoTrack()
    this.startFlushTimer()
    this.initVisibilityChange()
  }

  async fetchTrackConfig() {
    try {
      const result = await this.fetchConfigWithFallback([
        `${this.config.serverUrl}/api/event-manage/all`,
        `${this.config.serverUrl}/api/track-config/all`
      ])
      if (result.code === 200) {
        this.currentTrackConfig = result.data || []
        if (this.config.debug) {
          console.log('Track config fetched:', this.currentTrackConfig)
        }
        // 配置加载完成后触发首次页面曝光采集
        if (this.config.autoTrack && this.config.autoTrack.pageView) {
          this.trackPageView()
        }
      }
    } catch (error) {
      console.error('Failed to fetch track config:', error)
    }
  }

  async fetchConfigWithFallback(urls) {
    let lastError = null
    for (const url of urls) {
      try {
        const response = await fetch(url)
        if (!response.ok) continue
        return await response.json()
      } catch (error) {
        lastError = error
      }
    }
    if (lastError) {
      throw lastError
    }
    return { code: 500, message: 'fetch config failed', data: [] }
  }

  async fetchReferencedPaths() {
    try {
      const response = await fetch(`${this.config.serverUrl}/api/api-interface/referenced-paths`)
      const result = await response.json()
      if (result.code === 200) {
        this.referencedPaths = result.data || []
        // 将暂存区中匹配的响应转存到 localStorage
        this.flushPendingCache()
      }
    } catch (error) {
      if (this.config.debug) {
        console.error('Failed to fetch referenced paths:', error)
      }
    }
  }

  /**
   * 将暂存区中匹配 referencedPaths 的响应转存到 localStorage
   */
  flushPendingCache() {
    if (this._pendingCache.size === 0) return
    for (const [path, responseText] of this._pendingCache) {
      if (this.referencedPaths.includes(path)) {
        const cacheKey = 'track_api_' + path
        try {
          localStorage.setItem(cacheKey, responseText)
          if (this.config.debug) {
            console.log('[TrackSDK] Flushed pending cache for:', path)
          }
        } catch (e) {
          // ignore
        }
      }
    }
    this._pendingCache.clear()
  }

  setupNetworkInterception() {
    if (this._interceptionSetup) return
    this._interceptionSetup = true

    const self = this
    const originalOpen = XMLHttpRequest.prototype.open
    const originalSend = XMLHttpRequest.prototype.send

    XMLHttpRequest.prototype.open = function(method, url, ...args) {
      this._trackUrl = url
      return originalOpen.call(this, method, url, ...args)
    }

    XMLHttpRequest.prototype.send = function(...args) {
      const xhr = this

      xhr.addEventListener('load', function() {
        if (xhr.status >= 200 && xhr.status < 300) {
          self.handleXhrResponse(xhr._trackUrl, xhr.responseText)
        }
      })

      return originalSend.call(this, ...args)
    }
  }

  handleXhrResponse(url, responseText) {
    if (!url) return

    let urlPath = url
    try {
      if (urlPath.startsWith('http')) {
        const urlObj = new URL(urlPath)
        urlPath = urlObj.pathname
      } else {
        urlPath = urlPath.split('?')[0]
      }
    } catch (e) {
      urlPath = urlPath.split('?')[0]
    }

    // 跳过 SDK 自身的请求
    if (urlPath.startsWith('/api/track-config') || urlPath.startsWith('/api/event-manage') || urlPath.startsWith('/api/track-data') || urlPath.startsWith('/api/api-interface')) {
      return
    }

    try {
      JSON.parse(responseText)
    } catch (e) {
      return // 非JSON不缓存
    }

    if (this.referencedPaths.length > 0 && this.referencedPaths.includes(urlPath)) {
      // referencedPaths 已加载且匹配，直接缓存到 localStorage
      const cacheKey = 'track_api_' + urlPath
      try {
        localStorage.setItem(cacheKey, responseText)
        if (this.config.debug) {
          console.log('[TrackSDK] Cached API response for:', urlPath)
        }
      } catch (e) {
        // ignore
      }
    } else {
      // referencedPaths 尚未加载，暂存到内存
      this._pendingCache.set(urlPath, responseText)
    }
  }

  initAutoTrack() {
    const { autoTrack } = this.config

    if (autoTrack.pageView) {
      // 首次 trackPageView 在 fetchTrackConfig 完成后触发
      window.addEventListener('popstate', () => {
        this.trackPageView()
      })
    }

    if (autoTrack.click) {
      document.addEventListener('click', (e) => {
        this.handleClick(e)
      }, true)
    }

    window.addEventListener('beforeunload', () => {
      this.trackPageLeave()
      this.flush()
    })
  }

  initVisibilityChange() {
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        this.isVisible = false
        this.trackPageLeave()
      } else {
        this.isVisible = true
        this.pageEnterTime = Date.now()
        this.trackPageView()
      }
    })
  }

  handleClick(e) {
    let target = e.target
    let trackId = target.getAttribute('data-track-id') ||
                  target.getAttribute('track-id')

    // 如果当前节点没有 track-id，查找父节点
    if (!trackId) {
      const parentElement = this.findParentTrackElement(target)
      if (parentElement) {
        target = parentElement
        trackId = parentElement.getAttribute('data-track-id') ||
                  parentElement.getAttribute('track-id')
      }
    }

    if (trackId) {
      const pageUrl = getPageUrl()
      const matchedConfig = this.findMatchedConfig(pageUrl, 'click', trackId)

      if (matchedConfig) {
        const paramsConfig = this.parseParamsConfig(matchedConfig.params)

        // 根据配置动态获取参数（不再有基础参数）
        const dynamicParams = this.buildParams(paramsConfig, target)

        this.track(matchedConfig.eventCode, 'click', dynamicParams)
      } else if (this.config.debug) {
        console.log('Click event not tracked - no matching config found for trackId:', trackId)
      }
    }
  }

  /**
   * 查找父节点的 track 元素
   * @param {Element} element - 起始元素
   * @returns {Element|null} 带有 track-id 的父元素
   */
  findParentTrackElement(element) {
    let parent = element.parentElement
    while (parent && parent !== document.body) {
      const trackId = parent.getAttribute('data-track-id') ||
                      parent.getAttribute('track-id')
      if (trackId) {
        return parent
      }
      parent = parent.parentElement
    }
    return null
  }

  trackPageView() {
    const pageUrl = getPageUrl()
    const matchedConfig = this.findMatchedConfig(pageUrl, 'page_view')

    if (matchedConfig) {
      const paramsConfig = this.parseParamsConfig(matchedConfig.params)

      // 过滤掉仅点击事件支持的参数类型
      const validParamsConfig = paramsConfig.filter(p => p.sourceType !== 'node_content' && p.sourceType !== 'api_data')

      // 根据配置动态获取参数（无目标元素）
      const dynamicParams = this.buildParams(validParamsConfig, null)

      this.track(matchedConfig.eventCode, 'page_view', dynamicParams)
    }
  }

  trackPageLeave() {
    const duration = Date.now() - this.pageEnterTime
    const pageUrl = getPageUrl()
    const matchedConfig = this.findMatchedConfig(pageUrl, 'page_view')

    if (matchedConfig) {
      this.track(matchedConfig.eventCode + '_leave', 'page_view', {
        duration: duration
      }, duration)
    }
  }

  findMatchedConfig(url, eventType, trackId = null) {
    if (!this.currentTrackConfig) return null

    return this.currentTrackConfig.find(config => {
      if (config.eventType !== eventType) return false
      if (config.status !== 1) return false

      if (trackId) {
        // 使用 eventCode 匹配 data-track-id
        return config.eventCode === trackId
      }

      return matchUrlPattern(url, config.urlPattern)
    })
  }

  track(eventCode, eventType, params = {}, duration = null) {
    const data = {
      eventCode,
      eventType,
      url: getPageUrl(),
      params: JSON.stringify(params),
      userId: this.userId,
      sessionId: this.sessionId,
      userAgent: getUserAgent(),
      ip: '',
      duration: duration,
      eventTime: getTimestamp()
    }

    this.queue.push(data)

    if (this.config.debug) {
      console.log('Track event added to queue:', data)
    }

    if (this.queue.length >= this.maxQueueSize) {
      this.flush()
    }
  }

  startFlushTimer() {
    this.timer = setInterval(() => {
      if (this.queue.length > 0) {
        this.flush()
      }
    }, this.flushInterval)
  }

  async flush() {
    if (this.queue.length === 0) return

    const data = [...this.queue]
    this.queue = []

    try {
      const response = await fetch(`${this.config.serverUrl}/api/track-data/batch-report`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        keepalive: true
      })

      const result = await response.json()

      if (this.config.debug) {
        if (result.code === 200) {
          console.log('Track data reported successfully:', data)
        } else {
          console.error('Failed to report track data:', result.message)
        }
      }
    } catch (error) {
      console.error('Failed to report track data:', error)
      this.queue = [...data, ...this.queue]
    }
  }

  setUserId(userId) {
    this.userId = userId
  }

  setUserInfo(userInfo) {
    this.userInfo = userInfo
  }

  getSessionId() {
    return this.sessionId
  }

  // ===== 参数获取方法 =====

  /**
   * 根据配置动态获取参数值
   * @param {Object} paramConfig - 参数配置对象
   * @param {Element} targetElement - 点击目标元素（仅node_content类型需要）
   * @returns {any} 参数值
   */
  getParamValue(paramConfig, targetElement = null) {
    const { attributeType, attributeField, sourceType, sourceValue, defaultValue, paramName } = paramConfig
    let value = defaultValue || ''

    try {
      if (attributeType === 'user') {
        value = this.getUserAttributeValue(attributeField)
      } else if (attributeType === 'system') {
        value = this.getSystemAttributeValue(attributeField)
      } else {
        switch (sourceType) {
          case 'node_content':
            value = this.getNodeContent(targetElement)
            break
          case 'api_data':
            value = this.getApiDataValue(paramConfig)
            break
          case 'global_object':
            value = this.getGlobalObjectValue(sourceValue)
            break
          case 'local_cache':
            value = this.getLocalCacheValue(sourceValue)
            break
          case 'static_value':
            value = sourceValue
            break
          default:
            value = defaultValue || ''
        }
      }
    } catch (error) {
      if (this.config.debug) {
        console.warn(`[TrackSDK] Failed to get param value for ${paramName}:`, error)
      }
      value = defaultValue || ''
    }

    return value
  }

  getUserAttributeValue(attributeField) {
    if (!attributeField) return ''
    if (window.userInfo && window.userInfo[attributeField] !== undefined && window.userInfo[attributeField] !== null) {
      return window.userInfo[attributeField]
    }
    if (this.userInfo && this.userInfo[attributeField] !== undefined && this.userInfo[attributeField] !== null) {
      return this.userInfo[attributeField]
    }
    return ''
  }

  getSystemAttributeValue(attributeField) {
    if (!attributeField) return ''
    if (window.system && window.system[attributeField] !== undefined && window.system[attributeField] !== null) {
      return window.system[attributeField]
    }
    return ''
  }

  /**
   * 获取节点内容（点击事件专用）
   * @param {Element} element - DOM元素
   * @returns {string} 内容文本
   */
  getNodeContent(element) {
    if (!element) return ''

    // 获取元素及其子元素的文本内容，多个子节点用逗号拼接
    const textContent = element.innerText || element.textContent || ''

    // 清理多余空白，逗号分隔
    const contents = textContent
      .split('\n')
      .map(s => s.trim())
      .filter(s => s)
      .join(',')

    return contents.substring(0, 200) // 限制长度
  }

  /**
   * 获取接口数据值（从缓存的接口响应中提取）
   * @param {Object} paramConfig - 参数配置对象
   * @returns {any} 参数值
   */
  getApiDataValue(paramConfig) {
    const { interfacePath, sourceValue } = paramConfig
    if (!interfacePath) return ''

    const cacheKey = 'track_api_' + interfacePath
    const cachedRaw = localStorage.getItem(cacheKey)
    if (!cachedRaw) return ''

    try {
      const cachedData = JSON.parse(cachedRaw)
      const value = this.resolveObjectPath(cachedData, sourceValue)
      return value !== undefined && value !== null ? value : ''
    } catch (e) {
      if (this.config.debug) {
        console.warn('[TrackSDK] Failed to read cached API data:', e)
      }
      return ''
    }
  }

  /**
   * 按路径解析对象的属性值
   * @param {Object} obj - 目标对象
   * @param {string} path - 属性路径，如 "data.productName"
   * @returns {any} 属性值
   */
  resolveObjectPath(obj, path) {
    if (!path || !obj) return undefined
    const keys = path.split('.')
    let value = obj
    for (const key of keys) {
      if (value === null || value === undefined) return undefined
      value = value[key]
    }
    return value
  }

  /**
   * 获取全局对象值
   * @param {string} path - 属性路径，如 "userInfo.id"
   * @returns {any} 值
   */
  getGlobalObjectValue(path) {
    if (!path) return ''

    const keys = path.split('.')
    let value = window

    for (const key of keys) {
      if (value === null || value === undefined) {
        return ''
      }
      value = value[key]
    }

    return value !== undefined && value !== null ? value : ''
  }

  /**
   * 获取localStorage缓存值
   * @param {string} key - localStorage的key
   * @returns {string} 缓存值
   */
  getLocalCacheValue(key) {
    if (!key) return ''

    const value = localStorage.getItem(key)

    // 尝试解析JSON
    if (value) {
      try {
        return JSON.parse(value)
      } catch {
        return value
      }
    }

    return ''
  }

  /**
   * 根据参数配置构建参数对象
   * @param {Array} paramsConfig - 参数配置数组
   * @param {Element} targetElement - 目标元素
   * @returns {Object} 参数对象
   */
  buildParams(paramsConfig, targetElement = null) {
    const params = {}

    if (!paramsConfig || !Array.isArray(paramsConfig)) {
      return params
    }

    paramsConfig.forEach(config => {
      const paramKey = config.attributeField || config.paramName
      if (paramKey) {
        params[paramKey] = this.getParamValue(config, targetElement)
      }
    })

    return params
  }

  parseParamsConfig(rawParams) {
    if (!rawParams) return []
    if (Array.isArray(rawParams)) return rawParams
    try {
      const parsed = JSON.parse(rawParams)
      return Array.isArray(parsed) ? parsed : []
    } catch (err) {
      if (this.config.debug) {
        console.warn('Failed to parse params config:', err)
      }
      return []
    }
  }
}

export default TrackCore
