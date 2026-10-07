package jvm.exercises;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderLine(String product, int quantity, BigDecimal unitPrice) {
    public OrderLine {
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(unitPrice, "unitPrice");
    }

    public BigDecimal revenue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
