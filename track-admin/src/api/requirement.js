import request from '../utils/request'

export function getRequirementList(params) {
  return request({
    url: '/requirement/list',
    method: 'get',
    params
  })
}

export function getRequirementDetail(requirementId) {
  return request({
    url: '/requirement/detail',
    method: 'get',
    params: { requirementId }
  })
}

export function addRequirement(data) {
  return request({
    url: '/requirement/add',
    method: 'post',
    data
  })
}

export function changeRequirementStatus(data) {
  return request({
    url: '/requirement/status-change',
    method: 'post',
    data
  })
}

export function resubmitRequirement(data) {
  return request({
    url: '/requirement/resubmit',
    method: 'post',
    data
  })
}

