package com.village.news.service;

import com.village.news.entity.User;
import com.village.news.entity.Video;
import com.village.news.repository.UserRepository;
import com.village.news.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {

    @Value("${video.storage.path:/opt/videos}")
    private String videoStoragePath;

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final UserService     userService;

    public VideoService(UserRepository userRepository, VideoRepository videoRepository, UserService userService) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.userService = userService;
    }


    // Method to get videos by user ID directly
    public List<Video> getVideosByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        try {
            // Try user_id first
            List<Video> videos = videoRepository.findByUserIdOrderByCreatedAtDesc(userId);

            // If no videos found, try uploader_id
            if (videos.isEmpty()) {
                videos = videoRepository.findByUploaderIdOrderByCreatedAtDesc(userId);
            }

            return videos;

        } catch (Exception e) {
            System.err.println("Error fetching videos for user ID " + userId + ": " + e.getMessage());
            // Fallback to flexible query
            return videoRepository.findByUserIdOrUploaderIdOrderByCreatedAtDesc(userId);
        }
    }

    public List<Video> getApprovedVideos() {
        try {
            return videoRepository.findByStatusWithUploaderOrderByCreatedAtDesc("APPROVED");
        } catch (Exception e) {
            System.err.println("Error fetching approved videos with uploader info: " + e.getMessage());
            // Fallback to simple query
            return videoRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
        }
    }

    public List<Video> getPendingVideos() {
        try {
            return videoRepository.findByStatusWithUploaderOrderByCreatedAtDesc("PENDING");
        } catch (Exception e) {
            System.err.println("Error fetching pending videos with uploader info: " + e.getMessage());
            return videoRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        }
    }

    public List<Video> getVideosByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // Use JOIN FETCH query to get videos with uploader info
            List<Video> videos = videoRepository.findByUserIdWithUploaderOrderByCreatedAtDesc(user.getId());

            // If no videos found with user_id, try uploader_id
            if (videos.isEmpty()) {
                videos = videoRepository.findByUploaderIdWithUploaderOrderByCreatedAtDesc(user.getId());
            }

            return videos;
        } catch (Exception e) {
            System.err.println("Error fetching user videos with uploader info: " + e.getMessage());
            // Fallback to flexible query
            return videoRepository.findByUserIdOrUploaderIdOrderByCreatedAtDesc(user.getId());
        }
    }

    // Video approval with proper constraint handling
    public void approveVideo(Long videoId, Long approverId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // Validate approver exists
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Use the business logic method that sets all required fields
        video.approve(approverId);

        videoRepository.save(video);

        System.out.println("Video approved successfully: ID=" + videoId +
                ", ApprovedBy=" + approverId +
                ", ApprovedAt=" + video.getApprovedAt());
    }

    public void rejectVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        video.reject();
        videoRepository.save(video);

        System.out.println("Video rejected successfully: ID=" + videoId);
    }

    public void reject(Long id, String adminEmail) {
        Video v = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        v.setStatus("REJECTED");
        v.setApprovedAt(LocalDateTime.now());
        v.setApprovedBy(userService.findByEmail(adminEmail).get().getId());
        videoRepository.save(v);
    }


    // Video upload method
    public void uploadByUserId(MultipartFile file, String title, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            Path uploadDir = Paths.get(videoStoragePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                originalFileName = "unknown_file";
            }

            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path filePath = uploadDir.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Video video = new Video();
            video.setTitle(title);
            video.setFilePath(storedFileName);
            video.setOriginalFileName(originalFileName);
            video.setUserId(userId);        // Set user_id
            video.setUploaderId(userId);    // Set uploader_id (same value)
            video.setStatus("PENDING");
            video.setFileSize(file.getSize());
            video.setContentType(file.getContentType());
            video.setMimeType(file.getContentType());

            videoRepository.save(video);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store video file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Video upload failed: " + e.getMessage(), e);
        }
    }

    // Backward compatibility method
    public void upload(MultipartFile file, String title, String uploaderEmail) {
        User user = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + uploaderEmail));
        uploadByUserId(file, title, user.getId());
    }

    public void deleteVideo(Long id) {
        Video video = videoRepository.findById(id).orElse(null);
        if (video != null) {
            try {
                Path filePath = Paths.get(videoStoragePath, video.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + e.getMessage());
            }

            videoRepository.deleteById(id);
        }
    }

    public void delete(Long id, String requesterEmail) {
        Video v = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        boolean isAdmin = userService.findByEmail(requesterEmail).get().getRole() == "ADMIN";

        if (isAdmin || v.getUser().getEmail().equals(requesterEmail)) {
            // Delete physical file
            Path p = Paths.get(videoStoragePath).resolve(v.getFilePath());
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {}

            // Delete database record
            videoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Unauthorized delete");
        }
    }

    // NEW: Delete all videos by user ID
    public void deleteByUserId(Long userId, String requesterEmail) {
        User requester = userService.findByEmail(requesterEmail).get();
        boolean isAdmin = requester.getRole() == "ADMIN";

        // Only admin can delete all videos of a user
        if (!isAdmin) {
            throw new RuntimeException("Only admin can delete all user videos");
        }

        List<Video> userVideos = videoRepository.findByUserIdOrderByCreatedAtDesc(userId);

        for (Video video : userVideos) {
            // Delete physical file
            Path p = Paths.get(videoStoragePath).resolve(video.getFilePath());
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {}
        }

        // Delete all database records for this user
        videoRepository.deleteAll(userVideos);
    }

    // NEW: Delete own videos (for user)
    public void deleteMyVideos(String userEmail) {
        User user = userService.findByEmail(userEmail).get();
        List<Video> userVideos = videoRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        for (Video video : userVideos) {
            // Delete physical file
            Path p = Paths.get(videoStoragePath).resolve(video.getFilePath());
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {}
        }

        // Delete all database records for this user
        videoRepository.deleteAll(userVideos);
    }

}
