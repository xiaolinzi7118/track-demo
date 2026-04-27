import request from '../utils/request'

export function getAttributeList(params) {
  return request({
    url: '/attribute/list',
    method: 'get',
    params
  })
}

export function getAttributeDetail(id) {
  return request({
    url: '/attribute/detail',
    method: 'get',
    params: { id }
  })
}

export function getAllAttributes() {
  return request({
    url: '/attribute/all',
    method: 'get'
  })
}

export function addAttribute(data) {
  return request({
    url: '/attribute/add',
    method: 'post',
    data
  })
}

export function updateAttribute(data) {
  return request({
    url: '/attribute/update',
    method: 'post',
    data
  })
}

export function deleteAttribute(id) {
  return request({
    url: '/attribute/delete',
    method: 'post',
    data: { id }
  })
}

export function importAttribute(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/attribute/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
