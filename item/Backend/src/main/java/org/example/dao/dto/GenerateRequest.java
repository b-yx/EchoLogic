package org.example.dao.dto;

import java.util.List;

public class GenerateRequest {
    private List<String> contents;
    private String goal;

    public List<String> getContents() { return contents; }
    public void setContents(List<String> contents) { this.contents = contents; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}