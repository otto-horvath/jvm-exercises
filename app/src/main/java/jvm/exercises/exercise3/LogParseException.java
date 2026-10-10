package jvm.exercises.exercise3;

public class LogParseException extends RuntimeException {
    private final String line;

    public LogParseException(String line, String reason) {
        super(reason);
        this.line = line;
    }

    public String line() {
        return line;
    }

    public String reason() {
        return getMessage();
    }
}
