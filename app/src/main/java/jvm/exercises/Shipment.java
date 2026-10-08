package jvm.exercises;

public sealed interface Shipment
        permits StandardShipment, ExpressShipment, InternationalShipment {
}
