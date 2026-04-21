package com.example.myapplication.model;

public class DocumentItem {
    private final String title;
    private final String fileType;
    private int status;
    private String fileUrl; // NEW: Stores the Supabase public URL

    // Original constructor for missing docs
    public DocumentItem(String title, String fileType, int status) {
        this.title = title;
        this.fileType = fileType;
        this.status = status;
        this.fileUrl = null;
    }

    // New constructor for uploaded docs
    public DocumentItem(String title, String fileType, int status, String fileUrl) {
        this.title = title;
        this.fileType = fileType;
        this.status = status;
        this.fileUrl = fileUrl;
    }

    public String getTitle() { return title; }
    public String getFileType() { return fileType; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}