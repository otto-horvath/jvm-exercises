package jvm.exercises;

import java.util.Objects;

public record ExpressShipment(String destination, WeightKg weight) implements Shipment {
    public ExpressShipment {
        Objects.requireNonNull(weight, "weight");
    }
}
