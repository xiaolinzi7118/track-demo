<template>
  <div class="reset-data">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>重置数据</span>
        </div>
      </template>

      <div class="reset-buttons">
        <el-button v-permission="'system-reset-data:operate'" type="danger" @click="clearTrackFn('config')" :loading="loading.config">
          <el-icon><Delete /></el-icon>
          清空埋点配置表
        </el-button>
        <el-button v-permission="'system-reset-data:operate'" type="danger" @click="clearTrackFn('data')" :loading="loading.data">
          <el-icon><Delete /></el-icon>
          清空数据回检表
        </el-button>
      </div>

      <div class="warning">
        <el-alert
          title="警告"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <div>
              <p>1. 此操作会清空对应表中的所有数据，操作不可恢复</p>
              <p>2. 请谨慎操作，建议在操作前备份数据</p>
            </div>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { clearTrackConfig, clearTrackData } from '../../api/track'

const loading = ref({
  config: false,
  data: false
})


const clearTrackFn = async (type) => {
  try {
    loading.value.data = true
    let response = null
    let str = ''
    if (type === 'data') {
      str = '数据回检表'
      response = await clearTrackData()
    } else {
      str = '埋点配置表'
      response = await clearTrackConfig()
    }
    if (response.code === 200) {
      ElMessage.success(`清空${str}成功`)
    } else {
      ElMessage.error(`清空${str}失败`)
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loading.value.data = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reset-buttons {
  margin: 20px 0;
  display: flex;
  gap: 20px;
}

.warning {
  margin-top: 20px;
}
</style>