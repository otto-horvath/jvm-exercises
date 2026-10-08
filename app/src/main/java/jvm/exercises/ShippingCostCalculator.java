package jvm.exercises;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ShippingCostCalculator {
    private static final BigDecimal STANDARD_RATE_PER_KG = new BigDecimal("4.375");
    private static final BigDecimal EXPRESS_RATE_PER_KG = new BigDecimal("7.25");
    private static final BigDecimal INTERNATIONAL_RATE_PER_KG = new BigDecimal("13");

    public BigDecimal calculate(Shipment shipment) {
        return switch (shipment) {
            case StandardShipment standard -> BigDecimal.valueOf(standard.weight().value())
                    .multiply(STANDARD_RATE_PER_KG)
                    .setScale(2, RoundingMode.HALF_UP);
            case ExpressShipment express -> BigDecimal.valueOf(express.weight().value())
                    .multiply(EXPRESS_RATE_PER_KG)
                    .setScale(2, RoundingMode.HALF_UP);
            case InternationalShipment international -> BigDecimal.valueOf(international.weight().value())
                    .multiply(INTERNATIONAL_RATE_PER_KG)
                    .setScale(2, RoundingMode.HALF_UP);
        };
    }
}
