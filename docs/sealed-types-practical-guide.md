# Sealed types: a practical guide

Sealed classes and interfaces let an API author say which types may directly
extend or implement an abstraction. They are useful when a set of alternatives
is known and the code handling those alternatives should not silently miss a
new one.

## The three choices for a permitted subtype

```java
public sealed interface PaymentResult
        permits Paid, Declined, NeedsCustomerAction {}

public record Paid(String receiptId) implements PaymentResult {}
public record Declined(String reason) implements PaymentResult {}
public record NeedsCustomerAction(String redirectUrl) implements PaymentResult {}
```

Each permitted subtype chooses what happens to its own branch:

- `final` closes the branch. Records are implicitly final.
- `sealed` keeps the branch restricted and names its permitted subtypes.
- `non-sealed` opens the branch to arbitrary further subtypes.

For example, `PaymentResult` above is closed to its three listed outcomes. If
`Paid` were a `non-sealed` class, other code could create subclasses of `Paid`,
but it still could not add another direct implementation of `PaymentResult`.

An open interface has no such restriction:

```java
public interface PaymentProvider {
    PaymentResult charge(PaymentRequest request);
}
```

This is a useful distinction: the set of **results** may be closed and easy to
handle exhaustively, while the set of **providers** can stay open so other
applications can integrate their own payment systems.

## Why close a hierarchy?

A pattern-matching switch can cover the permitted alternatives:

```java
String message = switch (result) {
    case Paid paid -> "Receipt: " + paid.receiptId();
    case Declined declined -> "Declined: " + declined.reason();
    case NeedsCustomerAction action -> "Continue at " + action.redirectUrl();
};
```

There is no `default` branch. If the application adds another permitted
payment result, the compiler can flag switches that no longer cover the
hierarchy. This makes a new alternative a deliberate change across its
consumers, rather than an unexpected runtime case. A `default` branch can
handle future cases, but it also removes that specific compiler reminder.

Sealing is most helpful when one team owns the alternatives and consumers need
to treat them differently. It is usually a poor fit for an extension point
where users, plugins, or generated proxies must supply new implementations.

## Examples in production Java projects

- [Apache Camel's `AiToolResult`](https://github.com/apache/camel/blob/main/components/camel-ai/camel-ai-tool/src/main/java/org/apache/camel/component/ai/tool/AiToolResult.java)
  classifies tool execution as success, argument error, execution error, or
  authorization denied. The result is a closed set of outcomes, while Camel's
  adapters choose how to handle each one.
- [Apache Kafka's `EpochState`](https://github.com/apache/kafka/blob/trunk/raft/src/main/java/org/apache/kafka/raft/EpochState.java)
  models the states of its Raft quorum election, including leader, follower,
  nominee, and resigned. The interface exposes state-related behavior and
  keeps the known state implementations controlled by Kafka.
- [Apache Kafka's `HostedPartition`](https://github.com/apache/kafka/blob/trunk/server/src/main/java/org/apache/kafka/server/HostedPartition.java)
  models a partition as absent, online, or offline. Its alternatives are
  records nested in the sealed interface, making the possible conditions
  explicit at call sites.
- [Spring Framework's `HttpStatusCode`](https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/http/HttpStatusCode.java)
  seals the implementation types to its enum and a default implementation.
  The default allows valid HTTP codes that are not named enum constants, so a
  closed set of implementation strategies can still represent an open range
  of values.

## How this relates to Exercise 2

The exercise's `Shipment` interface is sealed because the calculator is
expected to handle a known set: standard, express, and international shipments.
The `switch` in `ShippingCostCalculator` handles those alternatives. Adding a
new permitted shipment kind requires updating that switch.

The shipment records are final data carriers that implement the interface.
`WeightKg` is another record, with a constructor that enforces the shared
positive-and-finite weight rule before a shipment can contain that value.

Not every closed set needs to be sealed. International shipping distinguishes
standard and remote regions, and that set is modeled with the `ShippingRegion`
enum instead of another sealed hierarchy. An enum says "one of these named
constants" directly, and it carries the per-region surcharge as data, so the
calculator multiplies the international rate by `region.surcharge()` rather
than switching on a string.

Use an ordinary interface instead when extension is the goal. Sealed types
aren't a replacement for interfaces generally; they're a way to express a
closed set of variants when that constraint benefits the design.
