<template>
  <div class="event-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>事件管理</span>
          <el-button v-permission="'event-manage:add'" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增事件
          </el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="事件类型">
            <el-select v-model="searchForm.eventType" clearable placeholder="请选择" style="width: 160px">
              <el-option label="页面曝光" value="page_view" />
              <el-option label="点击交互" value="click" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="事件名称/事件编码"
              clearable
              style="width: 220px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
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
      >
        <el-table-column prop="eventName" label="事件名称" min-width="150" />
        <el-table-column prop="eventCode" label="事件编码" min-width="160" />
        <el-table-column prop="eventType" label="事件类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.eventType === 'page_view' ? 'success' : 'primary'">
              {{ row.eventType === 'page_view' ? '页面曝光' : '点击交互' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联需求" min-width="220">
          <template #default="{ row }">
            <div class="requirement-cell">
              <span class="requirement-title">{{ row.requirementTitle || '-' }}</span>
              <el-tag v-if="row.requirementStatusLabel" size="small" type="info">{{ row.requirementStatusLabel }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'event-manage:edit'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-permission="'event-manage:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
        >
          <template #total>
            共 {{ Math.ceil(total / pageSize) || 0 }} 页
          </template>
        </el-pagination>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑事件' : '新增事件'"
      width="1300px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="事件名称" prop="eventName">
              <el-input v-model="form.eventName" placeholder="请输入事件名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事件编码" prop="eventCode">
              <el-select
                v-model="form.eventCode"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="请选择或输入事件编码"
                style="width: 100%"
              >
                <el-option
                  v-for="item in eventCodeOptions"
                  :key="item.value"
                  :label="`${item.label} (${item.value})`"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事件类型" prop="eventType">
              <el-select v-model="form.eventType" placeholder="请选择" @change="onEventTypeChange">
                <el-option label="页面曝光" value="page_view" />
                <el-option label="点击交互" value="click" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联需求" prop="requirementId">
              <el-select
                v-model="form.requirementId"
                filterable
                remote
                clearable
                :remote-method="handleRequirementSearch"
                :loading="requirementLoading"
                placeholder="请输入需求标题搜索"
              >
                <el-option
                  v-for="item in requirementOptions"
                  :key="item.requirementId"
                  :label="`${item.title} [${item.statusLabel}]`"
                  :value="item.requirementId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生效页面URL">
              <el-select
                v-model="form.urlPattern"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="选填：请选择或输入URL正则，不填表示全页面生效"
                style="width: 100%"
              >
                <el-option
                  v-for="item in urlPatternOptions"
                  :key="item.value"
                  :label="`${item.label} (${item.value || '留空'})`"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="页面截图">
          <ImageUpload
            v-model="form.pageScreenshotFileId"
            :accept="['jpg', 'jpeg', 'png']"
            :max-size="2 * 1024 * 1024"
          />
        </el-form-item>

        <el-form-item label="关联属性">
          <div class="attribute-config">
            <div class="attribute-toolbar">
              <el-button size="small" type="primary" @click="openAttributeDialog">
                <el-icon><Plus /></el-icon>
                添加属性
              </el-button>
              <span class="hint">已选 {{ paramsList.length }} 个属性</span>
            </div>
            <el-table :data="paramsList" border size="small" style="margin-top: 10px">
              <el-table-column prop="attributeName" label="属性名称" min-width="120" />
              <el-table-column prop="attributeType" label="属性类型" width="110">
                <template #default="{ row }">
                  <el-tag size="small" :type="attributeTypeTagType(row.attributeType)">
                    {{ attributeTypeLabel(row.attributeType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="attributeField" label="属性字段" min-width="120" />
              <el-table-column label="SDK取值路径" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ buildSdkPath(row) }}
                </template>
              </el-table-column>
              <el-table-column label="属性描述" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.description" size="small" placeholder="描述该属性在当前事件中的用途" />
                </template>
              </el-table-column>
              <el-table-column label="来源类型配置" min-width="320">
                <template #default="{ row }">
                  <div v-if="row.attributeType === 'custom'" class="source-cell">
                    <el-select v-model="row.sourceType" size="small" placeholder="来源类型" style="width: 120px">
                      <el-option label="节点内容" value="node_content" :disabled="form.eventType !== 'click'" />
                      <el-option label="接口数据" value="api_data" :disabled="form.eventType !== 'click'" />
                      <el-option label="全局对象" value="global_object" />
                      <el-option label="本地缓存" value="local_cache" />
                      <el-option label="静态值" value="static_value" />
                    </el-select>
                    <template v-if="row.sourceType === 'api_data'">
                      <el-select
                        v-model="row.interfaceId"
                        size="small"
                        clearable
                        filterable
                        placeholder="选择接口"
                        style="width: 180px"
                        @change="updateInterfacePath(row)"
                      >
                        <el-option
                          v-for="item in interfaceOptions"
                          :key="item.id"
                          :label="`${item.name} (${item.path})`"
                          :value="item.id"
                        />
                      </el-select>
                      <el-input v-model="row.sourceValue" size="small" placeholder="如 data.productId" />
                    </template>
                    <el-input
                      v-else-if="row.sourceType !== 'node_content'"
                      v-model="row.sourceValue"
                      size="small"
                      :placeholder="sourceValuePlaceholder(row.sourceType)"
                    />
                    <span v-else class="auto-text">自动获取点击节点文本</span>
                    <el-input v-model="row.defaultValue" size="small" placeholder="默认值(选填)" />
                  </div>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="removeParam($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="attributeDialogVisible" title="选择属性" width="980px" destroy-on-close>
      <div class="attribute-selector-toolbar">
        <el-input
          v-model="attributeKeyword"
          placeholder="按属性名称/属性字段搜索"
          clearable
          style="width: 280px"
        />
      </div>
      <el-table
        ref="attributeTableRef"
        :data="filteredAttributeOptions"
        border
        max-height="460"
        @selection-change="onAttributeSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="attributeSelectable" />
        <el-table-column prop="attributeName" label="属性名称" min-width="130" />
        <el-table-column prop="attributeField" label="属性字段" min-width="120" />
        <el-table-column prop="attributeType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="attributeTypeTagType(row.attributeType)">
              {{ attributeTypeLabel(row.attributeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源类型" width="120">
          <template #default="{ row }">
            <span>{{ sourceTypeLabel(row.sourceType) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="可选状态" width="160">
          <template #default="{ row }">
            <el-tag v-if="selectedAttributeIds.has(row.attributeId)" size="small" type="info">已添加</el-tag>
            <el-tag
              v-else-if="isPageViewBlocked(row)"
              size="small"
              type="warning"
            >
              仅点击交互可用
            </el-tag>
            <el-tag v-else size="small" type="success">可选</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="SDK取值路径" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            {{ buildSdkPath(row) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="attributeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddAttributes">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addEventManage, deleteEventManage, getAllApiInterfaces, getEventManageList, getEventRequirementOptions, updateEventManage } from '../../api/track'
import { getAllAttributes } from '../../api/attribute'
import ImageUpload from '../../components/ImageUpload.vue'

const loading = ref(false)
const submitLoading = ref(false)
const requirementLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  eventType: '',
  keyword: ''
})

const form = reactive({
  id: undefined,
  eventName: '',
  eventCode: '',
  eventType: '',
  status: 1,
  requirementId: '',
  description: '',
  pageScreenshotFileId: '',
  urlPattern: ''
})

const paramsList = ref([])
const interfaceOptions = ref([])
const requirementOptions = ref([])
const urlPatternOptions = [
  { label: '全页面生效', value: '' },
  { label: '登录页', value: '#/login$' },
  { label: '首页', value: '#/$' },
  { label: '理财页', value: '#/wealth$' },
  { label: '生活页', value: '#/life$' },
  { label: '我的页', value: '#/mine$' },
  { label: '首页+理财页', value: '#/(|wealth)$' },
  { label: '底部Tab页(首页/理财/生活/我的)', value: '#/(|wealth|life|mine)$' }
]
const eventCodeOptions = [
  { value: 'tab_home', label: '底部导航-首页', region: '全局/TabBar' },
  { value: 'tab_wealth', label: '底部导航-理财', region: '全局/TabBar' },
  { value: 'tab_life', label: '底部导航-生活', region: '全局/TabBar' },
  { value: 'tab_mine', label: '底部导航-我的', region: '全局/TabBar' },

  { value: 'home_notification', label: '通知按钮', region: '首页/头部' },
  { value: 'home_banner', label: 'Banner', region: '首页/Banner' },
  { value: 'home_action_transfer', label: '快捷操作-转账汇款', region: '首页/快捷操作' },
  { value: 'home_action_wealth', label: '快捷操作-理财产品', region: '首页/快捷操作' },
  { value: 'home_action_creditcard', label: '快捷操作-信用卡', region: '首页/快捷操作' },
  { value: 'home_action_loan', label: '快捷操作-贷款', region: '首页/快捷操作' },
  { value: 'home_account_summary', label: '资产总览卡片', region: '首页/资产总览' },
  { value: 'home_wealth_more', label: '理财推荐-更多', region: '首页/理财推荐' },

  { value: 'wealth_tab_all', label: '分类Tab-全部', region: '理财页/分类Tab' },
  { value: 'wealth_tab_steady', label: '分类Tab-稳健型', region: '理财页/分类Tab' },
  { value: 'wealth_tab_aggressive', label: '分类Tab-进取型', region: '理财页/分类Tab' },
  { value: 'wealth_tab_fund', label: '分类Tab-基金', region: '理财页/分类Tab' },
  { value: 'wealth_tab_insurance', label: '分类Tab-保险', region: '理财页/分类Tab' },

  { value: 'life_service_recharge', label: '服务-手机充值', region: '生活页/服务宫格' },
  { value: 'life_service_utility', label: '服务-生活缴费', region: '生活页/服务宫格' },
  { value: 'life_service_movie', label: '服务-电影票', region: '生活页/服务宫格' },
  { value: 'life_service_takeout', label: '服务-外卖', region: '生活页/服务宫格' },
  { value: 'life_service_bus', label: '服务-公交地铁', region: '生活页/服务宫格' },
  { value: 'life_service_bike', label: '服务-共享单车', region: '生活页/服务宫格' },
  { value: 'life_service_hotel', label: '服务-酒店', region: '生活页/服务宫格' },
  { value: 'life_service_flight', label: '服务-机票', region: '生活页/服务宫格' },
  { value: 'life_service_hospital', label: '服务-医疗挂号', region: '生活页/服务宫格' },
  { value: 'life_service_car', label: '服务-车主服务', region: '生活页/服务宫格' },
  { value: 'life_service_lottery', label: '服务-彩票', region: '生活页/服务宫格' },
  { value: 'life_service_more', label: '服务-更多', region: '生活页/服务宫格' },
  { value: 'life_promo_banner', label: '推广Banner', region: '生活页/推广位' },
  { value: 'life_promo_btn', label: '推广按钮', region: '生活页/推广位' },

  { value: 'login_username_input', label: '用户名输入框', region: '登录页/表单' },
  { value: 'login_password_input', label: '密码输入框', region: '登录页/表单' },
  { value: 'login_submit_btn', label: '登录按钮', region: '登录页/表单' },

  { value: 'mine_balance_card', label: '资产卡片', region: '我的页/资产区' },
  { value: 'mine_menu_account', label: '菜单-我的账户', region: '我的页/菜单' },
  { value: 'mine_menu_transactions', label: '菜单-交易记录', region: '我的页/菜单' },
  { value: 'mine_menu_wealth', label: '菜单-我的理财', region: '我的页/菜单' },
  { value: 'mine_menu_creditcard', label: '菜单-信用卡管理', region: '我的页/菜单' },
  { value: 'mine_menu_security', label: '菜单-安全设置', region: '我的页/菜单' },
  { value: 'mine_menu_messages', label: '菜单-消息中心', region: '我的页/菜单' },
  { value: 'mine_menu_about', label: '菜单-关于我们', region: '我的页/菜单' },
  { value: 'mine_logout_btn', label: '退出登录', region: '我的页/操作区' },

  { value: 'home_product_recommend_${productId}', label: '理财推荐-产品卡片(动态)', region: '首页/理财推荐' },
  { value: 'wealth_product_click_${productId}', label: '产品卡片(动态)', region: '理财页/产品列表' }
]

const rules = reactive({
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  eventCode: [{ required: true, message: '请选择或输入事件编码', trigger: ['blur', 'change'] }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  requirementId: [{ required: true, message: '请选择关联需求', trigger: 'change' }]
})

const attributeDialogVisible = ref(false)
const attributeKeyword = ref('')
const attributeTableRef = ref(null)
const attributeOptions = ref([])
const selectedRows = ref([])

const selectedAttributeIds = computed(() => new Set(paramsList.value.map(item => item.attributeId)))
const filteredAttributeOptions = computed(() => {
  const keyword = attributeKeyword.value?.trim()
  if (!keyword) {
    return attributeOptions.value
  }
  return attributeOptions.value.filter(item =>
    item.attributeName?.includes(keyword) || item.attributeField?.includes(keyword)
  )
})

const safeParseParams = (raw) => {
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }
  return []
}

const normalizeParamRow = (row) => ({
  attributeId: row.attributeId,
  attributeType: row.attributeType,
  attributeName: row.attributeName,
  attributeField: row.attributeField,
  description: row.description || '',
  sourceType: row.attributeType === 'custom' ? (row.sourceType || 'global_object') : null,
  sourceValue: row.attributeType === 'custom' ? (row.sourceValue || '') : null,
  interfaceId: row.attributeType === 'custom' ? (row.interfaceId ?? null) : null,
  interfacePath: row.attributeType === 'custom' ? (row.interfacePath || null) : null,
  defaultValue: row.attributeType === 'custom' ? (row.defaultValue || '') : null
})

const fetchInterfaces = async () => {
  try {
    const res = await getAllApiInterfaces()
    if (res.code === 200) {
      interfaceOptions.value = res.data || []
    }
  } catch (error) {
    console.error('获取接口来源失败', error)
  }
}

const handleRequirementSearch = async (keyword = '') => {
  requirementLoading.value = true
  try {
    const res = await getEventRequirementOptions({ keyword })
    if (res.code === 200) {
      requirementOptions.value = res.data || []
    }
  } finally {
    requirementLoading.value = false
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getEventManageList({
      ...searchForm,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      tableData.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.eventType = ''
  searchForm.keyword = ''
  pageNum.value = 1
  handleSearch()
}

const resetForm = () => {
  form.id = undefined
  form.eventName = ''
  form.eventCode = ''
  form.eventType = ''
  form.status = 1
  form.requirementId = ''
  form.description = ''
  form.pageScreenshotFileId = ''
  form.urlPattern = ''
  paramsList.value = []
}

const handleAdd = async () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
  await handleRequirementSearch('')
}

const handleEdit = async (row) => {
  isEdit.value = true
  form.id = row.id
  form.eventName = row.eventName
  form.eventCode = row.eventCode
  form.eventType = row.eventType
  form.status = row.status
  form.requirementId = row.requirementId
  form.description = row.description || ''
  form.pageScreenshotFileId = row.pageScreenshotFileId || ''
  form.urlPattern = row.urlPattern || ''
  const originalParams = safeParseParams(row.params)
  const parsedParams = originalParams
    .filter(item => item && item.attributeId)
    .map(item => normalizeParamRow(item))
  if (originalParams.length && !parsedParams.length) {
    ElMessage.warning('该事件仍为旧参数结构，请重新选择属性后保存完成迁移')
  }
  paramsList.value = parsedParams
  dialogVisible.value = true
  await handleRequirementSearch('')
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除事件「${row.eventName}」？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await deleteEventManage(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      handleSearch()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  })
}

const onEventTypeChange = () => {
  if (form.eventType !== 'page_view') return
  const invalidCount = paramsList.value.filter(row =>
    row.attributeType === 'custom' && ['node_content', 'api_data'].includes(row.sourceType)
  ).length
  if (invalidCount > 0) {
    ElMessage.warning('当前存在仅点击交互可用的属性来源，请调整后再保存')
  }
}

const removeParam = (index) => {
  paramsList.value.splice(index, 1)
}

const sourceValuePlaceholder = (sourceType) => {
  const map = {
    global_object: '如 userInfo.id',
    local_cache: '如 token',
    static_value: '如 male',
    api_data: '如 data.productName'
  }
  return map[sourceType] || ''
}

const attributeTypeLabel = (type) => {
  const map = {
    user: '用户属性',
    system: '系统属性',
    custom: '个性化属性'
  }
  return map[type] || type
}

const attributeTypeTagType = (type) => {
  const map = {
    user: 'success',
    system: 'info',
    custom: 'warning'
  }
  return map[type] || ''
}

const sourceTypeLabel = (type) => {
  const map = {
    node_content: '节点内容',
    api_data: '接口数据',
    global_object: '全局对象',
    local_cache: '本地缓存',
    static_value: '静态值'
  }
  return map[type] || ''
}

const buildSdkPath = (row) => {
  if (row.attributeType === 'user') {
    return `window.userInfo.${row.attributeField}`
  }
  if (row.attributeType === 'system') {
    return `window.system.${row.attributeField}`
  }
  const sourceType = row.sourceType
  if (sourceType === 'node_content') return '点击节点文本'
  if (sourceType === 'api_data') return `${row.interfacePath || '接口响应'} -> ${row.sourceValue || ''}`
  if (sourceType === 'global_object') return `window.${row.sourceValue || ''}`
  if (sourceType === 'local_cache') return `localStorage['${row.sourceValue || ''}']`
  if (sourceType === 'static_value') return `静态值: ${row.sourceValue || ''}`
  return '-'
}

const isPageViewBlocked = (row) => {
  return form.eventType === 'page_view' &&
    row.attributeType === 'custom' &&
    ['node_content', 'api_data'].includes(row.sourceType)
}

const attributeSelectable = (row) => !selectedAttributeIds.value.has(row.attributeId) && !isPageViewBlocked(row)

const onAttributeSelectionChange = (selection) => {
  selectedRows.value = selection || []
}

const openAttributeDialog = async () => {
  try {
    const res = await getAllAttributes()
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载属性失败')
      return
    }
    attributeOptions.value = (res.data || []).map(item => normalizeParamRow(item))
    attributeKeyword.value = ''
    selectedRows.value = []
    attributeDialogVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('加载属性失败')
  }
}

const confirmAddAttributes = () => {
  if (!selectedRows.value.length) {
    attributeDialogVisible.value = false
    return
  }
  const currentIds = new Set(paramsList.value.map(item => item.attributeId))
  selectedRows.value.forEach(row => {
    if (!currentIds.has(row.attributeId)) {
      paramsList.value.push(normalizeParamRow(row))
    }
  })
  attributeDialogVisible.value = false
}

const updateInterfacePath = (row) => {
  const iface = interfaceOptions.value.find(item => item.id === row.interfaceId)
  row.interfacePath = iface ? iface.path : null
}

const validateParamsBeforeSubmit = () => {
  for (const row of paramsList.value) {
    if (row.attributeType !== 'custom') continue
    if (!row.sourceType) {
      return `属性「${row.attributeName}」缺少来源类型`
    }
    if (form.eventType === 'page_view' && ['node_content', 'api_data'].includes(row.sourceType)) {
      return `属性「${row.attributeName}」当前来源类型仅点击交互可用`
    }
    if (row.sourceType !== 'node_content' && !row.sourceValue) {
      return `属性「${row.attributeName}」缺少变量路径/值`
    }
    if (row.sourceType === 'api_data') {
      if (!row.interfaceId) {
        return `属性「${row.attributeName}」缺少接口选择`
      }
      updateInterfacePath(row)
    } else {
      row.interfaceId = null
      row.interfacePath = null
    }
    if (row.sourceType === 'node_content') {
      row.sourceValue = null
    }
  }
  return null
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    const paramsError = validateParamsBeforeSubmit()
    if (paramsError) {
      ElMessage.warning(paramsError)
      return
    }

    const payload = {
      id: form.id,
      eventName: form.eventName,
      eventCode: form.eventCode,
      eventType: form.eventType,
      status: form.status,
      requirementId: form.requirementId,
      description: form.description,
      pageScreenshotFileId: form.pageScreenshotFileId || null,
      urlPattern: form.urlPattern,
      params: JSON.stringify(paramsList.value.map(row => normalizeParamRow(row)))
    }

    submitLoading.value = true
    try {
      const res = isEdit.value ? await updateEventManage(payload) : await addEventManage(payload)
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        handleSearch()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } finally {
      submitLoading.value = false
    }
  })
}

fetchInterfaces()
handleSearch()
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

.requirement-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.requirement-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attribute-config {
  width: 100%;
}

.attribute-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hint {
  font-size: 12px;
  color: #909399;
}

.source-cell {
  display: grid;
  grid-template-columns: 120px 180px 1fr 1fr;
  gap: 8px;
  align-items: center;
}

.auto-text {
  color: #909399;
  font-size: 12px;
}

.attribute-selector-toolbar {
  margin-bottom: 12px;
}
</style>

