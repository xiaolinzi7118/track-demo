<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo-text">招商银行</div>
      <div class="logo-sub">China Merchants Bank</div>
    </div>
    <div class="login-card">
      <h2 class="card-title">欢迎登录</h2>
      <div class="form-group">
        <input
          v-model="username"
          type="text"
          class="form-input"
          placeholder="请输入用户名"
          data-track-id="login_username_input"
          @keyup.enter="handleLogin"
        />
      </div>
      <div class="form-group">
        <input
          v-model="password"
          type="password"
          class="form-input"
          placeholder="请输入密码"
          data-track-id="login_password_input"
          @keyup.enter="handleLogin"
        />
      </div>
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <button
        class="login-btn"
        data-track-id="login_submit_btn"
        :disabled="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '登 录' }}
      </button>
      <div class="hint">默认账号：admin / 123456</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/user'
import { setUserId, setUserInfo } from '../utils/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await login({ username: username.value, password: password.value })
    if (res.code === 200) {
      setUserId(res.data.id)
      setUserInfo(res.data)
      router.push('/')
    } else {
      errorMsg.value = res.message
    }
  } catch (e) {
    errorMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #d4282d, #b91c22);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px;
}
.login-header {
  text-align: center;
  padding: 60px 0 40px;
}
.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4px;
}
.logo-sub {
  font-size: 12px;
  color: rgba(255,255,255,0.7);
  margin-top: 8px;
  letter-spacing: 1px;
}
.login-card {
  width: 100%;
  max-width: 340px;
  background: #fff;
  border-radius: 12px;
  padding: 32px 24px;
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 24px;
}
.form-group {
  margin-bottom: 16px;
}
.form-input {
  width: 100%;
  height: 44px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0 16px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}
.form-input:focus {
  border-color: var(--cmb-red);
}
.error-msg {
  color: var(--cmb-red);
  font-size: 13px;
  margin-bottom: 12px;
  text-align: center;
}
.login-btn {
  width: 100%;
  height: 44px;
  background: var(--cmb-red);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: opacity 0.2s;
}
.login-btn:disabled {
  opacity: 0.6;
}
.login-btn:active {
  opacity: 0.8;
}
.hint {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 16px;
}
</style>
