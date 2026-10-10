package jvm.exercises.exercise1;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderLine(String product, int quantity, BigDecimal unitPrice) {
    public OrderLine {
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public BigDecimal revenue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
