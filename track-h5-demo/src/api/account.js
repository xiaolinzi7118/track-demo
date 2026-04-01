import request from '../utils/request'

export function getAccountSummary(userId) {
  return request.get('/account/summary', { params: { userId } })
}
