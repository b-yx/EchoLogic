# 🚀 部署指南

## 项目架构

```
┌─────────────────┐         ┌──────────────────┐
│                 │         │                  │
│  travel_agent   │ HTTP    │  FastAPI Server  │
│  .html          ├────────►│  (port 8000)     │
│  (前端界面)      │         │                  │
│                 │         │  ┌─────────────┐ │
└─────────────────┘         │  │  LangGraph  │ │
                            │  │   Agent     │ │
                            │  └─────┬───────┘ │
                            │        │         │
                            │  ┌─────▼───────┐ │
                            │  │   Tools:    │ │
                            │  │ - Weather   │ │
                            │  │ - Scenic    │ │
                            │  └─────────────┘ │
                            └──────────────────┘
                                     │
                        ┌────────────┼────────────┐
                        │            │            │
                  ┌─────▼─────┐ ┌───▼────┐ ┌────▼─────┐
                  │ DeepSeek  │ │ wttr.in│ │高德地图  │
                  │    API    │ │  API   │ │   API    │
                  └───────────┘ └────────┘ └──────────┘
```

## 本地开发部署

### 方式一：标准部署（推荐）

1. **安装依赖**
```bash
cd langgraph_agent
pip install -r requirements.txt
```

2. **配置环境变量**
```bash
# 创建.env文件
echo "DEEPSEEK_API_KEY=your_key_here" > .env
```

3. **启动服务**
```bash
# Windows
start_server.bat

# Linux/Mac
./start_server.sh
```

4. **访问前端**
- 打开浏览器访问 `travel_agent.html`
- 或使用 Live Server 插件

### 方式二：开发模式

适合需要频繁修改代码的场景：

```bash
# 安装开发依赖
pip install -e ".[dev]"

# 启动热重载服务器
uvicorn api_server:app --reload --host 0.0.0.0 --port 8000
```

## 生产环境部署

### Docker部署（推荐）

1. **创建Dockerfile**

```dockerfile
# langgraph_agent/Dockerfile
FROM python:3.11-slim

WORKDIR /app

# 安装依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 复制项目文件
COPY . .

# 暴露端口
EXPOSE 8000

# 启动命令
CMD ["uvicorn", "api_server:app", "--host", "0.0.0.0", "--port", "8000"]
```

2. **构建和运行**

```bash
# 构建镜像
docker build -t travel-agent-api .

# 运行容器
docker run -d \
  --name travel-agent \
  -p 8000:8000 \
  -e DEEPSEEK_API_KEY=your_key \
  travel-agent-api
```

3. **使用docker-compose**

```yaml
# docker-compose.yml
version: '3.8'

services:
  api:
    build: ./langgraph_agent
    ports:
      - "8000:8000"
    environment:
      - DEEPSEEK_API_KEY=${DEEPSEEK_API_KEY}
    restart: unless-stopped
    
  frontend:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./travel_agent.html:/usr/share/nginx/html/index.html
    depends_on:
      - api
```

启动：
```bash
docker-compose up -d
```

### 云服务部署

#### 部署到AWS

**使用EC2:**

```bash
# 1. 连接到EC2实例
ssh -i your-key.pem ec2-user@your-instance-ip

# 2. 安装Python和依赖
sudo yum install python3 python3-pip git -y

# 3. 克隆项目
git clone your-repo-url
cd travel_agent/langgraph_agent

# 4. 安装依赖
pip3 install -r requirements.txt

# 5. 配置环境变量
echo "DEEPSEEK_API_KEY=your_key" > .env

# 6. 使用systemd管理服务
sudo nano /etc/systemd/system/travel-agent.service
```

systemd配置：
```ini
[Unit]
Description=Travel Agent API
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/travel_agent/langgraph_agent
Environment="PATH=/usr/local/bin"
ExecStart=/usr/bin/python3 api_server.py
Restart=always

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable travel-agent
sudo systemctl start travel-agent
```

#### 部署到Heroku

```bash
# 1. 创建Procfile
echo "web: uvicorn api_server:app --host 0.0.0.0 --port $PORT" > Procfile

# 2. 创建runtime.txt
echo "python-3.11.0" > runtime.txt

# 3. 提交到Heroku
heroku create your-app-name
heroku config:set DEEPSEEK_API_KEY=your_key
git push heroku main
```

#### 部署到Railway

1. 连接GitHub仓库
2. 设置环境变量 `DEEPSEEK_API_KEY`
3. Railway会自动检测并部署

#### 部署到Vercel（仅前端）

```bash
# 安装Vercel CLI
npm i -g vercel

# 部署前端
cd travel_agent
vercel

# 配置API代理
# vercel.json
{
  "rewrites": [
    { "source": "/api/:path*", "destination": "https://your-backend-url/:path*" }
  ]
}
```

### Nginx反向代理

```nginx
# /etc/nginx/sites-available/travel-agent
server {
    listen 80;
    server_name your-domain.com;

    # 前端
    location / {
        root /var/www/travel-agent;
        index travel_agent.html;
        try_files $uri $uri/ =404;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8000/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        
        # CORS
        add_header Access-Control-Allow-Origin *;
    }
}
```

启用配置：
```bash
sudo ln -s /etc/nginx/sites-available/travel-agent /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 性能优化

### 1. 使用Gunicorn + Uvicorn Workers

```bash
pip install gunicorn

# 启动命令
gunicorn api_server:app \
  --workers 4 \
  --worker-class uvicorn.workers.UvicornWorker \
  --bind 0.0.0.0:8000 \
  --timeout 60
```

### 2. 添加Redis缓存

```python
# api_server.py
import redis
from functools import lru_cache

redis_client = redis.Redis(host='localhost', port=6379, db=0)

def get_cached_response(key):
    cached = redis_client.get(key)
    if cached:
        return json.loads(cached)
    return None

def set_cached_response(key, value, ttl=3600):
    redis_client.setex(key, ttl, json.dumps(value))
```

### 3. 启用HTTP/2和Gzip

Nginx配置：
```nginx
http2 on;
gzip on;
gzip_types text/plain application/json application/javascript;
gzip_min_length 1000;
```

## 监控和日志

### 1. 添加日志记录

```python
# api_server.py
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('app.log'),
        logging.StreamHandler()
    ]
)
```

### 2. 健康检查

```python
@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "conversations": len(conversations)
    }
```

### 3. 性能监控（使用Prometheus）

```bash
pip install prometheus-fastapi-instrumentator
```

```python
from prometheus_fastapi_instrumentator import Instrumentator

Instrumentator().instrument(app).expose(app)
```

## 安全配置

### 1. 启用HTTPS

```bash
# 使用Let's Encrypt
sudo certbot --nginx -d your-domain.com
```

### 2. 限流

```python
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter

@app.post("/chat")
@limiter.limit("10/minute")
async def chat(request: Request, chat_request: ChatRequest):
    # ...
```

### 3. API密钥验证

```python
from fastapi import Security, HTTPException
from fastapi.security import APIKeyHeader

API_KEY_HEADER = APIKeyHeader(name="X-API-Key")

async def verify_api_key(api_key: str = Security(API_KEY_HEADER)):
    if api_key != os.getenv("API_KEY"):
        raise HTTPException(status_code=403, detail="Invalid API Key")
```

## 故障排查

### 常见问题

1. **端口被占用**
```bash
# Windows
netstat -ano | findstr :8000
taskkill /PID <PID> /F

# Linux
lsof -ti:8000 | xargs kill -9
```

2. **内存不足**
- 减少worker数量
- 启用swap
- 升级服务器配置

3. **API超时**
- 增加timeout设置
- 优化工具函数
- 添加缓存

## 备份和恢复

### 备份对话数据

```python
# 定期备份
import pickle

def backup_conversations():
    with open('conversations_backup.pkl', 'wb') as f:
        pickle.dump(conversations, f)
```

### 数据库迁移（未来）

项目计划支持持久化存储：
- SQLite（单机）
- PostgreSQL（生产环境）
- MongoDB（NoSQL方案）

---

需要帮助？查看 [README.md](../README.md) 或提交 Issue

