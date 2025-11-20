# EchoLogic Travel 前端（Vue + CSS 动画）

本目录包含为旅行助手优化的零构建前端：使用 Vue 3（CDN）与纯 CSS 动画，具备主题切换、滚动渐显、按钮涟漪等交互。

## 结构
- `index.html`：入口页面，引用 Vue CDN 与样式、脚本
- `styles.css`：主题变量、玻璃态导航、渐变背景、过渡与滚动 reveal
- `app.js`：Vue 应用逻辑，首页/目的地/关于三页与指令

## 运行
1. 切换到本目录：
   - Windows：`cd /d "d:\学校\大三上软工\travel\EchoLogic\travel_agent\frontend"`
2. 启动任意静态服务器：
   - Python：`python -m http.server 5173`
   - Node（可选）：`npx serve -p 5173`
3. 浏览器访问：`http://localhost:5173/`

## 功能概览
- 主题切换：跟随系统偏好，支持浅/深色快速切换
- 动画效果：渐变背景、页面过渡（fade）、滚动渐显（IntersectionObserver）
- 交互细节：按钮涟漪反馈（CSS + 指令），卡片悬浮阴影

## 对接后端（可选）
后续可在 `app.js` 中添加调用 `langgraph_agent/api_server.py` 提供的接口，实现目的地搜索、行程规划等功能。

## 备注
如果希望使用现代工程化（Vite + Vue 3），可后续迁移至 Vite 项目并按需拆分组件与路由。