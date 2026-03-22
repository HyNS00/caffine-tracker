-- 카페인 섭취 기록 테스트 데이터
INSERT INTO users (id, email, password, name, daily_caffeine_limit, caffeine_half_life, bed_time, target_sleep_caffeine, created_at, updated_at)
VALUES (1, 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '테스트유저', 400, 5.0, '23:00:00', 50.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO preset_beverages (id, name, brand_name, category, volume_ml, caffeine_mg)
VALUES (1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0);

INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (1, 1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0, CURRENT_TIMESTAMP, 'PRESET', 1);

INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (2, 1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0, DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 'PRESET', 1);
