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
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
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

-- 创建记录和集合的多对多关联表
CREATE TABLE IF NOT EXISTS record_collection (
    record_id INT NOT NULL,
    collection_id INT NOT NULL,
    PRIMARY KEY (record_id, collection_id),
    FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE
);

-- 创建网页内容表，用于存储网页解析内容
CREATE TABLE IF NOT EXISTS web_content (
    id INT PRIMARY KEY AUTO_INCREMENT,
    collection_id INT NULL, -- 允许为NULL，支持不关联集合的记录
    source_url VARCHAR(255),
    title VARCHAR(255),
    parsed_text TEXT,
    cover_image_path VARCHAR(255),
    summary TEXT,
    tags VARCHAR(255),
    media_type VARCHAR(20),
    create_time DATETIME,
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE SET NULL
);

-- 创建web_content表的索引
CREATE INDEX idx_web_content_collection_id ON web_content(collection_id);
CREATE INDEX idx_web_content_media_type ON web_content(media_type);
CREATE INDEX idx_web_content_create_time ON web_content(create_time);




