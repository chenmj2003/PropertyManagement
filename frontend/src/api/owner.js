import request from '@/utils/request'

export function getOwnerList() {
  return request({
    url: '/api/admin/owners',  // 改成这个
    method: 'get'
  })
}