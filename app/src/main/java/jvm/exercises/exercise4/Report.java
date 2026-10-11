package jvm.exercises.exercise4;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Report(List<ReportSection> sections) {
    public Report {
        Objects.requireNonNull(sections, "sections");
        sections = List.copyOf(sections);
    }

    public List<String> titles() {
        return sections.stream().map(ReportSection::title).toList();
    }

    public Optional<String> content(String title) {
        Objects.requireNonNull(title, "title");
        return sections.stream()
                .filter(section -> section.title().equals(title))
                .map(ReportSection::content)
                .findFirst();
    }
}
