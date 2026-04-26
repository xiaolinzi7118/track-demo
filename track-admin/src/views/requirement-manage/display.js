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

const ACTION_TYPE_LABEL_MAP = {
  CREATE: '提交创建',
  STATUS_CHANGE: '状态变更',
  EDIT_RESUBMIT: '编辑重提'
}

export const requirementStatusOptions = Object.keys(STATUS_LABEL_MAP).map(key => ({
  value: key,
  label: STATUS_LABEL_MAP[key]
}))

export const statusText = (status) => STATUS_LABEL_MAP[status] || status || '-'

export const statusTagType = (status) => STATUS_TAG_TYPE_MAP[status] || 'info'

export const priorityText = (priority) => PRIORITY_LABEL_MAP[priority] || priority || '-'

export const priorityTagType = (priority) => PRIORITY_TAG_TYPE_MAP[priority] || 'info'

export const actionTypeText = (actionType) => ACTION_TYPE_LABEL_MAP[actionType] || actionType || '-'

export const formatDateTime = (time) => {
  if (!time) return '-'
  const text = String(time).replace('T', ' ')
  return text.length > 19 ? text.slice(0, 19) : text
}
