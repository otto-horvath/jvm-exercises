package jvm.exercises.exercise2;

import java.math.BigDecimal;

public enum ShippingRegion {
    STANDARD(BigDecimal.ONE),
    REMOTE(new BigDecimal("1.25"));

    private final BigDecimal surcharge;

    ShippingRegion(BigDecimal surcharge) {
        this.surcharge = surcharge;
    }

    public BigDecimal surcharge() {
        return surcharge;
    }
}
