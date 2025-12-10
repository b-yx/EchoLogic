const axios = require('axios');

async function testUrlImport() {
    try {
        // 测试1：传递null作为collectionId
        console.log('测试1：传递null作为collectionId');
        const response1 = await axios.post('http://localhost:8080/api/content/parse-url', {
            url: 'https://example.com',
            collectionId: null
        });
        console.log('响应1:', response1.data);
        console.log('collectionId:', response1.data.collectionId);
        
        // 测试2：不传递collectionId
        console.log('\n测试2：不传递collectionId');
        const response2 = await axios.post('http://localhost:8080/api/content/parse-url', {
            url: 'https://example.com'
        });
        console.log('响应2:', response2.data);
        console.log('collectionId:', response2.data.collectionId);
        
        // 测试3：传递有效的collectionId
        console.log('\n测试3：传递有效的collectionId');
        const response3 = await axios.post('http://localhost:8080/api/content/parse-url', {
            url: 'https://example.com',
            collectionId: 1
        });
        console.log('响应3:', response3.data);
        console.log('collectionId:', response3.data.collectionId);
        
        console.log('\n所有测试完成！');
    } catch (error) {
        console.error('测试失败:', error.response ? error.response.data : error.message);
    }
}

testUrlImport();
