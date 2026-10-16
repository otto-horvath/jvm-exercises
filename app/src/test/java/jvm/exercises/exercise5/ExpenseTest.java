package jvm.exercises.exercise5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExpenseTest {

    @Test
    void createsExpense() {
        Expense expense = new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("42.50"));

        assertEquals(LocalDate.of(2024, 1, 15), expense.date());
        assertEquals("Groceries", expense.category());
        assertEquals(new BigDecimal("42.50"), expense.amount());
    }

    @Test
    void rejectsNullDate() {
        assertThrows(NullPointerException.class, () -> new Expense(null, "Groceries", new BigDecimal("10.00")));
    }

    @Test
    void rejectsNullCategory() {
        assertThrows(
                NullPointerException.class,
                () -> new Expense(LocalDate.of(2024, 1, 15), null, new BigDecimal("10.00")));
    }

    @Test
    void rejectsBlankCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Expense(LocalDate.of(2024, 1, 15), "   ", new BigDecimal("10.00")));
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(NullPointerException.class, () -> new Expense(LocalDate.of(2024, 1, 15), "Groceries", null));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Expense(LocalDate.of(2024, 1, 15), "Groceries", BigDecimal.ZERO));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Expense(LocalDate.of(2024, 1, 15), "Groceries", new BigDecimal("-5.00")));
    }
}
