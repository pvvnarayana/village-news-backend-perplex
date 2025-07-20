package com.village.news.service;

import com.village.news.entity.User;
import com.village.news.entity.Video;
import com.village.news.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by Google ID
     */
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    /**
     * Create a new user from Google OAuth data
     */
    public User createUserFromGoogle(String email, String name, String pictureUrl, String googleId) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProfilePictureUrl(pictureUrl);
        user.setGoogleId(googleId);
        user.setRole("USER"); // Default role
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update existing user with Google data if needed
     */
    public User updateUserFromGoogle(User existingUser, String name, String pictureUrl, String googleId) {
        boolean updated = false;

        // Update name if it's different
        if (name != null && !name.equals(existingUser.getName())) {
            existingUser.setName(name);
            updated = true;
        }

        // Update profile picture if it's different
        if (pictureUrl != null && !pictureUrl.equals(existingUser.getProfilePictureUrl())) {
            existingUser.setProfilePictureUrl(pictureUrl);
            updated = true;
        }

        // Update Google ID if it's not set
        if (googleId != null && existingUser.getGoogleId() == null) {
            existingUser.setGoogleId(googleId);
            updated = true;
        }

        if (updated) {
            existingUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(existingUser);
        }

        return existingUser;
    }

    /**
     * Find or create user from Google OAuth
     */
    public User findOrCreateUserFromGoogle(String email, String name, String pictureUrl, String googleId) {
        // First try to find by email
        Optional<User> userByEmail = findByEmail(email);
        if (userByEmail.isPresent()) {
            return updateUserFromGoogle(userByEmail.get(), name, pictureUrl, googleId);
        }

        // Then try to find by Google ID
        Optional<User> userByGoogleId = findByGoogleId(googleId);
        if (userByGoogleId.isPresent()) {
            return updateUserFromGoogle(userByGoogleId.get(), name, pictureUrl, googleId);
        }

        // Create new user if not found
        return createUserFromGoogle(email, name, pictureUrl, googleId);
    }

    /**
     * Update user profile information
     */
    public User updateProfile(String email, String name, String profilePictureUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }

        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) {
            user.setProfilePictureUrl(profilePictureUrl.trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Get user profile by email
     */
    public User getUserProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    /**
     * Get user's videos (using the bidirectional relationship)
     */
    public List<Video> getUserVideos(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // This will work because of the @OneToMany relationship
        return user.getVideos();
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Check if user exists by Google ID
     */
    public boolean existsByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId).isPresent();
    }

    /**
     * Promote user to admin (for admin management)
     */
    public User promoteToAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setRole("ADMIN");
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Demote admin to user
     */
    public User demoteToUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setRole("USER");
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Check if user has admin role
     */
    public boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(user -> "ADMIN".equals(user.getRole()))
                .orElse(false);
    }

    /**
     * Get all users (for admin purposes)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Delete user account (soft delete by deactivating)
     */
    public void deactivateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Instead of deleting, you might want to add an 'active' flag
        // For now, we'll just mark with a special role
        user.setRole("INACTIVE");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Save user entity
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }


}
