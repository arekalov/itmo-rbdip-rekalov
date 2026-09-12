-- ЛР2, expand-шаг нормализации: отдельная таблица customers и ссылки
-- orders -> customers, order_items -> products. Старые денормализованные
-- колонки пока сохраняются - их удаляет отдельный contract-шаг (V3),
-- чтобы накат этой миграции не ломал работающих потребителей старых
-- колонок (см. LoadDuringMigrationReferenceTest, ЛР3).

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50)
);

ALTER TABLE orders ADD COLUMN customer_id BIGINT REFERENCES customers(id);

-- Backfill: по одной строке customers на уникальную комбинацию контактных
-- данных из существующих заказов.
INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT customer_full_name, customer_address, customer_phone
FROM orders;

UPDATE orders o
SET customer_id = c.id
FROM customers c
WHERE o.customer_full_name = c.full_name
  AND o.customer_address IS NOT DISTINCT FROM c.address
  AND o.customer_phone IS NOT DISTINCT FROM c.phone;

ALTER TABLE order_items ADD COLUMN product_id BIGINT REFERENCES products(id);

-- Backfill: связываем позиции с товарами по имени (при дублях имён
-- берём товар с минимальным id).
UPDATE order_items oi
SET product_id = p.id
FROM (SELECT MIN(id) AS id, name FROM products GROUP BY name) p
WHERE oi.product_name = p.name;
