import request from '../utils/request'

export function getTrackConfigList(params) {
  return request({
    url: '/track-config/list',
    method: 'get',
    params
  })
}

export function getAllTrackConfig() {
  return request({
    url: '/track-config/all',
    method: 'get'
  })
}

export function getTrackConfigDetail(id) {
  return request({
    url: '/track-config/detail',
    method: 'get',
    params: { id }
  })
}

export function addTrackConfig(data) {
  return request({
    url: '/track-config/add',
    method: 'post',
    data
  })
}

export function updateTrackConfig(data) {
  return request({
    url: '/track-config/update',
    method: 'post',
    data
  })
}

export function deleteTrackConfig(id) {
  return request({
    url: '/track-config/delete',
    method: 'post',
    data: { id }
  })
}

export function getTrackConfigStatistics() {
  return request({
    url: '/track-config/statistics',
    method: 'get'
  })
}

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

export function clearTrackConfig() {
  return request({
    url: '/track-config/clear',
    method: 'post'
  })
}

export function clearTrackData() {
  return request({
    url: '/track-data/clear',
    method: 'post'
  })
}
