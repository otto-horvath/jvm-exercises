# Concurrent report building with virtual threads: a practical guide

Exercise 4 turns a list of independent report tasks into one report by running
the tasks at the same time and combining their results in input order. The
builder owns an executor of virtual threads, so callers get the concurrency
without managing a thread pool themselves. This guide covers that design and
the stream, tuple, and execution-model lessons from the exercise.

## Own the executor and its lifetime

`ReportBuilder` has a small job: create a single executor, expose one domain
operation (`build`), and shut the executor down when the builder is closed.
This "resource-owning wrapper" is a common pattern — `BufferedReader`, JDBC
pools, and HTTP clients all hide the lifecycle of a resource behind
`AutoCloseable`.

```java
public final class ReportBuilder implements AutoCloseable {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public Report build(List<ReportTask> tasks) {
        if (executor.isShutdown()) {
            throw new IllegalStateException("ReportBuilder is closed");
        }
        // ...
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
```

The builder's public surface never leaks the `ExecutorService`; callers only
create a builder, call `build`, and rely on try-with-resources for `close()`.
The alternative — injecting an executor — makes the caller own the lifetime and
is the right choice when several components share one pool.

`ExecutorService` itself implements `AutoCloseable` since Java 19, and its
default `close()` does an orderly shutdown (stop accepting tasks, let running
tasks finish, wait). Delegating to `executor.close()` is therefore the shortest
correct implementation here. `shutdown()` alone is enough to satisfy the
exercise and is idempotent, which is why a second `close()` is a no-op.

## Submit everything, then collect

The trickiest bug in this exercise is hidden inside a stream:

```java
tasks.stream()
        .map(task -> new Pending(task.title(), executor.submit(task.body())))
        .map(pending -> new ReportSection(pending.title(), pending.future().get()))
        .toList();
```

This *looks* like it submits all tasks first and then waits, but it runs
sequentially. Stream intermediate operations are **lazy and fused per element**:
for element 0 the pipeline executes the submit `map` and the `get` `map`
together, blocking on `get()` before element 1 is even submitted. A long enough
`get()` becomes a barrier that prevents every later submission.

Two consecutive `map`s are one pass over the elements, not two phases. To force
a phase boundary, materialize the submitted tasks with a terminal operation
before waiting on any result:

```java
List<Pending> pending = tasks.stream()
        .map(task -> new Pending(task.title(), executor.submit(task.body())))
        .toList();                       // barrier: every task is now in flight

List<ReportSection> sections = pending.stream()
        .map(p -> new ReportSection(p.title(), await(p.future(), p.title())))
        .toList();
```

The rule of thumb: when a stream has side-effecting steps that must not be
interleaved, split the pipeline at the boundary with an intermediate terminal
operation. The same applies to deciding when an executor should see the work.

## Preserve input order while combining results

Tasks finish in a nondeterministic order, but the report must not be. Keep the
futures in input order while submitting, then call `get()` in the same order:

```java
for (int i = 0; i < tasks.size(); i++) {
    sections.set(i, new ReportSection(tasks.get(i).title(), futures.get(i).get()));
}
```

The report is assembled in the order the caller asked for tasks, so the
concurrency is invisible in the result. `Report` further defends this by
copying its section list in the constructor and exposing `titles()` and
`content(title)` instead of free access to the internal list.

## Fail loudly: unwrap `ExecutionException`

`Future.get()` never throws the task's own exception directly. It wraps it:

```java
try {
    return p.future().get();
} catch (ExecutionException e) {
    throw new ReportBuildException(p.title(), e.getCause());
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new ReportBuildException(p.title(), e);
}
```

`e.getCause()` is the original throwable — the exact instance the task threw —
which is what lets `assertSame(boom, exception.getCause())` pass. `Callable`
allows checked exceptions (`throws Exception`), so both a `RuntimeException`
and an `IOException` surface as the same `ReportBuildException`, carrying the
failing task title and the real cause.

An `InterruptedException` means the waiting thread was asked to stop while
blocked in `get()`. The convention is to restore the interrupt flag with
`Thread.currentThread().interrupt()` before continuing, so the interruption
does not get lost.

## Virtual threads and streams are different tools

It is tempting to reach for `parallelStream()`, and it is genuinely parallel —
but it is the wrong tool here:

| | Parallel streams | Virtual threads (`ExecutorService`) |
|---|---|---|
| Runs on | common `ForkJoinPool` platform threads | one virtual thread per task |
| `Thread.isVirtual()` | `false` | `true` |
| Parallelism | `CPU count - 1`, shared process-wide | unbounded, waits are cheap |
| Best for | CPU-bound, pure aggregation | blocking I/O, many concurrent units |
| Lifecycle | none — you cannot shut it down | explicit `close()` / `shutdown()` |
| Failure per task | exceptions surface wrapped, attribution is awkward | each `Future` identifies its task |
| Unit of work | elements flowing through a pipeline | submitted `Callable` with a result |
| Ordering | encounter order when the stream is ordered | whatever you choose when you collect |

Paragraph the practical difference: parallel streams express "take this
pipeline and fan it out over the elements" for CPU-bound folds, and they
borrow a process-wide pool you do not own. Blocking that pool (an I/O wait
inside a report task, say) can starve unrelated code. Virtual threads exist
exactly for many concurrent, possibly blocking units of work; each submitted
task is allowed to block without consuming a platform carrier thread. The
exercise's tasks *may* block, so the executor is the correct model — and the
`runsTasksOnVirtualThreads` test enforces it by asserting
`Thread.currentThread().isVirtual()`.

## Java has no tuples — use records

Python and Elixir ship a built-in tuple type; Java does not. The idiomatic
replacement is a `record`, which names the pair instead of indexing it:

```java
private record Pending(String title, Future<String> future) {}
```

Compared with a tuple like `(title, future)`:

- accessors are named methods (`pending.title()`), not positions (`p[0]`);
- the state is explicitly non-null friendly and type-checked;
- `equals`, `hashCode`, and `toString` come for free;
- the type has a name that says what the pair means.

The exercise solutions lean on this throughout: `ReportTask` pairs a title
with the `Callable` body, `ReportSection` pairs a title with its content, and
`Pending` pairs a title with its future. A general `Pair<A, B>` would compile
but would throw away the domain meaning records exist to add.

## Test concurrency through outcomes

The concurrency test never sleeps. Each task announces that it started, then
blocks on a shared gate; the test waits until all tasks have announced, and
only then opens the gate:

```java
CountDownLatch allStarted = new CountDownLatch(taskCount);
CountDownLatch release    = new CountDownLatch(1);

tasks = IntStream.range(0, taskCount)
        .mapToObj(i -> new ReportTask("task-" + i, () -> {
            allStarted.countDown();
            release.await(5, TimeUnit.SECONDS);   // block until the test says go
            return "result-" + i;
        }))
        .toList();

Future<Report> future = caller.submit(() -> builder.build(tasks));
assertTrue(allStarted.await(5, TimeUnit.SECONDS));  // all tasks must be in flight
release.countDown();                               // let them finish
```

A sequential implementation reaches the latch countdown for only the first
task and times out; a concurrent one reaches it for all tasks immediately.
The timeouts are safety nets, not the assertion — the assertion is "all tasks
started before any could finish", which is a property of the implementation,
not of timing luck.

## How this relates to Exercise 4

- `ReportTask` carries a title and a `Callable<String>` body; the two-argument
  record constructor rejects null components up front.
- `ReportBuilder` owns a virtual-thread-per-task executor, submits every task
  before waiting on any, collects in input order, and wraps failures in
  `ReportBuildException` with the offending task title and the original cause.
- `Report` stores an immutable, ordered list of `ReportSection` and exposes
  `titles()` and `content(title)` rather than the backing list.
- `ReportBuildException` extends `RuntimeException` so `build` can fail
  loudly without a checked-exception ceremony at call sites.

The natural sequence while developing was: get the ordered result for trivial
tasks → prove virtual threads → prove true concurrency with the latches →
handle empty input and null → add failure propagation. Run the focused tests
as you go:

```shell
./gradlew test --tests jvm.exercises.exercise4.ReportBuilderTest
```

## Further reading

- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [`ExecutorService`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ExecutorService.html)
- [`Executors`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Executors.html)
- [`Future`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Future.html)
- [`Callable`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Callable.html)
- [`ExecutionException`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ExecutionException.html)
- [`AutoCloseable`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/AutoCloseable.html)
- [`Record` (JEP 395)](https://openjdk.org/jeps/395)
- [`CountDownLatch`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CountDownLatch.html)