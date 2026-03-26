import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(router)

app.mount('#app')

if (window.TrackSDK) {
  window.TrackSDK.init({
    serverUrl: 'http://localhost:8080',
    appId: 'demo-app',
    debug: true,
    autoTrack: {
      pageView: true,
      click: true
    }
  })
}
