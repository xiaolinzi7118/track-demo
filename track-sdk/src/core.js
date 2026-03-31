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
    const target = e.target
    const trackId = target.getAttribute('data-track-id') || 
                    target.getAttribute('track-id') ||
                    this.findParentTrackId(target)

    if (trackId) {
      const pageUrl = getPageUrl()
      const matchedConfig = this.findMatchedConfig(pageUrl, 'click', trackId)
      
      if (matchedConfig) {
        const params = {
          elementTag: target.tagName,
          elementText: target.innerText?.substring(0, 50),
          elementClass: target.className,
          elementId: target.id
        }
        this.track(matchedConfig.eventCode, 'click', params)
      } else if (this.config.debug) {
        console.log('Click event not tracked - no matching config found for trackId:', trackId)
      }
    }
  }

  findParentTrackId(element) {
    let parent = element.parentElement
    while (parent && parent !== document.body) {
      const trackId = parent.getAttribute('data-track-id') || 
                      parent.getAttribute('track-id')
      if (trackId) {
        return trackId
      }
      parent = parent.parentElement
    }
    return null
  }

  trackPageView() {
    const pageUrl = getPageUrl()
    const matchedConfig = this.findMatchedConfig(pageUrl, 'page_view')
    
    if (matchedConfig) {
      this.track(matchedConfig.eventCode, 'page_view', {
        pageTitle: document.title
      })
    }
  }

  trackPageLeave() {
    const duration = Date.now() - this.pageEnterTime
    const pageUrl = getPageUrl()
    const matchedConfig = this.findMatchedConfig(pageUrl, 'page_view')
    
    if (matchedConfig) {
      this.track(matchedConfig.eventCode + '_leave', 'page_view', {
        duration: duration,
        pageTitle: document.title
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
}

export default TrackCore
