<template>
  <div class="track-data">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>埋点数据回检</span>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="事件编码">
            <el-input
              v-model="searchForm.eventCode"
              placeholder="请输入事件编码"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="事件类型">
            <el-select v-model="searchForm.eventType" clearable placeholder="请选择" style="width: 160px">
              <el-option label="页面曝光" value="page_view" />
              <el-option label="点击交互" value="click" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户ID">
            <el-input
              v-model="searchForm.userId"
              placeholder="请输入用户ID"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        size="default"
      >
        <el-table-column prop="eventCode" label="事件编码" min-width="140" show-overflow-tooltip />
        <el-table-column prop="eventType" label="事件类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.eventType === 'page_view' ? 'success' : 'primary'" size="small">
              {{ row.eventType === 'page_view' ? '页面曝光' : '点击交互' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="页面URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="sessionId" label="会话ID" width="120" show-overflow-tooltip />
        <el-table-column label="停留时长" width="110" align="right">
          <template #default="{ row }">
            <span v-if="row.duration">{{ row.duration >= 1000 ? (row.duration / 1000).toFixed(1) + 's' : row.duration + 'ms' }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="eventTime" label="事件时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="埋点数据详情" width="750px" destroy-on-close>
      <el-descriptions :column="1" border size="default" v-if="detailData">
        <el-descriptions-item label="事件编码">{{ detailData.eventCode }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">
          <el-tag :type="detailData.eventType === 'page_view' ? 'success' : 'primary'" size="small">
            {{ detailData.eventType === 'page_view' ? '页面曝光' : '点击交互' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="页面URL">{{ detailData.url || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailData.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会话ID">{{ detailData.sessionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="停留时长">
          <span v-if="detailData.duration">{{ detailData.duration >= 1000 ? (detailData.duration / 1000).toFixed(1) + 's' : detailData.duration + 'ms' }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent">{{ detailData.userAgent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="事件时间">{{ formatTime(detailData.eventTime) }}</el-descriptions-item>
        <el-descriptions-item label="上报参数">
          <pre class="detail-params">{{ formatParams(detailData.params) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-card title="数据统计" style="margin-top: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">上报数据总数</div>
            <div class="stat-value">{{ statistics.total || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">页面曝光事件</div>
            <div class="stat-value">{{ statistics.pageViewCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">点击交互事件</div>
            <div class="stat-value">{{ statistics.clickCount || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">总曝光时长(秒)</div>
            <div class="stat-value">{{ Math.floor((statistics.totalDuration || 0) / 1000) }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { getTrackDataList, getTrackDataStatistics } from '../../api/track'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statistics = reactive({})

const searchForm = reactive({
  eventCode: '',
  eventType: '',
  userId: ''
})

const formatParams = (params) => {
  if (!params) return '-'
  try {
    return JSON.stringify(JSON.parse(params), null, 2)
  } catch {
    return params
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  // Java LocalDateTime会丢失Z后缀，需要补上确保按UTC解析
  let timeStr = time
  if (typeof timeStr === 'string' && timeStr.includes('T') && !timeStr.endsWith('Z') && !timeStr.includes('+')) {
    timeStr = timeStr + 'Z'
  }
  const d = new Date(timeStr)
  if (isNaN(d.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const detailVisible = ref(false)
const detailData = ref(null)

const handleDetail = (row) => {
  detailData.value = row
  detailVisible.value = true
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getTrackDataList({
      ...searchForm,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data.content || res.data
      total.value = res.data.totalElements || res.data.length || 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.eventCode = ''
  searchForm.eventType = ''
  searchForm.userId = ''
  pageNum.value = 1
  handleSearch()
}

const fetchStatistics = async () => {
  try {
    const res = await getTrackDataStatistics()
    if (res.code === 200) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    console.error(error)
  }
}

handleSearch()
fetchStatistics()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.params-text {
  color: #409eff;
  cursor: pointer;
}

.params-text:hover {
  text-decoration: underline;
}

.text-muted {
  color: #c0c4cc;
}

.detail-params {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  color: #333;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
</style>
