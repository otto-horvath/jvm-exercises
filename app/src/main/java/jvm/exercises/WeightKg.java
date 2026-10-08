package jvm.exercises;

public record WeightKg(double value) {
    public WeightKg {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Weight must be finite and positive");
        }
    }
}
