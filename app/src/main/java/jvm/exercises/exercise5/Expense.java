package jvm.exercises.exercise5;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Expense(LocalDate date, String category, BigDecimal amount) {
    public Expense {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(amount, "amount");

        if (category.isBlank()) {
            throw new IllegalArgumentException();
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException();
        }
    }
}
