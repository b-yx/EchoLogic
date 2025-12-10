const axios = require('axios');

async function testParseUrl() {
  try {
    // 测试不提供collectionId的情况
    const response = await axios.post('http://localhost:8080/api/content/parse-url', {
      url: 'https://example.com',
      collectionId: null
    });
    
    console.log('API响应:', response.data);
    console.log('collectionId:', response.data.collectionId);
    console.log('\n修复成功！当collectionId为null时，不再自动创建集合。');
    
  } catch (error) {
    console.error('测试失败:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
  }
}

testParseUrl();