# Agent behavior for this repo

This is a test-first Java learning project. **The user writes the solution code to learn modern Java.**

When working on exercises:

- **Do not implement the solution.** The user implements production code to make the tests pass.
- **Do write tests and scaffold.** Create JUnit Jupiter tests that define the expected behavior, plus the minimal scaffold needed for those tests to compile and run (`package-info.java`, data records, and method-signature stubs that throw `UnsupportedOperationException`).
- Keep style consistent with existing code: `final` classes, records for data models, `BigDecimal` for money, `Objects.requireNonNull`, Palantir Java Format (Spotless), JUnit 5.
- Run `./gradlew test --tests jvm.exercises.exerciseN....` to check progress; `./gradlew spotlessApply` to format.

At end of session (only when the user asks):

- **Generate a `docs/what-i-learned-<exercise>.md` document** summarizing what was discussed and learned during the session.
- Base it on the previous files under `docs/` (e.g. `streams-and-collectors-practical-guide.md`, `sealed-types-practical-guide.md`) to match their format, sections, content style, and proper file naming.
- Ground the summary in the proposed exercise's "What you'll learn" points and what was actually discussed.
