package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Оркестратор создания заказа: валидация запроса, сборка позиций,
 * расчёт цены, сохранение и уведомление клиента делегируются
 * классам с одной ответственностью.
 */
@Service
public class OrderService {

    private static final String NEW_ORDER_STATUS = "new";
    private static final String DEFAULT_CUSTOMER_TYPE = "regular";
    private static final int DEFAULT_QUANTITY = 1;

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRequestValidator orderRequestValidator;
    private final PricingCalculator pricingCalculator;
    private final OrderConfirmationNotifier orderConfirmationNotifier;

    public OrderService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderRequestValidator orderRequestValidator,
            PricingCalculator pricingCalculator,
            OrderConfirmationNotifier orderConfirmationNotifier) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRequestValidator = orderRequestValidator;
        this.pricingCalculator = pricingCalculator;
        this.orderConfirmationNotifier = orderConfirmationNotifier;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        orderRequestValidator.validate(request);

        List<OrderLine> lines = resolveOrderLines(request.items());
        BigDecimal total = pricingCalculator.calculateOrderTotal(
                toLineItems(lines), customerTypeOrDefault(request), request.couponCode());

        Order order = persistOrder(request, lines);
        orderConfirmationNotifier.sendConfirmation(request.customerFullName(), order.getId(), total);

        return order;
    }

    private List<OrderLine> resolveOrderLines(List<CreateOrderRequest.Item> items) {
        List<OrderLine> lines = new ArrayList<>();
        for (CreateOrderRequest.Item item : items) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("product " + item.productId() + " not found"));
            int quantity = item.quantity() == null ? DEFAULT_QUANTITY : item.quantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            lines.add(new OrderLine(product, quantity));
        }
        return lines;
    }

    private List<PricingCalculator.LineItem> toLineItems(List<OrderLine> lines) {
        return lines.stream()
                .map(line -> new PricingCalculator.LineItem(line.product().getPrice(), line.quantity()))
                .toList();
    }

    private String customerTypeOrDefault(CreateOrderRequest request) {
        return request.customerType() == null ? DEFAULT_CUSTOMER_TYPE : request.customerType();
    }

    private Order persistOrder(CreateOrderRequest request, List<OrderLine> lines) {
        Order order = orderRepository.save(new Order(
                request.customerFullName(), request.customerAddress(), request.customerPhone(), NEW_ORDER_STATUS));
        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(
                    order.getId(), line.product().getName(), line.product().getPrice(), line.quantity()));
        }
        return order;
    }

    private record OrderLine(Product product, int quantity) {
    }
}
