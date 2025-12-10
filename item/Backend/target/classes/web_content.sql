-- 创建web_content表，用于存储网页解析内容
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

-- 如果需要，创建索引以提高查询性能
CREATE INDEX idx_web_content_collection_id ON web_content(collection_id);
CREATE INDEX idx_web_content_media_type ON web_content(media_type);
CREATE INDEX idx_web_content_create_time ON web_content(create_time);
