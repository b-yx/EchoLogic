<template>
  <div class="activity-heatmap">
    <!-- 头部信息 -->
    <div class="heatmap-header">
      <div class="header-content">
        <h3 class="heatmap-title">{{ title }}</h3>
        <div class="time-range-selector">
          <button 
            v-for="range in timeRanges" 
            :key="range.value"
            class="range-btn"
            :class="{ active: selectedRange === range.value }"
            @click="selectTimeRange(range.value)"
          >
            {{ range.label }}
          </button>
        </div>
      </div>
      
      <!-- 图例 -->
      <div class="heatmap-legend">
        <span class="legend-text">活动频率</span>
        <div class="legend-scale">
          <div class="legend-item">
            <div class="legend-color" :class="'legend-color-' + level" v-for="level in activityLevels" :key="level"></div>
          </div>
          <div class="legend-labels">
            <span>少</span>
            <span>多</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="heatmap-body">
      <!-- 星期标签 -->
      <div class="weekday-labels">
        <div v-for="day in weekdays" :key="day" class="weekday-label">{{ day }}</div>
      </div>
      
      <!-- 日期网格 -->
      <div class="date-grid month-calendar-view">
        <div 
          v-for="(cell, index) in heatmapCells" 
          :key="index"
          class="date-cell"
          :class="['activity-level-' + cell.level, { 'has-activity': cell.level > 0 }]"
          :title="`${cell.date}: ${cell.totalCount || 0}次活动`"
          @click="handleCellClick(cell)"
        ></div>
      </div>
    </div>

    <!-- 点击详情弹窗 -->
    <div v-if="selectedCell" class="cell-detail-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h4>{{ selectedCell.date }} 活动详情</h4>
          <button class="close-btn" @click="selectedCell = null">&times;</button>
        </div>
        <div class="modal-body">
          <p class="total-count">总活动次数: <strong>{{ selectedCell.totalCount }}</strong></p>
          <div v-if="selectedCell.details.length > 0" class="activity-details">
            <h5>活动类型详情:</h5>
            <ul class="detail-list">
              <li v-for="detail in selectedCell.details" :key="detail.type">
                <span class="detail-type">{{ formatActivityType(detail.type) }}:</span>
                <span class="detail-count">{{ detail.count }}次</span>
              </li>
            </ul>
          </div>
          <p v-else class="no-activity">当日无活动记录</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

// Props定义
const props = defineProps({
  title: {
    type: String,
    default: '用户活动统计'
  },
  data: {
    type: Array,
    default: () => []
  }
})

// 事件定义
const emit = defineEmits(['time-range-change', 'cell-click'])

// 当前选择的时间范围（默认当前月）
const getCurrentMonth = () => {
  const currentDate = new Date()
  const year = currentDate.getFullYear()
  const month = currentDate.getMonth() + 1
  return `${year}-${month.toString().padStart(2, '0')}`
}

const selectedRange = ref(getCurrentMonth())
const selectedCell = ref(null)

// 生成最近12个月的选项
const generateMonthOptions = () => {
  const months = []
  const currentDate = new Date()
  
  for (let i = 0; i < 12; i++) {
    const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1)
    const year = date.getFullYear()
    const month = date.getMonth() + 1 // 月份从1开始
    months.push({
      label: `${year}年${month}月`,
      value: `${year}-${month.toString().padStart(2, '0')}`
    })
  }
  
  return months
}

// 配置数据
const timeRanges = ref(generateMonthOptions())

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const activityLevels = [0, 1, 2, 3, 4, 5]
const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

// 活动类型映射
const activityTypeMap = {
  'CREATE_NOTE': '创建笔记',
  'UPDATE_NOTE': '更新笔记',
  'DELETE_NOTE': '删除笔记',
  'CREATE_TASK': '创建任务',
  'COMPLETE_TASK': '完成任务',
  'VIEW_NOTE': '查看笔记',
  'VIEW_TASK': '查看任务',
  'LOGIN': '登录',
  'LOGOUT': '退出'
}

// 时间范围选择
const selectTimeRange = (range) => {
  selectedRange.value = range
  emit('time-range-change', range)
}

// 日期格式化
const formatDate = (date) => {
  return date.toISOString().split('T')[0]
}

// 生成日期范围
const generateDateRange = () => {
  const dates = []
  
  // 解析选择的月份 (格式: YYYY-MM)
  const [year, month] = selectedRange.value.split('-').map(Number)
  
  // 获取该月第一天
  const firstDay = new Date(year, month - 1, 1)
  
  // 获取该月最后一天
  const lastDay = new Date(year, month, 0)
  const daysInMonth = lastDay.getDate()
  
  // 生成该月所有日期
  for (let day = 1; day <= daysInMonth; day++) {
    dates.push(new Date(year, month - 1, day))
  }
  
  return dates
}

// 计算活动级别
const calculateActivityLevel = (dateStr) => {
  const dataPoint = props.data.find(item => item.date === dateStr)
  
  if (!dataPoint || !dataPoint.total || dataPoint.total === 0) {
    return 0
  }
  
  const count = dataPoint.total
  if (count >= 10) return 5
  if (count >= 6) return 4
  if (count >= 4) return 3
  if (count >= 2) return 2
  if (count >= 1) return 1
  return 0
}

// 获取月份位置
const getMonthPosition = (index) => {
  // 对于月视图和周视图，不需要月份位置
  return '1 / span 1'
}

// 获取单元格tooltip
const getCellTooltip = (cell) => {
  let tooltip = `${cell.date} 日\n`
  
  if (cell.totalCount > 0) {
    tooltip += `总活动次数: ${cell.totalCount}次\n`
    if (cell.details.length > 0) {
      tooltip += '活动详情:\n'
      cell.details.forEach(detail => {
        tooltip += `${formatActivityType(detail.type)}: ${detail.count}次\n`
      })
    }
  } else {
    tooltip += '无活动记录'
  }
  
  return tooltip
}

// 格式化活动类型
const formatActivityType = (type) => {
  return activityTypeMap[type] || type
}

// 处理单元格点击
const handleCellClick = (cell) => {
  selectedCell.value = cell
  emit('cell-click', cell)
}

// 计算属性
const displayedMonths = computed(() => {
  // 周视图和月视图不需要显示月份标签
  return []
})

const heatmapCells = computed(() => {
  const dates = generateDateRange()
  const cells = []
  
  dates.forEach(date => {
    const dateStr = formatDate(date)
    const level = calculateActivityLevel(dateStr)
    const dataPoint = props.data.find(item => item.date === dateStr)
    
    const details = []
    if (dataPoint && dataPoint.details) {
      Object.entries(dataPoint.details).forEach(([type, count]) => {
        details.push({ type, count })
      })
    }
    
    cells.push({
      date: dateStr,
      level,
      totalCount: dataPoint ? dataPoint.total : 0,
      details
    })
  })
  
  return cells
})
</script>

<style scoped>
/* 基础样式 */
.activity-heatmap {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 头部样式 */
.heatmap-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.heatmap-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* 时间范围选择器 */
.time-range-selector {
  display: flex;
  gap: 8px;
}

.range-btn {
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.range-btn:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
}

.range-btn.active {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

/* 图例样式 */
.heatmap-legend {
  display: flex;
  align-items: center;
  gap: 16px;
}

.legend-text {
  font-size: 14px;
  color: #606266;
}

.legend-scale {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.legend-item {
  display: flex;
  gap: 4px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.legend-color-0 { background-color: #f5f7fa; }
.legend-color-1 { background-color: #e6f7e6; }
.legend-color-2 { background-color: #c3e6cb; }
.legend-color-3 { background-color: #7bc96f; }
.legend-color-4 { background-color: #4caf50; }
.legend-color-5 { background-color: #2e7d32; }

.legend-labels {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 12px;
  color: #909399;
}

/* 主体内容 */
.heatmap-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: start;
}

/* 星期标签 */
.weekday-labels {
  display: flex;
  flex-direction: row;
  gap: 4px;
  margin-top: 0;
}

.weekday-label {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 月份标题 */
.month-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  text-align: center;
  width: 100%;
}

/* 日期网格 */
.heatmap-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 月份标签 */
.month-labels {
  display: none;
}

/* 日期单元格 */
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 20px); /* 固定7列，对应一周7天 */
  gap: 2px;
  margin-top: 5px;
}

/* 周视图 */
.date-grid.week-view {
  /* 周视图样式 */
}

/* 月历视图 */
.date-grid.month-calendar-view {
  /* 月历视图样式 */
}

.date-cell {
  width: 20px;
  height: 20px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.date-cell:hover {
  transform: scale(1.2);
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.activity-level-0 { background-color: #f5f7fa; }
.activity-level-1 { background-color: #e6f7e6; }
.activity-level-2 { background-color: #c3e6cb; }
.activity-level-3 { background-color: #7bc96f; }
.activity-level-4 { background-color: #4caf50; }
.activity-level-5 { background-color: #2e7d32; }

/* 详情弹窗 */
.cell-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.modal-content {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 400px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.modal-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
}

.close-btn:hover {
  color: #606266;
}

.modal-body {
  padding: 16px;
}

.total-count {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #303133;
}

.activity-details h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.detail-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.detail-list li {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #f5f7fa;
}

.detail-list li:last-child {
  border-bottom: none;
}

.detail-type {
  font-weight: 500;
}

.detail-count {
  color: #303133;
  font-weight: 600;
}

.no-activity {
  text-align: center;
  color: #909399;
  font-style: italic;
  margin: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-heatmap {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .date-grid {
    grid-template-columns: repeat(53, 16px);
    gap: 2px;
  }
  
  .date-cell {
    width: 16px;
    height: 16px;
  }
  
  .month-labels {
    grid-template-columns: repeat(53, 16px);
    gap: 2px;
  }
}
</style>