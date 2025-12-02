package com.example.smartparse.utils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

public class FileUtils {

    public static String downloadImage(String imageUrl, String targetDir) {
        if (imageUrl == null || imageUrl.isEmpty()) return null;
        try {
            URL url = new URL(imageUrl);
            // 简单生成文件名
            String filename = UUID.randomUUID().toString() + ".jpg";
            Path uploadPath = Paths.get("static/" + targetDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream in = url.openStream()) {
                Files.copy(in, uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
            return targetDir + "/" + filename;
        } catch (Exception e) {
            System.err.println("图片下载失败: " + e.getMessage());
            return null; // 失败返回空，不影响主流程
        }
    }
}