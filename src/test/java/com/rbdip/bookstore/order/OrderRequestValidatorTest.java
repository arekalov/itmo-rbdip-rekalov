package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderRequestValidatorTest {

    private final OrderRequestValidator validator = new OrderRequestValidator();

    private static CreateOrderRequest request(String fullName, String address, List<CreateOrderRequest.Item> items) {
        return new CreateOrderRequest(fullName, address, null, "regular", null, items);
    }

    private static List<CreateOrderRequest.Item> oneItem() {
        return List.of(new CreateOrderRequest.Item(1L, 1));
    }

    @Test
    void acceptsValidRequest() {
        assertThatCode(() -> validator.validate(request("Ivan Petrov", "Moscow", oneItem())))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNullCustomerName() {
        assertThatThrownBy(() -> validator.validate(request(null, "Moscow", oneItem())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void rejectsBlankCustomerName() {
        assertThatThrownBy(() -> validator.validate(request("   ", "Moscow", oneItem())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void rejectsNullAddress() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", null, oneItem())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerAddress is required");
    }

    @Test
    void rejectsBlankAddress() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", "", oneItem())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerAddress is required");
    }

    @Test
    void rejectsNullItems() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", "Moscow", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must contain at least one item");
    }

    @Test
    void rejectsEmptyItems() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", "Moscow", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must contain at least one item");
    }
}
