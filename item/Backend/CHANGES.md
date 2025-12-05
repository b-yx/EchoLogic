# 后端本次改动摘要
- 快速收藏接口：`POST /api/records/quick-capture` 支持文本、URL、文件路径，URL 去重（冲突 409），可附带标签。
- 相似内容推荐：`GET /api/records/{id}/recommendations` 基于标签重合推荐最多 5 条记录。
- 记录模型与表结构新增来源字段：`source_type`、`source_url`(唯一)、`source_file_path`；`init.sql` 同步更新，已在 web01 执行相应 ALTER。
- Mapper/Service/Controller 增加上述逻辑，Record POJO 新增字段，QuickCaptureRequest DTO 新增。
