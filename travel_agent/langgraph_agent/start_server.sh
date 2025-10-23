#!/bin/bash
# 天气旅行助手 - 启动服务器 (Linux/Mac)

echo "========================================"
echo "天气旅行助手 - 启动服务器"
echo "========================================"
echo ""

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo "[错误] 未找到Python3，请先安装Python 3.9或更高版本"
    exit 1
fi

# 检查是否在虚拟环境中
if [[ -z "$VIRTUAL_ENV" ]]; then
    echo "[提示] 建议在虚拟环境中运行"
    echo "[提示] 创建虚拟环境: python3 -m venv venv"
    echo "[提示] 激活虚拟环境: source venv/bin/activate"
    echo ""
fi

# 检查.env文件
if [ ! -f .env ]; then
    echo "[警告] 未找到.env文件"
    echo "[提示] 请创建.env文件并配置DEEPSEEK_API_KEY"
    echo "[提示] 示例内容:"
    echo "DEEPSEEK_API_KEY=your_api_key_here"
    echo ""
    exit 1
fi

echo "[1/3] 检查依赖包..."
if ! python3 -c "import fastapi" &> /dev/null; then
    echo "[提示] 正在安装依赖包..."
    pip3 install -r requirements.txt
    if [ $? -ne 0 ]; then
        echo "[错误] 依赖安装失败"
        exit 1
    fi
else
    echo "[✓] 依赖包已安装"
fi

echo ""
echo "[2/3] 启动API服务器..."
echo "[提示] 服务器地址: http://localhost:8000"
echo "[提示] API文档: http://localhost:8000/docs"
echo "[提示] 按Ctrl+C停止服务器"
echo ""

# 启动服务器
python3 api_server.py

