package com.village.news.controller;

import com.village.news.entity.Article;
import com.village.news.service.ArticleService;
import com.village.news.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")

public class ArticleController {

    private final ArticleService articleService;
    private final JwtService jwtService;

    public ArticleController(ArticleService articleService, JwtService jwtService) {
        this.articleService = articleService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<Article>> approvedArticles() {
        return ResponseEntity.ok(articleService.getApprovedArticles());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createArticle(@RequestBody Map<String, Object> request,
                                           Authentication authentication) {

        // Check authentication
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        // Extract data from request
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        Object userIdObj = request.get("userId");

        // Debug logging
        System.out.println("Received article creation request:");
        System.out.println("Title: " + title);
        System.out.println("Content length: " + (content != null ? content.length() : 0));
        System.out.println("User ID: " + userIdObj);
        System.out.println("Authenticated user: " + authentication.getName());

        // Validate required fields
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Article title is required"));
        }

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Article content is required"));
        }

        if (userIdObj == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required"));
        }

        try {
            Long userId = Long.valueOf(userIdObj.toString());
            Article article = articleService.createArticleByUserId(title, content, userId);

            return ResponseEntity.ok(Map.of(
                    "message", "Article created successfully",
                    "articleId", article.getId(),
                    "status", article.getStatus()
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid user ID format"));
        } catch (Exception e) {
            System.err.println("Article creation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Article creation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Article>> myArticles(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<Article> userArticles = articleService.getArticlesByUser(authentication.getName());
            return ResponseEntity.ok(userArticles);
        } catch (Exception e) {
            System.err.println("Error fetching user articles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my/{userId}")
    public ResponseEntity<List<Article>> articlesByUserId(@PathVariable Long userId,
                                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<Article> userArticles = articleService.getArticlesByUserId(userId);
            return ResponseEntity.ok(userArticles);
        } catch (Exception e) {
            System.err.println("Error fetching articles by user ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Article update(@RequestHeader("Authorization") String hdr,
                          @PathVariable Long id,
                          @RequestParam String title,
                          @RequestParam String content) {
        String email = jwtService.getEmailFromToken(hdr.substring(7));
        return articleService.updateContent(id, title, content, email);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("Authorization") String hdr,
                                                      @PathVariable Long id) {
        try {
            String email = jwtService.getEmailFromToken(hdr.substring(7));
            articleService.deleteArticle(id);
            return ResponseEntity.ok(Map.of("message", "Article deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Delete all my articles (for current user)
    @DeleteMapping("/my/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMyArticles(@RequestHeader("Authorization") String hdr) {
        try {
            String email = jwtService.getEmailFromToken(hdr.substring(7));
            articleService.deleteMyArticles(email);
            return ResponseEntity.ok(Map.of("message", "All your articles deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Delete all articles by user ID (admin only)
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserArticles(@RequestHeader("Authorization") String hdr,
                                                                  @PathVariable Long userId) {
        try {
            String email = jwtService.getEmailFromToken(hdr.substring(7));
            articleService.deleteByUserId(userId, email);
            return ResponseEntity.ok(Map.of("message", "All user articles deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
