package jvm.exercises.exercise5;

public class ExpenseParseException extends RuntimeException {
    private final int lineNumber;
    private final String line;

    public ExpenseParseException(int lineNumber, String line, String reason) {
        super(reason + " (line " + lineNumber + ")");
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public int lineNumber() {
        return lineNumber;
    }

    public String line() {
        return line;
    }
}
