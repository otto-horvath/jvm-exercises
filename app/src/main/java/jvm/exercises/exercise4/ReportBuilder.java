package jvm.exercises.exercise4;

import java.util.List;

public final class ReportBuilder implements AutoCloseable {
    public ReportBuilder() {
        throw new UnsupportedOperationException("ReportBuilder not implemented");
    }

    public Report build(List<ReportTask> tasks) {
        throw new UnsupportedOperationException("build not implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("close not implemented");
    }
}
