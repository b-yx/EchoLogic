// 使用前端已安装的axios来测试API
import axios from 'axios';

// 创建axios实例，与前端配置一致
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

async function debugTest() {
  try {
    console.log('=== 调试测试开始 ===');
    
    // 测试1：传递null作为collectionId
    console.log('\n测试1：传递null作为collectionId');
    const response1 = await api.post('/content/parse-url', {
      url: 'https://example.com',
      collectionId: null
    });
    console.log('响应1状态码:', response1.status);
    console.log('响应1数据:', response1.data);
    console.log('response1.data.collectionId类型:', typeof response1.data.collectionId);
    console.log('response1.data.collectionId值:', response1.data.collectionId);
    
    // 测试2：传递undefined作为collectionId
    console.log('\n测试2：传递undefined作为collectionId');
    const response2 = await api.post('/content/parse-url', {
      url: 'https://example.com',
      collectionId: undefined
    });
    console.log('响应2状态码:', response2.status);
    console.log('响应2数据:', response2.data);
    console.log('response2.data.collectionId类型:', typeof response2.data.collectionId);
    console.log('response2.data.collectionId值:', response2.data.collectionId);
    
    // 测试3：不传递collectionId字段
    console.log('\n测试3：不传递collectionId字段');
    const response3 = await api.post('/content/parse-url', {
      url: 'https://example.com'
    });
    console.log('响应3状态码:', response3.status);
    console.log('响应3数据:', response3.data);
    console.log('response3.data.collectionId类型:', typeof response3.data.collectionId);
    console.log('response3.data.collectionId值:', response3.data.collectionId);
    
    // 测试4：传递不存在的collectionId
    console.log('\n测试4：传递不存在的collectionId');
    try {
      const response4 = await api.post('/content/parse-url', {
        url: 'https://example.com',
        collectionId: 999999
      });
      console.log('响应4状态码:', response4.status);
      console.log('响应4数据:', response4.data);
    } catch (error) {
      console.log('响应4错误:', error.response ? error.response.data : error.message);
    }
    
    console.log('\n=== 调试测试结束 ===');
    
  } catch (error) {
    console.error('测试失败:', error.response ? error.response.data : error.message);
  }
}

debugTest();
