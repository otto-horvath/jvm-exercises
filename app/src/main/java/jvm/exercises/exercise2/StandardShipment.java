package jvm.exercises.exercise2;

import java.util.Objects;

public record StandardShipment(String destination, WeightKg weight) implements Shipment {
    public StandardShipment {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(weight, "weight");
    }
}
