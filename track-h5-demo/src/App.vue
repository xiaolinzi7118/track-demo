<template>
  <div class="app">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
:root {
  --cmb-red: #d4282d;
  --cmb-red-dark: #b91c22;
  --cmb-red-light: #fff0f0;
  --cmb-gold: #c9a96e;
  --cmb-bg: #f5f5f5;
  --cmb-white: #ffffff;
  --cmb-text-primary: #333333;
  --cmb-text-secondary: #666666;
  --cmb-text-hint: #999999;
  --cmb-border: #eeeeee;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app, .app {
  height: 100%;
  width: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #f5f5f5;
  -webkit-font-smoothing: antialiased;
}
</style>
