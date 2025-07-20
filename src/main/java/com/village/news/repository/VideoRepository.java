package com.village.news.repository;

import com.village.news.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByStatus(String status);
    List<Video> findByStatusOrderByCreatedAtDesc(String status);

    List<Video> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Video> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    // ADD THESE JOIN FETCH QUERIES to load uploader information
    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.uploader WHERE v.status = :status ORDER BY v.createdAt DESC")
    List<Video> findByStatusWithUploaderOrderByCreatedAtDesc(@Param("status") String status);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.uploader WHERE v.userId = :userId ORDER BY v.createdAt DESC")
    List<Video> findByUserIdWithUploaderOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.uploader WHERE v.uploaderId = :uploaderId ORDER BY v.createdAt DESC")
    List<Video> findByUploaderIdWithUploaderOrderByCreatedAtDesc(@Param("uploaderId") Long uploaderId);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.uploader WHERE (v.userId = :id OR v.uploaderId = :id) ORDER BY v.createdAt DESC")
    List<Video> findByUserIdOrUploaderIdOrderByCreatedAtDesc(@Param("id") Long id);

    @Query("SELECT v FROM Video v LEFT JOIN FETCH v.user LEFT JOIN FETCH v.uploader WHERE v.id = :id")
    Optional<Video> findByIdWithUploader(@Param("id") Long id);

    // Count methods
    long countByUserId(Long userId);
    long countByUploaderId(Long uploaderId);

    // Status and user combination queries
    List<Video> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    List<Video> findByUploaderIdAndStatusOrderByCreatedAtDesc(Long uploaderId, String status);
}
