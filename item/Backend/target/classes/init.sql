-- 创建集合表
CREATE TABLE IF NOT EXISTS collection (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建记录表
CREATE TABLE IF NOT EXISTS record (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    source_type VARCHAR(50) DEFAULT 'TEXT',
    source_url VARCHAR(500) UNIQUE,
    source_file_path VARCHAR(500),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建标签表
CREATE TABLE IF NOT EXISTS tag (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20) DEFAULT '#007bff'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建记录和标签的多对多关联表
CREATE TABLE IF NOT EXISTS record_tag (
    record_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (record_id, tag_id),
    FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建记录和集合的多对多关联表
CREATE TABLE IF NOT EXISTS record_collection (
    record_id INT NOT NULL,
    collection_id INT NOT NULL,
    PRIMARY KEY (record_id, collection_id),
    FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 成长感知与激励模块
-- ----------------------------

-- 用户行为表
CREATE TABLE IF NOT EXISTS user_behavior (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  behavior_type VARCHAR(50) NOT NULL COMMENT '行为类型: COLLECT, INCUBATE',
  behavior_time DATETIME NOT NULL COMMENT '行为发生时间',
  related_id BIGINT DEFAULT NULL COMMENT '关联的Collection或Record ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_time (user_id, behavior_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 每日任务模板表
CREATE TABLE IF NOT EXISTS daily_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
  behavior_type VARCHAR(50) NOT NULL COMMENT '关联的行为类型',
  target_count INT NOT NULL COMMENT '目标数量',
  reward INT DEFAULT 0 COMMENT '奖励（如AI次数）',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日任务模板表';

-- 用户每日任务表
CREATE TABLE IF NOT EXISTS user_daily_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  create_date DATE NOT NULL COMMENT '任务日期',
  current_count INT DEFAULT 0 COMMENT '当前完成数量',
  is_completed TINYINT(1) DEFAULT 0 COMMENT '是否完成 (0:否 1:是)',
  reward_received TINYINT(1) DEFAULT 0 COMMENT '是否领取奖励 (0:否 1:是)',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_task_date (user_id, task_id, create_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日任务关联表';

-- 用户成就表
CREATE TABLE IF NOT EXISTS user_achievement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  achievement_name VARCHAR(255) NOT NULL COMMENT '成就名称',
  behavior_type VARCHAR(50) DEFAULT NULL COMMENT '关联的行为类型',
  unlock_condition VARCHAR(255) DEFAULT NULL COMMENT '解锁条件描述',
  condition_value INT NOT NULL COMMENT '条件阈值',
  current_value INT DEFAULT 0 COMMENT '当前进度',
  is_unlocked TINYINT(1) DEFAULT 0 COMMENT '是否解锁 (0:否 1:是)',
  unlock_time DATETIME DEFAULT NULL COMMENT '解锁时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- 插入示例数据
INSERT INTO daily_task (task_name, behavior_type, target_count, reward) VALUES ('今日收藏3条内容', 'COLLECT', 3, 10);
INSERT INTO daily_task (task_name, behavior_type, target_count, reward) VALUES ('今日孵化1个文摘', 'INCUBATE', 1, 20);

INSERT INTO user_achievement (user_id, achievement_name, unlock_condition, condition_value, behavior_type) VALUES (1, '收藏新星', '累计收藏10条内容', 10, 'COLLECT');
INSERT INTO user_achievement (user_id, achievement_name, unlock_condition, condition_value, behavior_type) VALUES (1, '收藏达人', '累计收藏100条内容', 100, 'COLLECT');
INSERT INTO user_achievement (user_id, achievement_name, unlock_condition, condition_value, behavior_type) VALUES (1, '孵化先锋', '累计孵化5个文摘', 5, 'INCUBATE');

-- 用户AI配额
CREATE TABLE IF NOT EXISTS user_ai_quota (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  quota INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_quota (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
