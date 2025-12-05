# 新增接口文档（后端）

## 快速收藏 /api/records/quick-capture
- Method: POST
- Content-Type: application/json
- 请求体：
```json
{
  "type": "TEXT | URL | FILE",
  "text": "string，可选，type=TEXT 时必填",
  "url": "string，可选，type=URL 时必填",
  "filePath": "string，可选，type=FILE 时必填，仅存路径/文件名",
  "title": "string，可选",
  "tagIds": [1, 2]
}
```
- 说明：
  - type 默认 TEXT；URL 去重：同一 source_url 已存在且未删除返回 409，提示已收藏。
  - 失败时返回 400（缺字段）或 409（重复 URL）。
- 响应：创建成功的 Record 对象。

## 相似内容推荐 /api/records/{id}/recommendations
- Method: GET
- Path Param: id（记录 ID）
- 逻辑：基于标签重合，统计当前记录的标签与其他记录的重叠数，按重叠数降序、更新时间降序，最多 5 条。
- 响应：Record 数组（可能为空）。

## 记录模型新增字段
- `source_type`: VARCHAR(50) 来源类型，默认 TEXT
- `source_url`: VARCHAR(500) 收藏的 URL（唯一约束，用于去重）
- `source_file_path`: VARCHAR(500) 拖拽文件路径/文件名

