package jvm.exercises.exercise1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderLineTest {
    @Test
    void calculatesRevenueAsUnitPriceTimesQuantity() {
        OrderLine line = new OrderLine("Coffee", 3, new BigDecimal("2.50"));

        assertEquals(new BigDecimal("7.50"), line.revenue());
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLine("Coffee", 0, new BigDecimal("2.50")));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLine("Coffee", -1, new BigDecimal("2.50")));
    }
}
