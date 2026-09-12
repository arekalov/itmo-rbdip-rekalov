package com.rbdip.bookstore.order;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerRepository;
import com.rbdip.bookstore.customer.PersonName;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Сохранение заказа: клиент, заказ и его позиции. Единственная точка
 * записи заказа в БД.
 */
@Component
public class OrderPersister {

    private static final String NEW_ORDER_STATUS = "new";

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderPersister(
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order persistNewOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Customer customer = customerRepository.save(new Customer(
                PersonName.fromFullName(request.customerFullName()),
                request.customerAddress(),
                request.customerPhone()));
        Order order = orderRepository.save(new Order(customer, NEW_ORDER_STATUS));
        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(order.getId(), line.product(), line.quantity()));
        }
        return order;
    }
}
