package jvm.exercises.exercise4;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ReportBuilder implements AutoCloseable {
    private ExecutorService executor;

    public ReportBuilder() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public Report build(List<ReportTask> tasks) {
        if(executor.isShutdown()) {
            throw new IllegalStateException("ReportBuilder is closed");
        }
        
        List<Pending> pending = tasks.stream()
                .map(task -> new Pending(task.title(), executor.submit(task.body())))
                .toList();

        List<ReportSection> sections = pending.stream()
                .map(p -> {
                    try {
                        return new ReportSection(p.title(), p.future().get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new ReportBuildException(p.title, e.getCause());
                    }
                })
                .toList();

        return new Report(sections);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    private record Pending(String title, Future<String> future) {}
}
