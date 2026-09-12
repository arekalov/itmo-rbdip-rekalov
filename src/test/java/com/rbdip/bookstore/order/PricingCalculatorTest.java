package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Характеризационные тесты PricingCalculator (ЛР2): фиксируют фактическое
 * поведение всех веток скидок, граничные и отрицательные случаи перед
 * дальнейшим рефакторингом.
 */
class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    private static PricingCalculator.LineItem item(String price, int quantity) {
        return new PricingCalculator.LineItem(new BigDecimal(price), quantity);
    }

    @Test
    void sumsLinesWithoutAnyDiscounts() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(item("19.99", 2), item("5.00", 1)), "regular", null);

        assertThat(total).isEqualByComparingTo("44.98");
    }

    @Test
    void emptyOrderCostsZero() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(), "regular", null);

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void appliesBulkDiscountAboveTenUnits() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("10.00", 11)), "regular", null);

        assertThat(total).isEqualByComparingTo("104.50");
    }

    @Test
    void doesNotApplyBulkDiscountAtExactlyTenUnits() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("10.00", 10)), "regular", null);

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void bulkDiscountAppliesPerLineNotPerOrder() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(item("10.00", 11), item("5.00", 2)), "regular", null);

        assertThat(total).isEqualByComparingTo("114.50");
    }

    @Test
    void appliesVipDiscount() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "vip", null);

        assertThat(total).isEqualByComparingTo("90.00");
    }

    @Test
    void appliesWholesaleDiscount() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "wholesale", null);

        assertThat(total).isEqualByComparingTo("85.00");
    }

    @Test
    void unknownCustomerTypeGetsNoDiscount() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "gold", null);

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void customerTypeIsCaseSensitive() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "VIP", null);

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void nullCustomerTypeGetsNoDiscount() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), null, null);

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void save10CouponSubtractsTenAfterCustomerDiscount() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "vip", "SAVE10");

        assertThat(total).isEqualByComparingTo("80.00");
    }

    @Test
    void save20PercentCouponMultipliesTotal() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "regular", "SAVE20PERCENT");

        assertThat(total).isEqualByComparingTo("80.00");
    }

    @Test
    void unknownCouponIsIgnored() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("100.00", 1)), "regular", "SAVE30");

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void save10OnCheapOrderClampsTotalToZero() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("5.00", 1)), "regular", "SAVE10");

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void appliesLargeOrderDiscountAboveThousand() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("600.00", 2)), "regular", null);

        assertThat(total).isEqualByComparingTo("1176.00");
    }

    @Test
    void doesNotApplyLargeOrderDiscountAtExactlyThousand() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("500.00", 2)), "regular", null);

        assertThat(total).isEqualByComparingTo("1000.00");
    }

    @Test
    void largeOrderDiscountAppliesToTotalAfterCoupon() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(item("2000.00", 1)), "regular", "SAVE20PERCENT");

        assertThat(total).isEqualByComparingTo("1568.00");
    }

    @Test
    void combinesBulkVipAndCouponDiscounts() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("20.00", 11)), "vip", "SAVE10");

        assertThat(total).isEqualByComparingTo("178.10");
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("0.335", 1)), "regular", null);

        assertThat(total).isEqualByComparingTo("0.34");
    }

    @Test
    void totalHasScaleOfTwo() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(item("10.00", 1)), "regular", null);

        assertThat(total.scale()).isEqualTo(2);
    }
}
