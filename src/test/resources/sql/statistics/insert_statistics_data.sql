-- 통계 테스트용 데이터 (사용자 1, 프리셋 음료 2종, 최근 7일간 섭취 기록)
INSERT INTO users (id, email, password, name, daily_caffeine_limit, caffeine_half_life, bed_time, target_sleep_caffeine, created_at, updated_at)
VALUES (1, 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '테스트유저', 400, 5.0, '23:00:00', 50.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO preset_beverages (id, name, brand_name, category, volume_ml, caffeine_mg)
VALUES (1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0);

INSERT INTO preset_beverages (id, name, brand_name, category, volume_ml, caffeine_mg)
VALUES (2, '카페라떼', '스타벅스', 'LATTE', 355, 75.0);

-- 오늘 섭취 (2건)
INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (1, 1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0, CURRENT_TIMESTAMP, 'PRESET', 1);

INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (2, 1, '카페라떼', '스타벅스', 'LATTE', 355, 75.0, DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'PRESET', 2);

-- 어제 섭취 (1건)
INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (3, 1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'PRESET', 1);

-- 2일 전 섭취 (2건)
INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (4, 1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'PRESET', 1);

INSERT INTO caffeine_intakes (id, user_id, beverage_name, brand_name, category, volume_ml, caffeine_mg, consumed_at, source_type, source_beverage_id)
VALUES (5, 1, '아메리카노', '스타벅스', 'AMERICANO', 355, 150.0, DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'PRESET', 1);
