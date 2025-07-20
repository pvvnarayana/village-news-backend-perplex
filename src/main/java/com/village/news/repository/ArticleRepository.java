package com.village.news.repository;

import com.village.news.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByStatus(String status);
    List<Article> findByStatusOrderByCreatedAtDesc(String status);

    List<Article> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    // Custom queries with JOIN FETCH to load author information
    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.user WHERE a.status = :status ORDER BY a.createdAt DESC")
    List<Article> findByStatusWithAuthorOrderByCreatedAtDesc(@Param("status") String status);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.user WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<Article> findByUserIdWithUserOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.user WHERE a.authorId = :authorId ORDER BY a.createdAt DESC")
    List<Article> findByAuthorIdWithAuthorOrderByCreatedAtDesc(@Param("authorId") Long authorId);

    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.user WHERE a.id = :id")
    Optional<Article> findByIdWithAuthor(@Param("id") Long id);
}
