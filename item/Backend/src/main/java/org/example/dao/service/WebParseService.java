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

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

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
    public WebContent parseAndSave(String url, Integer collectionId) throws IOException {
        // 忽略SSL证书验证
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            System.err.println("SSL配置失败: " + e.getMessage());
        }

        // 1. Jsoup 抓取网页
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get();

        String title = doc.title();
        String content = doc.body().text(); // 获取纯文本快照

        // 尝试获取封面图 (og:image)
        String coverUrl = null;
        String description = null;
        Element metaImage = doc.selectFirst("meta[property=og:image]");
        if (metaImage != null) {
            coverUrl = metaImage.attr("content");
        }
        
        // 尝试获取描述 (og:description)
        Element metaDescription = doc.selectFirst("meta[property=og:description]");
        if (metaDescription != null) {
            description = metaDescription.attr("content");
        }

        // 2. 下载封面图到本地 (static/images)
        String localCoverPath = downloadImage(coverUrl);

        // 3. 调用 AI 服务生成摘要和标签
        String summary = generateSummary(content, title, description);
        String tags = generateTags(content, title, description);

        Date now = new Date();

        // 5. 保存网页内容
        WebContent webContent = new WebContent();
        webContent.setCollectionId(collectionId);
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
    private String generateSummary(String content, String title, String description) {
        try {
            // 整合网页信息：标题、描述和内容，让AI生成更准确的摘要
            String combinedContent = "网页标题：" + title + "\n网页描述：" + description + "\n网页内容：" + content;
            // 调用AI服务生成摘要，明确要求1-2句话
            String prompt = "请基于以下网页信息生成一个1-2句话的简洁摘要，突出核心信息：";
            String aiResponse = aiService.callDeepSeekApi(prompt, combinedContent);
            return aiResponse != null ? aiResponse.trim() : "无法生成摘要";
        } catch (Exception e) {
            System.err.println("生成摘要失败: " + e.getMessage());
            return "无法生成摘要";
        }
    }

    /**
     * 使用AI生成标签
     */
    private String generateTags(String content, String title, String description) {
        try {
            // 整合网页信息：标题、描述和内容，让AI生成更准确的标签
            String combinedContent = "网页标题：" + title + "\n网页描述：" + description + "\n网页内容：" + content;
            // 调用AI服务生成标签，明确要求5-6个标签
            String prompt = "请为以下网页内容生成5-6个相关标签，用逗号分隔，标签要简洁明了：";
            String aiResponse = aiService.callDeepSeekApi(prompt, combinedContent);
            return aiResponse != null ? aiResponse.trim() : "";
        } catch (Exception e) {
            System.err.println("生成标签失败: " + e.getMessage());
            return "";
        }
    }


}