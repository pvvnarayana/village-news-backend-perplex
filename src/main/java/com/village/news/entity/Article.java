package com.village.news.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, columnDefinition = "varchar(255) default 'PENDING'")
    private String status = "PENDING";

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    @JsonIgnore
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    @JsonIgnore
    private User approver;

    // JSON properties to expose author information without circular references
    @JsonProperty("authorName")
    public String getAuthorName() {
        if (author != null) {
            return author.getName();
        } else if (user != null) {
            return user.getName();
        }
        return null;
    }

    @JsonProperty("authorEmail")
    public String getAuthorEmail() {
        if (author != null) {
            return author.getEmail();
        } else if (user != null) {
            return user.getEmail();
        }
        return null;
    }

    @JsonProperty("approverName")
    public String getApproverName() {
        return approver != null ? approver.getName() : null;
    }

    // Default constructor
    public Article() {}

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

    // All getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", authorId=" + authorId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}
