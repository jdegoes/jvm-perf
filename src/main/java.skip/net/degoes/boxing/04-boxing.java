/**
 * BOXING
 *
 * The JVM draws a sharp distinction between primitive types (such as integers, floats, and bytes)
 * and reference types (such as String and user-defined classes).
 *
 * Primitive types may be stored on the stack, and when they are stored on the heap (for example, as
 * part of a user-defined class), they are stored in a very compact form. Finally, arrays are
 * specialized for primitive types, which enable very compact and performant access to their
 * elements.
 *
 * In this section, you will explore the nature and overhead of boxing.
 *
 * EXERCISE 1
 *
 * Design a benchmark to measure the overhead of boxing. In order to be fair to the boxing
 * benchmark, you should design it to have a similar structure and process. The difference is that
 * it will not box the individual integers in an array.
 *
 * Discuss the overhead of boxing and how it compared with your initial expectations.
 *
 * See boxing/BoxedBenchmark.java
 *
 * EXERCISE 2
 *
 * Boxing is not just something that occurs with generic data structures, such as lists, sets, and
 * maps. It occurs also with interfaces that provide generic functionality.
 *
 * In this exercise, you will explore the cost of boxing with the Comparator interface. The
 * Comparator interface is a generic interface that allows you to compare two values of the same
 * type. Create a specialized version to see the overhead of boxing in this example.
 *
 * See boxing/BoxedComparatorBenchmark.java
 */