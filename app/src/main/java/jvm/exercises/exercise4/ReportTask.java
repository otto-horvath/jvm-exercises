package jvm.exercises.exercise4;

import java.util.Objects;
import java.util.concurrent.Callable;

public record ReportTask(String title, Callable<String> body) {
    public ReportTask {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(body, "body");
    }
}
