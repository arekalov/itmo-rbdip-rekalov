package com.rbdip.bookstore.order;

import org.springframework.stereotype.Component;

/**
 * Валидация входящего запроса на создание заказа. Проверки уровня
 * позиций (существование товара, положительное количество) выполняются
 * при сборке позиций в {@link OrderService}, чтобы сохранить порядок
 * ошибок исходного поведения.
 */
@Component
public class OrderRequestValidator {

    public void validate(CreateOrderRequest request) {
        requireNonBlank(request.customerFullName(), "customerFullName is required");
        requireNonBlank(request.customerAddress(), "customerAddress is required");
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
