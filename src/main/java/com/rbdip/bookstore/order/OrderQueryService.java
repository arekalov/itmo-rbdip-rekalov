package com.rbdip.bookstore.order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Чтение заказов для API. Загружает данные фиксированным числом
 * запросов независимо от количества заказов (устранение N+1, ЛР4):
 * один запрос на заказы с клиентами и один - на все позиции с товарами.
 */
@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderQueryService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> listOrders() {
        List<Order> orders = orderRepository.findAllWithCustomers();
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrderId = orderItemRepository.findWithProductsByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream()
                .map(order -> toSummary(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    private OrderSummary toSummary(Order order, List<OrderItem> items) {
        List<OrderSummary.Item> itemSummaries = items.stream()
                .map(item -> new OrderSummary.Item(item.getProductName(), item.getQuantity()))
                .toList();
        return new OrderSummary(order.getId(), order.getCustomerFullName(), order.getStatus(), itemSummaries);
    }
}
