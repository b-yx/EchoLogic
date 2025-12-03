import http from './http'

export default {
  // 获取所有集合
  getAllCollections() {
    return http.get('/collections')
  },

  // 根据ID获取集合
  getCollectionById(id) {
    return http.get(`/collections/${id}`)
  },

  // 创建集合
  createCollection(collection) {
    return http.post('/collections', collection)
  },

  // 更新集合
  updateCollection(id, collection) {
    return http.put(`/collections/${id}`, collection)
  },

  // 删除集合
  deleteCollection(id) {
    return http.delete(`/collections/${id}`)
  },

  // AI生成集合
  generateCollectionByAI(request) {
    return http.post('/collections/ai-generate', request)
  },

  // 合并集合
  mergeCollections(mergeRequest) {
    return http.post('/collections/merge', mergeRequest)
  },

  // 将记录添加到集合
  addRecordToCollection(collectionId, recordId) {
    return http.post(`/collections/${collectionId}/records/${recordId}`)
  },

  // 将记录从集合中移除
  removeRecordFromCollection(collectionId, recordId) {
    return http.delete(`/collections/${collectionId}/records/${recordId}`)
  }
}