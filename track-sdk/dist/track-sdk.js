(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, global.TrackSDK = factory());
})(this, (function () { 'use strict';

  function generateId() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : r & 0x3 | 0x8;
      return v.toString(16);
    });
  }
  function getUUId() {
    let uuid = localStorage.getItem('track_uuid');
    if (!uuid) {
      uuid = generateId();
      localStorage.setItem('track_uuid', uuid);
    }
    return uuid;
  }
  function getSessionId() {
    let sessionId = sessionStorage.getItem('track_session_id');
    if (!sessionId) {
      sessionId = generateId();
      sessionStorage.setItem('track_session_id', sessionId);
    }
    return sessionId;
  }
  function getPageUrl() {
    return window.location.href;
  }
  function getUserAgent() {
    return window.navigator.userAgent;
  }
  function getTimestamp() {
    return new Date().toISOString();
  }
  function matchUrlPattern(url, pattern) {
    if (!pattern) return true;
    try {
      const regex = new RegExp(pattern);
      return regex.test(url);
    } catch (e) {
      console.error('Invalid URL pattern:', pattern, e);
      return false;
    }
  }

  class TrackCore {
    constructor(config) {
      this.config = config;
      this.userId = '';
      this.userInfo = {};
      this.sessionId = getSessionId();
      this.uuid = getUUId();
      this.queue = [];
      this.maxQueueSize = 20;
      this.flushInterval = 5000;
      this.timer = null;
      this.pageEnterTime = Date.now();
      this.currentTrackConfig = null;
      this.isVisible = true;
    }
    init() {
      this.fetchTrackConfig();
      this.initAutoTrack();
      this.startFlushTimer();
      this.initVisibilityChange();
    }
    async fetchTrackConfig() {
      try {
        const response = await fetch(`${this.config.serverUrl}/api/track-config/all`);
        const result = await response.json();
        if (result.code === 200) {
          this.currentTrackConfig = result.data || [];
          if (this.config.debug) {
            console.log('Track config fetched:', this.currentTrackConfig);
          }
        }
      } catch (error) {
        console.error('Failed to fetch track config:', error);
      }
    }
    initAutoTrack() {
      const {
        autoTrack
      } = this.config;
      if (autoTrack.pageView) {
        this.trackPageView();
        window.addEventListener('popstate', () => {
          this.trackPageView();
        });
      }
      if (autoTrack.click) {
        document.addEventListener('click', e => {
          this.handleClick(e);
        }, true);
      }
      window.addEventListener('beforeunload', () => {
        this.trackPageLeave();
        this.flush();
      });
    }
    initVisibilityChange() {
      document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
          this.isVisible = false;
          this.trackPageLeave();
        } else {
          this.isVisible = true;
          this.pageEnterTime = Date.now();
          this.trackPageView();
        }
      });
    }
    handleClick(e) {
      const target = e.target;
      const trackId = target.getAttribute('data-track-id') || target.getAttribute('track-id') || this.findParentTrackId(target);
      if (trackId) {
        const params = {
          elementTag: target.tagName,
          elementText: target.innerText?.substring(0, 50),
          elementClass: target.className,
          elementId: target.id
        };
        this.track(trackId, 'click', params);
      }
    }
    findParentTrackId(element) {
      let parent = element.parentElement;
      while (parent && parent !== document.body) {
        const trackId = parent.getAttribute('data-track-id') || parent.getAttribute('track-id');
        if (trackId) {
          return trackId;
        }
        parent = parent.parentElement;
      }
      return null;
    }
    trackPageView() {
      const pageUrl = getPageUrl();
      const matchedConfig = this.findMatchedConfig(pageUrl, 'page_view');
      if (matchedConfig) {
        this.track(matchedConfig.eventCode, 'page_view', {
          pageTitle: document.title
        });
      }
    }
    trackPageLeave() {
      const duration = Date.now() - this.pageEnterTime;
      const pageUrl = getPageUrl();
      const matchedConfig = this.findMatchedConfig(pageUrl, 'page_view');
      if (matchedConfig) {
        this.track(matchedConfig.eventCode + '_leave', 'page_view', {
          duration: duration,
          pageTitle: document.title
        }, duration);
      }
    }
    findMatchedConfig(url, eventType) {
      if (!this.currentTrackConfig) return null;
      return this.currentTrackConfig.find(config => {
        if (config.eventType !== eventType) return false;
        if (config.status !== 1) return false;
        return matchUrlPattern(url, config.urlPattern);
      });
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
      };
      this.queue.push(data);
      if (this.config.debug) {
        console.log('Track event added to queue:', data);
      }
      if (this.queue.length >= this.maxQueueSize) {
        this.flush();
      }
    }
    startFlushTimer() {
      this.timer = setInterval(() => {
        if (this.queue.length > 0) {
          this.flush();
        }
      }, this.flushInterval);
    }
    async flush() {
      if (this.queue.length === 0) return;
      const data = [...this.queue];
      this.queue = [];
      try {
        const response = await fetch(`${this.config.serverUrl}/api/track-data/batch-report`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data),
          keepalive: true
        });
        const result = await response.json();
        if (this.config.debug) {
          if (result.code === 200) {
            console.log('Track data reported successfully:', data);
          } else {
            console.error('Failed to report track data:', result.message);
          }
        }
      } catch (error) {
        console.error('Failed to report track data:', error);
        this.queue = [...data, ...this.queue];
      }
    }
    setUserId(userId) {
      this.userId = userId;
    }
    setUserInfo(userInfo) {
      this.userInfo = userInfo;
    }
    getSessionId() {
      return this.sessionId;
    }
  }

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
        console.error('TrackSDK init error: serverUrl is required');
        return;
      }
      this._config = {
        ...this._config,
        ...config
      };
      this._instance = new TrackCore(this._config);
      this._instance.init();
      if (this._config.debug) {
        console.log('TrackSDK initialized successfully', this._config);
      }
      return this;
    },
    track(eventCode, eventType, params = {}) {
      if (!this._instance) {
        console.error('TrackSDK is not initialized, please call init() first');
        return;
      }
      this._instance.track(eventCode, eventType, params);
    },
    trackPageView(eventCode, params = {}) {
      this.track(eventCode, 'page_view', params);
    },
    trackClick(eventCode, params = {}) {
      this.track(eventCode, 'click', params);
    },
    setUserId(userId) {
      if (this._instance) {
        this._instance.setUserId(userId);
      }
    },
    setUserInfo(userInfo) {
      if (this._instance) {
        this._instance.setUserInfo(userInfo);
      }
    },
    getSessionId() {
      return this._instance ? this._instance.getSessionId() : '';
    }
  };
  if (typeof window !== 'undefined') {
    window.TrackSDK = TrackSDK;
  }

  return TrackSDK;

}));
//# sourceMappingURL=track-sdk.js.map
