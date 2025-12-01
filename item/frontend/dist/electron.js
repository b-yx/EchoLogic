const { app, BrowserWindow, Menu, ipcMain, dialog } = require('electron')
const path = require('path')
const { spawn } = require('child_process')

let mainWindow
let backendProcess

// 创建主窗口
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 800,
    minHeight: 600,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    },
    icon: path.join(__dirname, '../src/assets/logo.png')
  })

  // 开发模式下加载 Vite 服务器
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
    mainWindow.webContents.openDevTools()
  } else {
    // 生产模式下加载构建后的文件
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })

  // 创建菜单
  createMenu()
}

// 创建应用菜单
function createMenu() {
  const template = [
    {
      label: '文件',
      submenu: [
        {
          label: '新建记录',
          accelerator: 'CmdOrCtrl+N',
          click: () => {
            mainWindow.webContents.send('menu-new-record')
          }
        },
        { type: 'separator' },
        {
          label: '退出',
          accelerator: process.platform === 'darwin' ? 'Cmd+Q' : 'Ctrl+Q',
          click: () => {
            app.quit()
          }
        }
      ]
    },
    {
      label: '编辑',
      submenu: [
        { label: '撤销', accelerator: 'CmdOrCtrl+Z', role: 'undo' },
        { label: '重做', accelerator: 'Shift+CmdOrCtrl+Z', role: 'redo' },
        { type: 'separator' },
        { label: '剪切', accelerator: 'CmdOrCtrl+X', role: 'cut' },
        { label: '复制', accelerator: 'CmdOrCtrl+C', role: 'copy' },
        { label: '粘贴', accelerator: 'CmdOrCtrl+V', role: 'paste' }
      ]
    },
    {
      label: '查看',
      submenu: [
        { label: '重新加载', accelerator: 'CmdOrCtrl+R', role: 'reload' },
        { label: '强制重新加载', accelerator: 'CmdOrCtrl+Shift+R', role: 'forceReload' },
        { label: '开发者工具', accelerator: 'F12', role: 'toggleDevTools' },
        { type: 'separator' },
        { label: '实际大小', accelerator: 'CmdOrCtrl+0', role: 'resetZoom' },
        { label: '放大', accelerator: 'CmdOrCtrl+Plus', role: 'zoomIn' },
        { label: '缩小', accelerator: 'CmdOrCtrl+-', role: 'zoomOut' },
        { type: 'separator' },
        { label: '全屏', accelerator: 'F11', role: 'togglefullscreen' }
      ]
    },
    {
      label: '帮助',
      submenu: [
        {
          label: '关于 EchoLogic',
          click: () => {
            dialog.showMessageBox(mainWindow, {
              type: 'info',
              title: '关于 EchoLogic',
              message: 'EchoLogic',
              detail: '个人知识管理系统 v1.0.0\n基于 Electron + Vue3 + Spring Boot 构建'
            })
          }
        }
      ]
    }
  ]

  const menu = Menu.buildFromTemplate(template)
  Menu.setApplicationMenu(menu)
}

// 启动后端服务
function startBackend() {
  const backendPath = process.env.NODE_ENV === 'development'
    ? path.join(__dirname, '../../backend/target/echologic-backend-1.0.0.jar')
    : path.join(process.resourcesPath, 'backend/echologic-backend-1.0.0.jar')

  // 检查 JAR 文件是否存在
  const fs = require('fs')
  if (!fs.existsSync(backendPath)) {
    console.error('Backend JAR not found:', backendPath)
    return
  }

  backendProcess = spawn('java', ['-jar', backendPath], {
    stdio: 'pipe'
  })

  backendProcess.stdout.on('data', (data) => {
    console.log('Backend:', data.toString())
  })

  backendProcess.stderr.on('data', (data) => {
    console.error('Backend Error:', data.toString())
  })

  backendProcess.on('close', (code) => {
    console.log(`Backend process exited with code ${code}`)
  })
}

// 应用准备就绪
app.whenReady().then(() => {
  startBackend()
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

// 所有窗口关闭时退出应用（macOS 除外）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    // 关闭后端进程
    if (backendProcess) {
      backendProcess.kill()
    }
    app.quit()
  }
})

// 应用退出前清理
app.on('before-quit', () => {
  if (backendProcess) {
    backendProcess.kill()
  }
})

// IPC 处理程序
ipcMain.handle('select-file', async () => {
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openFile'],
    filters: [
      { name: 'All Files', extensions: ['*'] }
    ]
  })
  return result
})

ipcMain.handle('select-directory', async () => {
  const result = await dialog.showOpenDialog(mainWindow, {
    properties: ['openDirectory']
  })
  return result
})
