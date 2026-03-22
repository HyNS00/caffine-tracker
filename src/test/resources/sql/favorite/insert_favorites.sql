-- 즐겨찾기 테스트 데이터
INSERT INTO users (id, email, password, name, daily_caffeine_limit, caffeine_half_life, bed_time, target_sleep_caffeine, created_at, updated_at)
VALUES (1, 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '테스트유저', 400, 5.0, '23:00:00', 50.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO preset_beverages (id, name, brand_name, category, volume_ml, caffeine_mg)
VALUES (1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0);

INSERT INTO preset_beverages (id, name, brand_name, category, volume_ml, caffeine_mg)
VALUES (2, '카페라떼', '스타벅스', 'LATTE', 355, 75.0);

INSERT INTO favorite_beverages (id, user_id, preset_beverage_id, custom_beverage_id, display_order, created_at)
VALUES (1, 1, 1, NULL, 1, CURRENT_TIMESTAMP);

INSERT INTO favorite_beverages (id, user_id, preset_beverage_id, custom_beverage_id, display_order, created_at)
VALUES (2, 1, 2, NULL, 2, CURRENT_TIMESTAMP);
