<template>
  <div class="dashboard-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本周提交需求</div>
              <div class="stat-value">{{ dashboardStats.weekSubmitCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon waiting">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待开发需求</div>
              <div class="stat-value">{{ dashboardStats.waitDevelopCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon online">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月已上线需求</div>
              <div class="stat-value">{{ dashboardStats.monthOnlineCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon alert">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">异常进度告警</div>
              <div class="stat-value alert-value">{{ dashboardStats.alertCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="trend-card" shadow="never">
      <template #header>
        <div class="panel-header">
          <span class="panel-title">需求新增与上线趋势</span>
          <div class="range-switch">
            <el-button
              :type="trendDays === 7 ? 'primary' : 'default'"
              size="small"
              @click="changeTrendDays(7)"
            >
              近7天
            </el-button>
            <el-button
              :type="trendDays === 30 ? 'primary' : 'default'"
              size="small"
              @click="changeTrendDays(30)"
            >
              近30天
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="trendLoading" class="trend-wrapper">
        <el-empty v-if="!trendData.length" description="暂无趋势数据" />
        <div v-else ref="trendChartRef" class="trend-chart"></div>
      </div>
    </el-card>

    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="panel-header">
          <span class="panel-title">需求列表</span>
          <el-button type="primary" @click="goToRequirementManage">前往需求管理</el-button>
        </div>
      </template>

      <el-table v-loading="listLoading" :data="requirementList" border stripe>
        <el-table-column prop="requirementId" label="需求ID" min-width="150" />
        <el-table-column label="需求标题" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" link class="title-link" @click="openRequirementDetail(row)">
              {{ row.title }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" effect="light">
              {{ priorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="proposerName" label="提出人" width="120" />
        <el-table-column prop="expectedOnlineDate" label="期望上线时间" width="130" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          background
          layout="prev, pager, next"
          @current-change="loadRequirementList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Calendar, CircleCheck, Clock, WarningFilled } from '@element-plus/icons-vue'
import {
  getRequirementDashboardStatistics,
  getRequirementDashboardTrend,
  getRequirementList
} from '../../api/requirement'
import { showActionError } from '../../utils/feedback'

const router = useRouter()

const STATUS_LABEL_MAP = {
  PENDING_REVIEW: '待审核',
  SCHEDULING: '排期中',
  DEVELOPING: '开发中',
  TESTING: '测试中',
  ONLINE: '已上线',
  OFFLINE: '已下线',
  REJECTED: '审核不通过'
}

const STATUS_TAG_TYPE_MAP = {
  PENDING_REVIEW: 'warning',
  SCHEDULING: 'info',
  DEVELOPING: '',
  TESTING: 'success',
  ONLINE: 'success',
  OFFLINE: 'danger',
  REJECTED: 'danger'
}

const PRIORITY_LABEL_MAP = {
  P0: 'P0(高)',
  P1: 'P1(中)',
  P2: 'P2(低)'
}

const PRIORITY_TAG_TYPE_MAP = {
  P0: 'danger',
  P1: 'warning',
  P2: 'info'
}

const statusText = (status) => STATUS_LABEL_MAP[status] || status || '-'
const statusTagType = (status) => STATUS_TAG_TYPE_MAP[status] || 'info'
const priorityText = (priority) => PRIORITY_LABEL_MAP[priority] || priority || '-'
const priorityTagType = (priority) => PRIORITY_TAG_TYPE_MAP[priority] || 'info'

const dashboardStats = reactive({
  weekSubmitCount: 0,
  waitDevelopCount: 0,
  monthOnlineCount: 0,
  alertCount: 0
})

const trendLoading = ref(false)
const trendDays = ref(7)
const trendData = ref([])
const trendChartRef = ref(null)
let trendChart = null

const listLoading = ref(false)
const requirementList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 3

const loadDashboardStatistics = async () => {
  try {
    const res = await getRequirementDashboardStatistics()
    if (res.code === 200) {
      Object.assign(dashboardStats, res.data || {})
      return
    }
    showActionError('加载仪表盘统计失败')
  } catch (error) {
    showActionError('加载仪表盘统计失败')
  }
}

const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const res = await getRequirementDashboardTrend(trendDays.value)
    if (res.code === 200) {
      trendData.value = Array.isArray(res.data) ? res.data : []
      return
    }
    showActionError('加载趋势数据失败')
  } catch (error) {
    showActionError('加载趋势数据失败')
  } finally {
    trendLoading.value = false
  }
}

const loadRequirementList = async () => {
  listLoading.value = true
  try {
    const res = await getRequirementList({
      pageNum: pageNum.value,
      pageSize,
      sortField: 'createTime',
      sortOrder: 'desc'
    })
    if (res.code === 200) {
      requirementList.value = res.data?.content || []
      total.value = res.data?.totalElements || 0
      return
    }
    showActionError('加载需求列表失败')
  } catch (error) {
    showActionError('加载需求列表失败')
  } finally {
    listLoading.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value || !trendData.value.length) {
    return
  }

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const xAxisData = trendData.value.map(item => item.date?.slice(5) || item.date)
  const newCountData = trendData.value.map(item => item.newCount || 0)
  const onlineCountData = trendData.value.map(item => item.onlineCount || 0)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: 6,
      data: ['需求新增', '需求上线']
    },
    grid: {
      left: 32,
      right: 20,
      top: 48,
      bottom: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '需求新增',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        itemStyle: { color: '#ea3156' },
        lineStyle: { width: 3, color: '#ea3156' },
        areaStyle: {
          color: 'rgba(234, 49, 86, 0.16)'
        },
        data: newCountData
      },
      {
        name: '需求上线',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        itemStyle: { color: '#fb7185' },
        lineStyle: { width: 3, color: '#fb7185' },
        areaStyle: {
          color: 'rgba(251, 113, 133, 0.12)'
        },
        data: onlineCountData
      }
    ]
  })
}

const changeTrendDays = async (days) => {
  if (trendDays.value === days) return
  trendDays.value = days
  await loadTrendData()
}

const openRequirementDetail = (row) => {
  router.push({
    path: '/requirement-manage/detail',
    query: {
      requirementId: row.requirementId,
      mode: 'detail'
    }
  })
}

const goToRequirementManage = () => {
  router.push('/requirement-manage')
}

const handleResize = () => {
  if (trendChart) {
    trendChart.resize()
  }
}

watch(
  () => trendData.value,
  async () => {
    await nextTick()
    renderTrendChart()
  },
  { deep: true }
)

onMounted(async () => {
  await Promise.all([
    loadDashboardStatistics(),
    loadTrendData(),
    loadRequirementList()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  margin: 0;
}

.stat-card {
  border: 1px solid #f7c7d1;
  border-radius: 12px;
  background: linear-gradient(180deg, #fff 0%, #fff7f8 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.stat-icon.week {
  background: linear-gradient(140deg, #ea3156 0%, #f66582 100%);
}

.stat-icon.waiting {
  background: linear-gradient(140deg, #df3658 0%, #f1859c 100%);
}

.stat-icon.online {
  background: linear-gradient(140deg, #cc2648 0%, #eb5f7b 100%);
}

.stat-icon.alert {
  background: linear-gradient(140deg, #b91c3f 0%, #e24a6a 100%);
}

.stat-label {
  font-size: 13px;
  color: #8f2b41;
}

.stat-value {
  margin-top: 4px;
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
  color: #7a1a2f;
}

.alert-value {
  color: #d3224c;
}

.trend-card,
.list-card {
  border-radius: 12px;
  border: 1px solid #f6d3da;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #6d1428;
}

.range-switch {
  display: flex;
  gap: 8px;
}

.trend-wrapper {
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-chart {
  width: 100%;
  height: 100%;
}

.title-link {
  padding: 0;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 992px) {
  .trend-wrapper {
    height: 300px;
  }

  .panel-title {
    font-size: 16px;
  }
}
</style>
