package jvm.exercises;

import java.util.Objects;

public record StandardShipment(String destination, WeightKg weight) implements Shipment {
    public StandardShipment {
        Objects.requireNonNull(weight, "weight");
    }
}
