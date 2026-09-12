-- Contract-шаг (ЛР2 + ЛР3): удаление всех старых денормализованных
-- колонок. По конвенции курса (см. LoadDuringMigrationReferenceTest)
-- удаление старых колонок - строго последняя миграция цепочки.

ALTER TABLE orders
    DROP COLUMN customer_full_name,
    DROP COLUMN customer_address,
    DROP COLUMN customer_phone;

ALTER TABLE order_items
    DROP COLUMN product_name,
    DROP COLUMN product_price;

ALTER TABLE customers
    DROP COLUMN full_name;
