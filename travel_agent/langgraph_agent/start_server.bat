@echo off
chcp 65001 >nul
echo ========================================
echo 天气旅行助手 - 启动服务器
echo ========================================
echo.

REM 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Python，请先安装Python 3.9或更高版本
    pause
    exit /b 1
)

REM 检查是否在虚拟环境中
python -c "import sys; sys.exit(0 if hasattr(sys, 'real_prefix') or (hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix) else 1)" >nul 2>&1
if errorlevel 1 (
    echo [提示] 建议在虚拟环境中运行
    echo [提示] 创建虚拟环境: python -m venv venv
    echo [提示] 激活虚拟环境: venv\Scripts\activate
    echo.
)

REM 检查.env文件
if not exist .env (
    echo [警告] 未找到.env文件
    echo [提示] 请创建.env文件并配置DEEPSEEK_API_KEY
    echo [提示] 示例内容:
    echo DEEPSEEK_API_KEY=your_api_key_here
    echo.
    pause
    exit /b 1
)

echo [1/3] 检查依赖包...
pip show fastapi >nul 2>&1
if errorlevel 1 (
    echo [提示] 正在安装依赖包...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
) else (
    echo [✓] 依赖包已安装
)

echo.
echo [2/3] 启动API服务器...
echo [提示] 服务器地址: http://localhost:8000
echo [提示] API文档: http://localhost:8000/docs
echo [提示] 按Ctrl+C停止服务器
echo.

REM 启动服务器
python api_server.py

pause

