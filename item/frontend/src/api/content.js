import http from './http'

// 内容相关API接口
export default {
  // 解析网页并保存为收藏
  parseUrl(requestData) {
    // requestData包含url和可选的collectionId
    return http.post('/content/parse-url', requestData)
  },
  // 生成内容
  generateContent(request) {
    return http.post('/content/generate', request)
  },
  // 优化内容
  refineContent(request) {
    return http.post('/content/refine', request)
  }
}