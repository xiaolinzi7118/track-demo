import request from '../utils/request'

// ===== 事件管理 =====
export function getEventManageList(params) {
  return request({
    url: '/event-manage/list',
    method: 'get',
    params
  })
}

export function getAllEventManage() {
  return request({
    url: '/event-manage/all',
    method: 'get'
  })
}

export function getEventManageDetail(id) {
  return request({
    url: '/event-manage/detail',
    method: 'get',
    params: { id }
  })
}

export function getEventRequirementOptions(params) {
  return request({
    url: '/event-manage/requirement-options',
    method: 'get',
    params
  })
}

export function addEventManage(data) {
  return request({
    url: '/event-manage/add',
    method: 'post',
    data
  })
}

export function updateEventManage(data) {
  return request({
    url: '/event-manage/update',
    method: 'post',
    data
  })
}

export function deleteEventManage(id) {
  return request({
    url: '/event-manage/delete',
    method: 'post',
    data: { id }
  })
}

export function getEventManageStatistics() {
  return request({
    url: '/event-manage/statistics',
    method: 'get'
  })
}

// ===== 数据回检 =====
export function getTrackDataList(params) {
  return request({
    url: '/track-data/list',
    method: 'get',
    params
  })
}

export function getTrackDataStatistics() {
  return request({
    url: '/track-data/statistics',
    method: 'get'
  })
}

export function getTrackDataTrend() {
  return request({
    url: '/track-data/trend',
    method: 'get'
  })
}

// ===== 接口来源管理 =====
export function getApiInterfaceList(params) {
  return request({
    url: '/api-interface/list',
    method: 'get',
    params
  })
}

export function getAllApiInterfaces() {
  return request({
    url: '/api-interface/all',
    method: 'get'
  })
}

export function addApiInterface(data) {
  return request({
    url: '/api-interface/add',
    method: 'post',
    data
  })
}

export function updateApiInterface(data) {
  return request({
    url: '/api-interface/update',
    method: 'post',
    data
  })
}

export function deleteApiInterface(id) {
  return request({
    url: '/api-interface/delete',
    method: 'post',
    data: { id }
  })
}

// ===== 向后兼容旧命名 =====
export const getTrackConfigList = getEventManageList
export const getAllTrackConfig = getAllEventManage
export const getTrackConfigDetail = getEventManageDetail
export const addTrackConfig = addEventManage
export const updateTrackConfig = updateEventManage
export const deleteTrackConfig = deleteEventManage
export const getTrackConfigStatistics = getEventManageStatistics
