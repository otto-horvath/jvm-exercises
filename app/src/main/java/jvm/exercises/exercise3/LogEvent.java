package jvm.exercises.exercise3;

import java.time.Instant;
import java.util.Optional;

public record LogEvent(
        Instant timestamp,
        Level level,
        String logger,
        Optional<String> correlationId,
        String message) {
}
