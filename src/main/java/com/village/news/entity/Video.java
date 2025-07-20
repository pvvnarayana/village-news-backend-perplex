package com.village.news.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false, columnDefinition = "varchar(255) default 'PENDING'")
    private String status = "PENDING";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "approved_by")
    private Long approvedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships - keep these but don't serialize to avoid circular references
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", insertable = false, updatable = false)
    @JsonIgnore
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    @JsonIgnore
    private User approver;

    // ADD THESE JSON PROPERTIES to expose uploader information
    @JsonProperty("uploaderName")
    public String getUploaderName() {
        if (uploader != null) {
            return uploader.getName();
        } else if (user != null) {
            return user.getName();
        }
        return null;
    }

    @JsonProperty("uploaderEmail")
    public String getUploaderEmail() {
        if (uploader != null) {
            return uploader.getEmail();
        } else if (user != null) {
            return user.getEmail();
        }
        return null;
    }

    @JsonProperty("approverName")
    public String getApproverName() {
        return approver != null ? approver.getName() : null;
    }

    // Helper method to get file size in MB for display
    @JsonProperty("fileSizeInMB")
    public Double getFileSizeInMB() {
        if (fileSize != null && fileSize > 0) {
            return Math.round(fileSize / (1024.0 * 1024.0) * 100.0) / 100.0;
        }
        return 0.0;
    }

    // Business logic methods
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(this.status);
    }

    public void approve(Long approverId) {
        this.status = "APPROVED";
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = "REJECTED";
        this.approvedBy = null;
        this.approvedAt = null;
    }

    // All your existing getters and setters remain the same
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

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }

    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", filePath='" + filePath + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", status='" + status + '\'' +
                ", userId=" + userId +
                ", uploaderId=" + uploaderId +
                ", createdAt=" + createdAt +
                '}';
    }
}
