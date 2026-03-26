import TrackCore from './core'
import { generateId, getUUId, deepClone } from './utils'

const TrackSDK = {
  _config: {
    serverUrl: '',
    appId: '',
    debug: false,
    autoTrack: {
      pageView: true,
      click: true
    }
  },

  _instance: null,

  init(config) {
    if (!config.serverUrl) {
      console.error('TrackSDK init error: serverUrl is required')
      return
    }

    this._config = {
      ...this._config,
      ...config
    }

    this._instance = new TrackCore(this._config)
    this._instance.init()
    
    if (this._config.debug) {
      console.log('TrackSDK initialized successfully', this._config)
    }

    return this
  },

  track(eventCode, eventType, params = {}) {
    if (!this._instance) {
      console.error('TrackSDK is not initialized, please call init() first')
      return
    }
    this._instance.track(eventCode, eventType, params)
  },

  trackPageView(eventCode, params = {}) {
    this.track(eventCode, 'page_view', params)
  },

  trackClick(eventCode, params = {}) {
    this.track(eventCode, 'click', params)
  },

  setUserId(userId) {
    if (this._instance) {
      this._instance.setUserId(userId)
    }
  },

  setUserInfo(userInfo) {
    if (this._instance) {
      this._instance.setUserInfo(userInfo)
    }
  },

  getSessionId() {
    return this._instance ? this._instance.getSessionId() : ''
  }
}

if (typeof window !== 'undefined') {
  window.TrackSDK = TrackSDK
}

export default TrackSDK
