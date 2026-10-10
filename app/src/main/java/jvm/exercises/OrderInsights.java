package jvm.exercises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class OrderInsights {
    private static final Comparator<ProductSales> RANKING =
            Comparator.comparingInt(ProductSales::unitsSold).reversed()
                    .thenComparing(ProductSales::product);

    public Map<String, BigDecimal> revenueByCustomer(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                        Order::customer,
                        Collectors.mapping(
                                Order::revenue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    public List<ProductSales> topProducts(List<Order> orders, int limit) {
        return orders.stream()
                .flatMap(order -> order.lines().stream())
                .collect(Collectors.groupingBy(
                        OrderLine::product,
                        Collectors.summingInt(OrderLine::quantity)))
                .entrySet().stream()
                .map(entry -> new ProductSales(entry.getKey(), entry.getValue()))
                .sorted(RANKING)
                .limit(limit)
                .toList();
        }

    public BigDecimal averageOrderValue(List<Order> orders) {
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = orders.stream()
                .map(Order::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
        }
}
