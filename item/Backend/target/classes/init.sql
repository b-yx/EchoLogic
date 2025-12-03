-- 创建集合表
CREATE TABLE IF NOT EXISTS collection (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 创建记录表
CREATE TABLE IF NOT EXISTS record (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    collection_id INT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE
);

-- 创建标签表
CREATE TABLE IF NOT EXISTS tag (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#007bff'
);

-- 创建记录和标签的多对多关联表
CREATE TABLE IF NOT EXISTS record_tag (
    record_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (record_id, tag_id),
    FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- ----------------------------
-- 成长感知与激励模块
-- ----------------------------

-- 用户行为表
CREATE TABLE `user_behavior` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `behavior_type` varchar(50) NOT NULL COMMENT '行为类型: COLLECT, INCUBATE',
  `behavior_time` datetime NOT NULL COMMENT '行为发生时间',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联的Collection或Record ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `behavior_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 每日任务模板表
CREATE TABLE `daily_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(255) NOT NULL COMMENT '任务名称',
  `target_count` int(11) NOT NULL COMMENT '目标数量',
  `reward` int(11) DEFAULT '0' COMMENT '奖励（如AI次数）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日任务模板表';

-- 用户每日任务表
CREATE TABLE `user_daily_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `task_id` bigint(20) NOT NULL,
  `current_count` int(11) DEFAULT '0' COMMENT '当前完成数量',
  `is_completed` tinyint(1) DEFAULT '0' COMMENT '是否完成 (0:否, 1:是)',
  `reward_received` tinyint(1) DEFAULT '0' COMMENT '是否领取奖励 (0:否, 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_task_date` (`user_id`, `task_id`) -- 假设每日任务通过日期逻辑保证唯一性
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日任务关联表';

-- 用户成就表
CREATE TABLE `user_achievement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `achievement_name` varchar(255) NOT NULL COMMENT '成就名称',
  `unlock_condition` varchar(255) DEFAULT NULL COMMENT '解锁条件描述',
  `condition_value` int(11) NOT NULL COMMENT '条件阈值',
  `current_value` int(11) DEFAULT '0' COMMENT '当前进度',
  `is_unlocked` tinyint(1) DEFAULT '0' COMMENT '是否解锁 (0:否, 1:是)',
  `unlock_time` datetime DEFAULT NULL COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- 插入示例数据
INSERT INTO `daily_task` (task_name, target_count, reward) VALUES ('今日收藏3条内容', 3, 10);
INSERT INTO `daily_task` (task_name, target_count, reward) VALUES ('今日孵化1个文集', 1, 20);

INSERT INTO `user_achievement` (user_id, achievement_name, unlock_condition, condition_value) VALUES (1, '收藏新星', '累计收藏10条内容', 10);
INSERT INTO `user_achievement` (user_id, achievement_name, unlock_condition, condition_value) VALUES (1, '收藏达人', '累计收藏100条内容', 100);
INSERT INTO `user_achievement` (user_id, achievement_name, unlock_condition, condition_value) VALUES (1, '孵化先锋', '累计孵化5个文集', 5);
