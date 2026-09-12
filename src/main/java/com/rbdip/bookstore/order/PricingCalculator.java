package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Модуль расчёта итоговой цены заказа: скидка за объём по позиции,
 * скидка по типу клиента, купоны и скидка на крупный заказ.
 */
@Component
public class PricingCalculator {

    private static final int BULK_QUANTITY_THRESHOLD = 10;
    private static final BigDecimal BULK_LINE_DISCOUNT_MULTIPLIER = new BigDecimal("0.95");

    private static final String VIP_CUSTOMER_TYPE = "vip";
    private static final BigDecimal VIP_DISCOUNT_MULTIPLIER = new BigDecimal("0.9");
    private static final String WHOLESALE_CUSTOMER_TYPE = "wholesale";
    private static final BigDecimal WHOLESALE_DISCOUNT_MULTIPLIER = new BigDecimal("0.85");

    private static final String FLAT_COUPON_CODE = "SAVE10";
    private static final BigDecimal FLAT_COUPON_AMOUNT = BigDecimal.TEN;
    private static final String PERCENT_COUPON_CODE = "SAVE20PERCENT";
    private static final BigDecimal PERCENT_COUPON_MULTIPLIER = new BigDecimal("0.8");

    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal LARGE_ORDER_DISCOUNT_MULTIPLIER = new BigDecimal("0.98");

    private static final int MONEY_SCALE = 2;

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = itemsSubtotal(items);
        total = applyCustomerTypeDiscount(total, customerType);
        total = applyCoupon(total, couponCode);
        total = clampToZero(total);
        total = applyLargeOrderDiscount(total);
        return total.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal itemsSubtotal(List<LineItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineItem item : items) {
            subtotal = subtotal.add(lineTotal(item));
        }
        return subtotal;
    }

    private BigDecimal lineTotal(LineItem item) {
        BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
        if (item.quantity() > BULK_QUANTITY_THRESHOLD) {
            linePrice = linePrice.multiply(BULK_LINE_DISCOUNT_MULTIPLIER);
        }
        return linePrice;
    }

    private BigDecimal applyCustomerTypeDiscount(BigDecimal total, String customerType) {
        if (VIP_CUSTOMER_TYPE.equals(customerType)) {
            return total.multiply(VIP_DISCOUNT_MULTIPLIER);
        }
        if (WHOLESALE_CUSTOMER_TYPE.equals(customerType)) {
            return total.multiply(WHOLESALE_DISCOUNT_MULTIPLIER);
        }
        return total;
    }

    private BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        if (FLAT_COUPON_CODE.equals(couponCode)) {
            return total.subtract(FLAT_COUPON_AMOUNT);
        }
        if (PERCENT_COUPON_CODE.equals(couponCode)) {
            return total.multiply(PERCENT_COUPON_MULTIPLIER);
        }
        return total;
    }

    private BigDecimal clampToZero(BigDecimal total) {
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    private BigDecimal applyLargeOrderDiscount(BigDecimal total) {
        if (total.compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return total.multiply(LARGE_ORDER_DISCOUNT_MULTIPLIER);
        }
        return total;
    }
}
