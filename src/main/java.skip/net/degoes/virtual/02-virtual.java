/**
 * VIRTUAL DISPATCH
 *
 * Surprisingly, not all methods are equal: calling some methods can be quite fast, and calling
 * other methods can be dangerously slow, even if their implementations are *exactly* the same.
 *
 * This surprising fact is due to the way that object-oriented languages implement polymorphism.
 * Polymorphism allows us to write code that is generic over a type. For example, we might have some
 * business logic that can work with any key/value store, whether backed by a database, an in-memory
 * hash map, or a cloud API.
 *
 * In object-oriented programming languages, we achieve this type of polymorphism with inheritance,
 * and then implementing or overriding methods in a subtype.
 *
 * In this section, you will learn more about how this works, its impact on performance, and
 * potential workarounds for performance sensitive code.
 *
 * EXERCISE 1
 *
 * Every method invocation potentially goes through virtual dispatch, which is a process involving
 * looking up which concrete non-final method invocation is potentially a virtual dispatch.
 *
 * In this exercise, you will explore the cost of virtual dispatch. The current benchmark creates a
 * chunk of operators, each one of which is a random operator chosen from among the provided set. At
 * runtime, the JVM does not know which element of the chunk has which concrete type, so it must
 * lookup the correct method to invoke on an object-by-object basis. This results in lower
 * performance.
 *
 * Augment this benchmark with another benchmark, which uses another chunk, where every element of
 * the chunk uses the same concrete operator (e.g. Operator.DividedBy.type). In your new benchmark,
 * because the JVM knows the concrete type of the object, when it invokes the apply method, it knows
 * exactly where the code for that function is, and does not need to perform a preliminary lookup.
 * This should result in faster performance.
 *
 * See virtual/PolyBenchmark.java
 *
 * EXERCISE 2
 *
 * In this exercise, you will simulate the cost of a virtual dispatch by creating a benchark that
 * must lookup the correct method based on the virtual method table stored together with the data
 * for an object.
 *
 * Create an invokeVirtual benchmark that uses `obj.meta` to find the address of the method to be
 * invoked. Compare the performance of this benchmark to the invokeStatic benchmark.
 *
 * Note that this benchmark is not that realistic. There is no hash map lookup with invoke dynamic.
 * Nonetheless, getting a feel for the extra work the JVM must do to perform a virtual dispatch is
 * useful.
 */

// See virtual/PolySimBenchmark.java