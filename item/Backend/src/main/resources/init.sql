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