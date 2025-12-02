import http from './http'

export default {
  // 获取所有记录
  getAllRecords() {
    return http.get('/records')
  },

  // 根据ID获取记录
  getRecordById(id) {
    return http.get(`/records/${id}`)
  },

  getRecordsByCollectionId(collectionId) {
    return http.get(`/records/collection/${collectionId}`)
  },

  // 创建记录
  createRecord(data) {
    return http.post('/records', data)
  },

  // 更新记录
  updateRecord(id, data) {
    return http.put(`/records/${id}`, data)
  },

  // 删除记录
  deleteRecord(id) {
    return http.delete(`/records/${id}`)
  },

  // 搜索记录
  searchRecords(keyword) {
    return http.get('/records/search', { params: { keyword } })
  },

  // 根据类型查询记录
  getRecordsByType(contentType) {
    return http.get(`/records/type/${contentType}`)
  },

  // 保存草稿
  saveDraft(id, content) {
    return http.put(`/records/${id}/draft`, content)
  },

  // 恢复记录
  restoreRecord(id) {
    return http.put(`/records/${id}/restore`)
  },

  // 生成标签推荐
  generateTagRecommendations(id) {
    return http.get(`/records/${id}/tag-recommendations`)
  },
}
