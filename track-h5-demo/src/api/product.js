import request from '../utils/request'

export function getProductList(params) {
  return request.get('/financial-product/list', { params })
}

export function getProductDetail(id) {
  return request.get('/financial-product/detail', { params: { id } })
}
