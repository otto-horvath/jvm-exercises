package jvm.exercises.exercise5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExpenseParseExceptionTest {

    @Test
    void carriesLineNumberAndLine() {
        ExpenseParseException exception = new ExpenseParseException(7, "2024-01-01,,10.00", "blank category");

        assertEquals(7, exception.lineNumber());
        assertEquals("2024-01-01,,10.00", exception.line());
        assertTrue(exception.getMessage().contains("blank category"));
        assertTrue(exception.getMessage().contains("line 7"));
    }
}
