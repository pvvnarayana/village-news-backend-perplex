package com.village.news.service;

import com.village.news.entity.User;
import com.village.news.entity.Video;
import com.village.news.repository.UserRepository;
import com.village.news.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {

    @Value("${video.storage.path:/opt/videos}")
    private String videoStoragePath;

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public VideoService(UserRepository userRepository, VideoRepository videoRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
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
}
