package jvm.exercises.exercise1;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderInsightsTest {
    private final OrderInsights insights = new OrderInsights();
    private final List<Order> orders = List.of(
            new Order("Ava", List.of(
                    new OrderLine("Coffee", 2, money("3.50")),
                    new OrderLine("Tea", 1, money("2.00")))),
            new Order("Ava", List.of(
                    new OrderLine("Tea", 1, money("3.00")))),
            new Order("Ben", List.of(
                    new OrderLine("Coffee", 2, money("3.50")),
                    new OrderLine("Cake", 2, money("2.50")))));

    @Test
    void totalsRevenuePerCustomerAcrossOrders() {
        assertEquals(Map.of("Ava", money("12.00"), "Ben", money("12.00")),
                insights.revenueByCustomer(orders));
    }

    @Test
    void ranksProductsByUnitsSoldAndUsesNameToBreakTies() {
        assertEquals(List.of(
                        new ProductSales("Coffee", 4),
                        new ProductSales("Cake", 2)),
                insights.topProducts(orders, 2));
    }

    @Test
    void calculatesAverageOrderValue() {
        assertEquals(money("8.00"), insights.averageOrderValue(orders));
    }

    @Test
    void roundsAverageToTwoDecimalPlaces() {
        List<Order> wholeUnitPrices = List.of(
                new Order("Ava", List.of(new OrderLine("Coffee", 1, money("10")))),
                new Order("Ava", List.of(new OrderLine("Coffee", 1, money("10")))),
                new Order("Ava", List.of(new OrderLine("Coffee", 1, money("11")))));

        assertEquals(money("10.33"), insights.averageOrderValue(wholeUnitPrices));
    }

    @Test
    void returnsEmptyResultsForNoOrders() {
        assertEquals(Map.of(), insights.revenueByCustomer(List.of()));
        assertEquals(List.of(), insights.topProducts(List.of(), 3));
        assertEquals(BigDecimal.ZERO, insights.averageOrderValue(List.of()));
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }
}
