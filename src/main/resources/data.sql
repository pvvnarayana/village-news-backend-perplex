-- Sample users (admin and regular users)
INSERT INTO users (google_id, email, name, profile_picture_url, role) VALUES
('google_admin_123', 'admin@village.com', 'Village Admin', 'https://example.com/admin.jpg', 'ADMIN'),
('google_user_456', 'john.doe@village.com', 'John Doe', 'https://example.com/john.jpg', 'USER'),
('google_user_789', 'jane.smith@village.com', 'Jane Smith', 'https://example.com/jane.jpg', 'USER'),
('google_user_012', 'mike.wilson@village.com', 'Mike Wilson', 'https://example.com/mike.jpg', 'USER');

-- Sample articles
INSERT INTO articles (title, content, status, user_id) VALUES
('Village Festival Announcement', 
 'We are excited to announce the annual village festival will be held next month. Join us for food, music, and community celebrations!', 
 'PUBLISHED', 2),
('Road Maintenance Update', 
 'The main road through the village will undergo maintenance from Monday to Friday next week. Please use alternative routes.', 
 'PUBLISHED', 2),
('New Community Garden Project', 
 'We are starting a new community garden project behind the village hall. Volunteers are welcome to participate in this green initiative.', 
 'PUBLISHED', 3),
('Draft Article', 
 'This is a draft article that has not been published yet.', 
 'DRAFT', 3);

-- Sample videos (with different statuses)
INSERT INTO videos (title, description, file_path, original_file_name, file_size, mime_type, status, user_id) VALUES
('Village Market Day', 
 'Weekly market day at the village square with local vendors and fresh produce.', 
 'uuid123_market_day.mp4', 'market_day.mp4', 15728640, 'video/mp4', 'PENDING', 2),
('School Children Performance', 
 'Local school children performing traditional dance at the community center.', 
 'uuid456_school_dance.mp4', 'school_dance.mp4', 23456789, 'video/mp4', 'PENDING', 3),
('Village Council Meeting', 
 'Monthly village council meeting discussing community issues and development plans.', 
 'uuid789_council_meeting.mp4', 'council_meeting.mp4', 45678901, 'video/mp4', 'PENDING', 4);

-- Approve one video
UPDATE videos 
SET status = 'APPROVED', 
    approved_at = CURRENT_TIMESTAMP, 
    approved_by = 1 
WHERE id = 1;
