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

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
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
}
