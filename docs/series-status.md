# Congratulations! 🎉 Five Exercises. One Modern Java Refresh.

[![Java CI with Gradle](https://github.com/otto-horvath/jvm-exercises/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/otto-horvath/jvm-exercises/actions/workflows/gradle.yml) · [Repository](https://github.com/otto-horvath/jvm-exercises) · [Final PR #4](https://github.com/otto-horvath/jvm-exercises/pull/4)

**You completed the five-exercise series.** The final expense-file summary
exercise is implemented, documented, merged, and green. This page is a
celebration and a living snapshot of what Git and the current development
environment can tell us. It can be updated later as more machine and tool
history becomes available.

> Snapshot: merged `origin/master`, October 15, 2026 local time
> (October 16 UTC). Line counts are physical lines in the merged tree, not
> deduplicated logical LOC.

## The scoreboard 🏁

| Metric | Result |
| --- | ---: |
| Exercises completed | **5 / 5** |
| Automated tests passing | **70 / 70** |
| Commits on the merged project history | **26** |
| Java source and test files | **42** |
| Java production-code lines | **635** |
| Java test-code lines | **802** |
| Java lines, production + tests | **1,437** |
| Practical guides | **5** |
| Practical-guide lines | **894** |
| Pull requests merged | **4** (one CI/setup, three exercise-related) |

### The five checkpoints 🧭

| # | Exercise | Focus | Passing tests |
| ---: | --- | --- | ---: |
| 1 | Order insights | Records, streams/collectors, `BigDecimal`, comparators | 7 |
| 2 | Shipping cost calculator | Sealed types, records, validated values, pattern-matching `switch` | 5 |
| 3 | Log-line parser | Regex, `java.time`, parse errors and edge cases | 21 |
| 4 | Concurrent report builder | Executors, futures, virtual threads, lifecycle and failures | 10 |
| 5 | Expense-file summary | `Path`/`Files`, CSV validation, aggregation, rounding, formatting | 27 |

The test counts are based on the test annotations in the merged source tree;
their sum matches the full-suite result.

### Exercise 5: the finale ✨

- **4 commits** from scaffold to practical guide.
- **15 files changed**, with **754 lines added** in the pull request diff.
- **577 Java lines** across its production code and tests.
- The scaffold-to-merge Git timestamp window was about **1 hour, 18 minutes**.
  That is the span between commit timestamps—not measured hands-on time.
- The exercise covered file reading, row validation, contextual parse errors,
  `BigDecimal` aggregation and rounding, deterministic report formatting, and
  an end-to-end file test.

## A little timeline 🗓️

The first project commit is dated October 7, 2026; the Exercise 5 merge is
dated October 15 in the commit's local `-03:00` timezone (October 16 UTC).
That is **9 calendar dates inclusive**, or **8 days, 22 hours, and 43 minutes
elapsed** between those Git timestamps. Commits appear on **5 distinct calendar
dates**: October 7, 8, 10, 11, and 15.

The exercises were not all delivered in the same way: Exercises 1 and 3 were
completed through direct repository commits; Exercise 2 arrived with PR #2;
Exercise 4 with PR #3; and Exercise 5 with PR #4. PR #1 handled early CI and
repository setup. The four PRs are visible in the public repository history.

These dates describe recorded project activity. Git does not tell us how many
sessions there were, how long each session lasted, or whether work happened on
other machines without commits.

## The learning arc 📚

1. **Order insights** — records, streams and collectors, `BigDecimal`, and
   explicit ordering.
2. **Shipping cost calculator** — sealed types, records, value validation, and
   pattern matching in `switch`.
3. **Log-line parser** — regular expressions, `java.time`, and useful malformed
   input errors.
4. **Concurrent report builder** — executors, futures, virtual threads,
   resource lifetime, and failure propagation.
5. **Expense-file summary** — `Path`/`Files`, CSV row validation, aggregation,
   rounding, formatting, and file-backed integration tests.

The full repository test suite passed after the fifth exercise. The optional
Java 25 compact source file / instance `main` experiment remains a stretch goal,
not a requirement for the tested parser-to-report pipeline.

## Pull-request trail 🔀

| PR | What it records | Changed files | Diff |
| --- | --- | ---: | ---: |
| [#1](https://github.com/otto-horvath/jvm-exercises/pull/1) | Gradle/CI setup repair | 2 | +2 / −21 |
| [#2](https://github.com/otto-horvath/jvm-exercises/pull/2) | Shipping exercise and sealed-types guide | 9 | +205 |
| [#3](https://github.com/otto-horvath/jvm-exercises/pull/3) | Concurrent report builder and guide | 9 | +585 / −1 |
| [#4](https://github.com/otto-horvath/jvm-exercises/pull/4) | Expense summary, tests, and practical guide | 15 | +754 |

The repo totals **26 commits including merge commits**. The last exercise's
scaffold-to-merge commit window was about **1 hour, 18 minutes**. These are
timestamps from Git/GitHub—not a measure of time spent actively coding.

## Stack & tools observed 🧰

| Layer | Observed for this snapshot |
| --- | --- |
| Language/runtime | Java 25; OpenJDK `25.0.4.1` |
| Build | Gradle wrapper, Gradle `9.2.0` |
| Tests | JUnit Jupiter `5.12.1` |
| Formatting | Spotless `8.10.4`, Palantir Java Format |
| Operating environment | Linux kernel `6.18.33.2` under WSL2; Ubuntu OpenJDK runtime |
| Coding agent / interface | OpenCode |
| Model visible in this session | OpenAI GPT-6 Luna |
| Source control / PR tools | Git and GitHub CLI (`gh`) |
| CI | GitHub Actions with Gradle |

This is **current-session metadata**, not a claim that every exercise used this
exact machine, IDE, model, or toolchain. The repository contains VS Code
workspace settings, but that alone does not establish which editor was used for
every session. GitHub commit metadata explicitly lists Copilot as a co-author on
the CI-fix commit in PR #1 and the sealed-types exercise commit in PR #2. This
confirms Copilot attribution for those commits, but not the extent of its use
elsewhere. OpenCode with GPT-6 Luna is what is visible in this final session;
there is no reliable history here to attribute other sessions to a particular
model. Claude usage is not evidenced in the available repository metadata.

## The pull request 🚀

The final exercise was merged as [PR #4](https://github.com/otto-horvath/jvm-exercises/pull/4).
The change added the exercise scaffold and tests, implementation, and a
practical guide. The README links to both the [Exercise 5 guide](expense-file-summary-cli-practical-guide.md)
and this series status page.

## Update this page later 🔄

This snapshot uses Git history, the merged tree, build configuration, and
metadata visible in the current OpenCode environment. To extend it with details
from other machines or sessions, add only information that can be verified—for
example, IDE/version, OS/JDK, agent/model, or additional session dates—and label
whether each figure is observed, inferred, or unknown.

## Final toast 🍻

Five exercises, seventy green tests, and one very well-earned beer. Cheers to
finishing the series—and to whatever you build with all that modern Java next.
