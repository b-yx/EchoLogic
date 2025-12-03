# EchoLogic 项目启动指南

## 项目介绍

EchoLogic 是一个个人知识管理系统，基于 Electron + Vue3 + Spring Boot 构建。本系统支持集合、记录和标签的管理功能，并集成了 AI 对话功能，能够理解用户的自然语言指令并执行相应操作，如记录查询、筛选等（AI实际调用仍存在bug）。

## 项目结构

```
item/
├── backend/          # Spring Boot 后端项目
└── frontend/         # Vue3 + Electron 前端项目
```

## 环境要求

### 后端环境
- JDK 17 或更高版本
- Maven 3.6+ 或兼容的 IDE
- MySQL 数据库

### 前端环境
- Node.js 16.x 或更高版本
- npm 或 yarn 包管理工具


## 后端服务启动

### 1. 准备数据库

确保 MySQL 数据库已安装并运行，创建数据库 `web01`：

```sql
CREATE DATABASE web01 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**执行数据库初始化脚本**：

使用MySQL客户端工具（如MySQL Workbench、Navicat或命令行）连接到数据库，执行以下操作：

1. 选择刚刚创建的 `web01` 数据库：
   ```sql
   USE web01;
   ```

2. 执行项目中的初始化SQL脚本（位于 `backend/src/main/resources/init.sql`）：
   - 可以通过文件导入功能执行
   - 或直接复制脚本内容在MySQL客户端中执行

该初始化脚本将创建以下表：
- `collection` - 集合表
- `record` - 记录表
- `tag` - 标签表
- `record_tag` - 记录和标签的多对多关联表

数据库配置信息（已在 application.properties 中设置）：
- 用户名：`root`
- 密码：`123456`
- 数据库名：`web01`
- 端口：默认 3306

如需修改数据库配置，请编辑 `backend/src/main/resources/application.properties` 文件。

### 2. 启动后端服务

#### 方法一：使用 IDE 启动（推荐开发时使用）

在 IDE 中打开 backend 目录，运行主类 `org.example.dao.DaoApplication`。

#### 方法二：使用 Maven 命令启动

打开命令行工具，进入 backend 目录：

```bash
cd .......(自己的路径)\item\backend
mvn spring-boot:run
```

后端服务启动后，默认监听 8080 端口。

### 3. 验证后端服务

后端服务启动成功后，可以访问以下地址验证：
- Swagger 接口文档：http://localhost:8080/swagger-ui.html
- 健康检查接口：http://localhost:8080/api/collections

## 前端服务启动

### 1. 安装依赖

首次启动前，需要安装前端依赖：

```bash
cd .......(自己的路径)\item\frontend
npm install
```

### 2. 启动开发服务器

#### 方法一：使用启动脚本

运行项目根目录下的前端启动脚本：

```bash
# Windows 系统
.......(自己的路径)\item\frontend\start-frontend.cmd
```

#### 方法二：手动启动

打开命令行工具，进入 frontend 目录：

```bash
cd .......(自己的路径)\\item\frontend
npm run dev
```

前端开发服务器启动后，默认监听 5173 端口，可通过 http://localhost:5173 访问。





## 常见问题

### 1. 后端服务无法启动
- 检查 MySQL 服务是否运行
- 确认数据库配置正确
- 查看 IDE 或命令行中的错误日志

### 2. 前端无法连接后端
- 确认后端服务正在运行
- 检查 CORS 配置（已在 WebConfig 中配置为允许 localhost:5173）
- 检查浏览器控制台的网络请求和错误信息

### 3. 依赖安装失败
- 尝试使用 `npm cache clean --force` 清理缓存后重新安装
- 确保网络环境正常，可考虑使用 npm 镜像源

## 技术栈说明

- **前端**：Vue 3, Element Plus, Vue Router, Pinia, Electron
- **后端**：Spring Boot 3.x, MyBatis, MySQL, Swagger/OpenAPI
- **AI 功能**：集成 AI 对话接口，支持自然语言指令解析和执行

## 开发建议

1. 前端开发时，建议先启动后端服务，然后再启动前端开发服务器
2. 修改代码时，前端开发服务器支持热更新，后端需要重新启动或使用开发工具的热部署功能
3. 使用 Swagger 文档查看和测试后端 API
4. AI 对话功能支持自然语言指令，如"查看目前都有哪些记录"、"筛选出所有有关数字的记录"等

## AI 功能说明

### 对话功能

系统集成了 AI 对话面板，用户可以通过自然语言与系统交互，系统会解析用户指令并执行相应操作。

### 支持的指令

1. **列表指令**：
   - 示例："查看目前都有哪些记录"
   - 功能：导航到记录列表页面

2. **筛选指令**：
   - 示例："筛选出所有有关数字的记录"
   - 功能：筛选并显示符合条件的记录

### 指令格式

系统支持两种指令格式：
1. 自然语言指令：如"查看目前都有哪些记录"
2. JSON 格式指令：如`{"type":"command","command":"filter","params":{"keyword":"数字"}}`

系统会自动识别并执行这两种格式的指令。

### API-Key 配置

系统使用 DeepSeek AI 服务，需要配置 API-Key 才能正常使用 AI 功能。

#### 配置方式

1. **修改后端配置文件**：
   - 打开文件：`backend/src/main/resources/application.properties`
   - 找到以下配置项并修改为您自己的 API-Key：
   ```properties
   # DeepSeek AI API 配置
   ai.deepseek.api-url=https://api.deepseek.com/v1/chat/completions
   ai.deepseek.api-key=sk-your-api-key-here
   ```

2. **API-Key 获取方式**：
   - 访问 [DeepSeek 官方网站](https://deepseek.com/)
   - 注册并登录账号
   - 进入控制台获取 API-Key

#### 注意事项

- API-Key 是您的个人凭证，请妥善保管，不要泄露给他人
- 免费版 API-Key 有使用次数限制，请根据实际需求选择合适的套餐
- 更换 API-Key 后需要重启后端服务才能生效

## 端口占用情况

- 前端开发服务器：5173
- 后端 API 服务：8080
- MySQL 数据库：3306
- AI 服务接口：集成在后端服务中，无需额外端口

请确保这些端口没有被其他应用占用。