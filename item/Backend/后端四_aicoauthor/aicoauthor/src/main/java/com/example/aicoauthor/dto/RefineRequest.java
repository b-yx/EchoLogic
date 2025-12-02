package com.example.aicoauthor.dto;

public class RefineRequest {
    private String currentContent;
    private String instruction;

    public String getCurrentContent() { return currentContent; }
    public void setCurrentContent(String currentContent) { this.currentContent = currentContent; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
