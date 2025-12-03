import http from './http'

/**
 * AI对话接口
 * @param {Array} conversationHistory - 对话历史
 * @returns {Promise} - AI回复结果
 */
export const chatWithAI = (conversationHistory) => {
  return http.post('/ai/chat', conversationHistory)
}
