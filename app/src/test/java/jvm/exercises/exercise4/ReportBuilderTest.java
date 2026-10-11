package jvm.exercises.exercise4;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ReportBuilderTest {

    @Test
    void buildsSectionsInTaskOrder() {
        List<ReportTask> tasks = List.of(task("revenue", "100.00"), task("orders", "42"), task("refunds", "3"));

        try (ReportBuilder builder = new ReportBuilder()) {
            Report report = builder.build(tasks);

            assertEquals(List.of("revenue", "orders", "refunds"), report.titles());
            assertEquals(Optional.of("100.00"), report.content("revenue"));
            assertEquals(Optional.of("42"), report.content("orders"));
            assertEquals(Optional.of("3"), report.content("refunds"));
        }
    }

    @Test
    void combinesIndependentTaskResults() {
        List<ReportTask> tasks = List.of(task("a", "alpha"), task("b", "beta"));

        try (ReportBuilder builder = new ReportBuilder()) {
            Report report = builder.build(tasks);

            assertEquals(List.of("a", "b"), report.titles());
        }
    }

    @Test
    void runsTasksOnVirtualThreads() {
        AtomicBoolean virtual = new AtomicBoolean();
        List<ReportTask> tasks = List.of(new ReportTask("probe", () -> {
            virtual.set(Thread.currentThread().isVirtual());
            return "ok";
        }));

        try (ReportBuilder builder = new ReportBuilder()) {
            builder.build(tasks);
        }

        assertTrue(virtual.get(), "report tasks should run on virtual threads");
    }

    @Test
    void runsTasksConcurrently() throws Exception {
        int taskCount = 4;
        CountDownLatch allStarted = new CountDownLatch(taskCount);
        CountDownLatch release = new CountDownLatch(1);

        List<ReportTask> tasks = IntStream.range(0, taskCount)
                .mapToObj(index -> new ReportTask("task-" + index, () -> {
                    allStarted.countDown();
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("tasks did not run concurrently");
                    }
                    return "result-" + index;
                }))
                .toList();

        ExecutorService caller = Executors.newSingleThreadExecutor();
        try (ReportBuilder builder = new ReportBuilder()) {
            Future<Report> future = caller.submit(() -> builder.build(tasks));

            assertTrue(allStarted.await(5, TimeUnit.SECONDS), "all tasks should start before any completes");
            release.countDown();

            Report report = future.get(5, TimeUnit.SECONDS);
            assertEquals(taskCount, report.sections().size());
        } finally {
            release.countDown();
            caller.shutdownNow();
        }
    }

    @Test
    void returnsEmptyReportForNoTasks() {
        try (ReportBuilder builder = new ReportBuilder()) {
            Report report = builder.build(List.of());

            assertEquals(List.of(), report.sections());
            assertEquals(List.of(), report.titles());
        }
    }

    @Test
    void rejectsNullTaskList() {
        try (ReportBuilder builder = new ReportBuilder()) {
            assertThrows(NullPointerException.class, () -> builder.build(null));
        }
    }

    @Test
    void propagatesUncheckedTaskFailure() {
        RuntimeException boom = new RuntimeException("boom");
        List<ReportTask> tasks = List.of(task("ok", "fine"), new ReportTask("broken", () -> {
            throw boom;
        }));

        try (ReportBuilder builder = new ReportBuilder()) {
            ReportBuildException exception = assertThrows(ReportBuildException.class, () -> builder.build(tasks));

            assertEquals("broken", exception.taskTitle());
            assertSame(boom, exception.getCause());
        }
    }

    @Test
    void propagatesCheckedTaskFailure() {
        IOException boom = new IOException("disk full");
        List<ReportTask> tasks = List.of(new ReportTask("io", () -> {
            throw boom;
        }));

        try (ReportBuilder builder = new ReportBuilder()) {
            ReportBuildException exception = assertThrows(ReportBuildException.class, () -> builder.build(tasks));

            assertEquals("io", exception.taskTitle());
            assertSame(boom, exception.getCause());
        }
    }

    @Test
    void rejectsBuildAfterClose() {
        ReportBuilder builder = new ReportBuilder();
        builder.close();

        assertThrows(IllegalStateException.class, () -> builder.build(List.of(task("a", "1"))));
    }

    @Test
    void closeIsIdempotent() {
        ReportBuilder builder = new ReportBuilder();
        builder.close();

        assertDoesNotThrow(builder::close);
    }

    private static ReportTask task(String title, String content) {
        return new ReportTask(title, () -> content);
    }
}
