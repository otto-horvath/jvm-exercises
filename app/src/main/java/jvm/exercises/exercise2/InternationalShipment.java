package jvm.exercises.exercise2;

import java.util.Objects;

public record InternationalShipment(String destination, WeightKg weight, ShippingRegion region) implements Shipment {
    public InternationalShipment {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(region, "region");
    }
}
