import request from '../utils/request'

export function getDictParamList(params) {
  return request({
    url: '/dict-param/list',
    method: 'get',
    params
  })
}

export function getDictParamDetail(id) {
  return request({
    url: '/dict-param/detail',
    method: 'get',
    params: { id }
  })
}

export function addDictParam(data) {
  return request({
    url: '/dict-param/add',
    method: 'post',
    data
  })
}

export function updateDictParam(data) {
  return request({
    url: '/dict-param/update',
    method: 'post',
    data
  })
}

export function deleteDictParam(id) {
  return request({
    url: '/dict-param/delete',
    method: 'post',
    data: { id }
  })
}

export function getDictParamIdsList(paramIds) {
  return request({
    url: '/dict-param/ids-list',
    method: 'post',
    data: { paramIds }
  })
}
