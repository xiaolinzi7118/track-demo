import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(router)

app.mount('#app')

if (window.TrackSDK) {
  window.TrackSDK.init({
    serverUrl: 'http://localhost:8080',
    appId: 'cmb-app',
    debug: true,
    autoTrack: {
      pageView: true,
      click: true
    }
  })
}

// mock 用户信息和系统信息
window.userInfo = {
  "id": 1,
  "username": "admin",
  "password": null,
  "nickname": "管理员",
  "avatar": "",
  "status": 1,
  "createTime": "2026-04-08T17:20:29.344184",
  "updateTime": "2026-04-08T17:20:29.344184"
}

window.system = {
  os: 'iOS',
  browser: 'Safari',
  device: 'iPhone',
  appVersion: '1.0.0'
}