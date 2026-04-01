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
  }

  init() {
    this.fetchTrackConfig()
    this.initAutoTrack()
    this.startFlushTimer()
    this.initVisibilityChange()
  }

  async fetchTrackConfig() {
    try {
      const response = await fetch(`${this.config.serverUrl}/api/track-config/all`)
      const result = await response.json()
      if (result.code === 200) {
        this.currentTrackConfig = result.data || []
        if (this.config.debug) {
          console.log('Track config fetched:', this.currentTrackConfig)
        }
      }
    } catch (error) {
      console.error('Failed to fetch track config:', error)
    }
  }

  initAutoTrack() {
    const { autoTrack } = this.config

    if (autoTrack.pageView) {
      this.trackPageView()
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
        // 解析参数配置
        let paramsConfig = []
        try {
          paramsConfig = matchedConfig.params ? JSON.parse(matchedConfig.params) : []
        } catch (err) {
          if (this.config.debug) {
            console.warn('Failed to parse params config:', err)
          }
        }

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
      // 解析参数配置
      let paramsConfig = []
      try {
        paramsConfig = matchedConfig.params ? JSON.parse(matchedConfig.params) : []
      } catch (err) {
        if (this.config.debug) {
          console.warn('Failed to parse params config:', err)
        }
      }

      // 过滤掉 node_content 类型的参数（仅点击事件支持）
      const validParamsConfig = paramsConfig.filter(p => p.sourceType !== 'node_content')

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
    const { sourceType, sourceValue, defaultValue, paramName } = paramConfig
    let value = defaultValue || ''

    try {
      switch (sourceType) {
        case 'node_content':
          value = this.getNodeContent(targetElement)
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
    } catch (error) {
      if (this.config.debug) {
        console.warn(`[TrackSDK] Failed to get param value for ${paramName}:`, error)
      }
      value = defaultValue || ''
    }

    return value
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
      const { paramName } = config
      if (paramName) {
        params[paramName] = this.getParamValue(config, targetElement)
      }
    })

    return params
  }
}

export default TrackCore
