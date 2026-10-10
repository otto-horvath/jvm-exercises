package jvm.exercises.exercise2;

import java.util.Objects;

public record InternationalShipment(String destination, WeightKg weight, String region) implements Shipment {
    public InternationalShipment {
        Objects.requireNonNull(weight, "weight");
    }
}
