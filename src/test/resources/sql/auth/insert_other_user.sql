-- 두 번째 테스트 사용자 (BCrypt 해시: "password123")
INSERT INTO users (id, email, password, name, daily_caffeine_limit, caffeine_half_life, bed_time, target_sleep_caffeine, created_at, updated_at)
VALUES (2, 'other@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '다른유저', 300, 4.0, '22:00:00', 30.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
