package jvm.exercises.exercise2;

public sealed interface Shipment
        permits StandardShipment, ExpressShipment, InternationalShipment {
}
