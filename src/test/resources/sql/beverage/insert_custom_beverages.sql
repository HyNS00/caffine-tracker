-- 커스텀 음료 테스트 데이터 (사용자 1 소유)
INSERT INTO users (id, email, password, name, daily_caffeine_limit, caffeine_half_life, bed_time, target_sleep_caffeine, created_at, updated_at)
VALUES (1, 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '테스트유저', 400, 5.0, '23:00:00', 50.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO custom_beverages (id, user_id, name, category, volume_ml, caffeine_mg, created_at, updated_at)
VALUES (1, 1, '내 커피', 'AMERICANO', 300, 120.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO custom_beverages (id, user_id, name, category, volume_ml, caffeine_mg, created_at, updated_at)
VALUES (2, 1, '내 녹차', 'GREEN_TEA', 200, 30.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
