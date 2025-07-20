package com.village.news.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    private String role = "USER";

    @Column(name = "google_id")
    private String googleId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Video> videos;

    // Default constructor
    public User() {}

    // Constructor with all fields
    public User(Long id, String email, String name, String profilePictureUrl,
                String role, String googleId, LocalDateTime createdAt,
                LocalDateTime updatedAt, List<Video> videos) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.role = role;
        this.googleId = googleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.videos = videos;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getRole() {
        return role;
    }

    public String getGoogleId() {
        return googleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Video> getVideos() {
        return videos;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    // toString method
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", googleId='" + googleId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
