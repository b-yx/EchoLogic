package com.example.aicoauthor.dto;

public class GenerateResponse {
    private String result;
    private long timestamp;

    public GenerateResponse() {}
    public GenerateResponse(String result, long timestamp) {
        this.result = result;
        this.timestamp = timestamp;
    }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
