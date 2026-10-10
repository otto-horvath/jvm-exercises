package jvm.exercises.exercise2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ShippingCostCalculatorTest {
    private final ShippingCostCalculator calculator = new ShippingCostCalculator();

    @Test
    void calculatesStandardShippingCost() {
        Shipment standard = new StandardShipment("Berlin", new WeightKg(4.0));

        assertEquals(new BigDecimal("17.50"), calculator.calculate(standard));
    }

    @Test
    void calculatesExpressShippingCost() {
        Shipment express = new ExpressShipment("Paris", new WeightKg(4.0));

        assertEquals(new BigDecimal("29.00"), calculator.calculate(express));
    }

    @Test
    void calculatesInternationalShippingCostWithRegionalSurcharge() {
        Shipment international = new InternationalShipment("Tokyo", new WeightKg(4.0), "remote");

        assertEquals(new BigDecimal("52.00"), calculator.calculate(international));
    }

    @Test
    void rejectsNonPositiveOrNonFiniteWeights() {
        assertThrows(IllegalArgumentException.class, () -> new WeightKg(0.0));
        assertThrows(IllegalArgumentException.class, () -> new WeightKg(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new WeightKg(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new WeightKg(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new WeightKg(Double.NEGATIVE_INFINITY));
    }
}
