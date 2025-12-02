package com.example.smartparse.controller;

import com.example.smartparse.entity.Collection;
import com.example.smartparse.service.ParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ParseController {

    @Autowired
    private ParseService parseService;

    // 接口 1: 网页解析 (POST /api/parse)
    @PostMapping("/parse")
    public Collection parseUrl(@RequestBody Map<String, String> payload) {
        String url = payload.get("url");
        try {
            return parseService.parseAndSave(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 简单处理：报错返回空
        }
    }

    // 接口 2: 媒体上传 (POST /api/upload_media)
    @PostMapping("/upload_media")
    public Collection uploadMedia(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String type = "unknown";

        if (fileName != null) {
            if (fileName.endsWith(".mp3")) type = "audio";
            if (fileName.endsWith(".mp4")) type = "video";
        }

        // 注：这里省略了实际 saveFile 到磁盘的代码，专注于数据库记录和 AI 模拟
        return parseService.saveMediaRecord(fileName, type);
    }
}