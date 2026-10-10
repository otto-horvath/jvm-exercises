# Modern Java Evening Exercises

A test-first set of five small Java exercises for refreshing Java 8-era
experience with modern Java, standard-library APIs, JUnit, Gradle, and an
AI-assisted development workflow. The project targets Java 25 and uses JUnit
Jupiter.

[![Java CI with Gradle](https://github.com/otto-horvath/jvm-exercises/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/otto-horvath/jvm-exercises/actions/workflows/gradle.yml)

## The five-exercise proposal

The exercises build from everyday data transformations toward concurrency and
file-based CLI work. Each one should be implemented in small steps, with tests
guiding the behavior.

### 1. Order insights — solved

Given a list of customer orders, calculate revenue by customer, rank products
by units sold, and calculate average order value.

[What we’re learning and practical examples: streams and collectors](docs/streams-and-collectors-practical-guide.md)

**What you’ll learn**

- Use records for concise immutable data models.
- Transform and aggregate data with streams and collectors such as
  `groupingBy` and `summingInt`.
- Use `BigDecimal` for money rather than binary floating-point types.
- Make ordering and tie-breaking behavior explicit with `Comparator`.
- Handle empty stream results with `Optional` and define a rounding policy.

### 2. Shipping cost calculator — solved

Model standard, express, and international shipments and calculate their costs
under different rules.

[What we’re learning and practical examples: sealed types](docs/sealed-types-practical-guide.md)

**What you’ll learn**

- Model a closed set of alternatives with a sealed interface.
- Use records for shipment data.
- Represent validated positive weights with a value type.
- Use pattern matching in `switch` to handle each permitted shipment type.
- Write tests for boundaries and ensure new alternatives require deliberate
  handling.

### 3. Log-line parser

Parse timestamped log lines into structured events and clearly report
malformed input.

**What you’ll learn**

- Parse and represent timestamps with `java.time`.
- Design clear parsing results and exceptions.
- Validate input and test malformed timestamps, missing fields, whitespace,
  and format edge cases.
- Use `Optional` when a value may genuinely be absent.

### 4. Concurrent report builder

Run independent report tasks concurrently and combine their results.

**What you’ll learn**

- Use `ExecutorService`, `Future`, and virtual threads.
- Manage concurrent tasks and resource lifetimes.
- Propagate task failures instead of hiding them.
- Test concurrency through outcomes rather than fragile sleep-based timing.

### 5. Expense-file summary CLI

Read a CSV expense file and print spending summaries by category and month.

**What you’ll learn**

- Work with files using `Path` and `Files`.
- Parse CSV rows, validate malformed input, and aggregate totals.
- Keep monetary calculations in `BigDecimal`.
- Separate file I/O from calculation logic for focused unit tests, and add
  integration-style tests for file behavior.
- Optionally experiment with Java 25 compact source files and instance `main`
  methods in a scratch program.

## Working with the project

Install a Java 25 JDK and use the Gradle wrapper from PowerShell:

```powershell
.\gradlew.bat test
```

Run one test class while working on an exercise:

```powershell
.\gradlew.bat test --tests jvm.exercises.exercise1.OrderInsightsTest
```

JUnit Jupiter is configured for tests. A useful AI-assisted loop is to ask
Copilot for edge cases or one hint at a time, write a failing test, implement
the behavior yourself, run the focused test, and then ask Copilot to review
your implementation for missed cases.
