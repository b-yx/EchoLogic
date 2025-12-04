package org.example.dao.service;

import org.example.dao.mapper.CollectionMapper;
import org.example.dao.mapper.WebContentMapper;
import org.example.dao.pojo.CollectionEntity;
import org.example.dao.pojo.WebContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.UUID;

@Service
public class WebParseService {

    @Autowired
    private AIService aiService;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private WebContentMapper webContentMapper;

    // 图片存储路径
    private static final String IMAGE_STORAGE_PATH = "src/main/resources/static/images/";

    /**
     * 解析网页并保存到集合
     */
    @Transactional
    public WebContent parseAndSave(String url) throws IOException {
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
        String localCoverPath = downloadImage(coverUrl);

        // 3. 调用 AI 服务生成摘要和标签
        String summary = generateSummary(content);
        String tags = generateTags(content);

        // 4. 创建集合
        CollectionEntity collection = new CollectionEntity();
        collection.setName(title);
        collection.setDescription(summary);
        Date now = new Date();
        collection.setCreateTime(now);
        collection.setUpdateTime(now);
        collectionMapper.insert(collection);

        // 5. 保存网页内容
        WebContent webContent = new WebContent();
        webContent.setCollectionId(collection.getId());
        webContent.setSourceUrl(url);
        webContent.setTitle(title);
        webContent.setParsedText(content);
        webContent.setCoverImagePath(localCoverPath);
        webContent.setSummary(summary);
        webContent.setTags(tags);
        webContent.setMediaType("web");
        webContent.setCreateTime(now);
        webContentMapper.insert(webContent);

        return webContent;
    }



    /**
     * 下载图片到本地
     */
    private String downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            // 确保目录存在
            File directory = new File(IMAGE_STORAGE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + ".jpg";
            Path destinationPath = Paths.get(IMAGE_STORAGE_PATH + fileName);

            // 下载图片
            URL url = new URL(imageUrl);
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/images/" + fileName;
        } catch (Exception e) {
            System.err.println("下载图片失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 使用AI生成摘要
     */
    private String generateSummary(String content) {
        String prompt = "请对以下内容生成一个简洁的摘要，不超过200字：";
        String aiResponse = aiService.callDeepSeekApi(prompt, content);
        return aiResponse != null ? aiResponse.trim() : "";
    }

    /**
     * 使用AI生成标签
     */
    private String generateTags(String content) {
        String prompt = "请为以下内容生成3-5个关键词标签，用逗号分隔：";
        String aiResponse = aiService.callDeepSeekApi(prompt, content);
        return aiResponse != null ? aiResponse.trim() : "";
    }


}