package com.village.news.controller;

import com.village.news.entity.User;
import com.village.news.entity.Video;
import com.village.news.repository.UserRepository;
import com.village.news.service.JwtService;
import com.village.news.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/videos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVideoController {

    private final VideoService videoService;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    public AdminVideoController(VideoService videoService, UserRepository userRepository, JwtService jwtService) {
        this.videoService = videoService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Video>> getPendingVideos(Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Video> pendingVideos = videoService.getPendingVideos();
            return ResponseEntity.ok(pendingVideos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{videoId}/approve")
    public ResponseEntity<?> approveVideo(@PathVariable Long videoId,
                                          Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin privileges required"));
        }

        try {
            // Get admin user ID from authentication
            Long adminUserId = getUserIdFromAuthentication(authentication);

            videoService.approveVideo(videoId, adminUserId);
            return ResponseEntity.ok(Map.of(
                    "message", "Video approved successfully",
                    "videoId", videoId,
                    "approvedBy", adminUserId
            ));
        } catch (Exception e) {
            System.err.println("Video approval error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve video: " + e.getMessage()));
        }
    }

    @PostMapping("/{videoId}/reject")
    public ResponseEntity<?> rejectVideo(@PathVariable Long videoId,
                                         Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin privileges required"));
        }

        try {
            videoService.rejectVideo(videoId);
            return ResponseEntity.ok(Map.of(
                    "message", "Video rejected successfully",
                    "videoId", videoId
            ));
        } catch (Exception e) {
            System.err.println("Video rejection error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reject video: " + e.getMessage()));
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        try {
            // Extract user ID from email in authentication
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Admin user not found: " + email));
            return user.getId();
        } catch (Exception e) {
            System.err.println("Failed to get user ID from authentication: " + e.getMessage());
            throw new RuntimeException("Failed to identify admin user");
        }
    }

    // Delete any video (admin privilege)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteVideo(@RequestHeader("Authorization") String hdr,
                                                           @PathVariable Long id) {
        try {
            String adminEmail = jwtService.getEmailFromToken(hdr.substring(7));
            videoService.delete(id, adminEmail);
            return ResponseEntity.ok(Map.of("message", "Video deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete all videos by specific user
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteUserVideos(@RequestHeader("Authorization") String hdr,
                                                                @PathVariable Long userId) {
        try {
            String adminEmail = jwtService.getEmailFromToken(hdr.substring(7));
            videoService.deleteByUserId(userId, adminEmail);
            return ResponseEntity.ok(Map.of("message", "All user videos deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // Batch delete multiple videos
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, String>> batchDeleteVideos(@RequestHeader("Authorization") String hdr,
                                                                 @RequestBody List<Long> videoIds) {
        try {
            String adminEmail = jwtService.getEmailFromToken(hdr.substring(7));
            for (Long id : videoIds) {
                videoService.delete(id, adminEmail);
            }
            return ResponseEntity.ok(Map.of("message", videoIds.size() + " videos deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
