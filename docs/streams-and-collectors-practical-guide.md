# Streams and collectors: a practical guide

Exercise 1 turns a list of orders into revenue by customer, a product ranking,
and an average order value. Almost all of that work is a small set of stream
patterns. This guide collects the ones the exercise relies on and explains the
choices behind them.

## Aggregation means "collect", not "loop"

A `groupingBy` classifier alone returns a `Map<K, List<T>>` — every element
kept as-is:

```java
Map<String, List<Order>> byCustomer = orders.stream()
        .collect(Collectors.groupingBy(Order::customer));
```

Usually you do not want the list; you want a value reduced from each group. The
two-argument `groupingBy(classifier, downstream)` sends each group through a
*downstream collector*. Revenue per customer combines a transform (`mapping`)
with a reduction (`reducing`):

```java
Map<String, BigDecimal> revenueByCustomer = orders.stream()
        .collect(Collectors.groupingBy(
                Order::customer,
                Collectors.mapping(Order::revenue,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
```

The useful downstream collectors in this exercise are:

- `mapping(fn, downstream)` — transform each element before reducing it.
- `reducing(identity, op)` — fold a group into one value.
- `summingInt(fn)` — a ready-made `int` sum of a property.

## Choose the sum that matches the shape

When the value you want already exists as a property, a built-in summing
collector is the shortest expression:

```java
Map<String, Integer> unitsByProduct = orders.stream()
        .flatMap(order -> order.lines().stream())
        .collect(Collectors.groupingBy(
                OrderLine::product,
                Collectors.summingInt(OrderLine::quantity)));
```

When the value must be derived first (here `OrderLine.revenue()` = price ×
quantity), combine `mapping` with `reducing`, as in the revenue example above.
`flatMap` is what flattens the orders into their lines before grouping; each
order maps to a stream of lines, and `flatMap` splices those streams together.

## Money is `BigDecimal`, never `double`

Binary floating point cannot represent most decimal fractions exactly:
`0.1 + 0.2` evaluates to `0.30000000000000004`. For money this is a correctness
bug, so amounts are `BigDecimal`.

A `BigDecimal` carries a *scale* (digits after the point) as well as a value, and
`equals` compares both. Two aggregates can be numerically equal and still fail
`equals` if their scales differ. That makes the scale a deliberate decision, not
an accident.

Division is where this bites. Of the four `divide` overloads, the one that takes
only a `RoundingMode` uses the **dividend's** scale for the result:

```java
// scale comes from `total`; result scale is whatever the inputs happened to have
total.divide(BigDecimal.valueOf(orders.size()), RoundingMode.HALF_UP);
```

That is fragile: the rounding of the average changes with the scale of the
summed revenues. State the scale explicitly instead:

```java
total.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
```

Now "two decimal places, half-up" is a visible policy, and a test can pin it.

## Comparators make ordering a rule

Ranking products needs more than "sort by units": ties must be broken
predictably. `Comparator` chains express exactly that:

```java
Comparator.comparingInt(ProductSales::unitsSold).reversed()
        .thenComparing(ProductSales::product)
```

`reversed()` applies to everything before it, so this reads as "most units
first, then product name ascending". It is worth naming the chain as a constant
so the rule has a label:

```java
private static final Comparator<ProductSales> RANKING =
        Comparator.comparingInt(ProductSales::unitsSold).reversed()
                .thenComparing(ProductSales::product);
```

`Stream.sorted` is stable, so any remaining ties keep their encounter order —
which is why the secondary key should be one that actually distinguishes them.

## `Optional` means "genuinely absent"

The single-argument `reduce(BinaryOperator)` returns an `Optional`, because an
empty stream has no result:

```java
Optional<BigDecimal> total = orders.stream()
        .map(Order::revenue)
        .reduce(BigDecimal::add);
```

The two-argument `reduce(identity, op)` instead returns the identity for an empty
stream, which erases the difference between "no orders" and "orders summing to
zero". That is fine when the identity is the correct answer — but for an average
you must guard the divisor before dividing, because dividing by `orders.size()`
of zero throws:

```java
if (orders.isEmpty()) {
    return BigDecimal.ZERO;
}
```

Either form is valid; the important part is knowing which one you have and why.

## Records are the natural carriers

The data models are records: concise, immutable, and with accessors. A compact
constructor runs shared validation and normalization exactly once:

```java
public record OrderLine(String product, int quantity, BigDecimal unitPrice) {
    public OrderLine {
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
```

`Order` likewise copies its list with `List.copyOf`, so a caller cannot mutate the
order's contents after construction. Immutable inputs are what make the stream
aggregations safe to parallelize later.

## How this relates to Exercise 1

- `revenueByCustomer` — `groupingBy` with a `mapping` + `reducing` downstream to
  fold each customer's orders into a `BigDecimal` total.
- `topProducts` — `flatMap` lines, `summingInt` per product, project to
  `ProductSales`, then rank with the named `RANKING` comparator and `limit`.
- `averageOrderValue` — sum with `reduce`, guard the empty case, then divide with
  an explicit scale and `RoundingMode.HALF_UP`.
- `Order` / `OrderLine` — records with compact-constructor validation and
  defensive copying, keeping the data the streams operate on immutable.

## Examples in production Java projects

- The JDK's own [`Collectors`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Collectors.html)
  documentation is the reference for the downstream collectors used here; its
  examples are the patterns this exercise builds on.
- [Google Guava](https://github.com/google/guava) extends the same model with
  immutable collectors such as `ImmutableList.toImmutableList()`, showing a
  library building stream-friendly collectors on top of the JDK.
- [OpenJDK](https://github.com/openjdk/jdk) contains the `Stream` and `Collectors`
  implementations themselves, useful when you want to see how a downstream
  collector is actually built.

## Further reading

- [`java.util.stream.Collectors`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Collectors.html)
- [`java.util.stream` package summary](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/package-summary.html)
- [`java.util.stream.Stream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Stream.html)
- [`java.math.BigDecimal`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
- [`java.util.Comparator`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Comparator.html)
