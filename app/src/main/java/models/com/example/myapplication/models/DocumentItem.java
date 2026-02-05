package com.example.myapplication.models;

public class DocumentItem {

    private String title;
    private String type;
    private String uploadedDate;

    public DocumentItem(String title, String type, String uploadedDate) {
        this.title = title;
        this.type = type;
        this.uploadedDate = uploadedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }
}
