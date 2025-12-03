-- 创建记录集合关联表（中间表）
CREATE TABLE record_collection (
    record_id INT NOT NULL,
    collection_id INT NOT NULL,
    PRIMARY KEY (record_id, collection_id),
    FOREIGN KEY (record_id) REFERENCES record(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE
);
