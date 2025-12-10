package org.example.dao.dto;

public class ParseUrlRequest {
    private String url;
    private Integer collectionId; // 可选的集合ID

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }
}