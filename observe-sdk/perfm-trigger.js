class DataInfo {
  static getPerfmDataStructure() {
    return {
      pageId: '',
      deviceId: '',
      clientUid: '', // 单次会话生成的页面唯一ID
      cls: 0,
      fp: 0,
      fcp: 0,
      domload: 0,
      dns: 0,
      tcp: 0,
      fbyte: 0,
      lcp: 0,
      lcpurl: '',
      fid: 0,
      encodedSize: 0, // 资源在浏览器中解码后的byte
      transferSize: 0, // 网络上传输时的byte
      deviceType: 0, // 1 安卓 2 ios 0 其他
      clientUrl: '',
      clientAgent: '', // navigator.userAgent
      clientNavi: '', // 其他时间的指标
      clientTime: null,
      clientIp: '',
      clientPos: '',
      appVersion: '',
      deviceBrand: '',
      deviceSpec: '',
      networkType: '',
      offlinepkgVersion: ''
    }
  }
}

/**
 * 监控系列公共基类
 */
class BaseMonitor {
  constructor(_window) {
    this.context = _window
    this.perfmCacheKey = 'wlb-perfm-cache'
    this.localstoreKey = 'wlbobs'
    this.logStr = '[Opacteam Monitor]'
    this.isOptionChkPass = false
    this.inApp = this.context.navigator.userAgent.includes('mPaaS')

    // 读取配置
    if (this.context && this.context.wlbOpacteam) {
      const _options = this.context.wlbOpacteam.perfm
      this.options = _options || { pageId: 'unknownPageId', obDuration: 2, debounceMs: 8000, isOn: true, logOn: true }
      this.options.pageId = this.options.pageId ? this.options.pageId : 'unknownPageId'
      this.options.obDuration = this.options.obDuration ? this.options.obDuration : 2
      this.options.debounceMs = this.options.debounceMs ? this.options.debounceMs : 8000
      this.isOptionChkPass = true
    } else {
      this.context.console.error('请正确配置监控参数！')
      return
    }

    this.startTime = new Date().getTime() // 启动监控的时间戳
    this.log('基类已初始化', this.startTime)
  }

  log(message, ...args) {
    if (!this.isLoggingEnabled()) return
    this.context.console.log(`${this.logStr} ${message}`, ...args)
  }

  warn(message, ...args) {
    if (!this.isLoggingEnabled()) return
    this.context.console.warn(`${this.logStr} ${message}`, ...args)
  }

  error(message, ...args) {
    if (!this.isLoggingEnabled()) return
    this.context.console.error(`${this.logStr} ${message}`, ...args)
  }

  isLoggingEnabled() {
    return (this.options && this.options.logOn) || false
  }
}

/**
 * 指标抓取类
 */
class ObserveTrigger extends BaseMonitor {
  constructor(_window) {
    super(_window)

    this.perfmData = DataInfo.getPerfmDataStructure()

    this.init()

    // 启动数据提交
    // new ObservePoster(_window)
  }

  init() {
    // 清空缓存
    this.context.localStorage.setItem(this.perfmCacheKey, '')

    if (!this.context.PerformanceObserver) {
      const errMsg = '不支持PerformanceObserver'
      this.logLocal(errMsg)
      this.error(errMsg)
      return
    } else {
      const errMsg = '当前浏览器支持 PerformanceObserver'
      this.logLocal(errMsg)
      this.log(errMsg)
    }
  }

  logLocal(str) {
    let cacheData = this.context.localStorage.getItem(this.localstoreKey)
    this.context.localStorage.setItem(this.localstoreKey, cacheData ? cacheData + ',' + str : str)
  }

  saveLocal() {
    // 将监控数据临时缓存到localstorage
    this.context.localStorage.setItem(this.perfmCacheKey, JSON.stringify(this.perfmData))
  }

  mainHandler = entries => {
    // 主监控实例的处理逻辑
    const navEntr = entries.getEntriesByType('navigation')

    if (navEntr.length > 0) {
      const _navi = navEntr[0]

      // this.perfmData.clientNavi = JSON.stringify(_navi)
      this.perfmData.clientUrl = _navi.name
      this.perfmData.encodedSize = _navi.encodedBodySize
      this.perfmData.transferSize = _navi.transferSize
      // this.context.localStorage.setItem('_navi', this.perfmData.clientNavi)

      this.perfmData.domload = _navi.loadEventEnd - _navi.startTime
      this.perfmData.dns = _navi.domainLookupEnd - _navi.domainLookupStart
      this.perfmData.tcp = _navi.connectEnd - _navi.connectStart
      this.perfmData.fbyte = _navi.responseStart - _navi.requestStart

      this.logLocal(
        'mainHandler DOM加载时间：' +
          this.perfmData.domload +
          ' DNS查询时间：' +
          this.perfmData.dns +
          ' TCP连接时间：' +
          this.perfmData.tcp +
          ' FByte首字节时间：' +
          this.perfmData.fbyte
      )

      this.saveLocal()
    }

    const listEntr = entries.getEntries()
    for (const item of listEntr) {
      if (item.name === 'first-paint') {
        this.perfmData.fp = item.startTime
        this.logLocal('FP首次绘制时间：' + this.perfmData.fp)
        this.saveLocal()
      }

      if (item.name === 'first-contentful-paint') {
        this.perfmData.fcp = item.startTime
        this.logLocal('FCP首次内容绘制时间：' + this.perfmData.fcp)

        this.saveLocal()
      }

      if (item.entryType === 'largest-contentful-paint') {
        this.perfmData.lcp = item.startTime
        this.perfmData.lcpurl = item.url
        this.logLocal('LCP最大内容绘制时间' + this.perfmData.lcp)
        this.saveLocal()
      }
    }
  }

  resHandler = entries => {
    const listEntr = entries.getEntries()
    for (const item of listEntr) {

      if (item.entryType === 'largest-contentful-paint') {
        this.perfmData.lcp = item.startTime
        this.perfmData.lcpurl = item.url
        this.logLocal('LCP最大内容绘制时间' + this.perfmData.lcp)
        this.saveLocal()
      }
    }
  }

  clsHandler = entries => {
    // cls处理逻辑
    for (const entry of entries.getEntries()) {
      if (!entry.hadRecentInput) {
        this.perfmData.cls += isNaN(entry.value) ? 0 : entry.value
      }
    }

    this.logLocal('CLS累积布局偏移:' + this.perfmData.cls)
    this.saveLocal()
  }

  fidHandler = entries => {
    // fid处理逻辑
    for (const entry of entries.getEntries()) {
      if (entry.entryType === 'first-input') {
        this.perfmData.fid = entry.processingEnd - entry.processingStart
        this.logLocal(`FID首次输入延迟` + this.perfmData.fid)
      }
    }

    this.saveLocal()
  }

  // 根据处理方法new新的监控对象
  createob(_fn) {
    return new PerformanceObserver(entries => {
      _fn(entries)
    })
  }

  startObserve() {
    try {
      this.log('启动性能监控')

      const userAgent = navigator.userAgent
      this.perfmData.clientAgent = userAgent
      //   localStorage.setItem('userAgent', userAgent)
      const isiOS = /iPhone|iPad|iPod/i.test(userAgent)
      const isAndroid = /Android/.test(userAgent)

      if (isAndroid) {
        this.perfmData.deviceType = 1
      } else if (isiOS) {
        this.perfmData.deviceType = 2
      } else {
        this.perfmData.deviceType = 0
      }
      // const isiOS = true // 测试

      if (isiOS) {
        // iOS设备根据type逐个切换监控
        const typeKey = 'perfm-type'
        let perfmTypeIndex = localStorage.getItem(typeKey)
        const scheduleList = ['navi'] // 单次执行队列 控制执行频率
        let currentKey = perfmTypeIndex ? Number(perfmTypeIndex) : 0
        currentKey = currentKey >= scheduleList.length ? 0 : currentKey // 轮询完之后从0开始

        switch (scheduleList[currentKey]) {
          case 'navi':
            {
              window.obsMain = this.createob(this.mainHandler)
              window.obsMain.observe({ entryTypes: ['navigation', 'paint'] })
              this.logLocal(`mainHandlerOK`)
            }
            break

          case 'res':
            {
              window.obsRes = this.createob(this.resHandler)
              window.obsRes.observe({ entryTypes: ['largest-contentful-paint'] })
              this.logLocal(`resHandlerOK`)
            }
            break

          case 'cls':
            {
              window.obsCls = this.createob(this.clsHandler)
              window.obsCls.observe({ entryTypes: ['layout-shift'] })
              this.logLocal(`fidHandlerOK`)
            }
            break

          case 'fid':
            {
              window.obsFid = this.createob(this.fidHandler)
              window.obsFid.observe({ entryTypes: ['first-input'] })
              this.logLocal(`clsHandlerOK`)
            }
            break

          default:
            break
        }

        // 存入下次应该执行的index
        localStorage.setItem(typeKey, ++currentKey)
      } else {
        // 4个观察者一起执行
        this.context.obsMain = this.createob(this.mainHandler)
        this.context.obsRes = this.createob(this.resHandler)

        try {
          this.context.obsRes.observe({ entryTypes: ['resource', 'largest-contentful-paint'] })
          this.logLocal(`resHandlerOK`)
        } catch (error) {
          this.logLocal('报错了' + error.message)
        }

        try {
          this.context.obsMain.observe({ entryTypes: ['navigation', 'paint'] })
          this.logLocal(`mainHandlerOK`)
        } catch (error) {
          this.logLocal('报错了' + error.message)
        }
      }
    } catch (error) {
      this.logLocal('报错了' + error.message)
    }
  }
}

/**
 * 性能监控数据提交类
 */
class ObservePoster extends BaseMonitor {
  constructor(_window) {
    super(_window)

    this.MGSAPI = 'com.wlbbank.mnt.st.commitPerfm'
    this.commitFailCount = 0 // 记录提交错误次数

    // 轮询器实例
    this.instanceInteval = null

    this.lastCacheStr = null

    this.debounceTimeout = 0
    this.debounceLastExecuted = 0

    this.init()
  }

  getDeviceData() {
    return new Promise(resolve => {
      let params = {
        fieldsName: ['deviceTypeNo', 'phoneModel', 'version', 'networkType']
      }

      this.inApp
        ? this.jsBridgeReady(() => {
            this.context.AlipayJSBridge.call('getDeviceInfoData', params, res => {
              resolve(res.respJson)
            })
          })
        : resolve({ virtualDeviceId: 'testdevice' })
    })
  }

  jsBridgeReady(e) {
    this.context.AlipayJSBridge ? e && e() : document.addEventListener('AlipayJSBridgeReady', e, !1)
  }

  requestRPC(dataInfo) {
    // call MGS 接口
    if (!this.inApp) {
      this.log(`App中才可以提交`, dataInfo)
      return
    }

    let requestParams = [
      {
        _requestBody: dataInfo
      }
    ]

    this.jsBridgeReady(() => {
      this.context.AlipayJSBridge.call(
        'rpc',
        {
          operationType: this.MGSAPI,
          requestData: requestParams,
          headers: {
            'Content-Type': 'application/json'
          }
        },
        res => {
          this.log('rpc 结果: ', res)
          const isSuc = res.error === 'SUC0000'

          // 提交失败则重新提交
          if (!isSuc) {
            // 取缓存
            const perfmCache = this.context.localStorage.getItem(this.perfmCacheKey)

            if (!perfmCache) {
              return
            }

            let objCache = JSON.parse(perfmCache)

            // 增加提交失败次数
            objCache.commitFailCount = ++this.commitFailCount

            // 记录缓存 下次轮询检测到缓存有变化 重新提交
            this.context.localStorage.setItem(this.perfmCacheKey, JSON.stringify(objCache))
          }
        }
      )
    })
  }

  genUID() {
    // 时间戳base36编码 + 随机数base 36编码
    const timePart = Date.now().toString(36)
    const randomPart = Math.random().toString(36)
    return (timePart + randomPart).replace('.', '').toUpperCase()
  }

  testUID() {
    let elems = new Map()

    for (let i = 0; i < 100000; i++) {
      let id = this.genUID()
      elems.set(id, i)
    }

    console.log('testUID', elems.size)
  }

  debounce = function(fn, config) {
    const { wait = 0, immediate = false } = config

    return function(...args) {
      const now = Date.now()
      const elapsed = now - this.debounceLastExecuted

      if (immediate) {
        // 如果是立即模式，且时间差大于等于等待时间，立即执行
        if (elapsed >= wait) {
          fn.apply(this, args)
          this.debounceLastExecuted = now
        } else {
          // 如果时间差小于等待时间，清除之前的计时器，并设置新的计时器
          clearTimeout(this.debounceTimeout)
          this.debounceTimeout = setTimeout(() => {
            fn.apply(this, args)
            this.debounceLastExecuted = Date.now()
          }, wait - elapsed)
        }
      } else {
        // 非立即模式，清除之前的计时器，并设置新的计时器
        clearTimeout(this.debounceTimeout)
        this.debounceTimeout = setTimeout(() => {
          fn.apply(this, args)
          this.debounceLastExecuted = Date.now()
        }, wait)
      }
    }
  }

  async commit() {
    // 判断数据是否有变化 无则不处理
    this.log(`this.perfmCacheKey`, this.perfmCacheKey)
    const perfmCache = this.context.localStorage.getItem(this.perfmCacheKey)
    // this.log(typeof perfmCache)

    if (this.lastCacheStr && this.lastCacheStr === perfmCache) {
      this.log(`数据无变化`)
      return
    } else {
      this.lastCacheStr = perfmCache
      this.log(`开始处理数据提交`)
    }

    let perfmData = JSON.parse(perfmCache)
    perfmData.clientUid = this.genUID()
    perfmData.clientTime = new Date().getTime()
    perfmData.pageId = this.options.pageId

    try {
      const deviceRes = await this.getDeviceData()
      this.log('deviceRes', deviceRes)

      perfmData.deviceId = deviceRes.virtualDeviceId
      perfmData.clientIp = deviceRes.ip
      perfmData.appVersion = deviceRes.appVersion
      perfmData.deviceBrand = deviceRes.data.phoneModel
      perfmData.deviceSpec = deviceRes.data.deviceTypeNo
      perfmData.offlinepkgVersion = deviceRes.data.version
      perfmData.networkType = deviceRes.data.networkType
    } catch (error) {
      this.log('取设备号发生异常：', error.message)
      perfmData.deviceId = 'nodeviceid'
    }

    this.log(`perfmData`, perfmData)
    this.requestRPC(perfmData)
  }

  init() {
    this.log(`数据解析开始`)

    this.instanceInteval = setInterval(() => {
      const currentTime = new Date().getTime()
      const running = currentTime - this.startTime
      this.log(`开始判断数据提交 已持续${running / 1000}s / ${this.options.obDuration}m`)

      // 判断是否达到最大监控时间
      if (running >= this.options.obDuration * 1000 * 60) {
        // 到期了 结束监控 结束轮询
        if (this.context.obsMain) {
          this.context.obsMain.disconnect()
          this.log(`已关闭 obsMain`)
        }

        if (this.context.obsRes) {
          this.context.obsRes.disconnect()
          this.log(`已关闭 obsRes`)
        }

        if (this.context.obsFid) {
          this.context.obsFid.disconnect()
          this.log(`已关闭 obsFid`)
        }

        if (this.context.obsCls) {
          this.context.obsCls.disconnect()
          this.log(`已关闭 obsCls`)
        }

        this.log(`结束轮询`)
        clearInterval(this.instanceInteval)
      }

      this.commit()
    }, this.options.debounceMs)
  }
}

export { ObserveTrigger, ObservePoster }