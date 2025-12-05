package org.example.dao.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuickCaptureRequest {
    /**
     * TEXT / URL / FILE
     */
    private String type;
    /**
     * 文本内容（快速输入）
     */
    private String text;
    /**
     * 拖拽的 URL
     */
    private String url;
    /**
     * 拖拽的文件路径或文件名
     */
    private String filePath;
    /**
     * 可选标题
     */
    private String title;
    /**
     * 关联标签ID
     */
    private List<Integer> tagIds;
}
