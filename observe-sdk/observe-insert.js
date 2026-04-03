import { ObserveTrigger, ObservePoster } from './perfm-trigger'

window.wlbOpacteam = {
  expo: {
    // 曝光埋点配置
    isOn: true, // 总开关
    logOn: true, // 日志开关
    scanInteval: 3000 // 扫描dom间隔时间 ms
  },

  perfm: {
    // 性能监控埋点配置
    isOn: true, // 总开关
    logOn: true, // 日志开关
    pageId: 'MixinPage',
    obDuration: 1, // 监控时长 （分钟）
    debounceMs: 7000 // 提交防抖 （毫秒）
  }
}
let instanceTrigger = new ObserveTrigger(window)
instanceTrigger.startObserve()

// 启动数据提交
const instancePoster = new ObservePoster(window)

console.log(`genUID:`, instancePoster.genUID())

instancePoster.testUID()