package jvm.exercises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OrderInsights {
    public Map<String, BigDecimal> revenueByCustomer(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::customer,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                order -> order.lines().stream()
                                        .map(OrderLine::revenue)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                BigDecimal::add)));
    }

    public List<ProductSales> topProducts(List<Order> orders, int limit) {
        return orders.stream()
                .flatMap(order -> order.lines().stream())
                .collect(Collectors.groupingBy(
                        OrderLine::product,
                        Collectors.summingInt(OrderLine::quantity)))
                .entrySet().stream()
                .map(entry -> new ProductSales(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(ProductSales::unitsSold)
                        .reversed()
                        .thenComparing(ProductSales::product))
                .limit(limit)
                .toList();
        }

    public BigDecimal averageOrderValue(List<Order> orders) {
        return orders.stream()
                .map(Order::revenue)
                .reduce(BigDecimal::add)
                .map(total -> total.divide(
                        BigDecimal.valueOf(orders.size()),
                        RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
        }
}
