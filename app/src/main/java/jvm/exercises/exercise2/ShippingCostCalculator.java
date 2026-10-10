package jvm.exercises.exercise2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class ShippingCostCalculator {
    private static final BigDecimal STANDARD_RATE_PER_KG = new BigDecimal("4.375");
    private static final BigDecimal EXPRESS_RATE_PER_KG = new BigDecimal("7.25");
    private static final BigDecimal INTERNATIONAL_RATE_PER_KG = new BigDecimal("13");

    public BigDecimal calculate(Shipment shipment) {
        Objects.requireNonNull(shipment, "shipment");
        return switch (shipment) {
            case StandardShipment standard -> cost(standard.weight(), STANDARD_RATE_PER_KG);
            case ExpressShipment express -> cost(express.weight(), EXPRESS_RATE_PER_KG);
            case InternationalShipment international ->
                cost(
                        international.weight(),
                        INTERNATIONAL_RATE_PER_KG.multiply(
                                international.region().surcharge()));
        };
    }

    private static BigDecimal cost(WeightKg weight, BigDecimal ratePerKg) {
        return BigDecimal.valueOf(weight.value()).multiply(ratePerKg).setScale(2, RoundingMode.HALF_UP);
    }
}
