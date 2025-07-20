package com.village.news.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.village.news.dto.VideoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.village.news.entity.Video;
import com.village.news.service.JwtService;
import com.village.news.service.VideoService;

@RestController
@RequestMapping("/api/videos")

public class VideoController {

    private final VideoService videoService;
    private final JwtService   jwtService;

    public VideoController(VideoService videoService, JwtService jwtService) {
        this.videoService = videoService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<VideoDTO>> approvedVideos() {
        List<Video> videos = videoService.getApprovedVideos();
        List<VideoDTO> videoDTOs = videos.stream()
                .map(VideoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(videoDTOs);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file,
                                         @RequestParam("title") String title,
                                         @RequestParam("userId") Long userId,
                                         Authentication authentication) {

        // Check authentication
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a file to upload"));
        }

        // Validate title
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Video title is required"));
        }

        // Validate userId
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required"));
        }

        try {
            videoService.uploadByUserId(file, title.trim(), userId);
            return ResponseEntity.ok(Map.of("message", "Video uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }


    @GetMapping("/my")
    public ResponseEntity<List<VideoDTO>> myVideos(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<Video> userVideos = videoService.getVideosByUser(authentication.getName());
            List<VideoDTO> videoDTOs = userVideos.stream()
                    .map(VideoDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(videoDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("Authorization") String hdr,
                                                      @PathVariable Long id) {
        try {
            String token = extractToken(hdr);
            String email = jwtService.getEmailFromToken(token);
            videoService.delete(id, email);
            return ResponseEntity.ok(Map.of("message", "Video deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader; // Return as-is if no Bearer prefix
    }
    // NEW: Delete all my videos (for current user)
    @DeleteMapping("/my/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMyVideos(@RequestHeader("Authorization") String hdr) {
        try {
            String email = jwtService.getEmailFromToken(hdr.substring(7));
            videoService.deleteMyVideos(email);
            return ResponseEntity.ok(Map.of("message", "All your videos deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Delete all videos by user ID (admin only)
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserVideos(@RequestHeader("Authorization") String hdr,
                                                                @PathVariable Long userId) {
        try {
            String email = jwtService.getEmailFromToken(hdr.substring(7));
            videoService.deleteByUserId(userId, email);
            return ResponseEntity.ok(Map.of("message", "All user videos deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
