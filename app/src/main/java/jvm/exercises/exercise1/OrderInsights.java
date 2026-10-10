package jvm.exercises.exercise1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class OrderInsights {
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private static final Comparator<ProductSales> RANKING =
            Comparator.comparingInt(ProductSales::unitsSold).reversed().thenComparing(ProductSales::product);

    public Map<String, BigDecimal> revenueByCustomer(List<Order> orders) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::customer,
                        Collectors.mapping(Order::revenue, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    public List<ProductSales> topProducts(List<Order> orders, int limit) {
        Objects.requireNonNull(orders, "orders");
        return orders.stream()
                .flatMap(order -> order.lines().stream())
                .collect(Collectors.groupingBy(OrderLine::product, Collectors.summingInt(OrderLine::quantity)))
                .entrySet()
                .stream()
                .map(entry -> new ProductSales(entry.getKey(), entry.getValue()))
                .sorted(RANKING)
                .limit(limit)
                .toList();
    }

    public BigDecimal averageOrderValue(List<Order> orders) {
        Objects.requireNonNull(orders, "orders");
        if (orders.isEmpty()) {
            return ZERO_MONEY;
        }

        BigDecimal total = orders.stream().map(Order::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
    }
}
