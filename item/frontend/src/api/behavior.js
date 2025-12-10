import http from './http'

const behaviorApi = {
  // 记录用户行为
  recordBehavior: (userId, behaviorType, relatedId) => {
    return http.post('/behavior/record', {
      userId,
      behaviorType,
      relatedId
    })
  },
  
  // 获取用户行为统计数据（用于热点图）
  getBehaviorStats: (userId, timeRange = 'week') => {
    return http.get('/behavior/stat', {
      params: {
        userId,
        timeRange
      }
    })
  }
}

export default behaviorApi