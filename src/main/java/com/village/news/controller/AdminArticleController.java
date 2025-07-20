package com.village.news.controller;

import com.village.news.entity.Article;
import com.village.news.entity.User;
import com.village.news.repository.UserRepository;
import com.village.news.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/articles")

public class AdminArticleController {

    private final ArticleService articleService;
    private final UserRepository userRepository;

    public AdminArticleController(ArticleService articleService, UserRepository userRepository) {
        this.articleService = articleService;
        this.userRepository = userRepository;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Article>> getPendingArticles(Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Article> pendingArticles = articleService.getPendingArticles();
            return ResponseEntity.ok(pendingArticles);
        } catch (Exception e) {
            System.err.println("Error fetching pending articles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{articleId}/approve")
    public ResponseEntity<?> approveArticle(@PathVariable Long articleId,
                                            Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin privileges required"));
        }

        try {
            // Get the actual admin user ID from authentication
            Long adminUserId = getUserIdFromAuthentication(authentication);

            System.out.println("Approving article " + articleId + " by admin user ID: " + adminUserId);

            articleService.approveArticle(articleId, adminUserId);
            return ResponseEntity.ok(Map.of(
                    "message", "Article approved successfully",
                    "articleId", articleId,
                    "approvedBy", adminUserId
            ));
        } catch (Exception e) {
            System.err.println("Article approval error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to approve article: " + e.getMessage()));
        }
    }

    @PostMapping("/{articleId}/reject")
    public ResponseEntity<?> rejectArticle(@PathVariable Long articleId,
                                           Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin privileges required"));
        }

        try {
            articleService.rejectArticle(articleId);
            return ResponseEntity.ok(Map.of(
                    "message", "Article rejected successfully",
                    "articleId", articleId
            ));
        } catch (Exception e) {
            System.err.println("Article rejection error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reject article: " + e.getMessage()));
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        try {
            String email = authentication.getName();
            System.out.println("Getting user ID for email: " + email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Admin user not found with email: " + email));

            System.out.println("Found admin user: ID=" + user.getId() + ", Name=" + user.getName() + ", Role=" + user.getRole());

            return user.getId();
        } catch (Exception e) {
            System.err.println("Failed to get user ID from authentication: " + e.getMessage());
            throw new RuntimeException("Failed to identify admin user: " + e.getMessage());
        }
    }
}
