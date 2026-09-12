-- ЛР3, expand-шаг: разбиение customers.full_name на first_name/last_name
-- без даунтайма. Новые колонки добавляются рядом со старой, данные
-- переносятся (первое слово - имя, остаток - фамилия), NOT NULL со
-- старой колонки снимается, чтобы новая версия приложения могла писать
-- только first_name/last_name. Старую колонку удаляет contract-шаг (V4).

ALTER TABLE customers
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255);

UPDATE customers SET
    first_name = CASE WHEN position(' ' IN full_name) > 0
                      THEN substring(full_name FROM 1 FOR position(' ' IN full_name) - 1)
                      ELSE full_name END,
    last_name  = CASE WHEN position(' ' IN full_name) > 0
                      THEN substring(full_name FROM position(' ' IN full_name) + 1)
                      ELSE NULL END;

ALTER TABLE customers ALTER COLUMN full_name DROP NOT NULL;
