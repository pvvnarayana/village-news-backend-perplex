package com.village.news.dto;

import com.village.news.entity.Video;

import java.time.LocalDateTime;

public class VideoDTO {
    private Long id;
    private String title;
    private String description;
    private String filePath;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String status;
    private Long userId;
    private String uploaderName;
    private String uploaderEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public VideoDTO() {}

    // Constructor from Video entity
    public VideoDTO(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.filePath = video.getFilePath();
        this.originalFileName = video.getOriginalFileName();
        this.fileSize = video.getFileSize();
        this.contentType = video.getContentType();
        this.status = video.getStatus();
        this.userId = video.getUserId();
        this.createdAt = video.getCreatedAt();
        this.updatedAt = video.getUpdatedAt();
        
        // Safely get uploader info
        if (video.getUploader() != null) {
            this.uploaderName = video.getUploader().getName();
            this.uploaderEmail = video.getUploader().getEmail();
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
    
    public String getUploaderEmail() { return uploaderEmail; }
    public void setUploaderEmail(String uploaderEmail) { this.uploaderEmail = uploaderEmail; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
