package com.rbdip.bookstore.order;

import com.rbdip.bookstore.review.PurchaseVerifier;
import org.springframework.stereotype.Component;

/**
 * Реализация контракта {@link PurchaseVerifier} на стороне модуля
 * заказов: модуль review больше не обращается к репозиториям order
 * напрямую (ЛР4, Strangler Fig).
 */
@Component
public class OrderPurchaseVerifier implements PurchaseVerifier {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderPurchaseVerifier(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public boolean isVerifiedPurchase(Long productId) {
        // Сохраняем прежнюю "грубую" семантику проверки: есть ли вообще
        // заказы с позициями. Уточнение до конкретного товара - отдельное
        // продуктовое решение, не входящее в рефакторинг ЛР4.
        return orderRepository.count() > 0 && orderItemRepository.count() > 0;
    }
}
