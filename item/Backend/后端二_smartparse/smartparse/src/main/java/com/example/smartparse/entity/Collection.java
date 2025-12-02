package com.example.smartparse.entity;

import lombok.Data;
import jakarta.persistence.*; // 这里从 javax 改成了 jakarta
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceUrl;       // 来源链接
    private String title;           // 标题

    @Lob
    private String parsedText;      // 核心：正文纯文本快照 (2.3)

    private String coverImagePath;  // 核心：封面图本地路径 (2.3)

    @Lob
    private String summary;         // AI 摘要 (2.1 & 2.2)
    private String tags;            // AI 标签

    private String mediaType;       // web, audio, video

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}