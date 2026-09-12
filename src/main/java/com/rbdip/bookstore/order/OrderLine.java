package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;

/**
 * Позиция заказа на этапе сборки: товар и количество (с уже применённым
 * значением по умолчанию).
 */
record OrderLine(Product product, int quantity) {
}
