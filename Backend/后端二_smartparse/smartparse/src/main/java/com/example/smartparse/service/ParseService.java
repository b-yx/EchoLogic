package com.example.smartparse.service;

import com.example.smartparse.entity.Collection;
import com.example.smartparse.repository.CollectionRepository;
import com.example.smartparse.utils.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ParseService {

    @Autowired
    private AIService aiService;

    @Autowired
    private CollectionRepository repository;

    /**
     * 功能 2.1 & 2.3: 解析网页并保存
     */
    public Collection parseAndSave(String url) throws IOException {
        // 1. Jsoup 抓取网页
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get();

        String title = doc.title();
        String content = doc.body().text(); // 获取纯文本快照

        // 尝试获取封面图 (og:image)
        String coverUrl = null;
        Element metaImage = doc.selectFirst("meta[property=og:image]");
        if (metaImage != null) {
            coverUrl = metaImage.attr("content");
        }

        // 2. 下载封面图到本地 (static/images)
        String localCoverPath = FileUtils.downloadImage(coverUrl, "images");

        // 3. 调用 AI 模拟服务
        String summary = aiService.generateSummary(content);
        String tags = aiService.generateTags(content);

        // 4. 存入数据库
        Collection collection = new Collection();
        collection.setSourceUrl(url);
        collection.setTitle(title);
        collection.setParsedText(content); // 快照
        collection.setCoverImagePath(localCoverPath);
        collection.setSummary(summary);
        collection.setTags(tags);
        collection.setMediaType("web");

        return repository.save(collection);
    }

    /**
     * 功能 2.2: 保存上传的媒体记录
     */
    public Collection saveMediaRecord(String fileName, String type) {
        String aiSummary = aiService.summarizeMedia(type);

        Collection collection = new Collection();
        collection.setTitle("上传文件: " + fileName);
        collection.setMediaType(type);
        collection.setSummary(aiSummary);
        collection.setTags("多媒体,AI处理");
        collection.setParsedText("[媒体文件，无纯文本快照]");

        return repository.save(collection);
    }
}