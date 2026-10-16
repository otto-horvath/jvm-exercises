package jvm.exercises.exercise5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ExpenseCsvParser {
    public List<Expense> parse(Path file) {
        Objects.requireNonNull(file, "file");

        try {
            List<String> lines = Files.readAllLines(file);
            List<Expense> expenses = new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.isBlank()) {
                    expenses.add(parseLine(line, i + 1));
                }
            }

            return List.copyOf(expenses);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Expense parseLine(String line, int lineNumber) {
        Objects.requireNonNull(line, "line");

        var fields = Arrays.stream(line.split(",", -1)).map(String::trim).toList();

        if (fields.size() != 3) {
            throw new ExpenseParseException(lineNumber, line, "expected date, category, and amount");
        }

        if (fields.get(1).isBlank()) {
            throw new ExpenseParseException(lineNumber, line, "category must not be blank");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(fields.get(0));
        } catch (DateTimeParseException e) {
            throw new ExpenseParseException(lineNumber, line, "invalid date");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(fields.get(2));
        } catch (NumberFormatException e) {
            throw new ExpenseParseException(lineNumber, line, "invalid amount");
        }

        try {
            return new Expense(date, fields.get(1), amount);
        } catch (IllegalArgumentException e) {
            throw new ExpenseParseException(lineNumber, line, "amount must be positive");
        }
    }
}
