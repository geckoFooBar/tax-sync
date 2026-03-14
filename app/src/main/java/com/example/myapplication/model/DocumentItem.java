package com.example.myapplication.model;

public class DocumentItem {
    private String title;
    private String fileType; // e.g., "PDF", "JPG", "REQ" (Required)
    private int status; // 0 = Missing/Required, 1 = Pending Review, 2 = Verified

    public DocumentItem(String title, String fileType, int status) {
        this.title = title;
        this.fileType = fileType;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getFileType() { return fileType; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}