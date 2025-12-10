package org.example.dao.dto;

import java.util.List;
import java.util.Map;

public class RefineRequest {
    private String currentContent;
    private String instruction;
    private List<Map<String, String>> dialogueHistory;

    public String getCurrentContent() { return currentContent; }
    public void setCurrentContent(String currentContent) { this.currentContent = currentContent; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public List<Map<String, String>> getDialogueHistory() { return dialogueHistory; }
    public void setDialogueHistory(List<Map<String, String>> dialogueHistory) { this.dialogueHistory = dialogueHistory; }
}