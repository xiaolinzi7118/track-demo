<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --el-color-primary: #ea3156;
  --el-color-primary-dark-2: #bb2745;
  --el-color-primary-light-1: #ec4768;
  --el-color-primary-light-2: #ee5878;
  --el-color-primary-light-3: #ef6f89;
  --el-color-primary-light-4: #f28197;
  --el-color-primary-light-5: #f498ab;
  --el-color-primary-light-6: #f6a8b8;
  --el-color-primary-light-7: #f8c1cd;
  --el-color-primary-light-8: #fbd6de;
  --el-color-primary-light-9: #fcebef;
  --el-color-primary-rgb: 234, 49, 86;
  --el-menu-active-color: #ea3156;
  --el-border-radius-base: 4px;
  --el-border-radius-small: 4px;
  --el-border-radius-round: 20px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
}

body {
  background: #f5f6fa;
  color: #303133;
  font-size: 14px;
}

.el-card {
  border-radius: 6px;
  box-shadow: none;
}

.el-card__header {
  padding: 14px 16px;
}

.el-card__body {
  padding: 16px;
}

.el-button {
  border-radius: 4px;
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  border-radius: 4px;
}

.el-form--inline .el-form-item {
  margin-right: 16px;
  margin-bottom: 14px;
}

.el-table th.el-table__cell {
  background: #fafafa;
}

.el-pagination {
  margin-top: 2px;
}
</style>
