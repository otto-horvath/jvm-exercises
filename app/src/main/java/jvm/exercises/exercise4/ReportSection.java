package jvm.exercises.exercise4;

import java.util.Objects;

public record ReportSection(String title, String content) {
    public ReportSection {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
    }
}
