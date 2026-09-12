-- ЛР2, contract-шаг нормализации: удаление денормализованных колонок.
-- По конвенции курса (см. LoadDuringMigrationReferenceTest) удаление
-- старых колонок - последняя миграция цепочки.

ALTER TABLE orders
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;

ALTER TABLE order_items
    DROP COLUMN product_name,
    DROP COLUMN product_price;
