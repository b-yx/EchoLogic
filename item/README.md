# EchoLogic

EchoLogic是一个AI赋能的内容收集与整理工具，帮助用户高效管理和生成内容，并通过成长感知与激励系统鼓励用户持续使用。

## 技术栈

### 前端
- Vue.js 3
- Element Plus
- Vite

### 后端
- Spring Boot 2.7.x
- MyBatis
- MySQL 8.0

## 项目结构

```
EchoLogic
├── frontend/          # 前端代码
│   ├── src/          # 源代码
│   ├── public/       # 静态资源
│   ├── package.json  # 依赖配置
│   └── vite.config.js # Vite配置
├── Backend/          # 后端代码
│   ├── src/          # 源代码
│   ├── pom.xml       # Maven依赖
│   └── application.properties # 应用配置
└── README.md         # 项目说明
```

## 启动步骤

### 1. 数据库配置

1. 确保MySQL服务已启动（默认端口3306）
2. 创建名为`web01`的数据库
3. 数据库连接信息（可在`Backend/src/main/resources/application.properties`中修改）：
   - 用户名：`root`
   - 密码：`123456`
   - 数据库名：`web01`

### 2. 后端启动

```bash
# 进入后端目录
cd Backend

# 安装依赖
mvn install

# 启动应用
mvn spring-boot:run
```

后端服务将在`http://localhost:8080`启动

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在`http://localhost:5173`启动

## 核心功能

### 1. 内容管理
- 内容收集与整理
- 标签分类系统
- 集合管理

### 2. 成长感知与激励
- 用户活动热力图（记录最近12个月的使用情况）
- 每日任务系统
- 成就解锁机制
- AI使用次数奖励

### 3. 内容生成（孵化）
- 基于收集内容的AI生成功能
- 智能内容推荐

## 注意事项

1. **API密钥配置**：
   - DeepSeek AI API密钥已在`application.properties`中配置
   - 如果需要更换密钥，请修改`ai.deepseek.api-key`字段

2. **数据库初始化**：
   - 项目启动时会自动执行`init.sql`脚本创建表结构
   - 初始数据包含任务模板和示例成就

3. **端口占用**：
   - 确保8080（后端）和5173（前端）端口未被占用
   - 如需修改端口，请在相应配置文件中调整

4. **浏览器支持**：
   - 建议使用Chrome、Firefox、Safari等现代浏览器
   - 不推荐使用IE浏览器

## 开发说明

### 前端开发
- 主要组件位于`frontend/src/components/`
- 页面视图位于`frontend/src/views/`
- API请求封装在`frontend/src/api/`

### 后端开发
- Controller层位于`Backend/src/main/java/org/example/dao/controller/`
- Service层位于`Backend/src/main/java/org/example/dao/service/`
- Mapper层位于`Backend/src/main/java/org/example/dao/mapper/`
- MyBatis映射文件位于`Backend/src/main/resources/mapper/`

## 功能预览

### 活动热力图
- 显示用户最近12个月的使用活动情况
- 支持按月份查看不同时期的活动
- 颜色深浅表示活动频率

### 内容生成界面
- 简洁的孵化界面，支持快速生成内容
- 与收集的内容关联，实现智能推荐

## 许可证

MIT License