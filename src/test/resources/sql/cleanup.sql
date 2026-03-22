-- 테스트 데이터 정리 (매 테스트 전 실행)
SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE favorite_beverages;
TRUNCATE TABLE caffeine_intakes;
TRUNCATE TABLE custom_beverages;
TRUNCATE TABLE preset_beverages;
TRUNCATE TABLE users;

SET REFERENTIAL_INTEGRITY TRUE;
