<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon blue">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.total || 0 }}</div>
              <div class="stat-label">事件总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon green">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pageViewCount || 0 }}</div>
              <div class="stat-label">页面曝光事件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon orange">
              <el-icon><Mouse /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.clickCount || 0 }}</div>
              <div class="stat-label">点击交互事件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon purple">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dataStatistics.total || 0 }}</div>
              <div class="stat-label">上报数据总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="埋点事件趋势">
          <div class="chart-container">
            <el-empty v-if="trendData.length === 0" description="暂无数据" />
            <div v-else class="chart-placeholder">
              <p>趋势图（可集成ECharts实现）</p>
              <el-table :data="trendData.slice(-7)" size="small">
                <el-table-column prop="date" label="日期" />
                <el-table-column prop="count" label="事件数" />
              </el-table>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="上报数据统计">
          <div class="chart-container">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="页面曝光事件">{{ dataStatistics.pageViewCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="点击交互事件">{{ dataStatistics.clickCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="总曝光时长(秒)">{{ Math.floor((dataStatistics.totalDuration || 0) / 1000) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card title="快捷入口" class="quick-entry">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="entry-item" @click="goToConfig">
            <el-icon class="entry-icon"><Setting /></el-icon>
            <span>事件管理</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="entry-item" @click="goToData">
            <el-icon class="entry-icon"><Search /></el-icon>
            <span>数据回检查询</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="entry-item">
            <el-icon class="entry-icon"><Download /></el-icon>
            <span>SDK下载</span>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="entry-item">
            <el-icon class="entry-icon"><Document /></el-icon>
            <span>使用文档</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getEventManageStatistics, getTrackDataStatistics, getTrackDataTrend } from '../../api/track'

const router = useRouter()

const statistics = reactive({})
const dataStatistics = reactive({})
const trendData = reactive([])

const fetchData = async () => {
  try {
    const [configRes, dataRes, trendRes] = await Promise.all([
      getEventManageStatistics(),
      getTrackDataStatistics(),
      getTrackDataTrend()
    ])
    
    if (configRes.code === 200) {
      Object.assign(statistics, configRes.data)
    }
    if (dataRes.code === 200) {
      Object.assign(dataStatistics, dataRes.data)
    }
    if (trendRes.code === 200) {
      trendData.splice(0, trendData.length, ...trendRes.data)
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

const goToConfig = () => {
  router.push('/event-manage')
}

const goToData = () => {
  router.push('/track-data')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #ea3156, #f06a86);
}

.stat-icon.green {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #909399, #a6a9ad);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  width: 100%;
  text-align: center;
  color: #909399;
}

.chart-placeholder p {
  margin-bottom: 16px;
}

.quick-entry {
  margin-top: 20px;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.entry-item:hover {
  background: #fcebef;
  transform: translateY(-2px);
}

.entry-icon {
  font-size: 32px;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.entry-item span {
  font-size: 14px;
  color: #606266;
}
</style>
