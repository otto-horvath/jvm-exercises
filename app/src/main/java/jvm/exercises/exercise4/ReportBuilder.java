package jvm.exercises.exercise4;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ReportBuilder implements AutoCloseable {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public Report build(List<ReportTask> tasks) {
        Objects.requireNonNull(tasks, "tasks");
        if (executor.isShutdown()) {
            throw new IllegalStateException("ReportBuilder is closed");
        }

        List<Pending> pending = tasks.stream()
                .map(task -> new Pending(task.title(), executor.submit(task.body())))
                .toList();

        List<ReportSection> sections = new ArrayList<>(pending.size());
        for (int i = 0; i < pending.size(); i++) {
            Pending current = pending.get(i);
            try {
                sections.add(new ReportSection(current.title(), current.future().get()));
            } catch (ExecutionException e) {
                cancelRemaining(pending, i);
                throw new ReportBuildException(current.title(), e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancelRemaining(pending, i);
                throw new ReportBuildException(current.title(), e);
            }
        }

        return new Report(sections);
    }

    @Override
    public void close() {
        executor.close();
    }

    private static void cancelRemaining(List<Pending> pending, int fromIndex) {
        for (int i = fromIndex + 1; i < pending.size(); i++) {
            pending.get(i).future().cancel(true);
        }
    }

    private record Pending(String title, Future<String> future) {}
}
