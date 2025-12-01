# EchoLogic 项目启动指南

## 项目介绍

EchoLogic 是一个个人知识管理系统，基于 Electron + Vue3 + Spring Boot 构建。本系统支持集合、记录和标签的管理功能。

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

## 开发建议

1. 前端开发时，建议先启动后端服务，然后再启动前端开发服务器
2. 修改代码时，前端开发服务器支持热更新，后端需要重新启动或使用开发工具的热部署功能
3. 使用 Swagger 文档查看和测试后端 API

## 端口占用情况

- 前端开发服务器：5173
- 后端 API 服务：8080
- MySQL 数据库：3306

请确保这些端口没有被其他应用占用。