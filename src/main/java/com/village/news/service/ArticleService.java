package com.village.news.service;

import com.village.news.entity.Article;
import com.village.news.entity.User;
import com.village.news.repository.ArticleRepository;
import com.village.news.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final  UserService userService;

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository, UserService userService) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<Article> getApprovedArticles() {
        // Use custom query to fetch articles with author information
        try {
            return articleRepository.findByStatusWithAuthorOrderByCreatedAtDesc("APPROVED");
        } catch (Exception e) {
            System.err.println("Error fetching approved articles with author info: " + e.getMessage());
            // Fallback to simple query
            return articleRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
        }
    }

    public List<Article> getPendingArticles() {
        try {
            return articleRepository.findByStatusWithAuthorOrderByCreatedAtDesc("PENDING");
        } catch (Exception e) {
            System.err.println("Error fetching pending articles with author info: " + e.getMessage());
            return articleRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        }
    }

    public List<Article> getArticlesByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            return articleRepository.findByUserIdWithUserOrderByCreatedAtDesc(user.getId());
        } catch (Exception e) {
            System.err.println("Error fetching user articles with author info: " + e.getMessage());
            return articleRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }
    }

    public List<Article> getArticlesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        try {
            return articleRepository.findByUserIdWithUserOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            System.err.println("Error fetching articles by user ID with author info: " + e.getMessage());
            return articleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
    }

    public Article createArticleByUserId(String title, String content, Long userId) {
        try {
            User author = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            if (title == null || title.trim().isEmpty()) {
                throw new RuntimeException("Article title is required");
            }

            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("Article content is required");
            }

            Article article = new Article();
            article.setTitle(title.trim());
            article.setContent(content.trim());
            article.setUserId(userId);
            article.setAuthorId(userId);
            article.setStatus("PENDING");

            Article savedArticle = articleRepository.save(article);

            // Fetch the saved article with author information
            return articleRepository.findByIdWithAuthor(savedArticle.getId())
                    .orElse(savedArticle);

        } catch (Exception e) {
            System.err.println("Article creation failed: " + e.getMessage());
            throw new RuntimeException("Article creation failed: " + e.getMessage(), e);
        }
    }

    public void approveArticle(Long articleId, Long approverId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        article.approve(approverId);
        articleRepository.save(article);
    }

    public void rejectArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        article.reject();
        articleRepository.save(article);
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    // NEW: Delete all articles by user ID
    public void deleteByUserId(Long userId, String requesterEmail) {
        User requester = userService.findByEmail(requesterEmail).get();
        boolean isAdmin = requester.getRole() == "ADMIN";

        // Only admin can delete all articles of a user
        if (!isAdmin) {
            throw new RuntimeException("Only admin can delete all user articles");
        }

        List<Article> userArticles = articleRepository.findByUserIdOrderByCreatedAtDesc(userId);
        articleRepository.deleteAll(userArticles);
    }

    // NEW: Delete own articles (for user)
    public void deleteMyArticles(String userEmail) {
        User user = userRepository.findByEmail(userEmail).get();
        List<Article> userArticles = articleRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        articleRepository.deleteAll(userArticles);
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public List<Article> getDrafts() {
        return articleRepository.findByStatusOrderByCreatedAtDesc("DRAFT");
    }

    public void updateStatus(Long id, String status, String adminEmail) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new RuntimeException("Article not found"));
        article.setStatus(status.toUpperCase());
        article.setUpdatedAt(LocalDateTime.now());
        articleRepository.save(article);
    }

    public Article updateContent(Long id, String status, String content, String adminEmail) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new RuntimeException("Article not found"));
        article.setStatus(status.toUpperCase());
        article.setUpdatedAt(LocalDateTime.now());
        article.setContent(content);
        return articleRepository.save(article);
    }

    public void toggleFeature(Long id, boolean featured, String adminEmail) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new RuntimeException("Article not found"));
        // Assuming you add a 'featured' boolean field to Article entity
        // article.setFeatured(featured);
        article.setUpdatedAt(LocalDateTime.now());
        articleRepository.save(article);
    }

}
